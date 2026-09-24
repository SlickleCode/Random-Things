package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.BlockRenderLayer;

public class StainedBrickBlock extends Block
{
	private final boolean luminous;

	public StainedBrickBlock(boolean luminous)
	{
		super(withLight(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(2.0F, 10.0F).sound(SoundType.STONE), luminous));

		this.luminous = luminous;
	}

	private static Block.Properties withLight(Block.Properties properties, boolean luminous)
	{
		return luminous ? properties.lightValue(15) : properties;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return luminous ? BlockRenderLayer.CUTOUT_MIPPED : super.getRenderLayer();
	}
}
