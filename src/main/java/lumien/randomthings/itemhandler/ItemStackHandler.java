package lumien.randomthings.itemhandler;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Port of the old NeoForge {@code ItemStackHandler} on top of the 21.9+ {@link ItemStacksResourceHandler}: the slot API (insertItem, extractItem, ...)
 * and the old NBT format ({@code Size}, {@code Items} with {@code Slot}) are kept; the handler itself is the {@code ResourceHandler<ItemResource>}
 * automation talks to, so transactions work through the parent class.
 */
public class ItemStackHandler extends ItemStacksResourceHandler implements IItemHandler
{
	public ItemStackHandler()
	{
		this(1);
	}

	public ItemStackHandler(int size)
	{
		super(size);
	}

	public int getSlots()
	{
		return size();
	}

	public ItemStack getStackInSlot(int slot)
	{
		return stacks.get(slot);
	}

	public void setStackInSlot(int slot, ItemStack stack)
	{
		stacks.set(slot, stack);
		onContentsChanged(slot);
	}

	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		if (!isItemValid(slot, stack))
		{
			return stack;
		}

		ItemStack existing = stacks.get(slot);
		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

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
				stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			}
			else
			{
				existing.grow(reachedLimit ? limit : stack.getCount());
			}

			onContentsChanged(slot);
		}

		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
		{
			return ItemStack.EMPTY;
		}

		ItemStack existing = stacks.get(slot);

		if (existing.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (existing.getCount() <= toExtract)
		{
			if (!simulate)
			{
				stacks.set(slot, ItemStack.EMPTY);
				onContentsChanged(slot);
				return existing;
			}

			return existing.copy();
		}

		if (!simulate)
		{
			stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
			onContentsChanged(slot);
		}

		return existing.copyWithCount(toExtract);
	}

	public int getSlotLimit(int slot)
	{
		return 64;
	}

	public boolean isItemValid(int slot, ItemStack stack)
	{
		return true;
	}

	/** Called after the content of a slot changed (the old NeoForge hook; subclasses override this one). */
	protected void onContentsChanged(int slot)
	{
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack previous)
	{
		onContentsChanged(slot);
	}

	@Override
	protected int getCapacity(int index, ItemResource resource)
	{
		return Math.min(getSlotLimit(index), resource.getMaxStackSize());
	}

	@Override
	public boolean isValid(int index, ItemResource resource)
	{
		return isItemValid(index, resource.toStack());
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider)
	{
		ListTag items = new ListTag();

		for (int i = 0; i < stacks.size(); i++)
		{
			if (!stacks.get(i).isEmpty())
			{
				CompoundTag entry = StackTag.saveStack(stacks.get(i));
				entry.putInt("Slot", i);
				items.add(entry);
			}
		}

		CompoundTag tag = new CompoundTag();
		tag.put("Items", items);
		tag.putInt("Size", stacks.size());
		return tag;
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)
	{
		int size = tag.getIntOr("Size", stacks.size());

		if (size != stacks.size())
		{
			setStacks(NonNullList.withSize(size, ItemStack.EMPTY));
		}
		else
		{
			for (int i = 0; i < stacks.size(); i++)
			{
				stacks.set(i, ItemStack.EMPTY);
			}
		}

		ListTag items = tag.getListOrEmpty("Items");

		for (int i = 0; i < items.size(); i++)
		{
			CompoundTag entry = items.getCompoundOrEmpty(i);
			int slot = entry.getIntOr("Slot", -1);

			if (slot >= 0 && slot < stacks.size())
			{
				stacks.set(slot, StackTag.loadStack(entry));
			}
		}

		for (int i = 0; i < stacks.size(); i++)
		{
			onContentsChanged(i);
		}
	}
}
