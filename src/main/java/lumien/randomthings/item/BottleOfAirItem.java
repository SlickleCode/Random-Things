package lumien.randomthings.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Right-click while submerged to drink; fully refills the drinker's air in
 * one go and is consumed (shrinks by 1) in the process.
 * <p>
 * Deliberate deviation from 1.12.2, per explicit user direction: the
 * original was a reusable tool - hold right-click to continuously top up air
 * for as long as you wanted, never consumed. Per testing feedback, this port
 * instead makes it a proper single-use consumable with an instant full
 * refill, closer to how a "bottle" item conventionally behaves.
 */
public class BottleOfAirItem extends Item
{
	public BottleOfAirItem(Item.Properties properties)
	{
		super(properties.rarity(Rarity.RARE));
	}

	@Override
	public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user)
	{
		return 32;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack)
	{
		return ItemUseAnimation.DRINK;
	}

	@Override
	public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand)
	{
		ItemStack itemStack = playerIn.getItemInHand(hand);

		if (playerIn.isInWater())
		{
			playerIn.startUsingItem(hand);

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity livingEntity)
	{
		if (!worldIn.isClientSide)
		{
			livingEntity.setAirSupply(livingEntity.getMaxAirSupply());
		}

		stack.shrink(1);
		return stack;
	}
}
