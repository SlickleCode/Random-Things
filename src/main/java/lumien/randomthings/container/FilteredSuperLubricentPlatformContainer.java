package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * FilteredSuperLubricentPlatformContainer
 */
public class FilteredSuperLubricentPlatformContainer extends AbstractContainerMenu
{
	ContainerLevelAccess pos;

	public FilteredSuperLubricentPlatformContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(1), ContainerLevelAccess.NULL);
	}

	public FilteredSuperLubricentPlatformContainer(int windowId, Inventory playerInventory, IItemHandler filterInventory, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.FILTERED_SUPER_LUBRICENT_PLATFORM, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(filterInventory, 0, 80, 10));

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
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.FILTERED_SUPER_LUBRICENT_PLATFORM);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem())
		{
			ItemStack stackInSlot = slot.getItem();
			result = stackInSlot.copy();

			if (index < 1)
			{
				if (!this.moveItemStackTo(stackInSlot, 1, 37, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.moveItemStackTo(stackInSlot, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
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
