package lumien.randomthings.block.plates;

import lumien.randomthings.tileentity.ProcessingPlateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
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
 * A thin conveyor plate: sits on top of an inventory, feeds any dropped item
 * that touches it into the inventory below, and separately pulls items back
 * out of that same inventory on a timer to eject them toward its output
 * side - useful for wrapping a single-slot machine (like a furnace) into an
 * automated in/out loop.
 */
public class ProcessingPlateBlock extends PlateBlock
{
	public static final DirectionProperty OUTPUT_FACING = DirectionProperty.create("outputfacing", Direction.Plane.HORIZONTAL);

	public ProcessingPlateBlock()
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));

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
		return new ProcessingPlateTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		LivingEntity placer = context.getPlayer();
		Direction facing = placer != null ? placer.getHorizontalFacing() : Direction.NORTH;

		return this.getDefaultState().with(OUTPUT_FACING, facing);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		if (!worldIn.isRemote && entityIn instanceof ItemEntity && entityIn.isAlive())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof ProcessingPlateTileEntity)
			{
				((ProcessingPlateTileEntity) te).tryInsert((ItemEntity) entityIn);
			}
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (hit.getFace() == Direction.UP && player.isSneaking())
		{
			if (!worldIn.isRemote)
			{
				worldIn.setBlockState(pos, state.with(OUTPUT_FACING, state.get(OUTPUT_FACING).getOpposite()));
			}

			return true;
		}

		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof ProcessingPlateTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (ProcessingPlateTileEntity) te);
			}
		}

		return true;
	}
}
