package lumien.randomthings.enchantment;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Since 1.21 enchantments are data-driven: Magnetic is {@code data/randomthings/enchantment/magnetic.json} (no effects;
 * the pickup is done by {@code MagneticLootModifier}) and code only holds its key and looks the holder up in a registry access.
 */
public class ModEnchantments {
    public static final ResourceKey<Enchantment> MAGNETIC = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "magnetic"));

    public static Holder<Enchantment> holder(HolderLookup.Provider registries, ResourceKey<Enchantment> key) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }
}
