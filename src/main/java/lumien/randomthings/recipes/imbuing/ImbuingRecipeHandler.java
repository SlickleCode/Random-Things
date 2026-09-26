package lumien.randomthings.recipes.imbuing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;

public class ImbuingRecipeHandler {
    public static ArrayList<ImbuingRecipe> imbuingRecipes = new ArrayList<>();

    public static ItemStack getRecipeOutput(IItemHandler iItemHandler) {
        for (ImbuingRecipe ir : imbuingRecipes) {
            if (ir.matchesItemHandler(iItemHandler)) {
                return ir.getResult();
            }
        }

        return ItemStack.EMPTY;
    }

    public static void addRecipe(ItemStack ingredient1, ItemStack ingredient2, ItemStack ingredient3, ItemStack toImbue, ItemStack result) {
        imbuingRecipes.add(new ImbuingRecipe(toImbue, result, ingredient1, ingredient2, ingredient3));
    }
}
