package lumien.randomthings.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Direct port of 1.12.2's {@code SlotDyeable}: accepts anything except a block. */
public class DyeableSlot extends Slot {
    public DyeableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !(stack.getItem() instanceof BlockItem);
    }
}
