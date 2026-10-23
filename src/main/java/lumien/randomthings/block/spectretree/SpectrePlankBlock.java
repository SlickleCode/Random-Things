package lumien.randomthings.block.spectretree;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class SpectrePlankBlock extends Block
{
	public SpectrePlankBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.COLOR_PURPLE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava().noOcclusion().strength(2.0F, 5.0F).sound(SoundType.WOOD));
	}
}
