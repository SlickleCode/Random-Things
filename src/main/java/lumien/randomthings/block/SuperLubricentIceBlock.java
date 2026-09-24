package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.BlockRenderLayer;

public class SuperLubricentIceBlock extends Block
{
	public SuperLubricentIceBlock()
	{
		super(Block.Properties.create(Material.ICE, MaterialColor.ICE).hardnessAndResistance(0.5F).slipperiness(1F / 0.98F));
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
