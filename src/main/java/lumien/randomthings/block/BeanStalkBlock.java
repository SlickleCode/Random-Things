package lumien.randomthings.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Grows upward one segment per scheduled tick (faster for the "magic" bean
 * variant) until it hits the build height limit or something it can't grow
 * through, then - for the magic variant only - caps itself off with a
 * {@link PodBlock}. Simplified from the 1.12.2 version: dropped the
 * self-destroys-into-a-pod-when-blocked edge case and the custom
 * {@code isLadder} override (the push-while-climbing behavior below already
 * gives the same "climbable" feel).
 */
public class BeanStalkBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(6.4, 0, 6.4, 9.6, 16, 9.6);

	private final boolean strongMagic;

	public BeanStalkBlock(boolean strongMagic)
	{
		super(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().sound(SoundType.PLANT));

		this.strongMagic = strongMagic;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		if (entityIn.onGround || entityIn.collidedVertically)
		{
			return;
		}

		double speed = strongMagic ? 0.5 : 0.2;

		if (entityIn.getMotion().y >= 0.1)
		{
			entityIn.setPosition(entityIn.posX, entityIn.posY + speed, entityIn.posZ);
		}
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (worldIn.isRemote)
		{
			return;
		}

		BlockPos up = pos.up();

		if (up.getY() >= 255)
		{
			return;
		}

		if (strongMagic && up.getY() >= 253)
		{
			worldIn.setBlockState(up, ModBlocks.BEAN_POD.getDefaultState(), 3);
			return;
		}

		BlockState upState = worldIn.getBlockState(up);

		if (upState.isAir() || upState.getMaterial().isReplaceable())
		{
			worldIn.setBlockState(up, this.getDefaultState(), 3);
			worldIn.getPendingBlockTicks().scheduleTick(up, this, strongMagic ? 1 : 5);
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		Block below = worldIn.getBlockState(pos.down()).getBlock();
		return below == this || below == Blocks.GRASS_BLOCK || below == Blocks.DIRT;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
}
