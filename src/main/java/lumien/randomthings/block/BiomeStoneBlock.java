package lumien.randomthings.block;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.BiomeColors;

/**
 * Registered five times (cobble/smooth/brick/cracked/chiseled), each reusing
 * a vanilla stone-family texture with a biome tint applied - matching how the
 * 1.12.2 resources reused vanilla textures (e.g. "blocks/cobblestone") rather
 * than shipping dedicated art for this block.
 */
public class BiomeStoneBlock extends Block implements IRTBlockColor
{
	public BiomeStoneBlock()
	{
		super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F, 10.0F).sound(SoundType.STONE));
	}

	@Override
	public int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex)
	{
		if (worldIn == null || pos == null)
		{
			return 0xFFFFFF;
		}

		return BiomeColors.getGrassColor(worldIn, pos);
	}
}
