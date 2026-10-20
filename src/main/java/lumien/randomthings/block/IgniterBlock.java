package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.IgniterTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.LevelReader;

/**
 * A block version of flint and steel: reacts to redstone changes on itself
 * by igniting/extinguishing the block directly in front of it (per its
 * configurable mode).
 */
public class IgniterBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	public IgniterBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(1.5F));

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new IgniterTileEntity(pos, state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFireSource(BlockState state, LevelReader world, BlockPos pos, Direction side)
	{
		return side == state.getValue(FACING);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		BlockEntity te = worldIn.getBlockEntity(pos);

		if (te instanceof IgniterTileEntity)
		{
			((IgniterTileEntity) te).updatePowerState(worldIn.hasNeighborSignal(pos));
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

		if (!worldIn.isClientSide)
		{
			worldIn.scheduleTick(pos, this, 10);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
	{
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof IgniterTileEntity)
			{
				NetworkHooks.openScreen((ServerPlayer) player, (IgniterTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
