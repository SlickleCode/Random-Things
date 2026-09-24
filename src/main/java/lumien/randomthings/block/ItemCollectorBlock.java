package lumien.randomthings.block;

import lumien.randomthings.tileentity.ItemCollectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.BlockRenderLayer;

/**
 * A small directional plate mounted onto an inventory that vacuums up
 * nearby dropped items and feeds them in.
 */
public class ItemCollectorBlock extends Block
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private static final VoxelShape SHAPE_UP = Block.makeCuboidShape(6, 0, 6, 10, 3.5, 10);
	private static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(6, 12.5, 6, 10, 16, 10);
	private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(6, 6, 12.5, 10, 10, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(6, 6, 0, 10, 10, 3.5);
	private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(12.5, 6, 6, 16, 10, 10);
	private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(0, 6, 6, 3.5, 10, 10);

	public ItemCollectorBlock()
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(0.3F).doesNotBlockMovement());

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		switch (state.get(FACING))
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
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.empty();
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new ItemCollectorTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		// FACING points from this block toward the inventory it's mounted on,
		// i.e. the side of the existing block that was clicked to place it.
		return this.getDefaultState().with(FACING, context.getFace());
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
