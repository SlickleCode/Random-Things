package lumien.randomthings.handler.spectrelens;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

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
 * {@code DimensionSavedDataManager}, fetched via {@code server.getWorld(DimensionType.OVERWORLD)}
 * regardless of which dimension the call site is actually in, matching {@code World.getPerWorldStorage()}'s
 * original semantics exactly.
 */
public class SpectreLensHandler extends WorldSavedData {
    private static final String ID = "randomthings_spectre_lens_handler";

    private final Map<UUID, LensEntry> lensEntries = new HashMap<>();

    public SpectreLensHandler() {
        super(ID);
    }

    public static SpectreLensHandler get(MinecraftServer server) {
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(SpectreLensHandler::new, ID);
    }

    public void removeLens(UUID uuid) {
        if (lensEntries.remove(uuid) != null) {
            markDirty();
        }
    }

    public void addLens(UUID uuid, int levels, Effect primary, Effect secondary) {
        lensEntries.put(uuid, new LensEntry(levels, primary, secondary));
        markDirty();
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCounter() % 60 != 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();

        for (Map.Entry<UUID, LensEntry> entry : lensEntries.entrySet()) {
            ServerPlayerEntity player = playerList.getPlayerByUUID(entry.getKey());

            if (player == null) {
                continue;
            }

            LensEntry lens = entry.getValue();
            int amplifier = lens.primary != null && lens.primary == lens.secondary ? 1 : 0;

            if (lens.primary != null) {
                player.addPotionEffect(new EffectInstance(lens.primary, 20 * 10, amplifier, true, true));
            }

            if (lens.secondary != null && lens.secondary != lens.primary) {
                player.addPotionEffect(new EffectInstance(lens.secondary, 20 * 10, amplifier, true, true));
            }
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        lensEntries.clear();

        ListNBT list = nbt.getList("lensEntries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entryTag = list.getCompound(i);
            lensEntries.put(entryTag.getUniqueId("uuid"), LensEntry.read(entryTag));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (Map.Entry<UUID, LensEntry> entry : lensEntries.entrySet()) {
            CompoundNBT entryTag = entry.getValue().write();
            entryTag.putUniqueId("uuid", entry.getKey());
            list.add(entryTag);
        }

        compound.put("lensEntries", list);
        return compound;
    }

    private static class LensEntry {
        final int levels;
        final Effect primary;
        final Effect secondary;

        LensEntry(int levels, Effect primary, Effect secondary) {
            this.levels = levels;
            this.primary = primary;
            this.secondary = secondary;
        }

        CompoundNBT write() {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt("levels", levels);
            tag.putInt("primary", primary != null ? Effect.getId(primary) : -1);
            tag.putInt("secondary", secondary != null ? Effect.getId(secondary) : -1);
            return tag;
        }

        static LensEntry read(CompoundNBT tag) {
            int primaryId = tag.getInt("primary");
            int secondaryId = tag.getInt("secondary");

            return new LensEntry(tag.getInt("levels"), primaryId >= 0 ? Effect.get(primaryId) : null, secondaryId >= 0 ? Effect.get(secondaryId) : null);
        }
    }
}
