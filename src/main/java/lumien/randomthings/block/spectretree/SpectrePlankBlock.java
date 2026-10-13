package lumien.randomthings.block.spectretree;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

public class SpectrePlankBlock extends Block
{
	public SpectrePlankBlock()
	{
		super(Block.Properties.create(Material.WOOD, MaterialColor.PURPLE).notSolid().hardnessAndResistance(2.0F, 5.0F).sound(SoundType.WOOD));
	}
}
