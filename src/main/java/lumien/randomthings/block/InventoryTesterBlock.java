package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.InventoryTesterTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * A small redstone-comparator-like nub: emits weak power based on whether
 * the item in its own slot could be inserted into the inventory it faces.
 * <p>
 * Simplification, disclosed here: the original refused to be placed except
 * directly against a real inventory, and popped itself off if that neighbor
 * was later removed. This port allows placement anywhere (it simply reads
 * no power, i.e. {@code isPowered() == false}, when nothing valid is behind
 * it) rather than reproducing that placement-validation/auto-pop behavior.
 */
public class InventoryTesterBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private static final VoxelShape SHAPE_UP = Block.box(6, 0, 6, 10, 1, 10);
	private static final VoxelShape SHAPE_DOWN = Block.box(6, 15, 6, 10, 16, 10);
	private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 15, 10, 10, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 0, 10, 10, 1);
	private static final VoxelShape SHAPE_WEST = Block.box(15, 6, 6, 16, 10, 10);
	private static final VoxelShape SHAPE_EAST = Block.box(0, 6, 6, 1, 10, 10);

	public InventoryTesterBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).noOcclusion().strength(0.3F).noCollission());

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		switch (state.getValue(FACING))
		{
			case DOWN:
				return SHAPE_DOWN;
			case NORTH:
				return SHAPE_NORTH;
			case SOUTH:
				return SHAPE_SOUTH;
			case WEST:
				return SHAPE_WEST;
			case EAST:
				return SHAPE_EAST;
			case UP:
			default:
				return SHAPE_UP;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return Shapes.empty();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new InventoryTesterTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.INVENTORY_TESTER, (l, p, s2, be) -> be.tick());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		BlockEntity te = blockAccess.getBlockEntity(pos);
		return te instanceof InventoryTesterTileEntity && ((InventoryTesterTileEntity) te).isPowered() ? 15 : 0;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof InventoryTesterTileEntity)
			{
				((ServerPlayer) player).openMenu((InventoryTesterTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
