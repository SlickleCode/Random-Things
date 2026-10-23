package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Random;

import javax.annotation.Nullable;

import lumien.randomthings.tileentity.AdvancedRedstoneTorchTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.particles.ParticleTypes;

public class AdvancedRedstoneWallTorchBlock extends AdvancedRedstoneTorchBlock
{
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<AdvancedRedstoneTorchBlock.COLOR> COLOR_PROPERTY = AdvancedRedstoneTorchBlock.COLOR_PROPERTY;

	protected AdvancedRedstoneWallTorchBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noCollission().strength(0).lightLevel(state -> 7).sound(SoundType.WOOD).overrideDescription("block.randomthings.advanced_redstone_torch"));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COLOR_PROPERTY, AdvancedRedstoneTorchBlock.COLOR.RED));
	}

	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return WallTorchBlock.getShape(state);
	}

	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		return Blocks.WALL_TORCH.defaultBlockState().setValue(FACING, state.getValue(FACING)).canSurvive(worldIn, pos);
	}

	/**
	 * Update the provided state given the provided neighbor facing and neighbor state, returning a new state. For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately returns its solidified counterpart. Note that this method should ideally consider only the specific face passed in.
	 */
	public BlockState updateShape(BlockState stateIn, net.minecraft.world.level.LevelReader worldIn, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, net.minecraft.util.RandomSource random)
	{
		return Blocks.WALL_TORCH.defaultBlockState().setValue(FACING, stateIn.getValue(FACING)).updateShape(worldIn, tickAccess, currentPos, facing, facingPos, facingState, random).is(Blocks.AIR) ? Blocks.AIR.defaultBlockState() : stateIn;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
		return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
	}

	/**
	 * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless of whether the block can receive random update ticks
	 */
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand)
	{
		if (stateIn.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			Direction direction = stateIn.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepZ();
			worldIn.addParticle(DustParticleOptions.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
		else
		{
			Direction direction = stateIn.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepZ();
			worldIn.addParticle(GREEN_DUST, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected boolean shouldBeGreen(Level worldIn, BlockPos pos, BlockState state)
	{
		Direction direction = state.getValue(FACING).getOpposite();
		return worldIn.hasSignal(pos.relative(direction), direction);
	}

	/**
	 * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible. Implementing/overriding is fine.
	 */
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		AdvancedRedstoneTorchTileEntity te = (AdvancedRedstoneTorchTileEntity) blockAccess.getBlockEntity(pos);

		int strength = blockState.getValue(COLOR_PROPERTY) == COLOR.RED ? te.signalStrengthRed() : te.signalStrengthGreen();

		return blockState.getValue(FACING) != side ? strength : 0;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is fine.
	 */
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
	 * 
	 * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
	 */
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, COLOR_PROPERTY);
	}
}