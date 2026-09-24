package lumien.randomthings.block;

import lumien.randomthings.tileentity.InventoryTesterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.util.BlockRenderLayer;

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
public class InventoryTesterBlock extends Block
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private static final VoxelShape SHAPE_UP = Block.makeCuboidShape(6, 0, 6, 10, 1, 10);
	private static final VoxelShape SHAPE_DOWN = Block.makeCuboidShape(6, 15, 6, 10, 16, 10);
	private static final VoxelShape SHAPE_NORTH = Block.makeCuboidShape(6, 6, 15, 10, 10, 16);
	private static final VoxelShape SHAPE_SOUTH = Block.makeCuboidShape(6, 6, 0, 10, 10, 1);
	private static final VoxelShape SHAPE_WEST = Block.makeCuboidShape(15, 6, 6, 16, 10, 10);
	private static final VoxelShape SHAPE_EAST = Block.makeCuboidShape(0, 6, 6, 1, 10, 10);

	public InventoryTesterBlock()
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
		return new InventoryTesterTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(FACING, context.getFace());
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		TileEntity te = blockAccess.getTileEntity(pos);
		return te instanceof InventoryTesterTileEntity && ((InventoryTesterTileEntity) te).isPowered() ? 15 : 0;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof InventoryTesterTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (InventoryTesterTileEntity) te);
			}
		}

		return true;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
