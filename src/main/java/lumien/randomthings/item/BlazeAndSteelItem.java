package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BlazeAndSteelItem extends Item
{
	public BlazeAndSteelItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
		ItemStack stack = context.getItemInHand();

		if (player != null && !player.mayUseItemAt(pos, context.getClickedFace(), stack))
		{
			return InteractionResult.FAIL;
		}

		if (world.isEmptyBlock(pos))
		{
			world.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.8F + 0.8F);
			world.setBlock(pos, ModBlocks.BLAZING_FIRE.defaultBlockState(), 11);
		}

		if (player != null)
		{
			stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
		}

		return InteractionResult.SUCCESS;
	}
}
