package lumien.randomthings.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryUtil
{
	public static ItemStack getPlayerInventoryItem(Item item, PlayerEntity player)
	{
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);

			if (!stack.isEmpty() && stack.getItem() == item)
			{
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}
}
