package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
		super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F));

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(FACING, POWERED);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!isMoving && state.getBlock() != newState.getBlock())
		{
			if (state.get(POWERED))
			{
				this.notifyNeighbors(worldIn, pos, state.get(FACING));
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return blockState.get(POWERED) ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return blockState.get(POWERED) ? 15 : 0;
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	private void notifyNeighbors(World worldIn, BlockPos pos, Direction facing)
	{
		worldIn.notifyNeighborsOfStateChange(pos, this);

		for (Direction f : Direction.values())
		{
			worldIn.notifyNeighborsOfStateChange(pos.offset(f), this);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		LivingEntity placer = context.getPlayer();
		Direction facing = placer != null ? Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z) : Direction.NORTH;

		return this.getDefaultState().with(FACING, facing);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (placer != null)
		{
			Direction facing = Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z);
			worldIn.setBlockState(pos, state.with(FACING, facing), 2);
		}
	}

	/**
	 * Called by an external trigger (e.g. an entity walking into the block) to
	 * toggle this lever's powered state.
	 */
	public void activate(World world, BlockPos pos, Direction fromFacing)
	{
		BlockState state = world.getBlockState(pos);
		BlockState newState = state.with(POWERED, !state.get(POWERED));

		world.setBlockState(pos, newState, 3);
		this.notifyNeighbors(world, pos, fromFacing);
		world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, newState.get(POWERED) ? 0.6F : 0.5F);
	}
}
