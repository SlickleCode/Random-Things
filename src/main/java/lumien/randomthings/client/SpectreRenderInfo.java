package lumien.randomthings.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Sky and fog look of the Spectre dimension (the dimension type's {@code effects} id): no sun and moon, a fixed dark
 * teal fog colour and no fog distance, clouds below the pocket rooms. Port of the rendering overrides of 1.14.4's
 * {@code SpectreDimension} ({@code getFogColor}, {@code doesXZShowFog}, {@code getCloudHeight}, no sunrise colours).
 */
public class SpectreRenderInfo extends DimensionSpecialEffects
{
	public SpectreRenderInfo()
	{
		super(-5.0F, false, DimensionSpecialEffects.SkyType.NONE, true, false);
	}

	@Override
	public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float sunHeight)
	{
		return new Vec3(0.03D, 0.2D, 0.2D);
	}

	@Override
	public boolean isFoggyAt(int x, int z)
	{
		return false;
	}
}
