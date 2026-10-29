package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
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
 * Luminous Powder + any enchanted item: the result is a copy of the item
 * with the {@code luminousEnchantment} NBT flag (the glint is meant to glow
 * in the dark). Direct port of 1.12.2's {@code luminousPowder} recipe: exactly
 * one powder and one enchanted, not yet luminous item; the powder is used up.
 * See {@link #isLuminous} for the flag, which the tooltip reads.
 */
public class LuminousPowderRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<LuminousPowderRecipe> SERIALIZER = new SpecialRecipeSerializer<>(LuminousPowderRecipe::new);

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
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack powder = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

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
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.LUMINOUS_POWDER) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.setCount(1);
        result.getOrCreateTag().putByte(FLAG, (byte) 0);

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
