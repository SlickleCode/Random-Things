package lumien.randomthings.tileentity;

import lumien.randomthings.util.CapUtil;
import java.util.UUID;

import lumien.randomthings.block.SpectreCoilBlock;
import lumien.randomthings.block.SpectreCoilBlock.CoilType;
import lumien.randomthings.handler.spectrecoils.SpectreCoilHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;


import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Ported from 1.12.2's {@code TileEntitySpectreCoil} - the "output tap" of the Spectre energy network:
 * every tick, pushes energy out of the owner's {@link SpectreCoilHandler} pool into whatever's attached
 * on the block's supporting face (an external machine from another mod). {@code GENESIS} coils skip the
 * pool entirely and generate a flat, huge amount directly (matching 1.12.2's own {@code 10000000}/tick).
 *
 * <p>{@code NUMBER} coils (the dungeon-loot-only tier, injected by {@code LootHandler} behind the
 * {@code NumberedSpectreCoils} config option) also skip the pool and generate their flat {@code rate}
 * (128, 1.12.2's {@code NumberedSpectreCoilEnergy} default) per tick, like 1.12.2.
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
        if (level.isClientSide() || owner == null) {
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
        IEnergyStorage targetStorage = CapUtil.energy(targetTe, facing);

        if (targetStorage == null || !targetStorage.canReceive()) {
            return;
        }

        CoilType type = coilType();

        if (type == CoilType.GENESIS) {
            targetStorage.receiveEnergy(10000000, false);
            return;
        }

        if (type == CoilType.NUMBER) {
            targetStorage.receiveEnergy(type.rate, false);
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
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.saveAdditional(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (owner != null) {
            compound.put("owner", lumien.randomthings.util.TagIO.writeUuid(owner));
        }

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("owner")) {
            owner = lumien.randomthings.util.TagIO.readUuid(compound.get("owner"));
        }
    }
}
