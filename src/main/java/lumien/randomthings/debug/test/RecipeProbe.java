package lumien.randomthings.debug.test;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/**
 * Dev-only: reads a recipe's declared result and ingredients. 1.21.2 removed {@code getResultItem} and {@code getIngredients} from the
 * {@code Recipe} interface (the client no longer sees recipes, only displays); a plain recipe's result is whatever it assembles from an empty input.
 */
public final class RecipeProbe
{
	private RecipeProbe()
	{
	}

	public static ItemStack resultOf(Recipe<?> recipe, HolderLookup.Provider registries)
	{
		try
		{
			if (recipe instanceof CraftingRecipe crafting)
			{
				return crafting.assemble(CraftingInput.EMPTY, registries);
			}

			if (recipe instanceof SingleItemRecipe single)
			{
				return single.assemble(new SingleRecipeInput(ItemStack.EMPTY), registries);
			}

			if (recipe instanceof AbstractCookingRecipe cooking)
			{
				return cooking.assemble(new SingleRecipeInput(ItemStack.EMPTY), registries);
			}
		}
		catch (RuntimeException e)
		{
			// a custom recipe that needs real input: no declared result
		}

		return ItemStack.EMPTY;
	}

	/** The ingredient slots in order (shaped recipes keep their empty cells as {@code Optional.empty()}). */
	public static List<Optional<Ingredient>> ingredientsOf(Recipe<?> recipe)
	{
		if (recipe instanceof ShapedRecipe shaped)
		{
			return shaped.getIngredients();
		}

		return recipe.placementInfo().ingredients().stream().map(Optional::of).toList();
	}

	public static ItemStack[] optionsOf(Ingredient ingredient)
	{
		if (ingredient.isCustom())
		{
			net.neoforged.neoforge.common.crafting.ICustomIngredient custom = ingredient.getCustomIngredient();

			return custom.items().map(holder ->
			{
				ItemStack stack = new ItemStack(holder.value());

				// a components ingredient only matches stacks carrying the components it asks for
				if (custom instanceof net.neoforged.neoforge.common.crafting.DataComponentIngredient components)
				{
					stack.applyComponents(components.components().asPatch());
				}

				return stack;
			}).toArray(ItemStack[]::new);
		}

		return ingredient.items().map(holder -> new ItemStack(holder.value())).toArray(ItemStack[]::new);
	}
}
