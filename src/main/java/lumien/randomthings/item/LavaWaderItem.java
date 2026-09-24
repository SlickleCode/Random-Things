package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

/**
 * Lets the player walk on water AND lava (shared {@code LivingUpdateEvent}
 * listener in {@code RandomThings}), and fully negates lava damage by
 * spending an internal 0-200 charge that regenerates 1 point/tick whenever
 * it isn't on a 40-tick post-use cooldown - see the shared
 * {@code LivingAttackEvent} listener there for the lava-damage-cancel and
 * general fire-damage-chance-cancel mechanics, which both read this charge.
 */
public class LavaWaderItem extends ArmorItem
{
	public LavaWaderItem(Item.Properties properties)
	{
		super(ArmorMaterial.CHAIN, EquipmentSlotType.FEET, properties.rarity(Rarity.RARE));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return "randomthings:textures/models/armor/lavawader.png";
	}

	@Override
	public boolean isDamageable()
	{
		return false;
	}

	@Override
	public void onArmorTick(ItemStack stack, World world, PlayerEntity player)
	{
		if (world.isRemote)
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
