package lumien.randomthings.item.spectretools;

import net.minecraft.world.item.Item;

/** Spectre pickaxe: the vanilla tool on the spectre material, plus +3 block reach while held in the main hand. */
public class SpectrePickaxeItem extends Item implements SpectreItemTier.SpectreReachTool {
	public SpectrePickaxeItem(Item.Properties properties) {
		super(properties.pickaxe(SpectreItemTier.MATERIAL, 1.0F, -2.8F));
	}
}
