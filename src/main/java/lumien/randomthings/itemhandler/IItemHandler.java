package lumien.randomthings.itemhandler;

import net.minecraft.world.item.ItemStack;

/**
 * The slot based item handler interface NeoForge removed in 21.9 (replaced by {@code ResourceHandler<ItemResource>} and transactions). The mod keeps using
 * this simple API internally; {@link ItemHandlerAdapter} and {@link ResourceHandlerItemAdapter} translate to and from the new capability type.
 */
public interface IItemHandler
{
	int getSlots();

	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

	ItemStack extractItem(int slot, int amount, boolean simulate);

	int getSlotLimit(int slot);

	boolean isItemValid(int slot, ItemStack stack);
}
