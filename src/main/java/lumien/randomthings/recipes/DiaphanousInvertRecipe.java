package lumien.randomthings.recipes;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.item.block.DiaphanousBlockItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
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
    public static final RecipeSerializer<DiaphanousInvertRecipe> SERIALIZER = new CustomRecipe.Serializer<>(DiaphanousInvertRecipe::new);

    public DiaphanousInvertRecipe(net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
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
    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
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

        StackTag.edit(result, tag -> tag.putBoolean("inverted", !tag.getBoolean("inverted")));

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }


    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
