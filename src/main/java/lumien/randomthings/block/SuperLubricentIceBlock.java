package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/**
 * Zero friction (see {@link SuperLubricentPlatformBlock}'s class javadoc for
 * the friction-formula derivation), via the {@code slipperiness} property
 * below - matching {@link SuperLubricentPlatformBlock} and
 * {@link SuperLubricentStoneBlock} exactly. A boat gets a different, exact
 * value instead of this one (see the {@code getSlipperiness} override below
 * and {@link SuperLubricentPhysics}'s javadoc for why) - external cap on top
 * either way, not a port of 1.12.2. Boots-negation doesn't live here at all -
 * {@code SuperLubricentBootsTransformer.js} (a coremod, see
 * {@code lumien.randomthings.asm.AsmHandler#bootsMaxSlip}) intercepts
 * friction globally instead.
 */
public class SuperLubricentIceBlock extends Block
{
	public SuperLubricentIceBlock()
	{
		super(Block.Properties.of(Material.ICE, MaterialColor.ICE).noOcclusion().strength(0.5F).friction(1F / 0.91F));
	}

	/**
	 * See {@link SuperLubricentPhysics}'s class javadoc for why a boat needs
	 * exact {@code 1.0F} here instead of this block's normal zero-friction
	 * value.
	 */
	@Override
	public float getFriction(BlockState state, LevelReader world, BlockPos pos, Entity entity)
	{
		return SuperLubricentPhysics.slipperinessFor(entity, super.getFriction(state, world, pos, entity));
	}
}
