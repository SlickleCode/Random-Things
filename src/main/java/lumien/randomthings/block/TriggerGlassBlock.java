package lumien.randomthings.block;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
	private static final int MAX_CHAIN = 20;

	public TriggerGlassBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.QUARTZ).hardnessAndResistance(0.3F).sound(SoundType.GLASS));

		this.setDefaultState(this.stateContainer.getBaseState().with(TRIGGERED, false));
	}

	@Override
	public Material getMaterial(BlockState state)
	{
		return state.get(TRIGGERED) ? Material.PLANTS : super.getMaterial(state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return state.get(TRIGGERED) ? VoxelShapes.empty() : super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return !state.get(TRIGGERED);
	}

	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext)
	{
		return false;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		if (!worldIn.isRemote && !state.get(TRIGGERED) && worldIn.isBlockPowered(pos))
		{
			triggerConnected(worldIn, pos);
		}
	}

	/**
	 * Iterative, capped breadth-first flood fill starting from the directly-
	 * powered block: triggers it, then spreads to touching untriggered
	 * TriggerGlass blocks, stopping once {@link #MAX_CHAIN} blocks total have
	 * been triggered.
	 */
	private void triggerConnected(World world, BlockPos origin)
	{
		Queue<BlockPos> queue = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		queue.add(origin);
		seen.add(origin);

		int triggeredCount = 0;

		while (!queue.isEmpty() && triggeredCount < MAX_CHAIN)
		{
			BlockPos pos = queue.poll();
			BlockState state = world.getBlockState(pos);

			if (state.getBlock() != this || state.get(TRIGGERED))
			{
				continue;
			}

			world.setBlockState(pos, state.with(TRIGGERED, true), 3);
			world.getPendingBlockTicks().scheduleTick(pos, this, 60);
			triggeredCount++;

			for (Direction direction : Direction.values())
			{
				BlockPos neighborPos = pos.offset(direction);

				if (seen.add(neighborPos) && world.getBlockState(neighborPos).getBlock() == this)
				{
					queue.add(neighborPos);
				}
			}
		}
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (!worldIn.isRemote && state.get(TRIGGERED))
		{
			worldIn.setBlockState(pos, state.with(TRIGGERED, false));
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(TRIGGERED);
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
