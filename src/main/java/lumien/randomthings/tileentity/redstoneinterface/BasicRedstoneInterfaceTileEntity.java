package lumien.randomthings.tileentity.redstoneinterface;

import lumien.randomthings.container.BasicRedstoneInterfaceContainer;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Single-target half of the Redstone Interface family: bind a target with
 * {@link lumien.randomthings.item.RedstoneToolItem}, and this interface
 * pushes its own sensed weak/strong power onto that one remote position
 * (see {@link RedstoneInterfaceTileEntity} for how). Direct port of
 * 1.12.2's {@code TileEntityBasicRedstoneInterface}.
 */
public class BasicRedstoneInterfaceTileEntity extends RedstoneInterfaceTileEntity implements MenuProvider {
    private BlockPos target;

    public BasicRedstoneInterfaceTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.BASIC_REDSTONE_INTERFACE, pos, state);
    }

    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(BlockPos newTarget) {
        if (newTarget != null && newTarget.equals(target)) {
            return;
        }

        BlockPos oldTarget = this.target;
        this.target = newTarget;

        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

            if (!this.level.isClientSide) {
                if (oldTarget != null) {
                    notifyOne(oldTarget);
                }

                if (this.target != null) {
                    notifyOne(this.target);
                }
            }
        }
    }

    private void notifyOne(BlockPos targetPos, net.minecraft.world.level.block.Block neighborBlock) {
        this.level.getBlockState(targetPos).handleNeighborChanged(this.level, targetPos, neighborBlock, this.worldPosition, false);
        this.level.updateNeighborsAt(targetPos, neighborBlock);
    }

    private void notifyOne(BlockPos targetPos) {
        notifyOne(targetPos, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
    }

    @Override
    protected boolean isTargeting(BlockPos pos) {
        return this.target != null && this.target.equals(pos);
    }

    @Override
    protected void notifyTargets(net.minecraft.world.level.block.Block neighborBlock) {
        if (this.target != null) {
            notifyOne(this.target, neighborBlock);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        if (target != null) {
            compound.putInt("targetX", target.getX());
            compound.putInt("targetY", target.getY());
            compound.putInt("targetZ", target.getZ());
        }

    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        if (compound.contains("targetX")) {
            target = new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ"));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new BasicRedstoneInterfaceContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.basic_redstone_interface");
    }
}
