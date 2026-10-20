package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.world.level.Level;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.ItemCollectorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

/**
 * A small directional plate mounted onto an inventory that vacuums up
 * nearby dropped items and feeds them in.
 */
public class ItemCollectorBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private static final VoxelShape SHAPE_UP = Block.box(6, 0, 6, 10, 3.5, 10);
	private static final VoxelShape SHAPE_DOWN = Block.box(6, 12.5, 6, 10, 16, 10);
	private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 12.5, 10, 10, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 0, 10, 10, 3.5);
	private static final VoxelShape SHAPE_WEST = Block.box(12.5, 6, 6, 16, 10, 10);
	private static final VoxelShape SHAPE_EAST = Block.box(0, 6, 6, 3.5, 10, 10);

	public ItemCollectorBlock()
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
	    return new ItemCollectorTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.ITEM_COLLECTOR, (l, p, s2, be) -> be.tick());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		// FACING points from this block toward the inventory it's mounted on,
		// i.e. the side of the existing block that was clicked to place it.
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}
}
