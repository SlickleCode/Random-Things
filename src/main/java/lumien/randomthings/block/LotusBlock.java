package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Random;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;

/**
 * The 1.12.2 version also implemented IGrowable, but with canUseBonemeal
 * always false and an empty grow() body - dead code with no observable
 * effect, so it's dropped here rather than ported faithfully.
 */
public class LotusBlock extends BushBlock
{
	public static final com.mojang.serialization.MapCodec<LotusBlock> CODEC = simpleCodec(properties -> new LotusBlock());

	@Override
	public com.mojang.serialization.MapCodec<BushBlock> codec()
	{
		return (com.mojang.serialization.MapCodec<BushBlock>) (com.mojang.serialization.MapCodec<?>) CODEC;
	}

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

	private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
			Block.box(1, 0, 1, 15, 5, 15),
			Block.box(1, 0, 1, 15, 9, 15),
			Block.box(1, 0, 1, 15, 11, 15),
			Block.box(1, 0, 1, 15, 14, 15)
	};

	public LotusBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).strength(0.0F).randomTicks().sound(SoundType.GRASS).noCollision());

		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(AGE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE_BY_AGE[state.getValue(AGE)];
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		Block block = state.getBlock();
		return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.FARMLAND;
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
		int age = state.getValue(AGE);

		if (age < 3 && rand.nextInt(10) == 0)
		{
			worldIn.setBlock(pos, state.setValue(AGE, age + 1), 2);
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(AGE) == 3)
		{
			if (!worldIn.isClientSide())
			{
				worldIn.setBlockAndUpdate(pos, state.setValue(AGE, 0));

				ItemStack stack = new ItemStack(ModItems.LOTUS_BLOSSOM);

				if (!player.addItem(stack))
				{
					popResource(worldIn, pos, stack);
				}
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Override
	public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader worldIn, BlockPos pos, BlockState state, boolean includeData)
	{
		return new ItemStack(ModItems.LOTUS_SEEDS);
	}
}
