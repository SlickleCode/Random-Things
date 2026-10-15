package lumien.randomthings.block.spectretree;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class SpectrePlankBlock extends Block
{
	public SpectrePlankBlock()
	{
		super(Block.Properties.of(Material.WOOD, MaterialColor.COLOR_PURPLE).noOcclusion().strength(2.0F, 5.0F).sound(SoundType.WOOD));
	}
}
