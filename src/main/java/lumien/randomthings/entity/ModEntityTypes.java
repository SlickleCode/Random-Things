package lumien.randomthings.entity;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

/**
 * This port's non-tile-entity {@link EntityType} registrations.
 */
public class ModEntityTypes {
    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:floo_fireplace")
    public static EntityType<FlooFireplaceEntity> FLOO_FIREPLACE;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:eclipsed_clock")
    public static EntityType<EclipsedClockEntity> ECLIPSED_CLOCK;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:thrown_weather_egg")
    public static EntityType<ThrownWeatherEggEntity> THROWN_WEATHER_EGG;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:weather_cloud")
    public static EntityType<WeatherCloudEntity> WEATHER_CLOUD;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:time_accelerator")
    public static EntityType<TimeAcceleratorEntity> TIME_ACCELERATOR;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:thrown_golden_egg")
    public static EntityType<ThrownGoldenEggEntity> THROWN_GOLDEN_EGG;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:golden_chicken")
    public static EntityType<GoldenChickenEntity> GOLDEN_CHICKEN;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:artificial_end_portal")
    public static EntityType<ArtificialEndPortalEntity> ARTIFICIAL_END_PORTAL;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:spectre_illuminator")
    public static EntityType<SpectreIlluminatorEntity> SPECTRE_ILLUMINATOR;

    @ObjectHolder(registryName = "minecraft:entity_type", value = "randomthings:spirit")
    public static EntityType<SpiritEntity> SPIRIT;

    private static net.minecraft.resources.ResourceKey<EntityType<?>> key(String name) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.fromNamespaceAndPath(lumien.randomthings.lib.ModConstants.MOD_ID, name));
    }

    public static void registerEntityTypes(RTRegistry<EntityType<?>> registry) {

        EntityType.Builder<FlooFireplaceEntity> flooBuilder = EntityType.Builder.of(FlooFireplaceEntity::new, MobCategory.MISC);
        registry.register("floo_fireplace", flooBuilder.sized(2.0F, 1.0F).noSummon().build(key("floo_fireplace")));

        EntityType.Builder<EclipsedClockEntity> clockBuilder = EntityType.Builder.of(EclipsedClockEntity::new, MobCategory.MISC);
        registry.register("eclipsed_clock", clockBuilder.sized(0.5F, 0.5F).noSummon().build(key("eclipsed_clock")));

        EntityType.Builder<ThrownWeatherEggEntity> eggBuilder = EntityType.Builder.of(ThrownWeatherEggEntity::new, MobCategory.MISC);
        registry.register("thrown_weather_egg", eggBuilder.sized(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build(key("thrown_weather_egg")));

        EntityType.Builder<WeatherCloudEntity> cloudBuilder = EntityType.Builder.of(WeatherCloudEntity::new, MobCategory.MISC);
        registry.register("weather_cloud", cloudBuilder.sized(0.5F, 0.5F).noSummon().build(key("weather_cloud")));

        EntityType.Builder<TimeAcceleratorEntity> acceleratorBuilder = EntityType.Builder.of(TimeAcceleratorEntity::new, MobCategory.MISC);
        registry.register("time_accelerator", acceleratorBuilder.sized(0.1F, 0.1F).noSummon().build(key("time_accelerator")));

        EntityType.Builder<ThrownGoldenEggEntity> goldenEggBuilder = EntityType.Builder.of(ThrownGoldenEggEntity::new, MobCategory.MISC);
        registry.register("thrown_golden_egg", goldenEggBuilder.sized(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build(key("thrown_golden_egg")));

        EntityType.Builder<GoldenChickenEntity> goldenChickenBuilder = EntityType.Builder.of(GoldenChickenEntity::new, MobCategory.CREATURE);
        registry.register("golden_chicken", goldenChickenBuilder.sized(0.4F, 0.7F).build(key("golden_chicken")));

        EntityType.Builder<ArtificialEndPortalEntity> endPortalBuilder = EntityType.Builder.of(ArtificialEndPortalEntity::new, MobCategory.MISC);
        registry.register("artificial_end_portal", endPortalBuilder.sized(3.0F, 1.0F).noSummon().build(key("artificial_end_portal")));

        EntityType.Builder<SpectreIlluminatorEntity> illuminatorBuilder = EntityType.Builder.of(SpectreIlluminatorEntity::new, MobCategory.MISC);
        registry.register("spectre_illuminator", illuminatorBuilder.sized(0.5F, 0.5F).noSummon().build(key("spectre_illuminator")));

        EntityType.Builder<SpiritEntity> spiritBuilder = EntityType.Builder.of(SpiritEntity::new, MobCategory.MISC);
        registry.register("spirit", spiritBuilder.sized(0.25F, 0.25F).setTrackingRange(80).setUpdateInterval(1).noSummon().build(key("spirit")));
    }
}
