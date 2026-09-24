package lumien.randomthings.block.spectretree;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

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
}
