package lumien.randomthings.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryUtil
{
	public static ItemStack getPlayerInventoryItem(Item item, Player player)
	{
		for (int i = 0; i < player.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = player.getInventory().getItem(i);

			if (!stack.isEmpty() && stack.getItem() == item)
			{
				return stack;
			}
		}

		return ItemStack.EMPTY;
	}
}
