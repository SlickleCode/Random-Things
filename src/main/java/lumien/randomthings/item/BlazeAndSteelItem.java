package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlazeAndSteelItem extends Item
{
	public BlazeAndSteelItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		World world = context.getWorld();
		BlockPos pos = context.getPos().offset(context.getFace());
		ItemStack stack = context.getItem();

		if (player != null && !player.canPlayerEdit(pos, context.getFace(), stack))
		{
			return ActionResultType.FAIL;
		}

		if (world.isAirBlock(pos))
		{
			world.playSound(player, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, random.nextFloat() * 0.8F + 0.8F);
			world.setBlockState(pos, ModBlocks.BLAZING_FIRE.getDefaultState(), 11);
		}

		if (player != null)
		{
			stack.damageItem(1, player, (p) -> p.sendBreakAnimation(context.getHand()));
		}

		return ActionResultType.SUCCESS;
	}
}
