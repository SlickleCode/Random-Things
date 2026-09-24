package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

/**
 * Zero friction (see {@link SuperLubricentPlatformBlock}'s class javadoc for
 * the friction-formula derivation), via the {@code slipperiness} property
 * below - matching {@link SuperLubricentPlatformBlock} and
 * {@link SuperLubricentIceBlock} exactly. The speed cap is new behavior
 * beyond 1.12.2, enforced by a {@code LivingUpdateEvent} listener in
 * {@code RandomThings} rather than here - see {@link SuperLubricentPhysics}'s
 * javadoc for why. Boots-negation doesn't live here at all -
 * {@code SuperLubricentBootsMixin} intercepts friction globally instead.
 */
public class SuperLubricentStoneBlock extends Block
{
	public SuperLubricentStoneBlock()
	{
		super(Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F, 6.0F).slipperiness(1F / 0.91F));
	}
}
