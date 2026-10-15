package lumien.randomthings.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;

public class ModEffects {
    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_fire")
    public static MobEffect IMBUE_FIRE;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_poison")
    public static MobEffect IMBUE_POISON;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_experience")
    public static MobEffect IMBUE_EXPERIENCE;

    @ObjectHolder(registryName = "minecraft:mob_effect", value = "randomthings:imbue_wither")
    public static MobEffect IMBUE_WITHER;

    public static void registerEffects(IForgeRegistry<MobEffect> registry) {

        registry.register("imbue_fire", new ImbueEffect(Color.ORANGE.getRGB()));
        registry.register("imbue_poison", new ImbueEffect(Color.GREEN.darker().getRGB()));
        registry.register("imbue_experience", new ImbueEffect(Color.YELLOW.getRGB()));
        registry.register("imbue_wither", new ImbueEffect(Color.BLACK.brighter().getRGB()));
    }
}
