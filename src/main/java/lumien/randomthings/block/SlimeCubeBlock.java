package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.SlimeCubeTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * A small glowing block that always/never allows slime spawns in its own
 * chunk depending on whether it's redstone-powered - see
 * {@link SlimeCubeTileEntity}.
 */
public class SlimeCubeBlock extends Block implements EntityBlock {
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	private static final VoxelShape SHAPE = Block.box(6, 6, 6, 10, 10, 10);

	public SlimeCubeBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.CLAY).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.FLUTE).noOcclusion().noCollission().strength(0.5F));

		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(POWERED);
	}

	@Override
	public RenderShape getRenderShape(BlockState state)
	{
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new SlimeCubeTileEntity(pos, state);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving)
	{
		boolean powered = worldIn.hasNeighborSignal(pos);

		if (state.getValue(POWERED) != powered)
		{
			worldIn.setBlock(pos, state.setValue(POWERED, powered), 3);
		}

		BlockEntity te = worldIn.getBlockEntity(pos);

		if (te instanceof SlimeCubeTileEntity)
		{
			((SlimeCubeTileEntity) te).updatePowerState(powered);
		}
	}
}
