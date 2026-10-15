package lumien.randomthings.block.spectretree;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

/**
 * Vanilla's RotatedPillarBlock already handles the axis property and
 * rotation logic the 1.12.2 version hand-rolled, so no custom overrides are
 * needed here.
 */
public class SpectreLogBlock extends RotatedPillarBlock
{
	public SpectreLogBlock()
	{
		super(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_PURPLE).noOcclusion().strength(2.0F).sound(SoundType.WOOD));
	}
}
