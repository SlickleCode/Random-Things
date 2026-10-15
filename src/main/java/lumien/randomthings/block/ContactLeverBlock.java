package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * A lever-like block that is toggled by external code rather than a player
 * click, unlike vanilla's LeverBlock.
 */
public class ContactLeverBlock extends Block
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	public ContactLeverBlock()
	{
		super(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F));

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING, POWERED);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!isMoving && state.getBlock() != newState.getBlock())
		{
			if (state.getValue(POWERED))
			{
				this.notifyNeighbors(worldIn, pos, state.getValue(FACING));
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	private void notifyNeighbors(Level worldIn, BlockPos pos, Direction facing)
	{
		worldIn.updateNeighborsAt(pos, this);

		for (Direction f : Direction.values())
		{
			worldIn.updateNeighborsAt(pos.relative(f), this);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		LivingEntity placer = context.getPlayer();
		Direction facing = placer != null ? Direction.getNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z) : Direction.NORTH;

		return this.defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (placer != null)
		{
			Direction facing = Direction.getNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z);
			worldIn.setBlock(pos, state.setValue(FACING, facing), 2);
		}
	}

	/**
	 * Called by an external trigger (e.g. an entity walking into the block) to
	 * toggle this lever's powered state.
	 */
	public void activate(Level world, BlockPos pos, Direction fromFacing)
	{
		BlockState state = world.getBlockState(pos);
		BlockState newState = state.setValue(POWERED, !state.getValue(POWERED));

		world.setBlock(pos, newState, 3);
		this.notifyNeighbors(world, pos, fromFacing);
		world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, newState.getValue(POWERED) ? 0.6F : 0.5F);
	}
}
