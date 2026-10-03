package lumien.randomthings.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

/**
 * This port's non-tile-entity {@link EntityType} registrations.
 */
@ObjectHolder("randomthings")
public class ModEntityTypes {
    @ObjectHolder("floo_fireplace")
    public static EntityType<FlooFireplaceEntity> FLOO_FIREPLACE;

    @ObjectHolder("eclipsed_clock")
    public static EntityType<EclipsedClockEntity> ECLIPSED_CLOCK;

    @ObjectHolder("thrown_weather_egg")
    public static EntityType<ThrownWeatherEggEntity> THROWN_WEATHER_EGG;

    @ObjectHolder("weather_cloud")
    public static EntityType<WeatherCloudEntity> WEATHER_CLOUD;

    @ObjectHolder("time_accelerator")
    public static EntityType<TimeAcceleratorEntity> TIME_ACCELERATOR;

    @ObjectHolder("thrown_golden_egg")
    public static EntityType<ThrownGoldenEggEntity> THROWN_GOLDEN_EGG;

    @ObjectHolder("golden_chicken")
    public static EntityType<GoldenChickenEntity> GOLDEN_CHICKEN;

    @ObjectHolder("artificial_end_portal")
    public static EntityType<ArtificialEndPortalEntity> ARTIFICIAL_END_PORTAL;

    @ObjectHolder("spectre_illuminator")
    public static EntityType<SpectreIlluminatorEntity> SPECTRE_ILLUMINATOR;

    @ObjectHolder("spirit")
    public static EntityType<SpiritEntity> SPIRIT;

    public static void registerEntityTypes(Register<EntityType<?>> entityTypeRegistryEvent) {
        IForgeRegistry<EntityType<?>> registry = entityTypeRegistryEvent.getRegistry();

        EntityType.Builder<FlooFireplaceEntity> flooBuilder = EntityType.Builder.create(FlooFireplaceEntity::new, EntityClassification.MISC);
        registry.register(flooBuilder.size(2.0F, 1.0F).disableSummoning().build("floo_fireplace").setRegistryName("floo_fireplace"));

        EntityType.Builder<EclipsedClockEntity> clockBuilder = EntityType.Builder.create(EclipsedClockEntity::new, EntityClassification.MISC);
        registry.register(clockBuilder.size(0.5F, 0.5F).disableSummoning().build("eclipsed_clock").setRegistryName("eclipsed_clock"));

        EntityType.Builder<ThrownWeatherEggEntity> eggBuilder = EntityType.Builder.create(ThrownWeatherEggEntity::new, EntityClassification.MISC);
        registry.register(eggBuilder.size(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build("thrown_weather_egg").setRegistryName("thrown_weather_egg"));

        EntityType.Builder<WeatherCloudEntity> cloudBuilder = EntityType.Builder.create(WeatherCloudEntity::new, EntityClassification.MISC);
        registry.register(cloudBuilder.size(0.5F, 0.5F).disableSummoning().build("weather_cloud").setRegistryName("weather_cloud"));

        EntityType.Builder<TimeAcceleratorEntity> acceleratorBuilder = EntityType.Builder.create(TimeAcceleratorEntity::new, EntityClassification.MISC);
        registry.register(acceleratorBuilder.size(0.1F, 0.1F).disableSummoning().build("time_accelerator").setRegistryName("time_accelerator"));

        EntityType.Builder<ThrownGoldenEggEntity> goldenEggBuilder = EntityType.Builder.create(ThrownGoldenEggEntity::new, EntityClassification.MISC);
        registry.register(goldenEggBuilder.size(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build("thrown_golden_egg").setRegistryName("thrown_golden_egg"));

        EntityType.Builder<GoldenChickenEntity> goldenChickenBuilder = EntityType.Builder.create(GoldenChickenEntity::new, EntityClassification.CREATURE);
        registry.register(goldenChickenBuilder.size(0.4F, 0.7F).build("golden_chicken").setRegistryName("golden_chicken"));

        EntityType.Builder<ArtificialEndPortalEntity> endPortalBuilder = EntityType.Builder.create(ArtificialEndPortalEntity::new, EntityClassification.MISC);
        registry.register(endPortalBuilder.size(3.0F, 1.0F).disableSummoning().build("artificial_end_portal").setRegistryName("artificial_end_portal"));

        EntityType.Builder<SpectreIlluminatorEntity> illuminatorBuilder = EntityType.Builder.create(SpectreIlluminatorEntity::new, EntityClassification.MISC);
        registry.register(illuminatorBuilder.size(0.5F, 0.5F).disableSummoning().build("spectre_illuminator").setRegistryName("spectre_illuminator"));

        EntityType.Builder<SpiritEntity> spiritBuilder = EntityType.Builder.create(SpiritEntity::new, EntityClassification.MISC);
        registry.register(spiritBuilder.size(0.25F, 0.25F).setTrackingRange(80).setUpdateInterval(1).disableSummoning().build("spirit").setRegistryName("spirit"));
    }
}
