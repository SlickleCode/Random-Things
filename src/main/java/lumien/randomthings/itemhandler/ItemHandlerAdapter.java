package lumien.randomthings.itemhandler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes an {@link IItemHandler} as the {@code ResourceHandler<ItemResource>} the 21.9+ item capability needs. Changes are applied at once and a snapshot
 * of all slots is taken first, so an aborted transaction restores the old content.
 */
public class ItemHandlerAdapter implements ResourceHandler<ItemResource>
{
	private final IItemHandler handler;

	private final SnapshotJournal<List<ItemStack>> journal = new SnapshotJournal<>()
	{
		@Override
		protected List<ItemStack> createSnapshot()
		{
			List<ItemStack> copy = new ArrayList<>();

			for (int i = 0; i < handler.getSlots(); i++)
			{
				copy.add(handler.getStackInSlot(i).copy());
			}

			return copy;
		}

		@Override
		protected void revertToSnapshot(List<ItemStack> snapshot)
		{
			for (int i = 0; i < snapshot.size() && i < handler.getSlots(); i++)
			{
				handler.setStackInSlot(i, snapshot.get(i).copy());
			}
		}
	};

	public ItemHandlerAdapter(IItemHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public int size()
	{
		return handler.getSlots();
	}

	@Override
	public ItemResource getResource(int index)
	{
		return ItemResource.of(handler.getStackInSlot(index));
	}

	@Override
	public long getAmountAsLong(int index)
	{
		return handler.getStackInSlot(index).getCount();
	}

	@Override
	public long getCapacityAsLong(int index, ItemResource resource)
	{
		return Math.min(handler.getSlotLimit(index), resource.isEmpty() ? 64 : resource.getMaxStackSize());
	}

	@Override
	public boolean isValid(int index, ItemResource resource)
	{
		return handler.isItemValid(index, resource.toStack());
	}

	@Override
	public int insert(int index, ItemResource resource, int amount, TransactionContext transaction)
	{
		if (resource.isEmpty() || amount <= 0)
		{
			return 0;
		}

		ItemStack remainder = handler.insertItem(index, resource.toStack(amount), true);
		int inserted = amount - remainder.getCount();

		if (inserted <= 0)
		{
			return 0;
		}

		journal.updateSnapshots(transaction);
		handler.insertItem(index, resource.toStack(inserted), false);
		return inserted;
	}

	@Override
	public int extract(int index, ItemResource resource, int amount, TransactionContext transaction)
	{
		if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getStackInSlot(index)))
		{
			return 0;
		}

		ItemStack extracted = handler.extractItem(index, amount, true);

		if (extracted.isEmpty())
		{
			return 0;
		}

		journal.updateSnapshots(transaction);
		return handler.extractItem(index, extracted.getCount(), false).getCount();
	}
}
