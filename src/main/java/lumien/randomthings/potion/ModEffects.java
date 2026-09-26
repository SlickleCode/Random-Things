package lumien.randomthings.potion;

import net.minecraft.potion.Effect;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;

@ObjectHolder("randomthings")
public class ModEffects {
    @ObjectHolder("imbue_fire")
    public static Effect IMBUE_FIRE;

    @ObjectHolder("imbue_poison")
    public static Effect IMBUE_POISON;

    @ObjectHolder("imbue_experience")
    public static Effect IMBUE_EXPERIENCE;

    @ObjectHolder("imbue_wither")
    public static Effect IMBUE_WITHER;

    public static void registerEffects(Register<Effect> effectRegistryEvent) {
        IForgeRegistry<Effect> registry = effectRegistryEvent.getRegistry();

        registry.register(new ImbueEffect(Color.ORANGE.getRGB()).setRegistryName("imbue_fire"));
        registry.register(new ImbueEffect(Color.GREEN.darker().getRGB()).setRegistryName("imbue_poison"));
        registry.register(new ImbueEffect(Color.YELLOW.getRGB()).setRegistryName("imbue_experience"));
        registry.register(new ImbueEffect(Color.BLACK.brighter().getRGB()).setRegistryName("imbue_wither"));
    }
}
