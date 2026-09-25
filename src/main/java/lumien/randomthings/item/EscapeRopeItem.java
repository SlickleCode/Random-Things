package lumien.randomthings.item;

import lumien.randomthings.util.EscapeRopeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Hold right-click under open sky (or where you could see it if the block
 * above were cleared) to start {@link EscapeRopeHandler} searching outward
 * from you for the nearest path to daylight, then teleport you there once
 * found. Direct port of 1.12.2's {@code ItemEscapeRope} - the actual search
 * algorithm lives in {@code EscapeRopeHandler}, not this class, matching the
 * original split.
 * <p>
 * Disclosed simplification: the original spawned a custom spiraling yellow
 * smoke particle while charging ({@code EntityColoredSmokeFX}, a bespoke
 * particle class); this port hasn't built custom-particle infrastructure yet
 * (nothing else in this project needs one), so it reuses vanilla's
 * {@code ParticleTypes.PORTAL} for the same "something is gathering" cue
 * instead of adding a new particle type just for this one cosmetic effect.
 */
public class EscapeRopeItem extends Item
{
	public EscapeRopeItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 20 * 60;
	}

	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BOW;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack stack = playerIn.getHeldItem(hand);

		if (!worldIn.getDimension().hasSkyLight() || worldIn.canBlockSeeSky(playerIn.getPosition()) || !worldIn.isAirBlock(playerIn.getPosition()))
		{
			return new ActionResult<>(ActionResultType.FAIL, stack);
		}

		playerIn.setActiveHand(hand);

		if (!worldIn.isRemote)
		{
			EscapeRopeHandler.getInstance().addTask((ServerPlayerEntity) playerIn);
		}

		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		return Minecraft.getInstance().player != null && Minecraft.getInstance().player.getActiveItemStack() == stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity entity, int count)
	{
		if (!entity.world.isRemote)
		{
			return;
		}

		float alpha = Math.min(1, (getUseDuration(stack) - count) * (1 / 60F));

		for (int i = 0; i < 7; i += 1)
		{
			for (int c = 0; c < 20; c += 10)
			{
				double x = Math.sin((count + i * 20) / (10F + c));
				double z = Math.cos((count + i * 20) / (10F + c));
				double y = Math.sin((count + i * 20) / (15F + c));

				entity.world.addParticle(ParticleTypes.PORTAL, entity.posX + x, entity.posY + 1 + y, entity.posZ + z, 0, alpha * 0.1D, 0);
			}
		}
	}
}
