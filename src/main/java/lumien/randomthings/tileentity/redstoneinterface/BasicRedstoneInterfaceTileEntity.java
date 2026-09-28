package lumien.randomthings.tileentity.redstoneinterface;

import lumien.randomthings.container.BasicRedstoneInterfaceContainer;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Single-target half of the Redstone Interface family: bind a target with
 * {@link lumien.randomthings.item.RedstoneToolItem}, and this interface
 * pushes its own sensed weak/strong power onto that one remote position
 * (see {@link RedstoneInterfaceTileEntity} for how). Direct port of
 * 1.12.2's {@code TileEntityBasicRedstoneInterface}.
 */
public class BasicRedstoneInterfaceTileEntity extends RedstoneInterfaceTileEntity implements INamedContainerProvider {
    private BlockPos target;

    public BasicRedstoneInterfaceTileEntity() {
        super(ModTileEntityTypes.BASIC_REDSTONE_INTERFACE);
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

        this.markDirty();

        if (this.world != null) {
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);

            if (!this.world.isRemote) {
                if (oldTarget != null) {
                    notifyOne(oldTarget);
                }

                if (this.target != null) {
                    notifyOne(this.target);
                }
            }
        }
    }

    private void notifyOne(BlockPos targetPos, net.minecraft.block.Block neighborBlock) {
        this.world.getBlockState(targetPos).neighborChanged(this.world, targetPos, neighborBlock, this.pos, false);
        this.world.notifyNeighborsOfStateChange(targetPos, neighborBlock);
    }

    private void notifyOne(BlockPos targetPos) {
        notifyOne(targetPos, net.minecraft.block.Blocks.REDSTONE_BLOCK);
    }

    @Override
    protected boolean isTargeting(BlockPos pos) {
        return this.target != null && this.target.equals(pos);
    }

    @Override
    protected void notifyTargets(net.minecraft.block.Block neighborBlock) {
        if (this.target != null) {
            notifyOne(this.target, neighborBlock);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (target != null) {
            compound.putInt("targetX", target.getX());
            compound.putInt("targetY", target.getY());
            compound.putInt("targetZ", target.getZ());
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("targetX")) {
            target = new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ"));
        }
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BasicRedstoneInterfaceContainer(windowId, IWorldPosCallable.of(this.world, pos));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.basic_redstone_interface");
    }
}
