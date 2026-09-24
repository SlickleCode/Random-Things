package lumien.randomthings.block.plates;

import lumien.randomthings.tileentity.FilteredRedirectorPlateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Like {@link RedirectorPlateBlock}, but instead of a fixed configured
 * output side, it picks between a left turn and a right turn per-entity
 * based on two {@link lumien.randomthings.lib.IEntityFilterItem} filter
 * slots (falling through straight if neither matches); the second slot
 * wins if both match, matching 1.12.2's sequential-overwrite order exactly.
 */
public class FilteredRedirectorPlateBlock extends PlateBlock
{
	public static final DirectionProperty INPUT_FACING = DirectionProperty.create("inputfacing", Direction.Plane.HORIZONTAL);

	public FilteredRedirectorPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(INPUT_FACING);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new FilteredRedirectorPlateTileEntity();
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		Direction inputSide = state.get(INPUT_FACING);

		Vec3d motionVec = entityIn.getMotion();
		Direction roughMovingFacing = Direction.getFacingFromVector((float) motionVec.x, (float) motionVec.y, (float) motionVec.z).getOpposite();

		Vec3d center = new Vec3d(pos).add(0.5, 0, 0.5);
		Vec3d difVec = center.subtract(entityIn.getPositionVec());
		Direction facing = Direction.getFacingFromVector((float) difVec.x, (float) difVec.y, (float) difVec.z).getOpposite();

		if ((facing != inputSide && facing != inputSide.getOpposite()) || facing != roughMovingFacing)
		{
			return;
		}

		TileEntity te = worldIn.getTileEntity(pos);

		if (!(te instanceof FilteredRedirectorPlateTileEntity))
		{
			return;
		}

		FilteredRedirectorPlateTileEntity frpte = (FilteredRedirectorPlateTileEntity) te;

		Direction output = facing.getOpposite();

		if (frpte.matches(0, entityIn))
		{
			output = inputSide.rotateY();
		}

		if (frpte.matches(1, entityIn))
		{
			output = inputSide.rotateYCCW();
		}

		Vec3d facingVec = new Vec3d(output.getDirectionVec()).scale(0.4).add(center);
		float dif = facing.getOpposite().getHorizontalAngle() - output.getHorizontalAngle();
		Vec3d outputMotionVec = motionVec.rotateYaw((float) Math.toRadians(dif));

		entityIn.setPosition(facingVec.x, facingVec.y, facingVec.z);
		entityIn.setMotion(outputMotionVec);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof FilteredRedirectorPlateTileEntity)
			{
				FilteredRedirectorPlateTileEntity frpte = (FilteredRedirectorPlateTileEntity) te;
				NonNullList<ItemStack> drops = NonNullList.create();
				drops.add(frpte.filterInventory().getStackInSlot(0));
				drops.add(frpte.filterInventory().getStackInSlot(1));
				InventoryHelper.dropItems(worldIn, pos, drops);
			}
		}

		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof FilteredRedirectorPlateTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (FilteredRedirectorPlateTileEntity) te);
			}
		}

		return true;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		this.setDefaultFacing(worldIn, pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(INPUT_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (placer != null)
		{
			worldIn.setBlockState(pos, state.with(INPUT_FACING, placer.getHorizontalFacing().getOpposite()), 2);
		}
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, BlockState state)
	{
		if (worldIn.isRemote)
		{
			return;
		}

		BlockState north = worldIn.getBlockState(pos.north());
		BlockState south = worldIn.getBlockState(pos.south());
		BlockState west = worldIn.getBlockState(pos.west());
		BlockState east = worldIn.getBlockState(pos.east());
		Direction facing = state.get(INPUT_FACING);

		if (facing == Direction.NORTH && north.isSolid() && !south.isSolid())
		{
			facing = Direction.SOUTH;
		}
		else if (facing == Direction.SOUTH && south.isSolid() && !north.isSolid())
		{
			facing = Direction.NORTH;
		}
		else if (facing == Direction.WEST && west.isSolid() && !east.isSolid())
		{
			facing = Direction.EAST;
		}
		else if (facing == Direction.EAST && east.isSolid() && !west.isSolid())
		{
			facing = Direction.WEST;
		}

		worldIn.setBlockState(pos, state.with(INPUT_FACING, facing), 2);
	}
}
