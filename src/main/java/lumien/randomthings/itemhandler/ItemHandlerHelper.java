package lumien.randomthings.itemhandler;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The few helpers of the old NeoForge ItemHandlerHelper this mod uses. */
public final class ItemHandlerHelper
{
	private ItemHandlerHelper()
	{
	}

	public static ItemStack insertItemStacked(IItemHandler inventory, ItemStack stack, boolean simulate)
	{
		if (inventory == null || stack.isEmpty())
		{
			return stack;
		}

		// first try to stack onto slots that already hold the same item, then into empty slots
		for (int i = 0; i < inventory.getSlots(); i++)
		{
			ItemStack slot = inventory.getStackInSlot(i);

			if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack))
			{
				stack = inventory.insertItem(i, stack, simulate);

				if (stack.isEmpty())
				{
					return ItemStack.EMPTY;
				}
			}
		}

		for (int i = 0; i < inventory.getSlots(); i++)
		{
			if (inventory.getStackInSlot(i).isEmpty())
			{
				stack = inventory.insertItem(i, stack, simulate);

				if (stack.isEmpty())
				{
					return ItemStack.EMPTY;
				}
			}
		}

		return stack;
	}

	public static void giveItemToPlayer(Player player, ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return;
		}

		ItemStack copy = stack.copy();
		player.getInventory().add(copy);

		if (!copy.isEmpty())
		{
			ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 0.5, player.getZ(), copy);
			entity.setPickUpDelay(0);
			player.level().addFreshEntity(entity);
		}
	}

	public static boolean canItemStacksStack(ItemStack a, ItemStack b)
	{
		return !a.isEmpty() && ItemStack.isSameItemSameComponents(a, b);
	}

	public static ItemStack copyStackWithSize(ItemStack stack, int size)
	{
		return size <= 0 ? ItemStack.EMPTY : stack.copyWithCount(size);
	}
}
