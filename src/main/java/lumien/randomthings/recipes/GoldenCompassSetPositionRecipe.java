package lumien.randomthings.recipes;

import lumien.randomthings.item.GoldenCompassItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Combine a {@link GoldenCompassItem} with a {@link PositionFilterItem} to
 * copy the filter's stored position onto the compass as its {@code targetX}/
 * {@code targetZ}. Direct port of 1.12.2's {@code goldenCompassSetPosition}
 * custom recipe - the position filter is returned unconsumed, matching the
 * original's {@code getRemainingItems}.
 */
public class GoldenCompassSetPositionRecipe extends SpecialRecipe
{
	public static final IRecipeSerializer<GoldenCompassSetPositionRecipe> SERIALIZER = new SpecialRecipeSerializer<>(GoldenCompassSetPositionRecipe::new);

	public GoldenCompassSetPositionRecipe(ResourceLocation id)
	{
		super(id);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack filter = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

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
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack filter = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

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
			CompoundNBT compound = result.getTag();

			if (compound == null)
			{
				result.setTag(compound = new CompoundNBT());
			}

			compound.putInt("targetX", pos.getX());
			compound.putInt("targetZ", pos.getZ());
		}

		return result;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

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
	public boolean canFit(int width, int height)
	{
		return true;
	}

	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}
}
