package lumien.randomthings.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

/**
 * Ported from 1.12.2's {@code EnchantmentMagnetic}. The original intercepted
 * {@code PlayerInteractionManager.tryHarvestBlock} via ASM to catch the
 * spawned drop entities and hand them straight to the player's inventory
 * instead. This Forge version has a purpose-built event for exactly that -
 * {@code BlockEvent.HarvestDropsEvent}, fired with the same mutable drop list
 * before it's spawned into the world, and canceling it clears that list
 * automatically (see {@code ForgeEventFactory.fireBlockHarvesting}) - so the
 * listener in {@code RandomThings} does the pickup itself with no coremod
 * needed, unlike the wiki's own "ASM?" note for this feature.
 */
public class MagneticEnchantment extends Enchantment {
    protected MagneticEnchantment(Rarity rarityIn, EquipmentSlotType... slots) {
        super(rarityIn, EnchantmentType.DIGGER, slots);
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 15;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return getMinEnchantability(enchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
