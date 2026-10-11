package lumien.randomthings.block.spectretree;

import net.minecraft.block.Block;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

/**
 * Vanilla's RotatedPillarBlock already handles the axis property and
 * rotation logic the 1.12.2 version hand-rolled, so no custom overrides are
 * needed here.
 */
public class SpectreLogBlock extends RotatedPillarBlock
{
	public SpectreLogBlock()
	{
		super(Block.Properties.create(Material.WOOD, MaterialColor.PURPLE).hardnessAndResistance(2.0F).sound(SoundType.WOOD));
	}
}
