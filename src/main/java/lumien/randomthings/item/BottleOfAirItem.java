package lumien.randomthings.item;

import net.minecraft.entity.Entity;
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
 * Right-click while submerged to drink; while active, tops up the drinker's
 * air every 5 ticks. 1.12.2 used reflection into a private "use count" field
 * to keep the drink action alive indefinitely past its short 32-tick base
 * duration (no clean API existed for "hold to keep using" back then). 1.14.4
 * doesn't need that hack: {@link #getUseDuration} simply returns a duration
 * long enough that the item is never actually consumed by finishing its use,
 * so holding right-click keeps drinking for as long as the player wants.
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
		return 72000;
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
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

		if (worldIn.isRemote || !(entityIn instanceof LivingEntity))
		{
			return;
		}

		LivingEntity livingEntity = (LivingEntity) entityIn;

		if (livingEntity.isHandActive() && livingEntity.getActiveItemStack() == stack)
		{
			if ((livingEntity.isInWater() || livingEntity.getAir() < 270) && worldIn.getGameTime() % 5 == 0)
			{
				if (livingEntity.getAir() < 270)
				{
					livingEntity.setAir(livingEntity.getAir() + 20);
				}
			}
		}
	}
}
