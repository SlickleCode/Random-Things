package lumien.randomthings.handler;

import com.mojang.serialization.MapCodec;

import lumien.randomthings.config.RTConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Data condition {@code randomthings:magnetic_enabled}: true while the {@code MagneticEnchantment} config option is on.
 * The Magnetic tag files (enchanting table, trades, random loot) carry it, so turning the option off removes the
 * enchantment from every source, as on 1.14.4 to 1.20.6.
 */
public class MagneticEnabledCondition implements ICondition
{
	public static final MagneticEnabledCondition INSTANCE = new MagneticEnabledCondition();
	public static final MapCodec<MagneticEnabledCondition> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public boolean test(IContext context)
	{
		return RTConfig.MAGNETIC_ENCHANTMENT.get();
	}

	@Override
	public MapCodec<? extends ICondition> codec()
	{
		return CODEC;
	}
}
