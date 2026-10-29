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
 * <p>One lens per player per dimension (1.12.2's {@code getPerWorldStorage()} was per dimension and only
 * applied effects to players in that world): a single store (the overworld's {@code DimensionSavedDataManager},
 * fetched via {@code server.getWorld(DimensionType.OVERWORLD)}) holds every lens keyed by (dimension id, owner),
 * and a lens only affects its owner while the owner is in that dimension.
 */
public class SpectreLensHandler extends WorldSavedData {
    private static final String ID = "randomthings_spectre_lens_handler";

    // dimension id -> (owner -> lens): one lens per player per dimension, the lens only affects its owner while the owner is in that dimension
    private final Map<Integer, Map<UUID, LensEntry>> lensEntries = new HashMap<>();

    public SpectreLensHandler() {
        super(ID);
    }

    public static SpectreLensHandler get(MinecraftServer server) {
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(SpectreLensHandler::new, ID);
    }

    public void removeLens(int dimension, UUID uuid) {
        Map<UUID, LensEntry> inDimension = lensEntries.get(dimension);

        if (inDimension != null && inDimension.remove(uuid) != null) {
            if (inDimension.isEmpty()) {
                lensEntries.remove(dimension);
            }
            markDirty();
        }
    }

    public void addLens(int dimension, UUID uuid, int levels, Effect primary, Effect secondary) {
        lensEntries.computeIfAbsent(dimension, d -> new HashMap<>()).put(uuid, new LensEntry(levels, primary, secondary));
        markDirty();
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCounter() % 60 != 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();

        for (Map.Entry<Integer, Map<UUID, LensEntry>> dimensionEntry : lensEntries.entrySet()) {
            for (Map.Entry<UUID, LensEntry> entry : dimensionEntry.getValue().entrySet()) {
                ServerPlayerEntity player = playerList.getPlayerByUUID(entry.getKey());

                // only applies while the owner is in the lens's own dimension
                if (player == null || player.world.getDimension().getType().getId() != dimensionEntry.getKey()) {
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
    }

    @Override
    public void read(CompoundNBT nbt) {
        lensEntries.clear();

        ListNBT list = nbt.getList("lensEntries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entryTag = list.getCompound(i);

            // old saves (no dimension tag) are dropped: the lens block entity re-registers itself in its own dimension within 80 ticks
            if (!entryTag.contains("dimension")) {
                continue;
            }

            lensEntries.computeIfAbsent(entryTag.getInt("dimension"), d -> new HashMap<>()).put(entryTag.getUniqueId("uuid"), LensEntry.read(entryTag));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (Map.Entry<Integer, Map<UUID, LensEntry>> dimensionEntry : lensEntries.entrySet()) {
            for (Map.Entry<UUID, LensEntry> entry : dimensionEntry.getValue().entrySet()) {
                CompoundNBT entryTag = entry.getValue().write();
                entryTag.putUniqueId("uuid", entry.getKey());
                entryTag.putInt("dimension", dimensionEntry.getKey());
                list.add(entryTag);
            }
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
