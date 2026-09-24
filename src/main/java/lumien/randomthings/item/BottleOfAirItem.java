package lumien.randomthings.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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
	public int getUseDuration(ItemStack stack)
	{
		return 32;
	}

	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack itemStack = playerIn.getHeldItem(hand);

		if (playerIn.isInWater())
		{
			playerIn.setActiveHand(hand);

			return new ActionResult<>(ActionResultType.SUCCESS, itemStack);
		}

		return new ActionResult<>(ActionResultType.FAIL, itemStack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity livingEntity)
	{
		if (!worldIn.isRemote)
		{
			livingEntity.setAir(livingEntity.getMaxAir());
		}

		stack.shrink(1);
		return stack;
	}
}
