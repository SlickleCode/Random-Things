package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.IronDropperTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import lumien.randomthings.itemhandler.IItemHandler;
import net.minecraft.world.InteractionResult;

/**
 * A block-form item dropper with a configurable redstone mode/pickup delay/
 * motion/effects set, standing in for vanilla dispenser's single fixed
 * behavior. Six-way facing like a dispenser, opens a GUI instead of
 * dispensing on right-click.
 */
public class IronDropperBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.values());

	public IronDropperBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.METAL).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.IRON_XYLOPHONE).strength(3.5F).sound(SoundType.METAL));

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
	    return new IronDropperTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.IRON_DROPPER, (l, p, s2, be) -> be.tick());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving)
	{
		BlockEntity te = worldIn.getBlockEntity(pos);

		if (te instanceof IronDropperTileEntity)
		{
			((IronDropperTileEntity) te).updatePowerState(worldIn.hasNeighborSignal(pos));
		}
	}

	public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof IronDropperTileEntity)
			{
				IItemHandler handler = ((IronDropperTileEntity) te).itemHandler();

				for (int i = 0; i < handler.getSlots(); i++)
				{
					Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
				}
			}
		}

	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof IronDropperTileEntity)
			{
				((ServerPlayer) player).openMenu((IronDropperTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
