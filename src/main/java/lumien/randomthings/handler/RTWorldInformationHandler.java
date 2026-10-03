package lumien.randomthings.handler;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

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
 * server.getWorld(DimensionType.OVERWORLD).getSavedData()}).
 */
public class RTWorldInformationHandler extends WorldSavedData {
    private static final String ID = "randomthings_world_info";

    private boolean enderDragonDefeated;

    public RTWorldInformationHandler() {
        super(ID);
    }

    public static RTWorldInformationHandler get(MinecraftServer server) {
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(RTWorldInformationHandler::new, ID);
    }

    public boolean isDragonDefeated() {
        return enderDragonDefeated;
    }

    public void setEnderDragonDefeated(boolean defeated) {
        if (defeated != enderDragonDefeated) {
            enderDragonDefeated = defeated;
            markDirty();
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        enderDragonDefeated = nbt.getBoolean("enderDragonDefeated");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("enderDragonDefeated", enderDragonDefeated);
        return compound;
    }
}
