package lumien.randomthings.item;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * A thrown-and-dropped ender pearl that never deals fall damage and, instead
 * of teleporting the thrower instantly, waits 140 ticks after landing and
 * then quietly pulls the bound player to its resting position. If nobody
 * bound it (or the binding is gone), it grabs the nearest living entity
 * within 10 blocks instead - a "random teleport" fallback, matching 1.12.2.
 * <p>
 * Disclosed simplification: 1.12.2 additionally handled the rare case of the
 * dropped item ending up in a different dimension than the bound player
 * (e.g. dropped near a portal) by force-transferring that player's
 * dimension via a hand-rolled {@code SimpleTeleporter}. 1.14.4's dimension
 * API is a substantial rewrite from 1.12.2's, and this edge case isn't worth
 * porting in isolation - the normal (overwhelmingly common) same-dimension
 * case is unaffected.
 */
public class StableEnderpearlItem extends Item
{
	private final Random rand = new Random();

	public StableEnderpearlItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag)
	{
		CompoundTag compound = stack.getTag();

		if (compound != null)
		{
			String playerName = compound.getString("player-name");
			tooltip.add(Component.translatable("tooltip.randomthings.stable_enderpearl.boundto", playerName));
		}
	}

	/**
	 * 1.12.2 hooked this via {@code Item.onEntityItemUpdate}, a per-tick
	 * callback Forge fired on the item type for every matching dropped
	 * {@code EntityItem}. That hook doesn't exist on {@code Item} in this
	 * Forge build - instead, {@code RandomThings} drives this from a
	 * {@code TickEvent.LevelTickEvent} listener that scans
	 * {@code ServerWorld.getEntities()} for dropped stacks of this item.
	 */
	public void tickDroppedPearl(ItemEntity entityItem)
	{
		CompoundTag data = entityItem.getPersistentData();
		int counter = data.getInt("counter");

		if (counter == 140)
		{
			if (!entityItem.level().isClientSide)
			{
				entityItem.discard();

				ItemStack itemStack = entityItem.getItem();
				ServerPlayer player = null;

				if (itemStack.getTag() != null && itemStack.getTag().contains("player-uuid"))
				{
					try
					{
						UUID uuid = UUID.fromString(itemStack.getTag().getString("player-uuid"));
						player = ((ServerLevel) entityItem.level()).getServer().getPlayerList().getPlayer(uuid);
					}
					catch (IllegalArgumentException e)
					{
						// malformed uuid: treat as unbound
					}
				}

				if (player != null)
				{
					player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
					player.connection.teleport(entityItem.getX(), entityItem.getY(), entityItem.getZ(), player.getYRot(), player.getXRot());
				}
				else
				{
					List<LivingEntity> entityList = entityItem.level().getEntitiesOfClass(LivingEntity.class, new AABB(entityItem.getX() - 10, entityItem.getY() - 10, entityItem.getZ() - 10, entityItem.getX() + 10, entityItem.getY() + 10, entityItem.getZ() + 10), (e) -> true);

					if (!entityList.isEmpty())
					{
						LivingEntity target = entityList.get(rand.nextInt(entityList.size()));

						if (target instanceof ServerPlayer)
						{
							ServerPlayer targetPlayer = (ServerPlayer) target;
							targetPlayer.level().playSound(null, targetPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
							targetPlayer.connection.teleport(entityItem.getX(), entityItem.getY(), entityItem.getZ(), targetPlayer.getYRot(), targetPlayer.getXRot());
						}
						else
						{
							target.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
							target.teleportTo(entityItem.getX(), entityItem.getY(), entityItem.getZ());
						}
					}
				}

				entityItem.level().playSound(null, entityItem.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
			}
		}
		else
		{
			if (!entityItem.level().isClientSide)
			{
				data.putInt("counter", counter + 1);
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand)
	{
		ItemStack itemStackIn = playerIn.getItemInHand(hand);

		if (!worldIn.isClientSide)
		{
			if (itemStackIn.getTag() == null)
			{
				itemStackIn.setTag(new CompoundTag());
			}

			CompoundTag compound = itemStackIn.getTag();
			GameProfile gameProfile = playerIn.getGameProfile();

			compound.putString("player-uuid", gameProfile.getId().toString());
			compound.putString("player-name", gameProfile.getName());
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
	}
}
