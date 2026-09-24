package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * A re-skinned crafting table. Extends vanilla {@link CraftingTableBlock}
 * directly, inheriting its GUI/container behavior unchanged.
 * <p>
 * Simplification, disclosed here: 1.12.2's original let the frame retexture
 * itself to match any placed wood type at runtime, via a custom
 * {@code IUnlistedProperty}-backed baked model keyed off the TileEntity's
 * stored wood block/meta. Reproducing that needs either a custom
 * {@code IBakedModel} or a full per-wood-type multi-element model set -
 * infrastructure this mod hasn't needed anywhere else yet - so this port
 * ships a single fixed skin (this mod's own "tools on top" art on the
 * sides/top, vanilla oak planks on the underside) rather than the dynamic
 * per-wood recolor. The result is functionally identical (right-click opens
 * the standard crafting grid); only the wood-matching cosmetic is dropped.
 */
public class CustomWorkbenchBlock extends CraftingTableBlock
{
	public CustomWorkbenchBlock()
	{
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2.5F).sound(SoundType.WOOD));
	}
}
