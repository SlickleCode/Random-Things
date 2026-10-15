package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class LuminousBlock extends Block
{
	public LuminousBlock()
	{
		super(Block.Properties.of(Material.DIRT, MaterialColor.WOOL).strength(0.3F).lightLevel(state -> 15).sound(SoundType.GLASS));
	}
}
