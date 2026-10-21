package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import java.util.UUID;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

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
		return !ItemStack.isSameItem(oldStack, newStack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		if (!worldIn.isClientSide && worldIn.getGameTime() % 20 == 0)
		{
			CompoundTag compound = StackTag.get(stack);

			if (compound != null && compound.contains("uuid"))
			{
				UUID uuid = UUID.fromString(compound.getString("uuid"));
				ServerPlayer targetPlayer = ((ServerLevel) worldIn).getServer().getPlayerList().getPlayer(uuid);

				if (targetPlayer != null)
				{
					BlockPos targetPos = targetPlayer.blockPosition();

					compound.putInt("targetX", targetPos.getX());
					compound.putInt("targetZ", targetPos.getZ());
				}
				else
				{
					compound.remove("targetX");
					compound.remove("targetZ");
				}

				StackTag.set(stack, compound);
			}
		}
	}
}
