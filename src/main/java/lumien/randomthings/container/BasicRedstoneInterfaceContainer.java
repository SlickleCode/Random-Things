package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.core.BlockPos;

/**
 * Read-only status display - the actual target is set by right-clicking
 * with a Redstone Tool, not through this GUI. Same pattern as {@link
 * RedstoneObserverContainer}.
 */
public class BasicRedstoneInterfaceContainer extends AbstractContainerMenu {
    ContainerLevelAccess pos;

    public DataSlot hasTarget = DataSlot.standalone();
    public DataSlot targetX = DataSlot.standalone();
    public DataSlot targetY = DataSlot.standalone();
    public DataSlot targetZ = DataSlot.standalone();

    public BasicRedstoneInterfaceContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, ContainerLevelAccess.NULL);
    }

    public BasicRedstoneInterfaceContainer(int windowId, ContainerLevelAccess pos) {
        super(ModContainerTypes.BASIC_REDSTONE_INTERFACE, windowId);

        this.pos = pos;

        this.addDataSlot(hasTarget);
        this.addDataSlot(targetX);
        this.addDataSlot(targetY);
        this.addDataSlot(targetZ);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.BASIC_REDSTONE_INTERFACE);
    }

    @Override
    public void broadcastChanges() {
        this.pos.execute((world, pos) -> {
            BlockEntity te = world.getBlockEntity(pos);

            if (te instanceof BasicRedstoneInterfaceTileEntity) {
                BlockPos target = ((BasicRedstoneInterfaceTileEntity) te).getTarget();

                this.hasTarget.set(target != null ? 1 : 0);
                this.targetX.set(target != null ? target.getX() : 0);
                this.targetY.set(target != null ? target.getY() : 0);
                this.targetZ.set(target != null ? target.getZ() : 0);
            }
        });

        super.broadcastChanges();
    }
}
