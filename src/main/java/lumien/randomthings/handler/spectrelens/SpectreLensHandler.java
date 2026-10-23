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
 * <p>Deliberately kept the original's cross-dimension scoping (1.12.2's {@code getPerWorldStorage()},
 * unlike {@code SpectreCoilHandler}'s deliberately-preserved per-dimension one) - the overworld's own
 * {@code DimensionSavedDataManager}, fetched via {@code server.getLevel(World.OVERWORLD)}
 * regardless of which dimension the call site is actually in, matching {@code World.getPerWorldStorage()}'s
 * original semantics exactly.
 */
public class SpectreLensHandler extends SavedData {
    private static final String ID = "randomthings_spectre_lens_handler";

    private final Map<UUID, LensEntry> lensEntries = new HashMap<>();

    public SpectreLensHandler() {
    }

    public static SpectreLensHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        SpectreLensHandler data = new SpectreLensHandler();
        data.readData(nbt);
        return data;
    }

    public static SpectreLensHandler get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(SpectreLensHandler::new, SpectreLensHandler::loadFrom, null), ID);
    }

    public void removeLens(UUID uuid) {
        if (lensEntries.remove(uuid) != null) {
            setDirty();
        }
    }

    public void addLens(UUID uuid, int levels, Holder<MobEffect> primary, Holder<MobEffect> secondary) {
        lensEntries.put(uuid, new LensEntry(levels, primary, secondary));
        setDirty();
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 60 != 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();

        for (Map.Entry<UUID, LensEntry> entry : lensEntries.entrySet()) {
            ServerPlayer player = playerList.getPlayer(entry.getKey());

            if (player == null) {
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

    public void readData(CompoundTag nbt) {
        lensEntries.clear();

        ListTag list = nbt.getList("lensEntries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            lensEntries.put(entryTag.getUUID("uuid"), LensEntry.read(entryTag));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, LensEntry> entry : lensEntries.entrySet()) {
            CompoundTag entryTag = entry.getValue().write();
            entryTag.putUUID("uuid", entry.getKey());
            list.add(entryTag);
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
            int primaryId = tag.getInt("primary");
            int secondaryId = tag.getInt("secondary");

            return new LensEntry(tag.getInt("levels"), primaryId >= 0 ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(primaryId).orElse(null) : null, secondaryId >= 0 ? net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(secondaryId).orElse(null) : null);
        }
    }
}
