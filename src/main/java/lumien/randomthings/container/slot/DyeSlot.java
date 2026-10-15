package lumien.randomthings.container.slot;

import lumien.randomthings.util.DyeUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Direct port of 1.12.2's {@code SlotDye}. */
public class DyeSlot extends Slot {
    public DyeSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return DyeUtil.isVanillaDye(stack);
    }
}
