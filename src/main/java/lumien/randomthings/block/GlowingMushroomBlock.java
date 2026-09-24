package lumien.randomthings.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

public class GlowingMushroomBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(4.8, 0, 4.8, 11.2, 6.4, 11.2);

	public GlowingMushroomBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.0F).lightValue(15).sound(SoundType.PLANT).tickRandomly().doesNotBlockMovement());
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.empty();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return canBlockStay(worldIn, pos);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		this.checkAndDropBlock(worldIn, pos, state);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (rand.nextInt(20) == 0)
		{
			int remainingBudget = 5;

			for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4)))
			{
				if (worldIn.getBlockState(p).getBlock() == this)
				{
					if (--remainingBudget <= 0)
					{
						return;
					}
				}
			}

			BlockPos candidate = pos;

			for (int k = 0; k < 4; ++k)
			{
				BlockPos next = candidate.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);

				if (worldIn.isAirBlock(next) && canBlockStay(worldIn, next))
				{
					candidate = next;
				}
			}

			if (candidate != pos && worldIn.isAirBlock(candidate) && canBlockStay(worldIn, candidate))
			{
				worldIn.setBlockState(candidate, this.getDefaultState(), 2);
			}
		}

		this.checkAndDropBlock(worldIn, pos, state);
	}

	protected void checkAndDropBlock(World worldIn, BlockPos pos, BlockState state)
	{
		if (!canBlockStay(worldIn, pos))
		{
			spawnDrops(state, worldIn, pos);
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		}
	}

	public static boolean canBlockStay(IWorldReader worldIn, BlockPos pos)
	{
		if (pos.getY() < 0 || pos.getY() >= 256)
		{
			return false;
		}

		BlockState soil = worldIn.getBlockState(pos.down());

		if (soil.getBlock() == Blocks.MYCELIUM)
		{
			return true;
		}

		return worldIn.getLightSubtracted(pos, 0) < 13 && canSustainBush(soil);
	}

	protected static boolean canSustainBush(BlockState state)
	{
		Block block = state.getBlock();
		return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.STONE || block == ModBlocks.FERTILIZED_DIRT;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
