package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;

/** Spectre sword: the vanilla tool on the spectre material. */
public class SpectreSwordItem extends Item {
	public SpectreSwordItem(Item.Properties properties) {
		super(properties.sword(SpectreItemTier.MATERIAL, 3.0F, -2.4F));
	}
}
