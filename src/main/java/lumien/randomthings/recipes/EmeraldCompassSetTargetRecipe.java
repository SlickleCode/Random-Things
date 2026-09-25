package lumien.randomthings.recipes;

import java.util.UUID;

import lumien.randomthings.item.EmeraldCompassItem;
import lumien.randomthings.item.IdCardItem;
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
 * Combine an {@link EmeraldCompassItem} with an {@link IdCardItem} to bind
 * it to that card's player UUID. Direct port of 1.12.2's
 * {@code emeraldcompass_settarget} custom recipe - the ID card is returned
 * unconsumed, matching the original's {@code getRemainingItems}. (The
 * original's {@code matches} also checked the target stack for a
 * "spectreAnchor" NBT tag - copy-paste leftover from the unrelated Spectre
 * Anchor recipe right above it in {@code ModRecipes.java}, always true once
 * the item is already confirmed to be an ID card, so not reproduced here.)
 */
public class EmeraldCompassSetTargetRecipe extends SpecialRecipe
{
	public static final IRecipeSerializer<EmeraldCompassSetTargetRecipe> SERIALIZER = new SpecialRecipeSerializer<>(EmeraldCompassSetTargetRecipe::new);

	public EmeraldCompassSetTargetRecipe(ResourceLocation id)
	{
		super(id);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack card = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

			if (stack.isEmpty())
			{
				continue;
			}

			if (stack.getItem() == ModItems.EMERALD_COMPASS)
			{
				if (!compass.isEmpty())
				{
					return false;
				}
				compass = stack;
			}
			else if (stack.getItem() == ModItems.ID_CARD)
			{
				if (!card.isEmpty())
				{
					return false;
				}
				card = stack;
			}
			else
			{
				return false;
			}
		}

		return !compass.isEmpty() && !card.isEmpty() && IdCardItem.getUUID(card) != null;
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack card = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

			if (stack.getItem() == ModItems.EMERALD_COMPASS)
			{
				compass = stack;
			}
			else if (stack.getItem() == ModItems.ID_CARD)
			{
				card = stack;
			}
		}

		ItemStack result = compass.copy();
		UUID uuid = IdCardItem.getUUID(card);

		if (uuid != null)
		{
			CompoundNBT compound = result.getTag();

			if (compound == null)
			{
				result.setTag(compound = new CompoundNBT());
			}

			compound.putString("uuid", uuid.toString());
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

			if (stack.getItem() == ModItems.ID_CARD)
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
