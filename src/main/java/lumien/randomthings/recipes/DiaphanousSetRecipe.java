package lumien.randomthings.recipes;

import lumien.randomthings.item.block.DiaphanousBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Combine a Diaphanous Block with any other single block item (one with no
 * tile entity - matching 1.12.2's own restriction, since a tile-entity block
 * can't be safely represented by just its default {@code BlockState}) to set
 * which block it displays. Direct port of 1.12.2's {@code diaphanousSet}
 * ({@code SimpleRecipe} there) - {@code SpecialRecipe}/{@code
 * SpecialRecipeSerializer} is this port's own already-proven equivalent, same
 * pattern as {@link lumien.randomthings.recipes.PortkeyCamoRecipe}. The donor
 * block item is returned unconsumed (a copy, count 1), matching 1.12.2's own
 * {@code getRemainingItems} exactly. Disclosed simplification: 1.12.2 also
 * stored the target's numeric {@code meta}, letting it capture e.g. a
 * specific wool color or stair orientation; metadata doesn't exist in this
 * version, so this always captures the target block's plain default state
 * (most of what {@code meta} used to distinguish - e.g. wool colors - is now
 * its own separate registry entry anyway, matching the target block
 * directly).
 */
public class DiaphanousSetRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<DiaphanousSetRecipe> SERIALIZER = new SpecialRecipeSerializer<>(DiaphanousSetRecipe::new);

    public DiaphanousSetRecipe(ResourceLocation id) {
        super(id);
    }

    private static boolean isValidTarget(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }

        Block block = ((BlockItem) stack.getItem()).getBlock();
        return !block.hasTileEntity(block.getDefaultState());
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack diaphanous = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof DiaphanousBlockItem) {
                if (!diaphanous.isEmpty()) {
                    return false;
                }

                diaphanous = stack;
            } else {
                if (!target.isEmpty() || !isValidTarget(stack)) {
                    return false;
                }

                target = stack;
            }
        }

        return !diaphanous.isEmpty() && !target.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack diaphanous = ItemStack.EMPTY;
        Block targetBlock = Blocks.STONE;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof DiaphanousBlockItem) {
                diaphanous = stack;
            } else if (isValidTarget(stack)) {
                targetBlock = ((BlockItem) stack.getItem()).getBlock();
            }
        }

        ItemStack result = diaphanous.copy();
        result.setCount(1);

        CompoundNBT tag = result.getOrCreateTag();
        tag.putString("block", targetBlock.getRegistryName().toString());

        if (!tag.contains("inverted")) {
            tag.putBoolean("inverted", false);
        }

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && !(stack.getItem() instanceof DiaphanousBlockItem)) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                remaining.set(i, copy);
            }
        }

        return remaining;
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
