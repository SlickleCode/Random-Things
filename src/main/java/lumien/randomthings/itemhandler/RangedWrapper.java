package lumien.randomthings.itemhandler;

import net.minecraft.world.item.ItemStack;

/** The slots {@code [minSlot, maxSlot)} of another handler (the old NeoForge RangedWrapper). */
public class RangedWrapper implements IItemHandler
{
	private final IItemHandler compose;
	private final int minSlot;
	private final int maxSlot;

	public RangedWrapper(IItemHandler compose, int minSlot, int maxSlotExclusive)
	{
		this.compose = compose;
		this.minSlot = minSlot;
		this.maxSlot = maxSlotExclusive;
	}

	private boolean contains(int slot)
	{
		return slot >= 0 && slot < maxSlot - minSlot;
	}

	@Override
	public int getSlots()
	{
		return maxSlot - minSlot;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return contains(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		if (contains(slot))
		{
			compose.setStackInSlot(slot + minSlot, stack);
		}
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		return contains(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		return contains(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return contains(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return contains(slot) && compose.isItemValid(slot + minSlot, stack);
	}
}
