package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * FilteredRedirectorPlateContainer
 */
public class FilteredRedirectorPlateContainer extends Container
{
	IWorldPosCallable pos;

	public FilteredRedirectorPlateContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(2), IWorldPosCallable.DUMMY);
	}

	public FilteredRedirectorPlateContainer(int windowId, PlayerInventory playerInventory, IItemHandler filterInventory, IWorldPosCallable pos)
	{
		super(ModContainerTypes.FILTERED_REDIRECTOR_PLATE, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(filterInventory, 0, 62, 10));
		this.addSlot(new SlotItemHandler(filterInventory, 1, 98, 10));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 47 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 105));
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.PLATE_FILTERED_REDIRECTOR);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			result = stackInSlot.copy();

			if (index < 2)
			{
				if (!this.mergeItemStack(stackInSlot, 2, 38, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, 2, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (stackInSlot.getCount() == result.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, stackInSlot);
		}

		return result;
	}
}
