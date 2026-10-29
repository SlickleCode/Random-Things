package lumien.randomthings.handler.spectrelens;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
 * applied effects to players in that world): a single store (the overworld's data storage, fetched via
 * {@code server.getLevel(Level.OVERWORLD)}) holds every lens keyed by (dimension id, owner), and a lens
 * only affects its owner while the owner is in that dimension.
 */
public class SpectreLensHandler extends SavedData {
    private static final String ID = "randomthings_spectre_lens_handler";

    // dimension id (e.g. "minecraft:overworld") -> (owner -> lens): one lens per player per dimension, the lens only affects its owner while the owner is in that dimension
    private final Map<String, Map<UUID, LensEntry>> lensEntries = new HashMap<>();

    public SpectreLensHandler() {
    }

    public static SpectreLensHandler loadFrom(CompoundTag nbt) {
        SpectreLensHandler data = new SpectreLensHandler();
        data.readData(nbt);
        return data;
    }

    public static SpectreLensHandler get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(SpectreLensHandler::loadFrom, SpectreLensHandler::new, ID);
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

    public void addLens(String dimension, UUID uuid, int levels, MobEffect primary, MobEffect secondary) {
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
                if (player == null || !player.level.dimension().location().toString().equals(dimensionEntry.getKey())) {
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

        ListTag list = nbt.getList("lensEntries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);

            // old saves (no dimension tag) are dropped: the lens block entity re-registers itself in its own dimension within 80 ticks
            if (!entryTag.contains("dimension")) {
                continue;
            }

            lensEntries.computeIfAbsent(entryTag.getString("dimension"), d -> new HashMap<>()).put(entryTag.getUUID("uuid"), LensEntry.read(entryTag));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag list = new ListTag();

        for (Map.Entry<String, Map<UUID, LensEntry>> dimensionEntry : lensEntries.entrySet()) {
            for (Map.Entry<UUID, LensEntry> entry : dimensionEntry.getValue().entrySet()) {
                CompoundTag entryTag = entry.getValue().write();
                entryTag.putUUID("uuid", entry.getKey());
                entryTag.putString("dimension", dimensionEntry.getKey());
                list.add(entryTag);
            }
        }

        compound.put("lensEntries", list);
        return compound;
    }

    private static class LensEntry {
        final int levels;
        final MobEffect primary;
        final MobEffect secondary;

        LensEntry(int levels, MobEffect primary, MobEffect secondary) {
            this.levels = levels;
            this.primary = primary;
            this.secondary = secondary;
        }

        CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("levels", levels);
            tag.putInt("primary", primary != null ? MobEffect.getId(primary) : -1);
            tag.putInt("secondary", secondary != null ? MobEffect.getId(secondary) : -1);
            return tag;
        }

        static LensEntry read(CompoundTag tag) {
            int primaryId = tag.getInt("primary");
            int secondaryId = tag.getInt("secondary");

            return new LensEntry(tag.getInt("levels"), primaryId >= 0 ? MobEffect.byId(primaryId) : null, secondaryId >= 0 ? MobEffect.byId(secondaryId) : null);
        }
    }
}
