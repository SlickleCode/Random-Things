package lumien.randomthings.container.slot;

import lumien.randomthings.util.DyeUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

/** Direct port of 1.12.2's {@code SlotDye}. */
public class DyeSlot extends Slot {
    public DyeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return DyeUtil.isVanillaDye(stack);
    }
}
