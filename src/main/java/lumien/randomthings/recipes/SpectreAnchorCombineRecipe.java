package lumien.randomthings.recipes;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.nbt.ByteTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Combine a Spectre Anchor with any other single, non-stackable item to tag
 * that item as "anchored" - see {@code AsmHandler#dropAllItemsExceptAnchored}
 * for the death-drop skip this feeds, and {@code RandomThings}'s
 * {@code PlayerEvent.Clone} listener for the respawn-carryover. Direct port
 * of 1.12.2's {@code spectreAnchorCombine} ({@code SimpleRecipe} there) -
 * same {@code SpecialRecipe}/{@code SpecialRecipeSerializer} pattern as
 * {@link PortkeyCamoRecipe}. Unlike Portkey's camo recipe, both ingredients
 * are fully consumed here (the anchor item is destroyed, and the target
 * becomes the single output) - matching 1.12.2's own {@code getRemainingItems}
 * exactly, which returns an all-empty list.
 */
public class SpectreAnchorCombineRecipe extends CustomRecipe {
    public static final RecipeSerializer<SpectreAnchorCombineRecipe> SERIALIZER = new CustomRecipe.Serializer<>(SpectreAnchorCombineRecipe::new);

    public SpectreAnchorCombineRecipe(net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack anchor = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.SPECTRE_ANCHOR) {
                if (!anchor.isEmpty()) {
                    return false;
                }

                anchor = stack;
            } else {
                if (!target.isEmpty() || stack.getMaxStackSize() != 1) {
                    return false;
                }

                target = stack;
            }
        }

        return !anchor.isEmpty() && !target.isEmpty() && (!StackTag.has(target) || !StackTag.contains(target, "spectreAnchor"));
    }

    @Override
    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.SPECTRE_ANCHOR) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        StackTag.edit(result, t -> t.put("spectreAnchor", ByteTag.valueOf((byte) 0)));
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
