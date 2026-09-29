package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * Zero friction (see {@link SuperLubricentPlatformBlock}'s class javadoc for
 * the friction-formula derivation), via the {@code slipperiness} property
 * below - matching {@link SuperLubricentPlatformBlock} and
 * {@link SuperLubricentIceBlock} exactly. A boat gets a different, exact
 * value instead of this one (see the {@code getSlipperiness} override below
 * and {@link SuperLubricentPhysics}'s javadoc for why) - external cap on top
 * either way, not a port of 1.12.2. Boots-negation doesn't live here at all -
 * {@code SuperLubricentBootsTransformer.js} (a coremod, see
 * {@code lumien.randomthings.asm.AsmHandler#bootsMaxSlip}) intercepts
 * friction globally instead.
 */
public class SuperLubricentStoneBlock extends Block
{
	public SuperLubricentStoneBlock()
	{
		super(Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F, 6.0F).slipperiness(1F / 0.91F));
	}

	/**
	 * See {@link SuperLubricentPhysics}'s class javadoc for why a boat needs
	 * exact {@code 1.0F} here instead of this block's normal zero-friction
	 * value.
	 */
	@Override
	public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, Entity entity)
	{
		return SuperLubricentPhysics.slipperinessFor(entity, super.getSlipperiness(state, world, pos, entity));
	}
}
