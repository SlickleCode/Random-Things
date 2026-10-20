package lumien.randomthings.recipes;

import java.util.UUID;

import lumien.randomthings.item.EmeraldCompassItem;
import lumien.randomthings.item.IdCardItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

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
public class EmeraldCompassSetTargetRecipe extends CustomRecipe
{
	public static final RecipeSerializer<EmeraldCompassSetTargetRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(EmeraldCompassSetTargetRecipe::new);

	public EmeraldCompassSetTargetRecipe(ResourceLocation id, net.minecraft.world.item.crafting.CraftingBookCategory category)
	{
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack card = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);

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
	public ItemStack assemble(CraftingContainer inv, net.minecraft.core.RegistryAccess registryAccess)
	{
		ItemStack compass = ItemStack.EMPTY;
		ItemStack card = ItemStack.EMPTY;

		for (int i = 0; i < inv.getContainerSize(); i++)
		{
			ItemStack stack = inv.getItem(i);

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
			CompoundTag compound = result.getTag();

			if (compound == null)
			{
				result.setTag(compound = new CompoundTag());
			}

			compound.putString("uuid", uuid.toString());
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
