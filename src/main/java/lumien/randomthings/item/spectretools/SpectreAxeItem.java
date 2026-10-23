package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;

/** Spectre axe: the vanilla tool on the spectre material, plus +3 block reach while held in the main hand. */
public class SpectreAxeItem extends AxeItem implements SpectreItemTier.SpectreReachTool {
	public SpectreAxeItem(Item.Properties properties) {
		super(SpectreItemTier.MATERIAL, 5.0F, -3.0F, properties);
	}
}
