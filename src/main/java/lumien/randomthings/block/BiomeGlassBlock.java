package lumien.randomthings.block;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.BiomeColors;

/**
 * The 1.12.2 version tinted this via a bespoke BiomeDictionary-category color
 * heuristic (lumien.randomthings.util.client.RenderUtils.getBiomeColor).
 * Simplified here to vanilla's own grass-color biome lookup
 * (BiomeColors.getGrassColor) - still a real per-biome color, just using the
 * standard vanilla resolver instead of reimplementing the custom heuristic.
 */
public class BiomeGlassBlock extends Block implements IRTBlockColor
{
	public BiomeGlassBlock()
	{
		super(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.3F).sound(SoundType.GLASS));
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
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
