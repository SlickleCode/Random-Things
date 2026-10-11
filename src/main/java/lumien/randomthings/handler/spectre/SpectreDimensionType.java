package lumien.randomthings.handler.spectre;

import java.util.OptionalLong;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;

/**
 * The Spectre dimension's settings - what 1.14.4's {@code SpectreDimension} hard-coded: always bright, no sky light, a
 * fixed midnight sky angle (time 18000 gives the old {@code calculateCelestialAngle} of 0.5), no weather-style natural
 * rules. {@code DimensionType}'s constructor is protected, hence the subclass.
 */
public class SpectreDimensionType extends DimensionType
{
	public SpectreDimensionType()
	{
		super(OptionalLong.of(18000L), false, false, false, false, 1.0D, false, false, false, false, 256, new ResourceLocation("minecraft", "infiniburn_overworld"), new ResourceLocation("randomthings", "spectre"), 1.0F);
	}
}
