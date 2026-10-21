package lumien.randomthings.item.spectretools;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

/**
 * Ported from 1.12.2's {@code ItemSpectrePickaxe} - plain {@code ItemPickaxe} on the spectre material
 * (same 1/-2.8F damage/speed modifiers vanilla's own pickaxes use, matching 1.12.2 never overriding
 * them either), plus the same +3 block reach while held in the mainhand.
 */
public class SpectrePickaxeItem extends PickaxeItem {
    public SpectrePickaxeItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, properties.attributes(SpectreItemTier.withReach(net.minecraft.world.item.DiggerItem.createAttributes(SpectreItemTier.INSTANCE, 1.0F, -2.8F))));
    }

}
