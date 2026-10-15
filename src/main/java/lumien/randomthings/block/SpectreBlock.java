package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

/**
 * Plain decorative "Spectre" block: unbreakable and explosion-immune, like
 * vanilla Bedrock, achieved purely through block properties (no custom
 * explosion-resistance override needed).
 */
public class SpectreBlock extends Block
{
	public SpectreBlock()
	{
		super(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).noOcclusion().strength(-1.0F, 3600000.0F).sound(SoundType.GLASS).noDrops());
	}
}
