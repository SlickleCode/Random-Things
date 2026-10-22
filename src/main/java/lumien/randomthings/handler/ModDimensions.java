package lumien.randomthings.handler;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.handler.spectre.SpectreChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * The Spectre dimension (ported from 1.12.2's {@code ModDimensions}). Since 1.16 dimensions are data: a dimension type
 * ({@code data/randomthings/dimension_type/spectre.json}), a dimension ({@code data/randomthings/dimension/spectre.json}: our chunk
 * generator over a fixed {@code the_void} biome) and a client-side sky/fog look ({@code effects}, registered in {@code ClientSetup}) replace
 * 1.14.4's {@code ModDimension} / {@code RegisterDimensionsEvent} / {@code DimensionManager} / {@code Dimension} subclass. 1.20 loads the
 * mod's data pack for the Create World screen too, so (unlike 1.19.2) nothing has to be registered from code except the generator codec.
 */
public class ModDimensions {
    public static final ResourceLocation SPECTRE_ID = ResourceLocation.fromNamespaceAndPath("randomthings", "spectre");
    public static final ResourceKey<Level> SPECTRE_WORLD = ResourceKey.create(Registries.DIMENSION, SPECTRE_ID);
    public static final ResourceKey<DimensionType> SPECTRE_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, SPECTRE_ID);
    public static final ResourceKey<LevelStem> SPECTRE_DIMENSION = ResourceKey.create(Registries.LEVEL_STEM, SPECTRE_ID);

    /** Run once at mod setup, before any world is created or loaded: the chunk generator codec the dimension JSON refers to. */
    public static void registerBuiltin() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, SPECTRE_ID, SpectreChunkGenerator.CODEC);
    }

    public static boolean isSpectre(Level world) {
        return world != null && SPECTRE_WORLD.equals(world.dimension());
    }

    /** The loaded Spectre world. */
    public static ServerLevel getSpectreWorld(MinecraftServer server) {
        return server == null ? null : server.getLevel(SPECTRE_WORLD);
    }
}
