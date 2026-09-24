package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

/**
 * The hop-across-water effect while worn is driven generically by the shared
 * {@code LivingUpdateEvent} listener in {@code RandomThings} - this class
 * only carries the texture/rarity/indestructibility.
 */
public class WaterWalkingBootsItem extends ArmorItem
{
	public WaterWalkingBootsItem(Item.Properties properties)
	{
		super(ArmorMaterial.CHAIN, EquipmentSlotType.FEET, properties.rarity(Rarity.RARE));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return "randomthings:textures/models/armor/waterwalkingboots.png";
	}

	@Override
	public boolean isDamageable()
	{
		return false;
	}
}
