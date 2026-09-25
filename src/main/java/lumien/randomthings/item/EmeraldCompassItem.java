package lumien.randomthings.item;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Points at a bound player as long as they're online, going back to a random
 * wobble when they log off. Bound by combining with an {@link IdCardItem} at
 * a crafting table (see {@code recipes/EmeraldCompassSetTargetRecipe}) -
 * that recipe writes the target player's {@code uuid}; this item's own job
 * is just re-resolving that UUID to a live position once a second while the
 * stack exists anywhere in a player's inventory.
 */
public class EmeraldCompassItem extends CompassItemBase
{
	public EmeraldCompassItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		if (!worldIn.isRemote && worldIn.getGameTime() % 20 == 0)
		{
			CompoundNBT compound = stack.getTag();

			if (compound != null && compound.contains("uuid"))
			{
				UUID uuid = UUID.fromString(compound.getString("uuid"));
				ServerPlayerEntity targetPlayer = ((ServerWorld) worldIn).getServer().getPlayerList().getPlayerByUUID(uuid);

				if (targetPlayer != null)
				{
					BlockPos targetPos = targetPlayer.getPosition();

					compound.putInt("targetX", targetPos.getX());
					compound.putInt("targetZ", targetPos.getZ());
				}
				else
				{
					compound.remove("targetX");
					compound.remove("targetZ");
				}
			}
		}
	}
}
