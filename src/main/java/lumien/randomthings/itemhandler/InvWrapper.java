package lumien.randomthings.itemhandler;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** An {@link IItemHandler} over a vanilla {@link Container} (the old NeoForge InvWrapper). */
public class InvWrapper implements IItemHandler
{
	private final Container inv;

	public InvWrapper(Container inv)
	{
		this.inv = inv;
	}

	@Override
	public int getSlots()
	{
		return inv.getContainerSize();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inv.getItem(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		inv.setItem(slot, stack);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		if (!inv.canPlaceItem(slot, stack))
		{
			return stack;
		}

		ItemStack existing = inv.getItem(slot);
		int limit = Math.min(inv.getMaxStackSize(), stack.getMaxStackSize());

		if (!existing.isEmpty())
		{
			if (!ItemStack.isSameItemSameComponents(stack, existing))
			{
				return stack;
			}

			limit -= existing.getCount();
		}

		if (limit <= 0)
		{
			return stack;
		}

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate)
		{
			if (existing.isEmpty())
			{
				inv.setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			}
			else
			{
				existing.grow(reachedLimit ? limit : stack.getCount());
			}

			inv.setChanged();
		}

		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
		{
			return ItemStack.EMPTY;
		}

		ItemStack existing = inv.getItem(slot);

		if (existing.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		if (simulate)
		{
			return existing.copyWithCount(Math.min(existing.getCount(), amount));
		}

		ItemStack extracted = inv.removeItem(slot, amount);
		inv.setChanged();
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return inv.canPlaceItem(slot, stack);
	}
}
