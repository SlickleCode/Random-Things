package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

/**
 * Negates the slide from every Super Lubricent block - see the shared
 * {@code getSlipperiness} override on {@code SuperLubricentIceBlock}/
 * {@code SuperLubricentPlatformBlock}/{@code SuperLubricentStoneBlock},
 * which each check for these boots directly rather than this class carrying
 * any logic of its own.
 */
public class SuperLubricentBootsItem extends ArmorItem
{
	public SuperLubricentBootsItem(Item.Properties properties)
	{
		super(ArmorMaterial.IRON, EquipmentSlotType.FEET, properties.rarity(Rarity.UNCOMMON));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type)
	{
		return "randomthings:textures/models/armor/superlubricentboots.png";
	}
}
