package lumien.randomthings.util;

import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Biomes are data-driven and 1.19 removed {@code getRegistryName()} and {@code BiomeDictionary}: a world's {@link Biome}
 * instances live in its {@link RegistryAccess} (not in {@code ForgeRegistries.BIOMES}), their ids come from that registry
 * and their "types" from biome tags. The old dictionary type names (BEACH, COLD, ...) are kept as the names these helpers
 * return; each maps to the common ({@code c:is_<name>}) or vanilla ({@code minecraft:is_<name>}) biome tag of that name.
 */
public class BiomeUtil {
    /** Dictionary type name -> tag; names not listed use {@code c:is_<lowercase name>}. */
    private static final Map<String, Identifier> VANILLA_TAGS = new HashMap<>();

    static {
        VANILLA_TAGS.put("FOREST", Identifier.fromNamespaceAndPath("minecraft", "is_forest"));
        VANILLA_TAGS.put("HILLS", Identifier.fromNamespaceAndPath("minecraft", "is_hill"));
        VANILLA_TAGS.put("JUNGLE", Identifier.fromNamespaceAndPath("minecraft", "is_jungle"));
        VANILLA_TAGS.put("MESA", Identifier.fromNamespaceAndPath("minecraft", "is_badlands"));
        VANILLA_TAGS.put("MOUNTAIN", Identifier.fromNamespaceAndPath("minecraft", "is_mountain"));
        VANILLA_TAGS.put("NETHER", Identifier.fromNamespaceAndPath("minecraft", "is_nether"));
        VANILLA_TAGS.put("END", Identifier.fromNamespaceAndPath("minecraft", "is_end"));
        VANILLA_TAGS.put("OCEAN", Identifier.fromNamespaceAndPath("minecraft", "is_ocean"));
        VANILLA_TAGS.put("RIVER", Identifier.fromNamespaceAndPath("minecraft", "is_river"));
        VANILLA_TAGS.put("MESA", Identifier.fromNamespaceAndPath("c", "is_badlands"));
        VANILLA_TAGS.put("CONIFEROUS", Identifier.fromNamespaceAndPath("c", "is_taiga"));
        VANILLA_TAGS.put("DENSE", Identifier.fromNamespaceAndPath("c", "is_dense_vegetation"));
        VANILLA_TAGS.put("SPARSE", Identifier.fromNamespaceAndPath("c", "is_sparse_vegetation"));
        VANILLA_TAGS.put("WATER", Identifier.fromNamespaceAndPath("c", "is_aquatic"));
    }

    /** Every type name {@link #types} can report (the cases of the biome colour switch plus the ones worldgen asks for). */
    public static final String[] TYPE_NAMES = { "BEACH", "COLD", "CONIFEROUS", "DEAD", "DENSE", "DRY", "END", "FOREST", "HILLS", "HOT", "JUNGLE", "LUSH", "MAGICAL", "MESA", "MOUNTAIN", "MUSHROOM", "NETHER", "OCEAN", "PLAINS", "RIVER", "SANDY", "SAVANNA", "SNOWY", "SPARSE", "SPOOKY", "SWAMP", "WASTELAND", "WATER", "WET" };

    /** The registry accesses a biome instance may belong to: the client level's first (client thread), then the server's. */
    private static List<RegistryAccess> accesses() {
        List<RegistryAccess> list = new ArrayList<>(2);

        if (FMLEnvironment.getDist().isClient()) {
            net.minecraft.client.multiplayer.ClientLevel level = net.minecraft.client.Minecraft.getInstance().level;

            if (level != null) {
                list.add(level.registryAccess());
            }
        }

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            list.add(ServerLifecycleHooks.getCurrentServer().registryAccess());
        }

        return list;
    }

    /** The registry holder of a biome instance (a world's own, or the static {@code ForgeRegistries} one), if it can be found. */
    public static Optional<Holder<Biome>> holder(Biome biome) {
        if (biome == null) {
            return Optional.empty();
        }

        List<RegistryAccess> accesses = accesses();

        for (RegistryAccess access : accesses) {
            Registry<Biome> registry = access.lookupOrThrow(Registries.BIOME);
            Optional<ResourceKey<Biome>> key = registry.getResourceKey(biome);

            if (key.isPresent()) {
                return registry.get(key.get()).map(h -> (Holder<Biome>) h);
            }
        }

        Identifier id = null;

        if (id != null) {
            for (RegistryAccess access : accesses) {
                Optional<Holder<Biome>> holder = access.lookupOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, id)).map(h -> (Holder<Biome>) h);

                if (holder.isPresent()) {
                    return holder;
                }
            }
        }

        return Optional.empty();
    }

    /** A biome by id from the current world's registry (1.19.3+: biomes are data-pack entries, {@code ForgeRegistries.BIOMES} is empty). */
    public static Biome byId(Identifier id) {
        for (RegistryAccess access : accesses()) {
            Biome biome = access.lookupOrThrow(Registries.BIOME).getValue(id);

            if (biome != null) {
                return biome;
            }
        }

        return null;
    }

    /** Every biome id of the current world's registry (empty when no world is loaded). */
    public static Set<Identifier> ids() {
        for (RegistryAccess access : accesses()) {
            return access.lookupOrThrow(Registries.BIOME).keySet();
        }

        return Set.of();
    }

    public static Identifier id(Biome biome) {
        Optional<Holder<Biome>> holder = holder(biome);

        if (holder.isPresent() && holder.get().unwrapKey().isPresent()) {
            return holder.get().unwrapKey().get().identifier();
        }

        return null;
    }

    public static ResourceKey<Biome> key(Biome biome) {
        Identifier id = id(biome);

        return id == null ? null : ResourceKey.create(Registries.BIOME, id);
    }

    public static boolean hasType(Biome biome, String type) {
        Optional<Holder<Biome>> holder = holder(biome);

        return holder.isPresent() && holder.get().is(tag(type));
    }

    public static Set<String> types(Biome biome) {
        Optional<Holder<Biome>> holder = holder(biome);

        if (holder.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> types = new HashSet<>();

        for (String name : TYPE_NAMES) {
            if (holder.get().is(tag(name))) {
                types.add(name);
            }
        }

        return types;
    }

    private static TagKey<Biome> tag(String type) {
        Identifier id = VANILLA_TAGS.getOrDefault(type, Identifier.fromNamespaceAndPath("c", "is_" + type.toLowerCase(java.util.Locale.ROOT)));

        return TagKey.create(Registries.BIOME, id);
    }

    public static Component displayName(Biome biome) {
        Identifier id = id(biome);

        if (id == null) {
            return Component.translatable("biome.minecraft.unknown");
        }

        return Component.translatable("biome." + id.getNamespace() + "." + id.getPath());
    }
}
