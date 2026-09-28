package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

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
public class SpectreAnchorCombineRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<SpectreAnchorCombineRecipe> SERIALIZER = new SpecialRecipeSerializer<>(SpectreAnchorCombineRecipe::new);

    public SpectreAnchorCombineRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack anchor = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

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
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.SPECTRE_ANCHOR) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.getOrCreateTag().put("spectreAnchor", new ByteNBT((byte) 0));

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
