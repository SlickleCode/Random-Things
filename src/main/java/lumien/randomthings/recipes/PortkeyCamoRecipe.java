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
 * Combine a Portkey with any other single item to disguise the Portkey as
 * that item - see {@code PortkeyItem}'s javadoc and {@code
 * PortkeyItemRenderer} for the render side this feeds. Direct port of
 * 1.12.2's {@code portKeyCamoRecipe} ({@code SimpleRecipe} there) - {@code
 * SpecialRecipe}/{@code SpecialRecipeSerializer} is this port's own already-
 * proven equivalent, same pattern as {@link GoldenCompassSetPositionRecipe}.
 * The donor item is returned unconsumed (a copy, count 1), matching 1.12.2's
 * own {@code getRemainingItems} exactly - it's only being read, not spent.
 */
public class PortkeyCamoRecipe extends SpecialRecipe {
    public static final IRecipeSerializer<PortkeyCamoRecipe> SERIALIZER = new SpecialRecipeSerializer<>(PortkeyCamoRecipe::new);

    public PortkeyCamoRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.PORTKEY) {
                if (!portkey.isEmpty()) {
                    return false;
                }

                portkey = stack;
            } else {
                if (!camo.isEmpty()) {
                    return false;
                }

                camo = stack;
            }
        }

        return !portkey.isEmpty() && !camo.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack.getItem() == ModItems.PORTKEY) {
                portkey = stack;
            } else if (!stack.isEmpty()) {
                camo = stack;
            }
        }

        ItemStack result = portkey.copy();
        CompoundNBT resultTag = result.getOrCreateTag();

        ItemStack camoCopy = camo.copy();
        camoCopy.setCount(1);

        CompoundNBT camoCompound = new CompoundNBT();
        camoCompound.put("stack", camoCopy.write(new CompoundNBT()));
        resultTag.put("camo", camoCompound);

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.PORTKEY) {
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
