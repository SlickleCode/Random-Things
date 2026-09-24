package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class LapisLampBlock extends Block
{
	public LapisLampBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.BLUE).hardnessAndResistance(0.3F).lightValue(15).sound(SoundType.GLASS));
	}
}
