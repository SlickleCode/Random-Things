package lumien.randomthings.tileentity;

import java.util.UUID;

import lumien.randomthings.block.SpectreCoilBlock;
import lumien.randomthings.block.SpectreCoilBlock.CoilType;
import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Ported from 1.12.2's {@code TileEntitySpectreCoil} - the "output tap" of the Spectre energy network:
 * every tick, pushes energy out of the owner's {@link SpectreCoilHandler} pool into whatever's attached
 * on the block's supporting face (an external machine from another mod). {@code GENESIS} coils skip the
 * pool entirely and generate a flat, huge amount directly (matching 1.12.2's own {@code 10000000}/tick).
 *
 * <p>{@code NUMBER} coils (a config-gated, dungeon-loot-table-only tier in 1.12.2 - see
 * {@code LootHandler}/{@code Worldgen.NUMBERED_COILS}) are deliberately not ported: they need real
 * loot-table-injection infrastructure this port hasn't built for anything yet, and are a separate,
 * lower-value worldgen concern from the energy network itself. Flag for a future slice if wanted.
 */
public class SpectreCoilTileEntity extends TileEntity implements ITickableTileEntity {
    private UUID owner;

    public SpectreCoilTileEntity() {
        super(ModTileEntityTypes.SPECTRE_COIL);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    private CoilType coilType() {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof SpectreCoilBlock ? ((SpectreCoilBlock) state.getBlock()).getCoilType() : CoilType.NORMAL;
    }

    @Override
    public void tick() {
        if (world.isRemote || owner == null) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof SpectreCoilBlock)) {
            return;
        }

        // FACING points away from the block this coil is mounted on (matching the torch/lever
        // convention already used elsewhere in this port), so the support sits the opposite way.
        Direction facing = state.get(SpectreCoilBlock.FACING);
        BlockPos targetPos = pos.offset(facing.getOpposite());
        TileEntity targetTe = world.getTileEntity(targetPos);

        if (targetTe == null) {
            return;
        }

        // The support's face touching this coil is the one pointing back at it, i.e. `facing` itself.
        LazyOptional<IEnergyStorage> targetCap = targetTe.getCapability(CapabilityEnergy.ENERGY, facing);
        IEnergyStorage targetStorage = targetCap.orElse(null);

        if (targetStorage == null || !targetStorage.canReceive()) {
            return;
        }

        CoilType type = coilType();

        if (type == CoilType.GENESIS) {
            targetStorage.receiveEnergy(10000000, false);
            return;
        }

        IEnergyStorage coilStorage = SpectreCoilHandler.get(world).getStorageCoil(owner);

        int available = coilStorage.extractEnergy(type.rate, true);
        int remaining = available - targetStorage.receiveEnergy(available, false);

        if (remaining != available) {
            coilStorage.extractEnergy(available - remaining, false);
        }
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
