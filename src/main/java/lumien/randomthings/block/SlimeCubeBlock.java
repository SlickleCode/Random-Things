package lumien.randomthings.block;

import lumien.randomthings.tileentity.SlimeCubeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

/**
 * A small glowing block that always/never allows slime spawns in its own
 * chunk depending on whether it's redstone-powered - see
 * {@link SlimeCubeTileEntity}.
 */
public class SlimeCubeBlock extends Block
{
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	private static final VoxelShape SHAPE = Block.makeCuboidShape(6, 6, 6, 10, 10, 10);

	public SlimeCubeBlock()
	{
		super(Block.Properties.create(Material.CLAY).doesNotBlockMovement().hardnessAndResistance(0.5F));

		this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(POWERED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new SlimeCubeTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		boolean powered = worldIn.isBlockPowered(pos);

		if (state.get(POWERED) != powered)
		{
			worldIn.setBlockState(pos, state.with(POWERED, powered), 3);
		}

		TileEntity te = worldIn.getTileEntity(pos);

		if (te instanceof SlimeCubeTileEntity)
		{
			((SlimeCubeTileEntity) te).updatePowerState(powered);
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
