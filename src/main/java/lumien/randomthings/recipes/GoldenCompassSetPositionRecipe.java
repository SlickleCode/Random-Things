package lumien.randomthings.recipes;

import lumien.randomthings.item.GoldenCompassItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Combine a {@link GoldenCompassItem} with a {@link PositionFilterItem} to
 * copy the filter's stored position onto the compass as its {@code targetX}/
 * {@code targetZ}. Direct port of 1.12.2's {@code goldenCompassSetPosition}
 * custom recipe - the position filter is returned unconsumed, matching the
 * original's {@code getRemainingItems}.
 */
public class GoldenCompassSetPositionRecipe extends CustomRecipe
{
	public static final RecipeSerializer<GoldenCompassSetPositionRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(GoldenCompassSetPositionRecipe::new);

	public GoldenCompassSetPositionRecipe(ResourceLocation id, net.minecraft.world.item.crafting.CraftingBookCategory category)
	{
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack filter = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);

			if (stack.isEmpty())
			{
				continue;
			}

			if (stack.getItem() == ModItems.GOLDEN_COMPASS)
			{
				if (!compass.isEmpty())
				{
					return false;
				}
				compass = stack;
			}
			else if (stack.getItem() == ModItems.POSITION_FILTER)
			{
				if (!filter.isEmpty())
				{
					return false;
				}
				filter = stack;
			}
			else
			{
				return false;
			}
		}

		return !compass.isEmpty() && !filter.isEmpty() && PositionFilterItem.getPosition(filter) != null;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, net.minecraft.core.RegistryAccess registryAccess)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack filter = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);

			if (stack.getItem() == ModItems.GOLDEN_COMPASS)
			{
				compass = stack;
			}
			else if (stack.getItem() == ModItems.POSITION_FILTER)
			{
				filter = stack;
			}
		}

		ItemStack result = compass.copy();
		BlockPos pos = PositionFilterItem.getPosition(filter);

		if (pos != null)
		{
			CompoundTag compound = result.getTag();

			if (compound == null)
			{
				result.setTag(compound = new CompoundTag());
			}

			compound.putInt("targetX", pos.getX());
			compound.putInt("targetZ", pos.getZ());
		}

		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);

			if (stack.getItem() == ModItems.POSITION_FILTER)
			{
				ItemStack copy = stack.copy();
				copy.setCount(1);
				remaining.set(i, copy);
			}
		}

		return remaining;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}
}
