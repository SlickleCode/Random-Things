package lumien.randomthings.item.spectretools;

import java.util.UUID;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Ported from 1.12.2's {@code EnumHelper.addToolMaterial("spectre", 3, 2000, 8, 3, 22)} (repair item
 * Spectre Ingot) - 1.14.4 replaced the enum-extension trick with a plain {@link IItemTier}
 * implementation, modeled directly on vanilla's own {@code ItemTier} enum constants. The repair
 * {@link Ingredient} is wrapped in a lazily exactly like every vanilla tier, since
 * {@code ModItems#SPECTRE_INGOT} isn't populated by its {@code ObjectHolder} until after the item
 * registry event this tier is constructed during has finished.
 */
public class SpectreItemTier implements Tier {
    public static final SpectreItemTier INSTANCE = new SpectreItemTier();

    /**
     * Shared by every Spectre tool but the sword - matches 1.12.2's +3 block reach on pickaxe/axe/shovel.
     *
     * <p>The tooltip line this modifier produces needs {@code lang/en_us.json}'s
     * {@code attribute.name.generic.reachDistance} entry - ground-truthed that this exact build's own
     * {@code forge/lang/en_us.json} ships a mismatched key for it ({@code generic.reachDistance} with no
     * {@code attribute.name.} prefix, plus an unrelated {@code attribute.name.generic.reach_distance}
     * snake_case key that doesn't match {@link net.minecraft.world.entity.player.Player#REACH_DISTANCE}'s
     * real camelCase name either), so without our own entry the tooltip showed the raw untranslated key
     * instead of "Reach Distance".
     */
    public static final net.minecraft.resources.ResourceLocation REACH_MODIFIER_ID = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("randomthings", "spectre_range_modifier");

    private Ingredient repairMaterial;

    /** The +3 block reach of the pickaxe, axe and shovel while held in the main hand, added to their normal tool attributes. */
    public static net.minecraft.world.item.component.ItemAttributeModifiers withReach(net.minecraft.world.item.component.ItemAttributeModifiers base) {
        return base.withModifierAdded(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(REACH_MODIFIER_ID, 3, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public int getUses() {
        return 2000;
    }

    @Override
    public float getSpeed() {
        return 8.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F;
    }

    @Override
    public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
        return net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public Ingredient getRepairIngredient() {
        if (repairMaterial == null) {
            repairMaterial = Ingredient.of(ModItems.SPECTRE_INGOT);
        }
        return repairMaterial;
    }
}
