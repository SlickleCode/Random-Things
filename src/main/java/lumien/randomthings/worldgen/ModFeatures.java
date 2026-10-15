package lumien.randomthings.worldgen;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.data.BuiltinRegistries;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.resources.ResourceLocation;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * This port's worldgen {@link Feature} registrations - the counterpart to
 * {@code ModBlocks}/{@code ModItems} for the world-generation registry.
 * Empty until this session, since Blood Rose (the only previous occupant,
 * itself new-to-1.14.4 content with no 1.12.2 source or wiki page) was
 * deleted entirely - see the "Wiki as authoritative scope" note in the plan
 * file. Its registration/biome-injection pattern (this class, plus the
 * {@code ForgeRegistries.BIOMES.forEach} loop in {@code RandomThings
 * #setupCommon}) is preserved here even though the feature itself is gone,
 * since it's the exact mechanism 1.14.4/Forge 28.2.26 needs - confirmed via
 * a jar-content check that {@code BiomeLoadingEvent} (the cleaner, data-
 * driven-biome-era replacement) doesn't exist in this Forge version at all.
 */
public class ModFeatures {
    @ObjectHolder(registryName = "minecraft:worldgen/feature", value = "randomthings:bean_sprout")
    public static BeanSproutFeature BEAN_SPROUT;

    @ObjectHolder(registryName = "minecraft:worldgen/feature", value = "randomthings:lotus")
    public static LotusFeature LOTUS;

    @ObjectHolder(registryName = "minecraft:worldgen/feature", value = "randomthings:glowing_mushroom")
    public static GlowingMushroomFeature GLOWING_MUSHROOM;

    @ObjectHolder(registryName = "minecraft:worldgen/feature", value = "randomthings:ancient_furnace")
    public static AncientFurnaceFeature ANCIENT_FURNACE;

    public static void registerFeatures(IForgeRegistry<Feature<?>> registry) {
        registry.register("bean_sprout", new BeanSproutFeature(NoneFeatureConfiguration.CODEC));
        registry.register("lotus", new LotusFeature(NoneFeatureConfiguration.CODEC));
        registry.register("ancient_furnace", new AncientFurnaceFeature(NoneFeatureConfiguration.CODEC));
        registry.register("glowing_mushroom", new GlowingMushroomFeature(NoneFeatureConfiguration.CODEC));
    }

    private static Holder<PlacedFeature> BEAN_SPROUT_PLACED;
    private static Holder<PlacedFeature> LOTUS_PLACED;
    private static Holder<PlacedFeature> ANCIENT_FURNACE_PLACED;
    private static Holder<PlacedFeature> GLOWING_MUSHROOM_PLACED;

    /**
     * 1.18 biome features are {@code PlacedFeature}s (a configured feature plus placement modifiers) that must be
     * registered (biomes are serialized by registry reference). The old 1.16 "chance, then square, then heightmap"
     * decorator chain is now {@code RarityFilter} (1 in n chunks), {@code InSquarePlacement} (random column in the
     * chunk) and {@code HeightmapPlacement}. Each feature's own {@code place} still does the per-attempt
     * biome/temperature gating. Call from {@code FMLCommonSetupEvent.enqueueWork} (after the feature registry event).
     */
    public static void registerConfiguredFeatures() {
        BEAN_SPROUT_PLACED = placed("bean_sprout", 2);
        LOTUS_PLACED = placed("lotus", 10);
        ANCIENT_FURNACE_PLACED = placed("ancient_furnace", 400);
        GLOWING_MUSHROOM_PLACED = placed("glowing_mushroom", 4);
    }

    private static Holder<PlacedFeature> placed(String name, int chance) {
        ResourceLocation id = new ResourceLocation(ModConstants.MOD_ID, name);
        @SuppressWarnings("unchecked")
        Feature<NoneFeatureConfiguration> feature = (Feature<NoneFeatureConfiguration>) net.minecraftforge.registries.ForgeRegistries.FEATURES.getValue(id);
        if (feature == null) throw new IllegalStateException("feature not registered: " + id);
        Holder<ConfiguredFeature<?, ?>> configured = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, id, new ConfiguredFeature<>(feature, NoneFeatureConfiguration.INSTANCE));

        return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, id, new PlacedFeature(configured,
                List.of(RarityFilter.onAverageOnceEvery(chance), InSquarePlacement.spread(), HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING), BiomeFilter.biome())));
    }
}
