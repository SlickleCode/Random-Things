package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

/** Spectre pickaxe: the vanilla tool on the spectre material, plus +3 block reach while held in the main hand. */
public class SpectrePickaxeItem extends PickaxeItem implements SpectreItemTier.SpectreReachTool {
	public SpectrePickaxeItem(Item.Properties properties) {
		super(SpectreItemTier.MATERIAL, 1.0F, -2.8F, properties);
	}
}
