package lumien.randomthings.handler;

import lumien.randomthings.handler.spectre.SpectreChunkGenerator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import java.util.Optional;
import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import lumien.randomthings.handler.spectre.SpectreDimensionType;

/**
 * The Spectre dimension (ported from 1.12.2's {@code ModDimensions}). 1.16 made dimensions data: a dimension type
 * (here {@link SpectreDimensionType}), a dimension (our chunk generator over a fixed {@code the_void} biome) and a
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
    public static final RegistryKey<World> SPECTRE_WORLD = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, SPECTRE_ID);
    public static final RegistryKey<DimensionType> SPECTRE_TYPE = RegistryKey.getOrCreateKey(Registry.DIMENSION_TYPE_KEY, SPECTRE_ID);
    public static final RegistryKey<Dimension> SPECTRE_DIMENSION = RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, SPECTRE_ID);

    /** Run once at mod setup, before any world is created or loaded. */
    public static void registerBuiltin() {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, SPECTRE_ID, SpectreChunkGenerator.CODEC);

        DynamicRegistries.Impl builtin = ObfuscationReflectionHelper.getPrivateValue(DynamicRegistries.class, null, "field_243600_c");
        Optional<MutableRegistry<DimensionType>> types = builtin.func_230521_a_(Registry.DIMENSION_TYPE_KEY);

        if (types.isPresent() && !types.get().getOptional(SPECTRE_ID).isPresent()) {
            types.get().register(SPECTRE_TYPE, new SpectreDimensionType(), Lifecycle.stable());
        }
    }

    /** Adds the Spectre dimension to this world's dimension list if it is not there yet (new worlds, and old worlds on first load). */
    public static void addToServer(MinecraftServer server) {
        SimpleRegistry<Dimension> dimensions = server.getServerConfiguration().getDimensionGeneratorSettings().func_236224_e_();

        if (dimensions.getOptional(SPECTRE_ID).isPresent()) {
            return;
        }

        Registry<DimensionType> types = server.getDynamicRegistries().getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<Biome> biomes = server.getDynamicRegistries().getRegistry(Registry.BIOME_KEY);
        DimensionType type = types.getOrThrow(SPECTRE_TYPE);

        dimensions.register(SPECTRE_DIMENSION, new Dimension(() -> type, new SpectreChunkGenerator(new SingleBiomeProvider(biomes.getOrThrow(Biomes.THE_VOID)))), Lifecycle.stable());
    }

    public static boolean isSpectre(World world) {
        return world != null && SPECTRE_WORLD.equals(world.getDimensionKey());
    }

    /** The loaded Spectre world. */
    public static ServerWorld getSpectreWorld(MinecraftServer server) {
        return server == null ? null : server.getWorld(SPECTRE_WORLD);
    }
}
