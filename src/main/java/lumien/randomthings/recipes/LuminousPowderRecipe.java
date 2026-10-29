package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Luminous Powder + any enchanted item: the result is a copy of the item
 * with the {@code luminousEnchantment} NBT flag (the glint is meant to glow
 * in the dark). Direct port of 1.12.2's {@code luminousPowder} recipe: exactly
 * one powder and one enchanted, not yet luminous item; the powder is used up.
 * See {@link #isLuminous} for the flag, which the tooltip reads. The glow
 * rendering itself is not implemented.
 */
public class LuminousPowderRecipe extends CustomRecipe {
    public static final RecipeSerializer<LuminousPowderRecipe> SERIALIZER = new SimpleRecipeSerializer<>(LuminousPowderRecipe::new);

    public static final String FLAG = "luminousEnchantment";

    public LuminousPowderRecipe(ResourceLocation id) {
        super(id);
    }

    public static boolean isLuminous(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(FLAG);
    }

    private static boolean isEnchantedStack(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("Enchantments", 9) && !stack.getTag().getList("Enchantments", 10).isEmpty();
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
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.LUMINOUS_POWDER) {
                target = stack;
            }
        }

        if (target.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = target.copy();
        result.setCount(1);
        result.getOrCreateTag().putBoolean(FLAG, true);

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
