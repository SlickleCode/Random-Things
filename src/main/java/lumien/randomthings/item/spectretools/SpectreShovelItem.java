package lumien.randomthings.item.spectretools;


import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;

/**
 * Ported from 1.12.2's {@code ItemSpectreShovel} - plain {@code ItemSpade} on the spectre material
 * (same 1.5F/-3.0F damage/speed modifiers vanilla's own shovels use, matching 1.12.2 never overriding
 * them either), plus the same +3 block reach while held in the mainhand.
 */
public class SpectreShovelItem extends ShovelItem {
    public SpectreShovelItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, properties.attributes(SpectreItemTier.withReach(net.minecraft.world.item.DiggerItem.createAttributes(SpectreItemTier.INSTANCE, 1.5F, -3.0F))));
    }

}
