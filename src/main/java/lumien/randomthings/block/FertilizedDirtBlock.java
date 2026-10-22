package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Random;

import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.server.level.ServerLevel;


public class FertilizedDirtBlock extends Block
{
	private static final VoxelShape SHAPE_TILLED = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

	public static final BooleanProperty TILLED = BooleanProperty.create("tilled");

	public FertilizedDirtBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL).randomTicks());

		this.registerDefaultState(this.stateDefinition.any().setValue(TILLED, false));
	}

	/** 1.20.5 made {@code randomTick} a no-op by default (it used to call {@code tick}), so random ticks are forwarded explicitly. */
	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
	{
		this.tick(state, worldIn, pos, random);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
	{
		super.tick(state, worldIn, pos, random);

		for (int i = 0; i < 3; i++)
		{
			BlockState aboveState = worldIn.getBlockState(pos.above());
			Block aboveBlock = aboveState.getBlock();

			if (aboveBlock instanceof net.minecraft.world.level.block.BushBlock && aboveState.isRandomlyTicking())
			{
				aboveState.randomTick(worldIn, pos.above(), random);
			}
			else
			{
				break;
			}
		}
	}

	public boolean func_220074_n(BlockState state)
	{
		return state.getValue(TILLED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return state.getValue(TILLED) ? SHAPE_TILLED : Shapes.block();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(TILLED);
	}

	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type)
	{
		return false;
	}

	/**
	 * 1.21 dropped {@code IPlantable}/{@code PlantType}: the plant now arrives as a block state, so the old plant types
	 * are derived from the plant block (stems and crops need tilled soil, desert plants untilled, the rest follow "plains").
	 */
	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, BlockState plant)
	{
		boolean tilled = state.getValue(TILLED);
		Block b = plant.getBlock();

		if (b instanceof AttachedStemBlock)
		{
			return TriState.TRUE;
		}

		if (b instanceof net.minecraft.world.level.block.NetherWartBlock || b instanceof net.minecraft.world.level.block.WaterlilyBlock)
		{
			return TriState.FALSE;
		}

		if (b instanceof net.minecraft.world.level.block.DeadBushBlock || b instanceof net.minecraft.world.level.block.CactusBlock || b instanceof net.minecraft.world.level.block.SugarCaneBlock)
		{
			return tilled ? TriState.FALSE : TriState.TRUE;
		}

		if (b instanceof net.minecraft.world.level.block.CropBlock || b instanceof net.minecraft.world.level.block.StemBlock)
		{
			return tilled ? TriState.TRUE : TriState.FALSE;
		}

		boolean plains = !tilled || world.getBlockState(pos.above()).getBlock() == Blocks.BEETROOTS;
		return plains ? TriState.TRUE : TriState.FALSE;
	}

	@Override
	public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos)
	{
		return true;
	}
}
