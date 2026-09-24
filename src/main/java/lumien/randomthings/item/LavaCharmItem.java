package lumien.randomthings.item;

import net.minecraft.item.Item;

/**
 * Carried anywhere in the player's inventory (no equip slot needed) to
 * absorb lava damage by spending a slowly-regenerating internal charge - see
 * the shared handler in {@code RandomThings}'s {@code LivingAttackEvent}
 * listener for the actual mechanic.
 * <p>
 * Disclosed simplification: 1.12.2 could also be worn in a Baubles amulet
 * slot as an alternative to carrying it in the main inventory. Third-party
 * mod compat (including Baubles) is dropped for this port, and 1.12.2's own
 * logic already fell back to a plain inventory-carried check when no Baubles
 * slot was equipped, so that fallback is all this port needs - the item
 * still works, just always via the inventory-carried path.
 */
public class LavaCharmItem extends Item
{
	public LavaCharmItem(Item.Properties properties)
	{
		super(properties);
	}
}
