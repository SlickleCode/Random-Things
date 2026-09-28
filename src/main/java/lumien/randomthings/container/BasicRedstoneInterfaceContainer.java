package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;

/**
 * Read-only status display - the actual target is set by right-clicking
 * with a Redstone Tool, not through this GUI. Same pattern as {@link
 * RedstoneObserverContainer}.
 */
public class BasicRedstoneInterfaceContainer extends Container {
    IWorldPosCallable pos;

    public IntReferenceHolder hasTarget = IntReferenceHolder.single();
    public IntReferenceHolder targetX = IntReferenceHolder.single();
    public IntReferenceHolder targetY = IntReferenceHolder.single();
    public IntReferenceHolder targetZ = IntReferenceHolder.single();

    public BasicRedstoneInterfaceContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, IWorldPosCallable.DUMMY);
    }

    public BasicRedstoneInterfaceContainer(int windowId, IWorldPosCallable pos) {
        super(ModContainerTypes.BASIC_REDSTONE_INTERFACE, windowId);

        this.pos = pos;

        this.trackInt(hasTarget);
        this.trackInt(targetX);
        this.trackInt(targetY);
        this.trackInt(targetZ);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.pos, playerIn, ModBlocks.BASIC_REDSTONE_INTERFACE);
    }

    @Override
    public void detectAndSendChanges() {
        this.pos.consume((world, pos) -> {
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof BasicRedstoneInterfaceTileEntity) {
                BlockPos target = ((BasicRedstoneInterfaceTileEntity) te).getTarget();

                this.hasTarget.set(target != null ? 1 : 0);
                this.targetX.set(target != null ? target.getX() : 0);
                this.targetY.set(target != null ? target.getY() : 0);
                this.targetZ.set(target != null ? target.getZ() : 0);
            }
        });

        super.detectAndSendChanges();
    }
}
