package lumien.randomthings.block.plates;

import lumien.randomthings.block.ModBlocks;
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
import net.minecraft.world.World;

/**
 * Mirrors the two-block "unpowered/powered" pair the 1.12.2 version used
 * (rather than a single block with a boolean property), so the two
 * registered blocks swap places when redstone power changes.
 */
public class RedstonePlateBlock extends PlateBlock
{
	public static final DirectionProperty INPUT_FACING = DirectionProperty.create("inputfacing", Direction.Plane.HORIZONTAL);
	public static final DirectionProperty OUTPUT_FACING = DirectionProperty.create("outputfacing", Direction.Plane.HORIZONTAL);

	private final boolean powered;

	public RedstonePlateBlock(boolean powered)
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));

		this.powered = powered;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(INPUT_FACING, OUTPUT_FACING);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);

		boolean shouldBePowered = worldIn.isBlockPowered(pos);

		if (this.powered != shouldBePowered)
		{
			Block swapTo = shouldBePowered ? ModBlocks.REDSTONE_PLATE_POWERED : ModBlocks.REDSTONE_PLATE;

			worldIn.setBlockState(pos, swapTo.getDefaultState().with(INPUT_FACING, state.get(INPUT_FACING)).with(OUTPUT_FACING, state.get(OUTPUT_FACING)));
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		Direction currentInput = state.get(INPUT_FACING);
		Direction currentOutput = powered ? currentInput.getOpposite() : state.get(OUTPUT_FACING);

		RedirectorPlateBlock.redirect(state, pos, entityIn, currentInput, currentOutput);
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
}
