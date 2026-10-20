package lumien.randomthings.block.spectretree;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;

/**
 * Vanilla's RotatedPillarBlock already handles the axis property and
 * rotation logic the 1.12.2 version hand-rolled, so no custom overrides are
 * needed here.
 */
public class SpectreLogBlock extends RotatedPillarBlock
{
	public SpectreLogBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava().noOcclusion().strength(2.0F).sound(SoundType.WOOD));
	}
}
