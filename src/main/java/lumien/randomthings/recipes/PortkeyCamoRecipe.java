package lumien.randomthings.recipes;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

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
public class PortkeyCamoRecipe extends CustomRecipe {
    public static final RecipeSerializer<PortkeyCamoRecipe> SERIALIZER = new CustomRecipe.Serializer<>(PortkeyCamoRecipe::new);

    public PortkeyCamoRecipe(net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

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
    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() == ModItems.PORTKEY) {
                portkey = stack;
            } else if (!stack.isEmpty()) {
                camo = stack;
            }
        }

        ItemStack result = portkey.copy();

        ItemStack camoCopy = camo.copy();
        camoCopy.setCount(1);

        CompoundTag camoCompound = new CompoundTag();
        camoCompound.put("stack", StackTag.saveStack(camoCopy));
        StackTag.edit(result, resultTag -> resultTag.put("camo", camoCompound));

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.PORTKEY) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                remaining.set(i, copy);
            }
        }

        return remaining;
    }


    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
