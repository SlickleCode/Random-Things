package lumien.randomthings.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
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
 * self-destroys-into-a-pod-when-blocked edge case only - the {@code isLadder}
 * override below was previously (incorrectly) dropped too, on the mistaken
 * assumption that the push-while-already-moving-upward behavior in
 * {@link #onEntityCollision} gave an equivalent "climbable" feel. It doesn't:
 * that push only ever fires once you're already moving upward (e.g. off a
 * jump), it can't get you climbing from a standstill the way a real ladder
 * does. Restored to match the original.
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

			// New feature, not a 1.12.2 port: a small sound cue every time the
			// stalk grows a segment taller, per explicit user request.
			worldIn.playSound(null, up, this.getSoundType(state).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		// 1.12.2 checked "instanceof BlockGrass || instanceof BlockDirt" - in that
		// version BlockDirt was one class backing dirt/coarse dirt/podzol as
		// variants of a single block. 1.14.4 split those into separate Block
		// instances, so matching the same soil range needs all three explicitly.
		// Farmland is a deliberate addition beyond the original (which never
		// supported it either), per explicit user request - it's a natural
		// planting surface players expect this to work on.
		Block below = worldIn.getBlockState(pos.down()).getBlock();
		return below == this || below == Blocks.GRASS_BLOCK || below == Blocks.DIRT || below == Blocks.COARSE_DIRT || below == Blocks.PODZOL || below == Blocks.FARMLAND;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isLadder(BlockState state, IWorldReader world, BlockPos pos, LivingEntity entity)
	{
		return true;
	}
}
