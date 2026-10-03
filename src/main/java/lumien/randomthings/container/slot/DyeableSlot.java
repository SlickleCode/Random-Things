package lumien.randomthings.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/** Direct port of 1.12.2's {@code SlotDyeable}: accepts anything except a block. */
public class DyeableSlot extends Slot {
    public DyeableSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !(stack.getItem() instanceof BlockItem);
    }
}
