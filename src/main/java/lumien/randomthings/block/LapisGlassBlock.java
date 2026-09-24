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

public class LapisGlassBlock extends Block
{
	public LapisGlassBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.BLUE).hardnessAndResistance(0.3F).sound(SoundType.GLASS));
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
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Entity entity = context.getEntity();

		if (entity instanceof PlayerEntity)
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
