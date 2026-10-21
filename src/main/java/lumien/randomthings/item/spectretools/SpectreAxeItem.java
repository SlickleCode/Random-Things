package lumien.randomthings.item.spectretools;


import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;

/**
 * Ported from 1.12.2's {@code ItemSpectreAxe} - 8.0 attack damage / -3.0 attack speed hardcoded
 * directly onto the item there (bypassing the material's own damage bonus), reproduced here as a
 * {@code 5.0F} damage modifier since 1.14.4's {@code AxeItem} constructor adds the tier's own
 * {@link SpectreItemTier#getAttackDamage() 3.0F} on top (5 + 3 = 8, matching exactly); attack speed
 * isn't tier-additive in this version's {@code ToolItem}, so {@code -3.0F} carries over unchanged.
 * Plus the same +3 block reach while held in the mainhand.
 *
 * <p>1.12.2's {@code getDestroySpeed} override (full efficiency on wood/plant/vine materials
 * regardless of the block-specific {@code EFFECTIVE_ON} list) needs no equivalent here - vanilla's own
 * {@code AxeItem#getDestroySpeed} already does exactly that check (material != WOOD/PLANTS/
 * TALL_PLANTS/BAMBOO -&gt; full efficiency), confirmed from the real 1.14.4 source before concluding no
 * override was needed.
 */
public class SpectreAxeItem extends AxeItem {
    public SpectreAxeItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, properties.attributes(SpectreItemTier.withReach(net.minecraft.world.item.DiggerItem.createAttributes(SpectreItemTier.INSTANCE, 5.0F, -3.0F))));
    }

}
