package lumien.randomthings.potion;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

import java.awt.*;

public class ModEffects {
    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_fire")
    public static Holder<MobEffect> IMBUE_FIRE;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_poison")
    public static Holder<MobEffect> IMBUE_POISON;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_experience")
    public static Holder<MobEffect> IMBUE_EXPERIENCE;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_wither")
    public static Holder<MobEffect> IMBUE_WITHER;

    public static void registerEffects(RTRegistry<MobEffect> registry) {

        registry.register("imbue_fire", new ImbueEffect(Color.ORANGE.getRGB()));
        registry.register("imbue_poison", new ImbueEffect(Color.GREEN.darker().getRGB()));
        registry.register("imbue_experience", new ImbueEffect(Color.YELLOW.getRGB()));
        registry.register("imbue_wither", new ImbueEffect(Color.BLACK.brighter().getRGB()));
    }
}
