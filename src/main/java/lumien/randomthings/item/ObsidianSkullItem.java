package lumien.randomthings.item;

import net.minecraft.item.Item;

/**
 * Carried anywhere in the player's inventory (no equip slot needed) to grant
 * a chance to shrug off fire damage - see the shared handler in
 * {@code RandomThings}'s {@code LivingAttackEvent} listener for the actual
 * mechanic, which this item has no logic of its own for.
 */
public class ObsidianSkullItem extends Item
{
	public ObsidianSkullItem(Item.Properties properties)
	{
		super(properties);
	}
}
