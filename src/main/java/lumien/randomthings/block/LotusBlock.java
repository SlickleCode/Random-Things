package lumien.randomthings.block;

import java.util.Random;

import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * The 1.12.2 version also implemented IGrowable, but with canUseBonemeal
 * always false and an empty grow() body - dead code with no observable
 * effect, so it's dropped here rather than ported faithfully.
 */
public class LotusBlock extends BushBlock
{
	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
			Block.makeCuboidShape(1, 0, 1, 15, 5, 15),
			Block.makeCuboidShape(1, 0, 1, 15, 9, 15),
			Block.makeCuboidShape(1, 0, 1, 15, 11, 15),
			Block.makeCuboidShape(1, 0, 1, 15, 14, 15)
	};

	public LotusBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.0F).tickRandomly().sound(SoundType.PLANT).doesNotBlockMovement());

		this.setDefaultState(this.stateContainer.getBaseState().with(AGE, 0));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(AGE);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE_BY_AGE[state.get(AGE)];
	}

	@Override
	protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		Block block = state.getBlock();
		return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND;
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		int age = state.get(AGE);

		if (age < 3 && rand.nextInt(10) == 0)
		{
			worldIn.setBlockState(pos, state.with(AGE, age + 1), 2);
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (state.get(AGE) == 3)
		{
			if (!worldIn.isRemote)
			{
				worldIn.setBlockState(pos, state.with(AGE, 0));

				ItemStack stack = new ItemStack(ModItems.LOTUS_BLOSSOM);

				if (!player.addItemStackToInventory(stack))
				{
					spawnAsEntity(worldIn, pos, stack);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state)
	{
		return new ItemStack(ModItems.LOTUS_SEEDS);
	}
}
