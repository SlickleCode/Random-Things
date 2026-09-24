package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

/**
 * Carried anywhere in the player's inventory (no equip slot needed) to
 * absorb lava damage by spending a slowly-regenerating internal charge - see
 * the shared handler in {@code RandomThings}'s {@code LivingAttackEvent}
 * listener for the actual mechanic, and {@link #inventoryTick} below for the
 * charge regeneration (the inventory-item equivalent of
 * {@link LavaWaderItem#onArmorTick} - a bug found in testing: this item had
 * no charge-regeneration logic at all, so its NBT tag - and therefore any
 * charge - never existed, silently failing the protection check's
 * {@code hasTag()} gate every time).
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

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		if (worldIn.isRemote)
		{
			return;
		}

		if (!stack.hasTag())
		{
			stack.setTag(new CompoundNBT());
		}

		CompoundNBT compound = stack.getTag();

		int chargeCooldown = compound.getInt("chargeCooldown");

		if (chargeCooldown > 0)
		{
			compound.putInt("chargeCooldown", chargeCooldown - 1);
		}
		else
		{
			int charge = compound.getInt("charge");

			if (charge < 200)
			{
				compound.putInt("charge", charge + 1);
			}
		}
	}
}
