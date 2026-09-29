package lumien.randomthings.item.spectretools;

import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;

/**
 * Ported from 1.12.2's {@code ItemSpectreSword} - plain {@code ItemSword} on the spectre material, no
 * extra overrides there either.
 *
 * <p>Disclosed simplification: 1.12.2 had an ASM hook (real 1.12.2 enchantment glow still required -
 * this class never forces {@code hasEffect}) recoloring this sword's enchant-glow tint to white
 * whenever it's actually enchanted. Dropped rather than porting a new render hook for a purely
 * cosmetic detail with no infrastructure precedent in this port - same call already made for Redstone
 * Observer's own dropped red-glow recolor (see {@code WIKI_FEATURE_STATUS.md}). The sword still glows
 * normally via a real enchantment (high 22 enchantability makes that easy), just with vanilla's
 * default tint instead of white.
 *
 * <p>1.12.2's {@code EntitySpirit} let this sword (and only this sword) damage an otherwise
 * near-invulnerable Spirit mob - not wired up here since {@code EntitySpirit} itself isn't ported yet
 * (a separate NOT-STARTED feature); revisit this class if/when that mob is added.
 */
public class SpectreSwordItem extends SwordItem {
    public SpectreSwordItem(Item.Properties properties) {
        super(SpectreItemTier.INSTANCE, 3, -2.4F, properties);
    }
}
