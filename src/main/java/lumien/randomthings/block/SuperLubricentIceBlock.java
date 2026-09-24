package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.BlockRenderLayer;

/**
 * Zero friction (see {@link SuperLubricentPlatformBlock}'s class javadoc for
 * the friction-formula derivation), via the {@code slipperiness} property
 * below - matching {@link SuperLubricentPlatformBlock} and
 * {@link SuperLubricentStoneBlock} exactly. The speed cap is new behavior
 * beyond 1.12.2, enforced by a {@code LivingUpdateEvent} listener in
 * {@code RandomThings} rather than here - see {@link SuperLubricentPhysics}'s
 * javadoc for why. Boots-negation doesn't live here at all -
 * {@code SuperLubricentBootsMixin} intercepts friction globally instead.
 */
public class SuperLubricentIceBlock extends Block
{
	public SuperLubricentIceBlock()
	{
		super(Block.Properties.create(Material.ICE, MaterialColor.ICE).hardnessAndResistance(0.5F).slipperiness(1F / 0.91F));
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
