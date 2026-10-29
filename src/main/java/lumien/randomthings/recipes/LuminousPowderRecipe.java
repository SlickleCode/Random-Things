package lumien.randomthings.recipes;

import lumien.randomthings.enchantment.ModEnchantments;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Luminous Powder + any enchanted item: the result is a copy of the item
 * with the {@code luminousEnchantment} flag in its custom data (the glint is meant to glow
 * in the dark). Direct port of 1.12.2's {@code luminousPowder} recipe: exactly
 * one powder and one enchanted, not yet luminous item; the powder is used up.
 * See {@link #isLuminous} for the flag, which the tooltip reads.
 */
public class LuminousPowderRecipe extends CustomRecipe {
    public static final RecipeSerializer<LuminousPowderRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(LuminousPowderRecipe::new);

    public static final String FLAG = "luminousEnchantment";

    public LuminousPowderRecipe(net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(category);
    }

    public static boolean isLuminous(ItemStack stack) {
        return StackTag.contains(stack, FLAG);
    }

    /** True if the stack carries at least one enchantment (level above 0 for any registered enchantment). */
    private static boolean isEnchantedStack(ItemStack stack, Level level) {
        for (ResourceLocation id : level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).keySet()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);

            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.holder(level.registryAccess(), key), stack) > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack powder = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.LUMINOUS_POWDER) {
                if (!powder.isEmpty()) {
                    return false;
                }

                powder = stack;
            } else {
                if (!target.isEmpty() || !isEnchantedStack(stack, worldIn)) {
                    return false;
                }

                target = stack;
            }
        }

        return !powder.isEmpty() && !target.isEmpty() && !isLuminous(target);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.LUMINOUS_POWDER) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.setCount(1);
        StackTag.edit(result, t -> t.putBoolean(FLAG, true));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
