package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.BlockRenderLayer;

/**
 * Plain decorative "Spectre" block: unbreakable and explosion-immune, like
 * vanilla Bedrock, achieved purely through block properties (no custom
 * explosion-resistance override needed).
 */
public class SpectreBlock extends Block
{
	public SpectreBlock()
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.BLACK).hardnessAndResistance(-1.0F, 3600000.0F).sound(SoundType.GLASS).noDrops());
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
}
