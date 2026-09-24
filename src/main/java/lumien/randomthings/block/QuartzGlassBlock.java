package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.util.BlockRenderLayer;

public class QuartzGlassBlock extends Block
{
	public QuartzGlassBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.QUARTZ).hardnessAndResistance(0.3F).sound(SoundType.GLASS));
	}

	/**
	 * Passable for players, solid for everything else - the mirror image of
	 * {@link LapisGlassBlock}. See that class's javadoc on this same method
	 * for why the "empty" branch has to be the default (covering the
	 * dummy/null-entity context too), not just the fallback for a
	 * *confirmed* player: {@code Entity.pushOutOfBlocks}'s continuous
	 * anti-embedding check bypasses this method's entity-aware context
	 * entirely and only ever asks the dummy-context question, so leaving
	 * that case solid meant players got shoved back out the instant they
	 * stepped in, even though the real per-tick movement collision (which
	 * does receive the real player entity) already let them through.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Entity entity = context.getEntity();

		if (entity != null && !(entity instanceof PlayerEntity))
		{
			return super.getCollisionShape(state, worldIn, pos, context);
		}

		return VoxelShapes.empty();
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	/**
	 * Explicit belt-and-braces version of what the dummy-context branch of
	 * {@link #getCollisionShape} above already implies - stated directly so
	 * it doesn't depend on the reader tracing through that chain.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return false;
	}
}
