package lumien.randomthings.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Ported from 1.12.2's {@code TileEntitySpectreEnergyInjector} - exposes the placing player's
 * {@link SpectreCoilHandler} pool as a receive-only {@code IEnergyStorage} capability on every side, so
 * any adjacent pipe/machine from another mod can push energy into the owner's pool. The capability
 * object itself delegates live to {@code SpectreCoilHandler.get(world).getStorage(owner)} on every call
 * (matching the original, which built a fresh wrapper on every single {@code getCapability} call)
 * rather than baking in a value at construction time, since 1.14.4's {@link LazyOptional} would
 * otherwise cache a stale first read.
 */
public class SpectreEnergyInjectorTileEntity extends TileEntity {
    private UUID owner;

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return owner == null ? 0 : SpectreCoilHandler.get(world).getStorage(owner).receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return owner == null ? 0 : SpectreCoilHandler.get(world).getStorage(owner).getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return owner == null ? 0 : SpectreCoilHandler.get(world).getStorage(owner).getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return owner != null;
        }
    });

    public SpectreEnergyInjectorTileEntity() {
        super(ModTileEntityTypes.SPECTRE_ENERGY_INJECTOR);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyCap.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        energyCap.invalidate();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (owner != null) {
            compound.putUniqueId("owner", owner);
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("owner")) {
            owner = compound.getUniqueId("owner");
        }
    }
}
