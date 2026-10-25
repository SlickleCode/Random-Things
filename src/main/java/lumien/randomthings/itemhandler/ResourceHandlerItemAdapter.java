package lumien.randomthings.itemhandler;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Lets the mod's slot based code talk to the {@code ResourceHandler<ItemResource>} of any other block (every call is its own transaction). */
public class ResourceHandlerItemAdapter implements IItemHandler
{
	private final ResourceHandler<ItemResource> handler;

	public ResourceHandlerItemAdapter(ResourceHandler<ItemResource> handler)
	{
		this.handler = handler;
	}

	@Override
	public int getSlots()
	{
		return handler.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		ItemResource resource = handler.getResource(slot);
		return resource.isEmpty() ? ItemStack.EMPTY : resource.toStack(handler.getAmountAsInt(slot));
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		throw new UnsupportedOperationException("setStackInSlot on a ResourceHandler view");
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		try (Transaction transaction = Transaction.openRoot())
		{
			int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);

			if (!simulate && inserted > 0)
			{
				transaction.commit();
			}

			return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
		}
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		ItemResource resource = handler.getResource(slot);

		if (resource.isEmpty() || amount <= 0)
		{
			return ItemStack.EMPTY;
		}

		try (Transaction transaction = Transaction.openRoot())
		{
			int extracted = handler.extract(slot, resource, amount, transaction);

			if (!simulate && extracted > 0)
			{
				transaction.commit();
			}

			return extracted <= 0 ? ItemStack.EMPTY : resource.toStack(extracted);
		}
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return handler.isValid(slot, ItemResource.of(stack));
	}
}
