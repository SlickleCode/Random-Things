package lumien.randomthings.handler.spectrelens;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.12.2's {@code SpectreLensHandler} - tracks which players have an active Spectre Lens
 * (see {@link lumien.randomthings.tileentity.SpectreLensTileEntity}) and, every 60 ticks, re-applies
 * that lens's bound Beacon's current primary/secondary effects to the owner directly, wherever they
 * are (any dimension, any distance from the actual beacon) - a personal, remote extension of the
 * beacon's own buffs. Matches the original's {@code amplifier = primary == secondary ? 1 : 0} rule
 * (the same "picking the same effect twice doubles its strength" behavior vanilla's own beacon UI has).
 *
 * <p>One lens per player per dimension (1.12.2's {@code getPerWorldStorage()} was per dimension and only
 * applied effects to players in that world): a single store (the overworld's, fetched via
 * {@code server.getLevel(Level.OVERWORLD)}) holds every lens keyed by (dimension id, owner),
 * and a lens only affects its owner while the owner is in that dimension.
 */
public class SpectreLensHandler extends SavedData {
    private static final String ID = "randomthings_spectre_lens_handler";
    /** 1.21.5+ saved data types are codec based: the old CompoundTag save/load code is wrapped by the compound codec. */
    public static final net.minecraft.world.level.saveddata.SavedDataType<SpectreLensHandler> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(ID, SpectreLensHandler::new,
            CompoundTag.CODEC.xmap(tag -> SpectreLensHandler.loadFrom(tag, lumien.randomthings.util.StackTag.provider()), data -> data.rtSave(new CompoundTag(), lumien.randomthings.util.StackTag.provider())));

    // dimension id -> (owner -> lens): one lens per player per dimension, the lens only affects its owner while the owner is in that dimension
    private final Map<String, Map<UUID, LensEntry>> lensEntries = new HashMap<>();

    public SpectreLensHandler() {
    }

    public static SpectreLensHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        SpectreLensHandler data = new SpectreLensHandler();
        data.readData(nbt);
        return data;
    }

    public static SpectreLensHandler get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public void removeLens(String dimension, UUID uuid) {
        Map<UUID, LensEntry> inDimension = lensEntries.get(dimension);

        if (inDimension != null && inDimension.remove(uuid) != null) {
            if (inDimension.isEmpty()) {
                lensEntries.remove(dimension);
            }
            setDirty();
        }
    }

    public void addLens(String dimension, UUID uuid, int levels, Holder<MobEffect> primary, Holder<MobEffect> secondary) {
        lensEntries.computeIfAbsent(dimension, d -> new HashMap<>()).put(uuid, new LensEntry(levels, primary, secondary));
        setDirty();
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 60 != 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();

        for (Map.Entry<String, Map<UUID, LensEntry>> dimensionEntry : lensEntries.entrySet()) {
            for (Map.Entry<UUID, LensEntry> entry : dimensionEntry.getValue().entrySet()) {
                ServerPlayer player = playerList.getPlayer(entry.getKey());

                // only applies while the owner is in the lens's own dimension
                if (player == null || !player.level().dimension().identifier().toString().equals(dimensionEntry.getKey())) {
                    continue;
                }

                LensEntry lens = entry.getValue();
                int amplifier = lens.primary != null && lens.primary == lens.secondary ? 1 : 0;

                if (lens.primary != null) {
                    player.addEffect(new MobEffectInstance(lens.primary, 20 * 10, amplifier, true, true));
                }

                if (lens.secondary != null && lens.secondary != lens.primary) {
                    player.addEffect(new MobEffectInstance(lens.secondary, 20 * 10, amplifier, true, true));
                }
            }
        }
    }

    public void readData(CompoundTag nbt) {
        lensEntries.clear();

        ListTag list = nbt.getListOrEmpty("lensEntries");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompoundOrEmpty(i);
            String dimension = entryTag.getStringOr("dimension", "");

            // old saves (no dimension tag) are dropped: the lens block entity re-registers itself in its own dimension within 80 ticks
            if (dimension.isEmpty()) {
                continue;
            }

            lensEntries.computeIfAbsent(dimension, d -> new HashMap<>()).put(lumien.randomthings.util.TagIO.readUuid(entryTag.get("uuid")), LensEntry.read(entryTag));
        }
    }

    public CompoundTag rtSave(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (Map.Entry<String, Map<UUID, LensEntry>> dimensionEntry : lensEntries.entrySet()) {
            for (Map.Entry<UUID, LensEntry> entry : dimensionEntry.getValue().entrySet()) {
                CompoundTag entryTag = entry.getValue().write();
                entryTag.put("uuid", lumien.randomthings.util.TagIO.writeUuid(entry.getKey()));
                entryTag.putString("dimension", dimensionEntry.getKey());
                list.add(entryTag);
            }
        }

        compound.put("lensEntries", list);
        return compound;
    }

    private static class LensEntry {
        final int levels;
        final Holder<MobEffect> primary;
        final Holder<MobEffect> secondary;

        LensEntry(int levels, Holder<MobEffect> primary, Holder<MobEffect> secondary) {
            this.levels = levels;
            this.primary = primary;
            this.secondary = secondary;
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("levels", levels);
            tag.putInt("primary", primary != null ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getId(primary.value()) : -1);
            tag.putInt("secondary", secondary != null ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getId(secondary.value()) : -1);
            return tag;
        }

        static LensEntry read(CompoundTag tag) {
            int primaryId = tag.getIntOr("primary", 0);
            int secondaryId = tag.getIntOr("secondary", 0);

            return new LensEntry(tag.getIntOr("levels", 0), primaryId >= 0 ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(primaryId).orElse(null) : null, secondaryId >= 0 ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(secondaryId).orElse(null) : null);
        }
    }
}
