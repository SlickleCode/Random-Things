package lumien.randomthings.util;

import java.awt.Color;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

/**
 * A biome's "representative" color, for tinting {@link
 * lumien.randomthings.block.BiomeRadarBlock}'s window and
 * {@link lumien.randomthings.item.BiomeCrystalItem}'s icon. Direct port of
 * 1.12.2's {@code RenderUtils.getBiomeColor}/{@code blend}.
 * <p>
 * Deliberately common (not {@code client.util.RenderUtils}, where the rest of
 * this port's rendering helpers live) even though every caller only ever
 * runs client-side: {@code BiomeRadarBlock}, {@code BiomeCrystalItem}, and
 * {@code BiomeRadarTileEntity} are all loaded on a dedicated server too, and
 * referencing a class that statically imports LWJGL/{@code GlStateManager}
 * from a class loaded on both sides risks a server classloading crash even
 * if the call itself is guarded by an {@code isRemote} check - the JVM still
 * has to resolve {@code RenderUtils} to verify the calling class's bytecode.
 * This class only touches {@link Biome}/{@link BiomeDictionary}/{@link
 * Color}, all common-safe.
 */
public class BiomeColorUtil
{
	private static final Cache<Biome, Integer> biomeColorCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	/**
	 * With a non-null {@code worldIn}, averages this over the 3x3 area around
	 * {@code pos} (smoothed biome-border color, matching how biome-tinted
	 * blocks like grass already blend); with {@code null}, colors the given
	 * {@code biome} directly regardless of position (used for a stored/target
	 * biome that isn't actually under the caller, e.g. a Biome Crystal).
	 */
	public static int getBiomeColor(IEnviromentBlockReader worldIn, final Biome biome, final BlockPos pos)
	{
		if (worldIn != null)
		{
			int i = 0;
			int j = 0;
			int k = 0;

			for (BlockPos p : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1)))
			{
				Biome biomeA = worldIn.getBiome(p);
				int l = singleBiomeColor(biomeA, p);

				i += (l & 16711680) >> 16;
				j += (l & 65280) >> 8;
				k += l & 255;
			}

