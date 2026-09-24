package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

/**
 * While worn (and not sneaking), makes every surface as slippery as a Super
 * Lubricent block - not just the three actual Super Lubricent blocks. This
 * item class carries no logic of its own; the effect is implemented by
 * {@code SuperLubricentBootsMixin}, which intercepts the friction lookup
 * inside {@code LivingEntity.travel} globally (a per-block override can't
 * reach blocks this item doesn't own).
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
