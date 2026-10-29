package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

/**
 * Grows upward one segment per scheduled tick (faster for the "magic" bean
 * variant) until it hits the build height limit or something it can't grow
 * through, then - for the magic variant only - caps itself off with a
 * {@link PodBlock}. The magic variant also breaks any breakable block in its
 * way (no drops) and turns into a pod when blocked by an unbreakable one, as
 * in 1.12.2. Simplified from the 1.12.2 version: the {@code isLadder}
 * override below was previously (incorrectly) dropped too, on the mistaken
 * assumption that the push-while-already-moving-upward behavior in
 * {@link #onEntityCollision} gave an equivalent "climbable" feel. It doesn't:
 * that push only ever fires once you're already moving upward (e.g. off a
 * jump), it can't get you climbing from a standstill the way a real ladder
 * does. Restored to match the original.
 */
public class BeanStalkBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.box(6.4, 0, 6.4, 9.6, 16, 9.6);

	private final boolean strongMagic;

	public BeanStalkBlock(boolean strongMagic)
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion().noCollission().sound(SoundType.GRASS));

		this.strongMagic = strongMagic;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn)
	{
		if (entityIn.onGround() || entityIn.verticalCollision)
		{
			return;
		}

		double speed = strongMagic ? 0.5 : 0.2;

		if (entityIn.getDeltaMovement().y >= 0.1)
		{
			entityIn.setPos(entityIn.getX(), entityIn.getY() + speed, entityIn.getZ());
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		if (worldIn.isClientSide)
		{
			return;
		}

		BlockPos up = pos.above();

		if (up.getY() >= worldIn.getMaxBuildHeight() - 1)
		{
			return;
		}

		if (strongMagic && up.getY() >= worldIn.getMaxBuildHeight() - 3)
		{
			worldIn.setBlock(up, ModBlocks.BEAN_POD.defaultBlockState(), 3);
			return;
		}

		BlockState upState = worldIn.getBlockState(up);

		if (strongMagic && !upState.isAir() && !upState.canBeReplaced())
		{
			// 1.12.2: the magic stalk breaks any breakable block in its way (no drops),
			// and turns the current segment into a pod when the block is unbreakable.
			if (upState.getDestroySpeed(worldIn, up) != -1)
			{
				worldIn.levelEvent(2001, up, Block.getId(upState));
				worldIn.setBlock(up, this.defaultBlockState(), 3);
				worldIn.scheduleTick(up, this, 1);
				worldIn.playSound(null, up, this.getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			else
			{
				worldIn.setBlock(pos, ModBlocks.BEAN_POD.defaultBlockState(), 3);
			}
			return;
		}

		if (upState.isAir() || upState.canBeReplaced())
		{
			worldIn.setBlock(up, this.defaultBlockState(), 3);
			worldIn.scheduleTick(up, this, strongMagic ? 1 : 5);

			// New feature, not a 1.12.2 port: a small sound cue every time the
			// stalk grows a segment taller, per explicit user request.
			worldIn.playSound(null, up, this.getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos)
	{
		// 1.12.2 checked "instanceof BlockGrass || instanceof BlockDirt" - in that
		// version BlockDirt was one class backing dirt/coarse dirt/podzol as
		// variants of a single block. 1.14.4 split those into separate Block
		// instances, so matching the same soil range needs all three explicitly.
		// Farmland is a deliberate addition beyond the original (which never
		// supported it either), per explicit user request - it's a natural
		// planting surface players expect this to work on.
		Block below = worldIn.getBlockState(pos.below()).getBlock();
		return below == this || below == Blocks.GRASS_BLOCK || below == Blocks.DIRT || below == Blocks.COARSE_DIRT || below == Blocks.PODZOL || below == Blocks.FARMLAND;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity)
	{
		return true;
	}
}
