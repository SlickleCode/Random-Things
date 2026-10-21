package lumien.randomthings.item;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
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
	/** Iron stats, but the boots' own armor texture ({@code textures/models/armor/superlubricentboots_layer_1.png}). */
	private static final Holder<ArmorMaterial> MATERIAL;

	static
	{
		ArmorMaterial iron = ArmorMaterials.IRON.value();
		MATERIAL = Holder.direct(new ArmorMaterial(iron.defense(), iron.enchantmentValue(), iron.equipSound(), iron.repairIngredient(), List.of(new ArmorMaterial.Layer(new ResourceLocation("randomthings", "superlubricentboots"))), iron.toughness(), iron.knockbackResistance()));
	}

	public SuperLubricentBootsItem(Item.Properties properties)
	{
		super(MATERIAL, ArmorItem.Type.BOOTS, properties.rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.BOOTS.getDurability(15)));
	}
}
