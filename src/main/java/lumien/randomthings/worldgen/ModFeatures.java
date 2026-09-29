package lumien.randomthings.worldgen;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.ObjectHolder;

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

    @ObjectHolder("ancient_furnace")
    public static AncientFurnaceFeature ANCIENT_FURNACE;

    public static void registerFeatures(Register<Feature<?>> featureRegistryEvent) {
        featureRegistryEvent.getRegistry().register(new BeanSproutFeature(NoFeatureConfig::deserialize).setRegistryName("bean_sprout"));
        featureRegistryEvent.getRegistry().register(new LotusFeature(NoFeatureConfig::deserialize).setRegistryName("lotus"));
        featureRegistryEvent.getRegistry().register(new AncientFurnaceFeature(NoFeatureConfig::deserialize).setRegistryName("ancient_furnace"));
    }
}
