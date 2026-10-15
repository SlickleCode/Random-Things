package lumien.randomthings.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("randomthings")
public class ModEnchantments {
    @ObjectHolder("magnetic")
    public static Enchantment MAGNETIC;

    public static void registerEnchantments(Register<Enchantment> enchantmentRegistryEvent) {
        IForgeRegistry<Enchantment> registry = enchantmentRegistryEvent.getRegistry();

        registry.register(new MagneticEnchantment(Enchantment.Rarity.RARE, EquipmentSlot.MAINHAND).setRegistryName("magnetic"));
    }
}
