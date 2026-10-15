package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class LuminousTranslucentBlock extends Block
{
	public LuminousTranslucentBlock()
	{
		super(Block.Properties.of(Material.DIRT, MaterialColor.WOOL).noOcclusion().strength(0.3F).lightLevel(state -> 15).sound(SoundType.GLASS));
	}
}
