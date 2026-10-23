package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class GlowingMushroomBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.box(4.8, 0, 4.8, 11.2, 6.4, 11.2);

	/**
	 * Light the mushroom gives off. Must stay below 13: the survival rule ({@link #canBlockStay}) rejects light of 13+ at the
	 * mushroom's own position (1.12.2's "not in a bright spot" rule), so a brighter mushroom counted its own glow, dropped itself
	 * on its first neighbour update and could never spread (its neighbours are one level dimmer than itself).
	 */
	public static final int GLOW_LEVEL = 12;

	public GlowingMushroomBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion().strength(0.0F).lightLevel(state -> GLOW_LEVEL).sound(SoundType.GRASS).randomTicks().noCollission());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		return canBlockStay(worldIn, pos);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving)
	{
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		this.checkAndDropBlock(worldIn, pos, state);
	}

	/** 1.20.5 made {@code randomTick} a no-op by default (it used to call {@code tick}), so random ticks are forwarded explicitly. */
	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		this.tick(state, worldIn, pos, rand);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		if (rand.nextInt(20) == 0)
		{
			int remainingBudget = 5;

			for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4)))
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
				BlockPos next = candidate.offset(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);

				if (worldIn.isEmptyBlock(next) && canBlockStay(worldIn, next))
				{
					candidate = next;
				}
			}

			if (candidate != pos && worldIn.isEmptyBlock(candidate) && canBlockStay(worldIn, candidate))
			{
				worldIn.setBlock(candidate, this.defaultBlockState(), 2);
			}
		}

		this.checkAndDropBlock(worldIn, pos, state);
	}

	protected void checkAndDropBlock(Level worldIn, BlockPos pos, BlockState state)
	{
		if (!canBlockStay(worldIn, pos))
		{
			dropResources(state, worldIn, pos);
			worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	/**
	 * Placement check for world generation: the soil rule of {@link #canBlockStay} without the light rule. Chunks that are still being
	 * generated have no light data (the 1.16 light engine reports full sky light there), so the light rule rejected every position
	 * and no patch ever generated; generation already requires "no sky access" separately.
	 */
	public static boolean canGrowDuringWorldgen(LevelReader worldIn, BlockPos pos)
	{
		if (pos.getY() < worldIn.getMinY() || pos.getY() >= worldIn.getMaxY() + 1)
		{
			return false;
		}

		BlockState soil = worldIn.getBlockState(pos.below());
		return soil.getBlock() == Blocks.MYCELIUM || canSustainBush(soil);
	}

	public static boolean canBlockStay(LevelReader worldIn, BlockPos pos)
	{
		if (pos.getY() < worldIn.getMinY() || pos.getY() >= worldIn.getMaxY() + 1)
		{
			return false;
		}

		BlockState soil = worldIn.getBlockState(pos.below());

		if (soil.getBlock() == Blocks.MYCELIUM)
		{
			return true;
		}

		return worldIn.getRawBrightness(pos, 0) < 13 && canSustainBush(soil);
	}

	protected static boolean canSustainBush(BlockState state)
	{
		Block block = state.getBlock();
		return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.STONE || block == ModBlocks.FERTILIZED_DIRT;
	}
}
