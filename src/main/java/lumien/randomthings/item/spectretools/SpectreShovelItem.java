package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/** Spectre shovel: the vanilla tool on the spectre material, plus +3 block reach while held in the main hand. */
public class SpectreShovelItem extends ShovelItem implements SpectreItemTier.SpectreReachTool {
	public SpectreShovelItem(Item.Properties properties) {
		super(SpectreItemTier.MATERIAL, 1.5F, -3.0F, properties);
	}
}
