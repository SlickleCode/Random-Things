package lumien.randomthings.block.spectretree;

import java.util.Random;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

/**
 * The 1.12.2 version generated its grown tree through a custom
 * {@code WorldGenerator} (world-gen code, out of scope for this blocks-only
 * batch). This is a simplified direct placement of a small log-and-leaves
 * tree; it should be revisited with a proper {@code Feature}-based generator
 * once the worldgen batch adapts the rest of the mod's tree/structure
 * generation to the 1.14.4 Feature API.
 */
public class SpectreSaplingBlock extends BushBlock implements IGrowable
{
	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(1.6, 0, 1.6, 14.4, 12.8, 14.4);

	public SpectreSaplingBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.0F).tickRandomly().sound(SoundType.PLANT).doesNotBlockMovement());

		this.setDefaultState(this.stateContainer.getBaseState().with(STAGE, 0));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);

		builder.add(STAGE);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (!worldIn.isRemote)
		{
			if (worldIn.getLightSubtracted(pos.up(), 0) >= 9 && rand.nextInt(14) == 0)
			{
				this.grow(worldIn, rand, pos, state);
			}
		}
	}

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient)
	{
		return true;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state)
	{
		return true;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, BlockState state)
	{
		if (state.get(STAGE) == 0)
		{
			worldIn.setBlockState(pos, state.with(STAGE, 1), 3);
		}
		else
		{
			this.generateTree(worldIn, pos, rand);
		}
	}

	private void generateTree(World worldIn, BlockPos pos, Random rand)
	{
		int height = 4 + rand.nextInt(3);

		BlockState log = ModBlocks.SPECTRE_LOG.getDefaultState();
		BlockState leaves = ModBlocks.SPECTRE_LEAF.getDefaultState();

		worldIn.setBlockState(pos, log, 3);

		for (int y = 1; y < height; y++)
		{
			worldIn.setBlockState(pos.up(y), log, 3);
		}

		for (int dx = -2; dx <= 2; dx++)
		{
			for (int dz = -2; dz <= 2; dz++)
			{
				for (int dy = -1; dy <= 1; dy++)
				{
					if (Math.abs(dx) == 2 && Math.abs(dz) == 2)
					{
						continue;
					}

					BlockPos leafPos = pos.add(dx, height + dy, dz);

					if (worldIn.getBlockState(leafPos).getBlock().isAir(worldIn.getBlockState(leafPos), worldIn, leafPos))
					{
						worldIn.setBlockState(leafPos, leaves, 3);
					}
				}
			}
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return worldIn.getBlockState(pos.down()).canSustainPlant(worldIn, pos.down(), net.minecraft.util.Direction.UP, this);
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
