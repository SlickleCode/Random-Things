package lumien.randomthings.block.spectretree;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.BlockRenderLayer;

/**
 * Vanilla's LeavesBlock already implements the distance-based leaf decay
 * algorithm the 1.12.2 version hand-rolled as a bespoke BFS flood fill, so
 * extending it directly replaces that logic entirely. The old "occasionally
 * drop an Ectoplasm ingredient" behavior is dropped for now since the
 * Ectoplasm item hasn't been ported yet (tracked for the items batch).
 */
public class SpectreLeafBlock extends LeavesBlock
{
	public SpectreLeafBlock()
	{
		super(Block.Properties.create(Material.LEAVES, MaterialColor.PURPLE).hardnessAndResistance(0.2F).tickRandomly().sound(SoundType.PLANT));
	}

	/**
	 * A previous pass assumed inheriting LeavesBlock's own getRenderLayer was
	 * "better than a 1:1 match," reasoning it dynamically follows the
	 * Fast/Fancy graphics setting - true (confirmed via `javap -c`: it reads a
	 * shared static field vanilla toggles for ALL leaves blocks based on that
	 * setting), but wrong: the original 1.12.2 BlockSpectreLeaf never used
	 * that shared toggle at all and hardcoded TRANSLUCENT unconditionally.
	 * Without this override, Spectre Leaves render solid/opaque under "Fast"
	 * graphics like any other leaves would, which is what was reported as a
	 * regression - restoring the original's fixed value.
	 */
	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
