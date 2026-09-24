package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RedirectorPlateBlock extends PlateBlock
{
	public static final DirectionProperty INPUT_FACING = DirectionProperty.create("inputfacing", Direction.Plane.HORIZONTAL);
	public static final DirectionProperty OUTPUT_FACING = DirectionProperty.create("outputfacing", Direction.Plane.HORIZONTAL);

	public RedirectorPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(INPUT_FACING, OUTPUT_FACING);
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		redirect(state, pos, entityIn, state.get(INPUT_FACING), state.get(OUTPUT_FACING));
	}

	protected static void redirect(BlockState state, BlockPos pos, Entity entityIn, Direction currentInput, Direction currentOutput)
	{
		Vec3d motionVec = entityIn.getMotion();

		Direction roughMovingFacing = Direction.getFacingFromVector((float) motionVec.x, (float) motionVec.y, (float) motionVec.z).getOpposite();

		Vec3d center = new Vec3d(pos).add(0.5, 0, 0.5);
		Vec3d difVec = center.subtract(entityIn.getPositionVec());

		Direction facing = Direction.getFacingFromVector((float) difVec.x, (float) difVec.y, (float) difVec.z).getOpposite();

		Direction outputFacing = null;
		if (facing == currentInput && roughMovingFacing == currentInput)
		{
			outputFacing = currentOutput;
		}
		else if (facing == currentOutput && roughMovingFacing == currentOutput)
		{
			outputFacing = currentInput;
		}

		if (outputFacing != null)
		{
			Vec3d facingVec = new Vec3d(outputFacing.getDirectionVec()).scale(0.4).add(center);

			float dif = facing.getOpposite().getHorizontalAngle() - outputFacing.getHorizontalAngle();

			Vec3d outputMotionVec = motionVec.rotateYaw((float) Math.toRadians(dif));
			entityIn.setPosition(facingVec.x, facingVec.y, facingVec.z);

			entityIn.setMotion(outputMotionVec);
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (hit.getFace() == Direction.UP)
		{
			Direction currentInput = state.get(INPUT_FACING);
			Direction currentOutput = state.get(OUTPUT_FACING);

			float hitX = (float) (hit.getHitVec().x - pos.getX());
			float hitZ = (float) (hit.getHitVec().z - pos.getZ());

			Direction newOutput = Direction.getFacingFromVector(hitX - 0.5F, 0, hitZ - 0.5F);

			if (currentInput != newOutput && currentOutput != newOutput)
			{
				if (!worldIn.isRemote)
				{
					worldIn.setBlockState(pos, state.with(OUTPUT_FACING, newOutput));
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		this.setDefaultFacing(worldIn, pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		Direction facing = context.getPlacementHorizontalFacing();
		return this.getDefaultState().with(INPUT_FACING, facing.getOpposite()).with(OUTPUT_FACING, facing);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (placer != null)
		{
			Direction facing = placer.getHorizontalFacing();
			worldIn.setBlockState(pos, state.with(INPUT_FACING, facing.getOpposite()).with(OUTPUT_FACING, facing), 2);
		}
	}

	private void setDefaultFacing(World worldIn, BlockPos pos, BlockState state)
	{
		if (!worldIn.isRemote)
		{
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

			worldIn.setBlockState(pos, state.with(INPUT_FACING, facing).with(OUTPUT_FACING, facing.getOpposite()), 2);
		}
	}
}
