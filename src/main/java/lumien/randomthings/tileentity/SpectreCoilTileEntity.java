package lumien.randomthings.tileentity;

import java.util.UUID;

import lumien.randomthings.block.SpectreCoilBlock;
import lumien.randomthings.block.SpectreCoilBlock.CoilType;
import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
public class SpectreCoilTileEntity extends BlockEntity {
    private UUID owner;

    public SpectreCoilTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SPECTRE_COIL, pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    private CoilType coilType() {
        BlockState state = level.getBlockState(worldPosition);
        return state.getBlock() instanceof SpectreCoilBlock ? ((SpectreCoilBlock) state.getBlock()).getCoilType() : CoilType.NORMAL;
    }

    public void tick() {
        if (level.isClientSide || owner == null) {
            return;
        }

        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof SpectreCoilBlock)) {
            return;
        }

        // FACING points away from the block this coil is mounted on (matching the torch/lever
        // convention already used elsewhere in this port), so the support sits the opposite way.
        Direction facing = state.getValue(SpectreCoilBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing.getOpposite());
        BlockEntity targetTe = level.getBlockEntity(targetPos);

        if (targetTe == null) {
            return;
        }

        // The support's face touching this coil is the one pointing back at it, i.e. `facing` itself.
        LazyOptional<IEnergyStorage> targetCap = targetTe.getCapability(ForgeCapabilities.ENERGY, facing);
        IEnergyStorage targetStorage = targetCap.orElse(null);

        if (targetStorage == null || !targetStorage.canReceive()) {
            return;
        }

        CoilType type = coilType();

        if (type == CoilType.GENESIS) {
            targetStorage.receiveEnergy(10000000, false);
            return;
        }

        IEnergyStorage coilStorage = SpectreCoilHandler.get(level).getStorageCoil(owner);

        int available = coilStorage.extractEnergy(type.rate, true);
        int remaining = available - targetStorage.receiveEnergy(available, false);

        if (remaining != available) {
            coilStorage.extractEnergy(available - remaining, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if (owner != null) {
            compound.putUUID("owner", owner);
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        }
    }
}
