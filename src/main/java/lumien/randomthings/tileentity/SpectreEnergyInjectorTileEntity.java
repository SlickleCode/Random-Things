package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import java.util.UUID;

import javax.annotation.Nullable;

import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;



import net.neoforged.neoforge.energy.IEnergyStorage;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ported from 1.12.2's {@code TileEntitySpectreEnergyInjector} - exposes the placing player's
 * {@link SpectreCoilHandler} pool as a receive-only {@code IEnergyStorage} capability on every side, so
 * any adjacent pipe/machine from another mod can push energy into the owner's pool. The capability
 * object itself delegates live to {@code SpectreCoilHandler.get(world).getStorage(owner)} on every call
 * (matching the original, which built a fresh wrapper on every single {@code getCapability} call)
 * rather than baking in a value at construction time.
 */
public class SpectreEnergyInjectorTileEntity extends BlockEntity {
    private UUID owner;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return owner == null ? 0 : SpectreCoilHandler.get(level).getStorage(owner).receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return owner == null ? 0 : SpectreCoilHandler.get(level).getStorage(owner).getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return owner == null ? 0 : SpectreCoilHandler.get(level).getStorage(owner).getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return owner != null;
        }
    };

    public SpectreEnergyInjectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SPECTRE_ENERGY_INJECTOR, pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        if (owner != null) {
            compound.putUUID("owner", owner);
        }

    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        }
    }
}
