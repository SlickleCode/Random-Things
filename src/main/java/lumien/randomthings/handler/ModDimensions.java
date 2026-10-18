package lumien.randomthings.handler;

import lumien.randomthings.handler.spectre.SpectreChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * The Spectre dimension (ported from 1.12.2's {@code ModDimensions}). 1.16 made dimensions data: a dimension type
 * (here the datapack file {@code dimension_type/spectre.json}), a dimension (our chunk generator over a fixed {@code the_void} biome) and a
 * client-side sky/fog look ({@code effects}, registered in {@code ClientSetup}) replace 1.14.4's
 * {@code ModDimension} / {@code RegisterDimensionsEvent} / {@code DimensionManager} / {@code Dimension} subclass.
 * <p>
 * The dimension type and the dimension are registered from code, not as datapack JSON: a datapack-defined dimension is
 * flagged as experimental, which makes the game show "Worlds using Experimental Settings are not supported" every time
 * a world is opened. The type goes into the builtin dynamic registry (copied into every world), the dimension is added
 * to the world's dimension list when the server is about to start (before the worlds are created).
 */
public class ModDimensions {
    public static final ResourceLocation SPECTRE_ID = new ResourceLocation("randomthings", "spectre");
    public static final ResourceKey<Level> SPECTRE_WORLD = ResourceKey.create(Registry.DIMENSION_REGISTRY, SPECTRE_ID);
    public static final ResourceKey<DimensionType> SPECTRE_TYPE = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, SPECTRE_ID);
    public static final ResourceKey<LevelStem> SPECTRE_DIMENSION = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, SPECTRE_ID);

    /**
     * Run once at mod setup, before any world is created or loaded. The dimension type itself is plain datapack JSON
     * ({@code data/randomthings/dimension_type/spectre.json}, {@code DimensionType}'s constructor is private since 1.18);
     * only the chunk generator codec has to be registered from code.
     */
    public static void registerBuiltin() {
        Registry.register(Registry.CHUNK_GENERATOR, SPECTRE_ID, SpectreChunkGenerator.CODEC);
        registerDimensionType();
    }

    /**
     * The "Create New World" screen builds its registries from the vanilla data pack only (Forge 1.19.2 adds the mod packs
     * to that load only once the Data Packs screen is opened), so a datapack-only dimension type is missing from a world
     * created through the UI and the server would stop at {@link #addToServer}. The same JSON is therefore also parsed
     * into the built-in registry, which every new world starts from.
     */
    private static void registerDimensionType() {
        if (BuiltinRegistries.DIMENSION_TYPE.getOptional(SPECTRE_TYPE).isPresent()) {
            return;
        }

        try (java.io.InputStream in = ModDimensions.class.getResourceAsStream("/data/randomthings/dimension_type/spectre.json");
             java.io.Reader reader = new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)) {
            DimensionType type = DimensionType.DIRECT_CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseReader(reader))
                    .getOrThrow(false, message -> {
                        throw new IllegalStateException("spectre dimension type: " + message);
                    });
            BuiltinRegistries.register(BuiltinRegistries.DIMENSION_TYPE, SPECTRE_TYPE, type);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Adds the Spectre dimension to this world's dimension list if it is not there yet (new worlds, and old worlds on first load). */
    public static void addToServer(MinecraftServer server) {
        MappedRegistry<LevelStem> dimensions = (MappedRegistry<LevelStem>) server.getWorldData().worldGenSettings().dimensions();

        if (dimensions.getOptional(SPECTRE_ID).isPresent()) {
            return;
        }

        Registry<DimensionType> types = server.registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> biomes = server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> structureSets = server.registryAccess().registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        // A world that already has a level.dat is read with a frozen dimension registry (only a world being created in this session
        // still has an open one), so the registry is opened for this one registration and frozen again.
        dimensions.unfreeze();

        try {
            dimensions.register(SPECTRE_DIMENSION, new LevelStem(types.getHolderOrThrow(SPECTRE_TYPE), new SpectreChunkGenerator(structureSets, new FixedBiomeSource(biomes.getHolderOrThrow(Biomes.THE_VOID)))), Lifecycle.stable());
        } finally {
            dimensions.freeze();
        }
    }

    public static boolean isSpectre(Level world) {
        return world != null && SPECTRE_WORLD.equals(world.dimension());
    }

    /** The loaded Spectre world. */
    public static ServerLevel getSpectreWorld(MinecraftServer server) {
        return server == null ? null : server.getLevel(SPECTRE_WORLD);
    }
}
