package lumien.randomthings.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.Level;

/**
 * A single persistent, world-global (not per-dimension - always resolved
 * against the overworld, matching 1.12.2's own {@code world.getWorld(0)}
 * lookup) flag: has the Ender Dragon been defeated at least once? Used by
 * {@link lumien.randomthings.entity.SpiritEntity}'s spawn-chance roll (see
 * {@code RandomThings}'s {@code LivingDeathEvent} listener) to boost the
 * chance once the End has been beaten. Direct port of 1.12.2's {@code
 * RTWorldInformation}, restructured onto 1.14.4's {@code WorldSavedData}/
 * {@code DimensionSavedDataManager} the same way {@link
 * lumien.randomthings.handler.spectrelens.SpectreLensHandler} already was
 * ({@code world.getMapStorage()} became {@code
 * server.getLevel(World.OVERWORLD).getSavedData()}).
 */
public class RTWorldInformationHandler extends SavedData {
    private static final String ID = "randomthings_world_info";

    private boolean enderDragonDefeated;

    public RTWorldInformationHandler() {
    }

    public static RTWorldInformationHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        RTWorldInformationHandler data = new RTWorldInformationHandler();
        data.readData(nbt);
        return data;
    }

    public static RTWorldInformationHandler get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(RTWorldInformationHandler::new, RTWorldInformationHandler::loadFrom, null), ID);
    }

    public boolean isDragonDefeated() {
        return enderDragonDefeated;
    }

    public void setEnderDragonDefeated(boolean defeated) {
        if (defeated != enderDragonDefeated) {
            enderDragonDefeated = defeated;
            setDirty();
        }
    }

    public void readData(CompoundTag nbt) {
        enderDragonDefeated = nbt.getBoolean("enderDragonDefeated");
    }

    @Override
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.putBoolean("enderDragonDefeated", enderDragonDefeated);
        return compound;
    }
}
