package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class QuartzGlassBlock extends Block
{
	public QuartzGlassBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.QUARTZ).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).noOcclusion().strength(0.3F).sound(SoundType.GLASS).isSuffocating((state, world, pos) -> false));
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
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc ? ecc.getEntity() : null;

		if (entity != null && !(entity instanceof Player))
		{
			return super.getCollisionShape(state, worldIn, pos, context);
		}

		return Shapes.empty();
	}

	/**
	 * Explicit belt-and-braces version of what the dummy-context branch of
	 * {@link #getCollisionShape} above already implies - stated directly so
	 * it doesn't depend on the reader tracing through that chain.
	 */}