			return (i / 9 & 255) << 16 | (j / 9 & 255) << 8 | k / 9 & 255;
		}
		else
		{
			return singleBiomeColor(biome, pos);
		}
	}

	private static int singleBiomeColor(final Biome biome, final BlockPos pos)
	{
		try
		{
			return biomeColorCache.get(biome, new Callable<Integer>()
			{
				@Override
				public Integer call() throws Exception
				{
					Set<Type> types = BiomeDictionary.getTypes(biome);

					Color foliageColor = new Color(biome.getFoliageColor(pos));
					Color waterColorMultiplier = new Color(biome.getWaterColor());
					Color grassColor = new Color(biome.getGrassColor(pos));

					Color colorResult = blend(blend(foliageColor, waterColorMultiplier, 0.5f), grassColor, 0.5f);

					for (Type t : types)
					{
						switch (t.getName())
						{
							case "BEACH":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.35f)), Math.min(255, (int) (colorResult.getGreen() * 1.3f)), Math.min(255, (int) (colorResult.getBlue() * 1.1f)));
								break;
							case "COLD":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.8f)), Math.min(255, (colorResult.getGreen())), Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
								break;
							case "CONIFEROUS":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), Math.min(255, (int) (colorResult.getGreen() * 1.1f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "DEAD":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.8f)), Math.min(255, (int) (colorResult.getGreen() * 0.8f)), Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
								break;
							case "DENSE":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1f)), Math.min(255, (int) (colorResult.getGreen() * 1.5f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "DRY":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), Math.min(255, (int) (colorResult.getGreen() * 1.1f)), Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
								break;
							case "END":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), Math.min(255, (int) (colorResult.getGreen() * 0.1f)), Math.min(255, (int) (colorResult.getBlue() * 0.4f)));
								break;
							case "FOREST":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.8f)), Math.min(255, (int) (colorResult.getGreen() * 0.9f)), Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
								break;
							case "HILLS":
								colorResult = new Color(Math.min(255, colorResult.getRed() + 40), Math.min(255, colorResult.getGreen() + 40), Math.min(255, colorResult.getBlue() + 40));
								break;
							case "HOT":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), Math.min(255, (int) (colorResult.getGreen() * 1f)), Math.min(255, (int) (colorResult.getBlue() * 0.8f)));
								break;
							case "JUNGLE":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.1f)), Math.min(255, (int) (colorResult.getGreen() * 1.5f)), Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
								break;
							case "LUSH":
								colorResult = new Color(Math.min(255, (colorResult.getRed())), Math.min(255, (int) (colorResult.getGreen() * 1.4f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "MAGICAL":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.5f)), Math.min(255, (int) (colorResult.getGreen() * 1.3f)), Math.min(255, (int) (colorResult.getBlue() * 1.5f)));
								break;
							case "MESA":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.9f)), Math.min(255, (int) (colorResult.getGreen() * 0.8f)), Math.min(255, (int) (colorResult.getBlue() * 0.5f)));
								break;
							case "MOUNTAIN":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), Math.min(255, (int) (colorResult.getGreen() * 1.2f)), Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
								break;
							case "MUSHROOM":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.3f)), Math.min(255, (int) (colorResult.getGreen() * 0.5f)), Math.min(255, (int) (colorResult.getBlue() * 1.3f)));
								break;
							case "NETHER":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.8f)), Math.min(255, (int) (colorResult.getGreen() * 0.5f)), Math.min(255, (int) (colorResult.getBlue() * 0.3f)));
								break;
							case "OCEAN":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), Math.min(255, (int) (colorResult.getGreen() * 0.4f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "PLAINS":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.9f)), Math.min(255, (int) (colorResult.getGreen() * 0.9f)), Math.min(255, (int) (colorResult.getBlue() * 0.9f)));
								break;
							case "RIVER":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.6f)), Math.min(255, (int) (colorResult.getGreen() * 0.6f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "SANDY":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.8)), Math.min(255, (int) (colorResult.getGreen() * 0.8)), Math.min(255, (int) (colorResult.getBlue() * 0.7f)));
								break;
							case "SAVANNA":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), Math.min(255, (int) (colorResult.getGreen() * 1.1f)), Math.min(255, (int) (colorResult.getBlue() * 0.9f)));
								break;
							case "SNOWY":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.4f)), Math.min(255, (int) (colorResult.getGreen() * 1.4f)), Math.min(255, (int) (colorResult.getBlue() * 1.5f)));
								break;
							case "SPARSE":
								colorResult = new Color(Math.min(255, (colorResult.getRed())), Math.min(255, (int) (colorResult.getGreen() * 0.8f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "SPOOKY":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.7f)), Math.min(255, (int) (colorResult.getGreen() * 0.7f)), Math.min(255, (int) (colorResult.getBlue() * 0.7f)));
								break;
							case "SWAMP":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.4f)), Math.min(255, (int) (colorResult.getGreen() * 0.6f)), Math.min(255, (int) (colorResult.getBlue() * 0.4f)));
								break;
							case "WASTELAND":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 1.2f)), Math.min(255, (int) (colorResult.getGreen() * 1.2f)), Math.min(255, (int) (colorResult.getBlue() * 1.2f)));
								break;
							case "WATER":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.5f)), Math.min(255, (int) (colorResult.getGreen() * 0.5f)), Math.min(255, (colorResult.getBlue())));
								break;
							case "WET":
								colorResult = new Color(Math.min(255, (int) (colorResult.getRed() * 0.6f)), Math.min(255, (colorResult.getGreen())), Math.min(255, (colorResult.getBlue())));
								break;
							default:
								break;
						}
					}

					return colorResult.getRGB();
				}
			});
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();

			return Color.WHITE.getRGB();
		}
	}

	public static Color blend(Color c1, Color c2, float ratio)
	{
		if (ratio > 1f)
			ratio = 1f;
		else if (ratio < 0f)
			ratio = 0f;
		float iRatio = 1.0f - ratio;

		int i1 = c1.getRGB();
		int i2 = c2.getRGB();

		int a1 = (i1 >> 24 & 0xff);
		int r1 = ((i1 & 0xff0000) >> 16);
		int g1 = ((i1 & 0xff00) >> 8);
		int b1 = (i1 & 0xff);

		int a2 = (i2 >> 24 & 0xff);
		int r2 = ((i2 & 0xff0000) >> 16);
		int g2 = ((i2 & 0xff00) >> 8);
		int b2 = (i2 & 0xff);

		int a = (int) ((a1 * iRatio) + (a2 * ratio));
		int r = (int) ((r1 * iRatio) + (r2 * ratio));
		int g = (int) ((g1 * iRatio) + (g2 * ratio));
		int b = (int) ((b1 * iRatio) + (b2 * ratio));

		return new Color(a << 24 | r << 16 | g << 8 | b);
	}
}
