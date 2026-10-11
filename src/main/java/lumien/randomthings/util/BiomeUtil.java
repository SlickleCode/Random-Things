package lumien.randomthings.util;

import java.util.Collections;
import java.util.Set;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

/**
 * 1.16 made biomes data-driven: the {@link Biome} instances in a world are not the ones in {@code ForgeRegistries},
 * and {@link BiomeDictionary} is keyed by {@link RegistryKey}. Forge still sets the registry name on every biome it
 * enhances, which is what these helpers go through.
 */
public class BiomeUtil {
    public static RegistryKey<Biome> key(Biome biome) {
        if (biome == null || biome.getRegistryName() == null) {
            return null;
        }

        return RegistryKey.getOrCreateKey(Registry.BIOME_KEY, biome.getRegistryName());
    }

    public static boolean hasType(Biome biome, Type type) {
        RegistryKey<Biome> key = key(biome);
        return key != null && BiomeDictionary.hasType(key, type);
    }

    public static Set<Type> types(Biome biome) {
        RegistryKey<Biome> key = key(biome);
        return key == null ? Collections.emptySet() : BiomeDictionary.getTypes(key);
    }

    public static TranslationTextComponent displayName(Biome biome) {
        if (biome.getRegistryName() == null) {
            return new TranslationTextComponent("biome.minecraft.unknown");
        }

        return new TranslationTextComponent("biome." + biome.getRegistryName().getNamespace() + "." + biome.getRegistryName().getPath());
    }
}
