package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

/**
 * Like {@link WaterWalkingBootsItem}, but also lava-safe for walking
 * purposes and counted as a valid fire-damage protector by the shared
 * {@code LivingAttackEvent} listener in {@code RandomThings} - both driven
 * generically there, not by this class.
 */
public class ObsidianWaterWalkingBootsItem extends ArmorItem
{
	public ObsidianWaterWalkingBootsItem(Item.Properties properties)
	{
		super(ArmorMaterial.CHAIN, EquipmentSlotType.FEET, properties.rarity(Rarity.RARE));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return "randomthings:textures/models/armor/obsidianwaterwalkingboots.png";
	}

	@Override
	public boolean isDamageable()
	{
		return false;
	}
}
