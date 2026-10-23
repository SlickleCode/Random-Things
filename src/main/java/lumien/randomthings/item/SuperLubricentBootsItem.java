package lumien.randomthings.item;

import java.util.EnumMap;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

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
	/** Iron stats, but the boots' own worn model ({@code assets/randomthings/equipment/superlubricentboots.json}). */
	private static final ArmorMaterial MATERIAL;

	static
	{
		ArmorMaterial iron = ArmorMaterials.IRON;
		MATERIAL = new ArmorMaterial(iron.durability(), new EnumMap<>(iron.defense()), iron.enchantmentValue(), iron.equipSound(), iron.toughness(), iron.knockbackResistance(), iron.repairIngredient(),
				ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("randomthings", "superlubricentboots")));
	}

	public SuperLubricentBootsItem(Item.Properties properties)
	{
		super(MATERIAL, ArmorType.BOOTS, properties.rarity(Rarity.UNCOMMON));
	}
}
