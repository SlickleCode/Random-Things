package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class LuminousBlock extends Block
{
	public LuminousBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOL).strength(0.3F).lightLevel(state -> 15).sound(SoundType.GLASS));
	}
}
