package lumien.randomthings.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.BlockRenderLayer;
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
		if (!worldIn.isRemote && !state.get(TRIGGERED))
		{
			boolean powered = worldIn.isBlockPowered(pos);

			BlockState fromState = worldIn.getBlockState(fromPos);

			if (powered || (fromState.getBlock() == this && fromState.get(TRIGGERED)))
			{
				worldIn.setBlockState(pos, state.with(TRIGGERED, true));
				worldIn.getPendingBlockTicks().scheduleTick(pos, this, 60);
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
