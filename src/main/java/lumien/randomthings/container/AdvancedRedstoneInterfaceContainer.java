package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * 9 Position Filter slots (one row) plus the usual player inventory - same
 * structure as {@link FilteredSuperLubricentPlatformContainer}, just 9
 * filter slots instead of 1. Direct port of 1.12.2's {@code
 * ContainerAdvancedRedstoneInterface} (minus its "Position Filter only"
 * item-type enforcement, which now lives on the {@code ItemStackHandler}
 * itself - see {@code AdvancedRedstoneInterfaceTileEntity}).
 */
public class AdvancedRedstoneInterfaceContainer extends AbstractContainerMenu {
    ContainerLevelAccess pos;

    public AdvancedRedstoneInterfaceContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, new ItemStackHandler(9), ContainerLevelAccess.NULL);
    }

    public AdvancedRedstoneInterfaceContainer(int windowId, Inventory playerInventory, IItemHandler positionInventory, ContainerLevelAccess pos) {
        super(ModContainerTypes.ADVANCED_REDSTONE_INTERFACE, windowId);

        this.pos = pos;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(positionInventory, col, 8 + col * 18, 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.ADVANCED_REDSTONE_INTERFACE);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
