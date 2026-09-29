package lumien.randomthings.recipes;

import lumien.randomthings.item.block.DiaphanousBlockItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * A lone Diaphanous Block in the grid, nothing else, toggles its "inverted"
 * flag (flips both the fade curve and whether it has a real collision box -
 * see {@link lumien.randomthings.block.DiaphanousBlock}). Direct port of
 * 1.12.2's {@code diaphanousInvert}, same {@code SpecialRecipe} shape as
 * {@link DiaphanousSetRecipe}.
 */
public class DiaphanousInvertRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<DiaphanousInvertRecipe> SERIALIZER = new SpecialRecipeSerializer<>(DiaphanousInvertRecipe::new);

    public DiaphanousInvertRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

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
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack diaphanous = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() instanceof DiaphanousBlockItem) {
                diaphanous = stack;
            }
        }

        if (diaphanous.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = diaphanous.copy();
        result.setCount(1);

        CompoundNBT tag = result.getOrCreateTag();
        tag.putBoolean("inverted", !tag.getBoolean("inverted"));

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
