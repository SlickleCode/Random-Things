package lumien.randomthings.util;

import java.util.Collections;
import java.util.Set;

import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

/**
 * 1.16 made biomes data-driven: the {@link Biome} instances in a world are not the ones in {@code ForgeRegistries},
 * and {@link BiomeDictionary} is keyed by {@link RegistryKey}. Forge still sets the registry name on every biome it
 * enhances, which is what these helpers go through.
 */
public class BiomeUtil {
    public static ResourceKey<Biome> key(Biome biome) {
        if (biome == null || biome.getRegistryName() == null) {
            return null;
        }

        return ResourceKey.create(Registry.BIOME_REGISTRY, biome.getRegistryName());
    }

    public static boolean hasType(Biome biome, Type type) {
        ResourceKey<Biome> key = key(biome);
        return key != null && BiomeDictionary.hasType(key, type);
    }

    public static Set<Type> types(Biome biome) {
        ResourceKey<Biome> key = key(biome);
        return key == null ? Collections.emptySet() : BiomeDictionary.getTypes(key);
    }

    public static TranslatableComponent displayName(Biome biome) {
        if (biome.getRegistryName() == null) {
            return new TranslatableComponent("biome.minecraft.unknown");
        }

        return new TranslatableComponent("biome." + biome.getRegistryName().getNamespace() + "." + biome.getRegistryName().getPath());
    }
}
