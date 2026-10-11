package lumien.randomthings.client;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Sky and fog look of the Spectre dimension (the dimension type's {@code effects} id): no sun and moon, a fixed dark
 * teal fog colour and no fog distance, clouds below the pocket rooms. Port of the rendering overrides of 1.14.4's
 * {@code SpectreDimension} ({@code getFogColor}, {@code doesXZShowFog}, {@code getCloudHeight}, no sunrise colours).
 */
public class SpectreRenderInfo extends DimensionRenderInfo
{
	public SpectreRenderInfo()
	{
		super(-5.0F, false, DimensionRenderInfo.FogType.NONE, true, false);
	}

	@Override
	public Vector3d func_230494_a_(Vector3d fogColor, float sunHeight)
	{
		return new Vector3d(0.03D, 0.2D, 0.2D);
	}

	@Override
	public boolean func_230493_a_(int x, int z)
	{
		return false;
	}
}
