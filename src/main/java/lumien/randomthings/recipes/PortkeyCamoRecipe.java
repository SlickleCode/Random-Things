package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
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
    public static final RecipeSerializer<PortkeyCamoRecipe> SERIALIZER = new SimpleRecipeSerializer<>(PortkeyCamoRecipe::new);

    public PortkeyCamoRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
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
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camo = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() == ModItems.PORTKEY) {
                portkey = stack;
            } else if (!stack.isEmpty()) {
                camo = stack;
            }
        }

        ItemStack result = portkey.copy();
        CompoundTag resultTag = result.getOrCreateTag();

        ItemStack camoCopy = camo.copy();
        camoCopy.setCount(1);

        CompoundTag camoCompound = new CompoundTag();
        camoCompound.put("stack", camoCopy.save(new CompoundTag()));
        resultTag.put("camo", camoCompound);

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < inv.getContainerSize(); i++) {
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
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
