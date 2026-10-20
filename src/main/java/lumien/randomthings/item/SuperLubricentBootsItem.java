package lumien.randomthings.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * While worn (and not sneaking), makes every surface as slippery as a Super
 * Lubricent block - not just the three actual Super Lubricent blocks. This
 * item class carries no logic of its own; the effect is implemented by
 * {@code SuperLubricentBootsTransformer.js} (a coremod, see
 * {@code lumien.randomthings.asm.AsmHandler#bootsMaxSlip}), which intercepts
 * the friction lookup inside {@code LivingEntity.travel} globally (a
 * per-block override can't reach blocks this item doesn't own).
 */
public class SuperLubricentBootsItem extends ArmorItem
{
	public SuperLubricentBootsItem(Item.Properties properties)
	{
		super(ArmorMaterials.IRON, ArmorItem.Type.BOOTS, properties.rarity(Rarity.UNCOMMON));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		return "randomthings:textures/models/armor/superlubricentboots.png";
	}
}
