package lumien.randomthings.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;

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
    protected MagneticEnchantment(Rarity rarityIn, EquipmentSlot... slots) {
        super(rarityIn, EnchantmentCategory.DIGGER, slots);
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 15;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return getMinCost(enchantmentLevel) + 50;
    }

    @Override
    public boolean canApplyAtEnchantingTable(net.minecraft.world.item.ItemStack stack) {
        return lumien.randomthings.config.RTConfig.MAGNETIC_ENCHANTMENT.get() && super.canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {
        return lumien.randomthings.config.RTConfig.MAGNETIC_ENCHANTMENT.get();
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
