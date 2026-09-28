package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
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
public class AdvancedRedstoneInterfaceContainer extends Container {
    IWorldPosCallable pos;

    public AdvancedRedstoneInterfaceContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(9), IWorldPosCallable.DUMMY);
    }

    public AdvancedRedstoneInterfaceContainer(int windowId, PlayerInventory playerInventory, IItemHandler positionInventory, IWorldPosCallable pos) {
        super(ModContainerTypes.ADVANCED_REDSTONE_INTERFACE, windowId);

        this.pos = pos;

        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(positionInventory, col, 8 + col * 18, 20));
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
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ADVANCED_REDSTONE_INTERFACE);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (index < 9) {
                if (!this.mergeItemStack(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
