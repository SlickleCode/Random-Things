package lumien.randomthings.worldgen;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.util.ResourceLocation;
import lumien.randomthings.lib.ModConstants;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraft.world.gen.GenerationStage;

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
@ObjectHolder("randomthings")
public class ModFeatures {
    @ObjectHolder("bean_sprout")
    public static BeanSproutFeature BEAN_SPROUT;

    @ObjectHolder("lotus")
    public static LotusFeature LOTUS;

    @ObjectHolder("glowing_mushroom")
    public static GlowingMushroomFeature GLOWING_MUSHROOM;

    @ObjectHolder("ancient_furnace")
    public static AncientFurnaceFeature ANCIENT_FURNACE;

    public static void registerFeatures(Register<Feature<?>> featureRegistryEvent) {
        featureRegistryEvent.getRegistry().register(new BeanSproutFeature(NoFeatureConfig.CODEC).setRegistryName("bean_sprout"));
        featureRegistryEvent.getRegistry().register(new LotusFeature(NoFeatureConfig.CODEC).setRegistryName("lotus"));
        featureRegistryEvent.getRegistry().register(new AncientFurnaceFeature(NoFeatureConfig.CODEC).setRegistryName("ancient_furnace"));
        featureRegistryEvent.getRegistry().register(new GlowingMushroomFeature(NoFeatureConfig.CODEC).setRegistryName("glowing_mushroom"));
    }

    private static ConfiguredFeature<?, ?> BEAN_SPROUT_CONFIGURED;
    private static ConfiguredFeature<?, ?> LOTUS_CONFIGURED;
    private static ConfiguredFeature<?, ?> ANCIENT_FURNACE_CONFIGURED;
    private static ConfiguredFeature<?, ?> GLOWING_MUSHROOM_CONFIGURED;

    /**
     * 1.16 biome features are {@code ConfiguredFeature}s that must be registered (biomes are serialized by registry
     * reference). The old {@code Placement.CHANCE_HEIGHTMAP} is now "with 1/n probability, at a random column of the
     * chunk, on the heightmap" - the three placements below applied from the last one to the first. Each feature's own
     * {@code generate} still does the per-attempt biome/temperature gating, as in 1.14.4. Call from
     * {@code FMLCommonSetupEvent.enqueueWork} (after the feature registry event).
     */
    public static void registerConfiguredFeatures() {
        BEAN_SPROUT_CONFIGURED = register("bean_sprout", chance(BEAN_SPROUT, 2));
        LOTUS_CONFIGURED = register("lotus", chance(LOTUS, 10));
        ANCIENT_FURNACE_CONFIGURED = register("ancient_furnace", chance(ANCIENT_FURNACE, 400));
        GLOWING_MUSHROOM_CONFIGURED = register("glowing_mushroom", chance(GLOWING_MUSHROOM, 4));
    }

    private static ConfiguredFeature<?, ?> chance(Feature<NoFeatureConfig> feature, int chance) {
        return feature.withConfiguration(NoFeatureConfig.INSTANCE)
                .withPlacement(Placement.HEIGHTMAP.configure(NoPlacementConfig.INSTANCE))
                .withPlacement(Placement.SQUARE.configure(NoPlacementConfig.INSTANCE))
                .withPlacement(Placement.CHANCE.configure(new ChanceConfig(chance)));
    }

    private static ConfiguredFeature<?, ?> register(String name, ConfiguredFeature<?, ?> configured) {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(ModConstants.MOD_ID, name), configured);
    }

    /** Injects this port's natural-surface features into every overworld-style biome (nether/end biomes cannot host them). */
    public static void onBiomeLoading(BiomeLoadingEvent event) {
        if (event.getCategory() == Biome.Category.NETHER || event.getCategory() == Biome.Category.THEEND || event.getCategory() == Biome.Category.NONE || BEAN_SPROUT_CONFIGURED == null) {
            return;
        }

        BiomeGenerationSettingsBuilder generation = event.getGeneration();
        generation.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, BEAN_SPROUT_CONFIGURED);
        generation.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, LOTUS_CONFIGURED);
        generation.withFeature(GenerationStage.Decoration.LOCAL_MODIFICATIONS, ANCIENT_FURNACE_CONFIGURED);
        generation.withFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, GLOWING_MUSHROOM_CONFIGURED);
    }
}
