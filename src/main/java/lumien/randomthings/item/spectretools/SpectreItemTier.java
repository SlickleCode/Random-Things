package lumien.randomthings.item.spectretools;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Ported from 1.12.2's {@code EnumHelper.addToolMaterial("spectre", 3, 2000, 8, 3, 22)} (repair item Spectre Ingot, via the item tag
 * {@code randomthings:spectre_repair}). 1.21.2 turned tool tiers into the {@link ToolMaterial} record.
 */
public final class SpectreItemTier
{
	public static final TagKey<Item> REPAIR_TAG = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_repair"));

	public static final ToolMaterial MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2000, 8.0F, 3.0F, 22, REPAIR_TAG);

	/**
	 * Shared by the pickaxe, axe and shovel - matches 1.12.2's +3 block reach. The tool constructors of 1.21.2+ build the whole attribute component themselves, so the
	 * reach is added through {@link ItemAttributeModifierEvent} instead (the tooltip line needs {@code attribute.name.block_interaction_range} from vanilla's lang file).
	 */
	public static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_range_modifier");

	private SpectreItemTier()
	{
	}

	public static void addReach(ItemAttributeModifierEvent event)
	{
		if (event.getItemStack().getItem() instanceof SpectreReachTool)
		{
			event.addModifier(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(REACH_MODIFIER_ID, 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		}
	}

	/** Marks the spectre tools that carry the extra reach. */
	public interface SpectreReachTool
	{
	}
}
