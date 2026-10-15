package lumien.randomthings.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

public class ModEnchantments {
    @ObjectHolder(registryName = "minecraft:enchantment", value = "randomthings:magnetic")
    public static Enchantment MAGNETIC;

    public static void registerEnchantments(IForgeRegistry<Enchantment> registry) {

        registry.register("magnetic", new MagneticEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND));
    }
}
