package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

/** Spectre sword: the vanilla tool on the spectre material. */
public class SpectreSwordItem extends SwordItem {
	public SpectreSwordItem(Item.Properties properties) {
		super(SpectreItemTier.MATERIAL, 3.0F, -2.4F, properties);
	}
}
