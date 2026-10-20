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

public class LapisGlassBlock extends Block
{
	public LapisGlassBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).noOcclusion().strength(0.3F).sound(SoundType.GLASS).isSuffocating((state, world, pos) -> false));
	}

	/**
	 * Solid for players, passable for everything else.
	 * <p>
	 * The obvious "empty unless proven otherwise" form of this check (return
	 * empty only when the context's entity is known and isn't a player) is
	 * wrong: `javap -c` on {@code Entity.pushOutOfBlocks} and
	 * {@code Block.causesSuffocation} showed both call a context-free
	 * internal check ({@code BlockState.func_224756_o}) that only ever
	 * queries this method with a null-entity "dummy" context - never the
	 * real player. If the dummy-context branch resolves to solid (as it did
	 * in an earlier version of this method), that separate anti-embedding
	 * safety net treats the block as permanently occupied for every entity,
	 * including causing needless suffocation damage. Making "unproven"
	 * (dummy/null) default to *empty* instead, and only returning solid once
	 * the entity is confirmed to be a player, fixes both: real player
	 * movement still correctly collides (movement resolution always supplies
	 * a real entity), while suffocation/push-out see this as non-obstructing.
	 */
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc ? ecc.getEntity() : null;

		if (entity instanceof Player)
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
