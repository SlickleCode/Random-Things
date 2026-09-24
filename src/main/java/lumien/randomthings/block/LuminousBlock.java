package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class LuminousBlock extends Block
{
	public LuminousBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.WOOL).hardnessAndResistance(0.3F).lightValue(15).sound(SoundType.GLASS));
	}
}
