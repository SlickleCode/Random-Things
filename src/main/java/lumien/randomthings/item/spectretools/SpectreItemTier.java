package lumien.randomthings.item.spectretools;

import java.util.UUID;

import lumien.randomthings.item.ModItems;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyLoadBase;

/**
 * Ported from 1.12.2's {@code EnumHelper.addToolMaterial("spectre", 3, 2000, 8, 3, 22)} (repair item
 * Spectre Ingot) - 1.14.4 replaced the enum-extension trick with a plain {@link IItemTier}
 * implementation, modeled directly on vanilla's own {@code ItemTier} enum constants. The repair
 * {@link Ingredient} is wrapped in a {@link LazyLoadBase} exactly like every vanilla tier, since
 * {@code ModItems#SPECTRE_INGOT} isn't populated by its {@code ObjectHolder} until after the item
 * registry event this tier is constructed during has finished.
 */
public class SpectreItemTier implements IItemTier {
    public static final SpectreItemTier INSTANCE = new SpectreItemTier();

    /**
     * Shared by every Spectre tool but the sword - matches 1.12.2's +3 block reach on pickaxe/axe/shovel.
     *
     * <p>The tooltip line this modifier produces needs {@code lang/en_us.json}'s
     * {@code attribute.name.generic.reachDistance} entry - ground-truthed that this exact build's own
     * {@code forge/lang/en_us.json} ships a mismatched key for it ({@code generic.reachDistance} with no
     * {@code attribute.name.} prefix, plus an unrelated {@code attribute.name.generic.reach_distance}
     * snake_case key that doesn't match {@link net.minecraft.entity.player.PlayerEntity#REACH_DISTANCE}'s
     * real camelCase name either), so without our own entry the tooltip showed the raw untranslated key
     * instead of "Reach Distance".
     */
    public static final UUID REACH_MODIFIER_UUID = UUID.nameUUIDFromBytes("SpectreRangeModifier".getBytes());

    private final LazyLoadBase<Ingredient> repairMaterial = new LazyLoadBase<>(() -> Ingredient.fromItems(ModItems.SPECTRE_INGOT));

    @Override
    public int getMaxUses() {
        return 2000;
    }

    @Override
    public float getEfficiency() {
        return 8.0F;
    }

    @Override
    public float getAttackDamage() {
        return 3.0F;
    }

    @Override
    public int getHarvestLevel() {
        return 3;
    }

    @Override
    public int getEnchantability() {
        return 22;
    }

    @Override
    public Ingredient getRepairMaterial() {
        return repairMaterial.getValue();
    }
}
