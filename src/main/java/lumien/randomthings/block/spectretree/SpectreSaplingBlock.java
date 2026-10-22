package lumien.randomthings.block.spectretree;

import net.minecraft.util.RandomSource;
import java.util.Random;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * The 1.12.2 version generated its grown tree through a custom
 * {@code WorldGenerator} (world-gen code, out of scope for this blocks-only
 * batch). This is a simplified direct placement of a small log-and-leaves
 * tree; it should be revisited with a proper {@code Feature}-based generator
 * once the worldgen batch adapts the rest of the mod's tree/structure
 * generation to the 1.14.4 Feature API.
 */
public class SpectreSaplingBlock extends BushBlock implements BonemealableBlock
{
	public static final com.mojang.serialization.MapCodec<SpectreSaplingBlock> CODEC = simpleCodec(properties -> new SpectreSaplingBlock());

	@Override
	protected com.mojang.serialization.MapCodec<? extends BushBlock> codec()
	{
		return CODEC;
	}

	public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
	protected static final VoxelShape SHAPE = Block.box(1.6, 0, 1.6, 14.4, 12.8, 14.4);

	public SpectreSaplingBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion().strength(0.0F).randomTicks().sound(SoundType.GRASS).noCollission());

		this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);

		builder.add(STAGE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
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
		if (!worldIn.isClientSide)
		{
			if (worldIn.getRawBrightness(pos.above(), 0) >= 9 && rand.nextInt(14) == 0)
			{
				this.performBonemeal(worldIn, rand, pos, state);
			}
		}
	}

	@Override
	public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader worldIn, BlockPos pos, BlockState state)
	{
		return true;
	}

	@Override
	public boolean isBonemealSuccess(Level worldIn, RandomSource rand, BlockPos pos, BlockState state)
	{
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel worldIn, RandomSource rand, BlockPos pos, BlockState state)
	{
		if (state.getValue(STAGE) == 0)
		{
			worldIn.setBlock(pos, state.setValue(STAGE, 1), 3);
		}
		else
		{
			this.generateTree(worldIn, pos, rand);
		}
	}

	private void generateTree(Level worldIn, BlockPos pos, RandomSource rand)
	{
		int height = 4 + rand.nextInt(3);

		BlockState log = ModBlocks.SPECTRE_LOG.defaultBlockState();
		BlockState leaves = ModBlocks.SPECTRE_LEAF.defaultBlockState();

		worldIn.setBlock(pos, log, 3);

		for (int y = 1; y < height; y++)
		{
			worldIn.setBlock(pos.above(y), log, 3);
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

					BlockPos leafPos = pos.offset(dx, height + dy, dz);

					if (worldIn.getBlockState(leafPos).isAir())
					{
						worldIn.setBlock(leafPos, leaves, 3);
					}
				}
			}
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		BlockState ground = worldIn.getBlockState(pos.below());
		net.neoforged.neoforge.common.util.TriState result = ground.canSustainPlant(worldIn, pos.below(), net.minecraft.core.Direction.UP, state);
		return result.isTrue() || result.isDefault() && (ground.is(net.minecraft.tags.BlockTags.DIRT) || ground.is(net.minecraft.world.level.block.Blocks.FARMLAND));
	}
}
