package lumien.randomthings.recipes.imbuing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * One imbuing conversion: 3 unordered ingredients (slots 0-2) plus a "to
 * imbue" item held in the center slot (3), consumed on completion for the
 * result. Direct port of 1.12.2's {@code ImbuingRecipe}; matching drops the
 * original's ore-dictionary fallback ({@code ItemUtil.areOreDictionaried}) in
 * favor of plain item+NBT equality, matching this port's established
 * ore-dict-free convention elsewhere (dye colors, wool/plank/glass
 * wildcards).
 */
public class ImbuingRecipe {
    private final ItemStack toImbue;
    private final ArrayList<ItemStack> ingredients;
    private final ItemStack result;

    public ImbuingRecipe(ItemStack toImbue, ItemStack result, ItemStack... ingredients) {
        this.toImbue = toImbue;
        this.ingredients = new ArrayList<>();
        this.result = result;

        for (ItemStack is : ingredients) {
            if (!is.isEmpty()) {
                this.ingredients.add(is);
            }
        }
    }

    public boolean matchesItemHandler(IItemHandler iItemHandler) {
        HashMap<ItemStack, Boolean> providedIngredients = new HashMap<>();
        ItemStack i1 = iItemHandler.getStackInSlot(0);
        ItemStack i2 = iItemHandler.getStackInSlot(1);
        ItemStack i3 = iItemHandler.getStackInSlot(2);
        ItemStack center = iItemHandler.getStackInSlot(3);

        providedIngredients.put(i1, false);
        providedIngredients.put(i2, false);
        providedIngredients.put(i3, false);

        if (!contentEquals(center, toImbue)) {
            return false;
        }

        for (ItemStack needed : ingredients) {
            if (!containsItemStack(providedIngredients, needed)) {
                return false;
            }
        }

        for (ItemStack is : providedIngredients.keySet()) {
            if (!providedIngredients.get(is) && !is.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private boolean containsItemStack(HashMap<ItemStack, Boolean> list, ItemStack is) {
        for (ItemStack testItemStack : list.keySet()) {
            if (contentEquals(testItemStack, is)) {
                list.put(testItemStack, true);
                return true;
            }
        }

        return false;
    }

    private static boolean contentEquals(ItemStack a, ItemStack b) {
        return !a.isEmpty() && !b.isEmpty() && ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public ItemStack toImbue() {
        return toImbue;
    }

    public ItemStack getResult() {
        return result;
    }
}
