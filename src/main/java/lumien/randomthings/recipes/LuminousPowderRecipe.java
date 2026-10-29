package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
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
 * See {@link #isLuminous} for the flag, which the tooltip reads. Same
 * {@code CustomRecipe} shape as {@link SpectreAnchorCombineRecipe}.
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
    private static boolean isEnchantedStack(ItemStack stack) {
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack powder = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
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
                if (!target.isEmpty() || !isEnchantedStack(stack)) {
                    return false;
                }

                target = stack;
            }
        }

        return !powder.isEmpty() && !target.isEmpty() && !isLuminous(target);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.LUMINOUS_POWDER) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.setCount(1);
        StackTag.edit(result, t -> t.put(FLAG, ByteTag.valueOf((byte) 0)));
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
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
