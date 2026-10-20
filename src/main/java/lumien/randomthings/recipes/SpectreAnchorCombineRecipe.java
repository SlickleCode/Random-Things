package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.nbt.ByteTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
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
    public static final RecipeSerializer<SpectreAnchorCombineRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(SpectreAnchorCombineRecipe::new);

    public SpectreAnchorCombineRecipe(ResourceLocation id, net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack anchor = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
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

        return !anchor.isEmpty() && !target.isEmpty() && (!target.hasTag() || !target.getTag().contains("spectreAnchor"));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, net.minecraft.core.RegistryAccess registryAccess) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.SPECTRE_ANCHOR) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.getOrCreateTag().put("spectreAnchor", ByteTag.valueOf((byte) 0));

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
