package lumien.randomthings.block.plates;

import lumien.randomthings.tileentity.ExtractionPlateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * A pressure-plate-thin block that pulls items out of the inventory below
 * (or behind its output side) and ejects them as dropped items.
 */
public class ExtractionPlateBlock extends PlateBlock
{
	public static final DirectionProperty OUTPUT_FACING = DirectionProperty.create("outputfacing", Direction.Plane.HORIZONTAL);

	public ExtractionPlateBlock()
	{
		super(Block.Properties.create(Material.ROCK, net.minecraft.block.material.MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));

		this.setDefaultState(this.stateContainer.getBaseState().with(OUTPUT_FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(OUTPUT_FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new ExtractionPlateTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		LivingEntity placer = context.getPlayer();
		Direction facing = placer != null ? placer.getHorizontalFacing() : Direction.NORTH;

		return this.getDefaultState().with(OUTPUT_FACING, facing);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (hit.getFace() == Direction.UP && player.isSneaking())
		{
			Direction newOutput = Direction.getFacingFromVector((float) (hit.getHitVec().x - pos.getX() - 0.5), 0, (float) (hit.getHitVec().z - pos.getZ() - 0.5));

			if (newOutput.getAxis() != Direction.Axis.Y && state.get(OUTPUT_FACING) != newOutput)
			{
				if (!worldIn.isRemote)
				{
					worldIn.setBlockState(pos, state.with(OUTPUT_FACING, newOutput));
				}

				return true;
			}
		}

		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof ExtractionPlateTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (ExtractionPlateTileEntity) te);
			}
		}

		return true;
	}
}
