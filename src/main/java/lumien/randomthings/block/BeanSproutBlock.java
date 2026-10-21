package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Extends vanilla's CropsBlock, which already implements the age/growth/
 * bonemeal logic the 1.12.2 version hand-rolled. The one bit of custom
 * behavior kept is the original's "harvest without breaking": right-clicking
 * a fully grown sprout resets it to age 0 and drops beans, instead of
 * breaking it like a normal crop.
 */
public class BeanSproutBlock extends CropBlock
{
	public BeanSproutBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noCollission().randomTicks().strength(0.0F).sound(SoundType.GRASS));
	}

	@Override
	protected ItemLike getBaseSeedId()
	{
		return ModItems.BEANS;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos)
	{
		// 1.12.2's sprout was a plain-type plant (grass, dirt, farmland); CropBlock alone would only allow farmland
		return state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.DIRT) || state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT) || state.is(net.minecraft.world.level.block.Blocks.PODZOL) || state.is(net.minecraft.world.level.block.Blocks.FARMLAND);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit)
	{
		if (this.isMaxAge(state))
		{
			if (!worldIn.isClientSide)
			{
				worldIn.setBlockAndUpdate(pos, this.getStateForAge(0));

				ItemStack stack = new ItemStack(ModItems.BEANS, worldIn.random.nextInt(2) + 1);

				if (!player.addItem(stack))
				{
					popResource(worldIn, pos, stack);
				}
			}

			return InteractionResult.SUCCESS;
		}

		return super.useWithoutItem(state, worldIn, pos, player, hit);
	}
}
