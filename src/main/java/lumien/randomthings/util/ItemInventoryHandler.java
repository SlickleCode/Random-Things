package lumien.randomthings.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * A fixed-size {@link ItemStackHandler} whose contents live in one item's own
 * NBT (under {@code childTagName}), loading on construction and saving back
 * on every change. Modern (capability-based) equivalent of 1.12.2's
 * {@code InventoryItem} - used by {@link lumien.randomthings.item.PortableSoundDampenerItem}
 * for its carried 9-slot filter, the same way {@link lumien.randomthings.item.PositionFilterItem}-style
 * items already store their own state directly in NBT, just for a whole
 * inventory instead of a few primitive tags.
 */
public class ItemInventoryHandler extends ItemStackHandler {
    private final ItemStack stack;
    private final String childTagName;

    public ItemInventoryHandler(ItemStack stack, String childTagName, int size) {
        super(size);

        this.stack = stack;
        this.childTagName = childTagName;

        if (StackTag.getElement(stack, childTagName) != null) {
            deserializeNBT(StackTag.provider(), StackTag.getElement(stack, childTagName));
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        StackTag.edit(stack, t -> t.put(childTagName, serializeNBT(StackTag.provider())));
    }
}
