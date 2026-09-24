package lumien.randomthings.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Plants {@link #plantedBlock} in the air space above the clicked block, like
 * a seed item. Shared by the three 1.12.2 "beans" metadata variants (plain
 * bean, lesser magic bean, magic bean), each now its own registered item
 * pointing at a different block per the discrete-block-per-variant
 * convention used throughout this port.
 */
public class BeanItem extends Item
{
	private final Block plantedBlock;
	private final boolean scheduleGrowth;

	public BeanItem(Item.Properties properties, Block plantedBlock, boolean scheduleGrowth)
	{
		super(properties);

		this.plantedBlock = plantedBlock;
		this.scheduleGrowth = scheduleGrowth;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		if (context.getFace() != Direction.UP)
		{
			return ActionResultType.FAIL;
		}

		World world = context.getWorld();
		BlockPos pos = context.getPos().up();
		ItemStack stack = context.getItem();

		if (context.getPlayer() != null && !context.getPlayer().canPlayerEdit(pos, Direction.UP, stack))
		{
			return ActionResultType.FAIL;
		}

		if (!world.isAirBlock(pos))
		{
			return ActionResultType.FAIL;
		}

		if (!plantedBlock.getDefaultState().isValidPosition(world, pos))
		{
			return ActionResultType.FAIL;
		}

		world.setBlockState(pos, plantedBlock.getDefaultState());

		if (scheduleGrowth)
		{
			world.getPendingBlockTicks().scheduleTick(pos, plantedBlock, 20);
		}

		world.playSound(null, pos, plantedBlock.getSoundType(plantedBlock.getDefaultState()).getPlaceSound(), SoundCategory.BLOCKS, 1, 1);

		stack.shrink(1);
		return ActionResultType.SUCCESS;
	}
}
