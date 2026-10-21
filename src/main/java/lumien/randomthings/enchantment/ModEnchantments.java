package lumien.randomthings.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

public class ModEnchantments {
    @ObjectHolder(registryName = "minecraft:enchantment", value = "randomthings:magnetic")
    public static Enchantment MAGNETIC;

    public static void registerEnchantments(RTRegistry<Enchantment> registry) {

        registry.register("magnetic", new MagneticEnchantment());
    }
}
