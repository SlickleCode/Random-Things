package lumien.randomthings.item;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		CompoundNBT compound = stack.getTag();

		if (compound != null)
		{
			String playerName = compound.getString("player-name");
			tooltip.add(new TranslationTextComponent("tooltip.randomthings.stable_enderpearl.boundto", playerName));
		}
	}

	/**
	 * 1.12.2 hooked this via {@code Item.onEntityItemUpdate}, a per-tick
	 * callback Forge fired on the item type for every matching dropped
	 * {@code EntityItem}. That hook doesn't exist on {@code Item} in this
	 * Forge build - instead, {@code RandomThings} drives this from a
	 * {@code TickEvent.WorldTickEvent} listener that scans
	 * {@code ServerWorld.getEntities()} for dropped stacks of this item.
	 */
	public void tickDroppedPearl(ItemEntity entityItem)
	{
		CompoundNBT data = entityItem.getEntityData();
		int counter = data.getInt("counter");

		if (counter == 140)
		{
			if (!entityItem.world.isRemote)
			{
				entityItem.remove();

				ItemStack itemStack = entityItem.getItem();
				ServerPlayerEntity player = null;

				if (itemStack.getTag() != null)
				{
					String uuidString = itemStack.getTag().getString("player-uuid");
					UUID uuid = UUID.fromString(uuidString);
					player = ((ServerWorld) entityItem.world).getServer().getPlayerList().getPlayerByUUID(uuid);
				}

				if (player != null)
				{
					player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
					player.connection.setPlayerLocation(entityItem.posX, entityItem.posY, entityItem.posZ, player.rotationYaw, player.rotationPitch);
				}
				else
				{
					List<LivingEntity> entityList = entityItem.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(entityItem.posX - 10, entityItem.posY - 10, entityItem.posZ - 10, entityItem.posX + 10, entityItem.posY + 10, entityItem.posZ + 10), (e) -> true);

					if (!entityList.isEmpty())
					{
						LivingEntity target = entityList.get(rand.nextInt(entityList.size()));

						if (target instanceof ServerPlayerEntity)
						{
							ServerPlayerEntity targetPlayer = (ServerPlayerEntity) target;
							targetPlayer.world.playSound(null, targetPlayer.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
							targetPlayer.connection.setPlayerLocation(entityItem.posX, entityItem.posY, entityItem.posZ, targetPlayer.rotationYaw, targetPlayer.rotationPitch);
						}
						else
						{
							target.world.playSound(null, target.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
							target.setPositionAndUpdate(entityItem.posX, entityItem.posY, entityItem.posZ);
						}
					}
				}

				entityItem.world.playSound(null, entityItem.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 1);
			}
		}
		else
		{
			if (!entityItem.world.isRemote)
			{
				data.putInt("counter", counter + 1);
			}
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack itemStackIn = playerIn.getHeldItem(hand);

		if (!worldIn.isRemote)
		{
			if (itemStackIn.getTag() == null)
			{
				itemStackIn.setTag(new CompoundNBT());
			}

			CompoundNBT compound = itemStackIn.getTag();
			GameProfile gameProfile = playerIn.getGameProfile();

			compound.putString("player-uuid", gameProfile.getId().toString());
			compound.putString("player-name", gameProfile.getName());
		}

		return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
	}
}
