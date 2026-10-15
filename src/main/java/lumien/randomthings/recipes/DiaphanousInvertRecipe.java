package lumien.randomthings.recipes;

import lumien.randomthings.item.block.DiaphanousBlockItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * A lone Diaphanous Block in the grid, nothing else, toggles its "inverted"
 * flag (flips both the fade curve and whether it has a real collision box -
 * see {@link lumien.randomthings.block.DiaphanousBlock}). Direct port of
 * 1.12.2's {@code diaphanousInvert}, same {@code SpecialRecipe} shape as
 * {@link DiaphanousSetRecipe}.
 */
public class DiaphanousInvertRecipe extends CustomRecipe {
    public static final RecipeSerializer<DiaphanousInvertRecipe> SERIALIZER = new SimpleRecipeSerializer<>(DiaphanousInvertRecipe::new);

    public DiaphanousInvertRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof DiaphanousBlockItem) || !diaphanous.isEmpty()) {
                return false;
            }

            diaphanous = stack;
        }

        return !diaphanous.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof DiaphanousBlockItem) {
                diaphanous = stack;
            }
        }

        if (diaphanous.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = diaphanous.copy();
        result.setCount(1);

        CompoundTag tag = result.getOrCreateTag();
        tag.putBoolean("inverted", !tag.getBoolean("inverted"));

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
