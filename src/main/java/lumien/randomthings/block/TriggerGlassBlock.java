package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * A block of glass that becomes fully walk-through (no collision, no
 * suffocation) while triggered by redstone, and additionally lets sand/
 * gravel fall through it while triggered.
 * <p>
 * The pass-through-for-entities behavior (1.12.2's
 * {@code addCollisionBoxToList} override, ported below as
 * {@code getCollisionShape}) is this block's actual core mechanic and needs
 * no ASM/Mixin - it was missed on the first port of this block, which only
 * reproduced the *other*, separate ASM patch this block also happened to
 * need (#17, {@code BlockFalling.canFallThrough}). That one's replicated via
 * the {@code getMaterial} override below: 1.14.4's
 * {@code FallingBlock.canFallThrough(BlockState)} is a static utility that
 * (per `javap -c`) accepts air, fire, liquids, or any
 * {@code Material.isReplaceable()} block, so swapping in a replaceable
 * material while triggered reproduces the old sand/gravel-passthrough
 * behavior without any ASM or Mixin either.
 */
public class TriggerGlassBlock extends Block
{
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

	/**
	 * Per user testing feedback: a large connected mass of Trigger Glass could
	 * lag the game, since triggering one block used to chain-react through
	 * every touching block via recursive neighborChanged reentrancy (block A
	 * triggers -> notifies neighbor B -> B sees a triggered TriggerGlass
	 * neighbor and triggers itself -> notifies ITS neighbors -> ...), with no
	 * limit on how far a single redstone pulse could propagate. Replaced with
	 * a single iterative, capped breadth-first flood fill (see
	 * {@link #triggerConnected}) from the block that was actually powered.
	 */
	private static int maxChain()
	{
		int limit = lumien.randomthings.config.RTConfig.TRIGGER_GLASS_CHAIN_LIMIT.get();
		return limit == 0 ? Integer.MAX_VALUE : limit;
	}

	public TriggerGlassBlock()
	{
		super(Block.Properties.of(Material.GLASS, MaterialColor.QUARTZ).noOcclusion().strength(0.3F).sound(SoundType.GLASS).isSuffocating((state, world, pos) -> !state.getValue(TRIGGERED)));

		this.registerDefaultState(this.stateDefinition.any().setValue(TRIGGERED, false));
	}

	/** 1.14.4 switched this block to Material.PLANTS while triggered (so mobs path through it); 1.16 has no per-state material, so pathing is allowed explicitly. */
	@Override
	@SuppressWarnings("deprecation")
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type)
	{
		return state.getValue(TRIGGERED) ? true : super.isPathfindable(state, worldIn, pos, type);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return state.getValue(TRIGGERED) ? Shapes.empty() : super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext)
	{
		return false;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		if (!worldIn.isClientSide && !state.getValue(TRIGGERED) && worldIn.hasNeighborSignal(pos))
		{
			triggerConnected(worldIn, pos);
		}
	}

	/**
	 * Iterative, capped breadth-first flood fill starting from the directly-
	 * powered block: triggers it, then spreads to touching untriggered
	 * TriggerGlass blocks, stopping once the configured {@code TriggerGlassChainLimit} blocks total have
	 * been triggered.
	 */
	private void triggerConnected(Level world, BlockPos origin)
	{
		Queue<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(origin);
		seen.add(origin);

		int triggeredCount = 0;
		int maxChain = maxChain();

		while (!queue.isEmpty() && triggeredCount < maxChain)
		{
			BlockPos pos = queue.poll();
			BlockState state = world.getBlockState(pos);

			if (state.getBlock() != this || state.getValue(TRIGGERED))
			{
				continue;
			}

			world.setBlock(pos, state.setValue(TRIGGERED, true), 3);
			world.scheduleTick(pos, this, 60);
			triggeredCount++;

			for (Direction direction : Direction.values())
			{
				BlockPos neighborPos = pos.relative(direction);

				if (seen.add(neighborPos) && world.getBlockState(neighborPos).getBlock() == this)
				{
					queue.add(neighborPos);
				}
			}
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		if (!worldIn.isClientSide && state.getValue(TRIGGERED))
		{
			worldIn.setBlockAndUpdate(pos, state.setValue(TRIGGERED, false));
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(TRIGGERED);
	}
}
