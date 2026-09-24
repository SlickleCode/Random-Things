package lumien.randomthings.block;

import lumien.randomthings.tileentity.IgniterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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
 * A block version of flint and steel: reacts to redstone changes on itself
 * by igniting/extinguishing the block directly in front of it (per its
 * configurable mode).
 */
public class IgniterBlock extends Block
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	public IgniterBlock()
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F));

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new IgniterTileEntity();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFireSource(BlockState state, IBlockReader world, BlockPos pos, Direction side)
	{
		return side == state.get(FACING);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		TileEntity te = worldIn.getTileEntity(pos);

		if (te instanceof IgniterTileEntity)
		{
			((IgniterTileEntity) te).updatePowerState(worldIn.isBlockPowered(pos));
		}
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, java.util.Random rand)
	{
		TileEntity te = worldIn.getTileEntity(pos);

		if (te instanceof IgniterTileEntity)
		{
			((IgniterTileEntity) te).checkKeepIgnited();
		}

		if (!worldIn.isRemote)
		{
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 10);
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (!worldIn.isRemote)
		{
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 10);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		LivingEntity placer = context.getPlayer();
		Direction facing = placer != null ? Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z) : Direction.NORTH;

		return this.getDefaultState().with(FACING, facing);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (placer != null)
		{
			Direction facing = Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z);
			worldIn.setBlockState(pos, state.with(FACING, facing), 2);
		}

		if (!worldIn.isRemote)
		{
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, 10);
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof IgniterTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (IgniterTileEntity) te);
			}
		}

		return true;
	}
}
