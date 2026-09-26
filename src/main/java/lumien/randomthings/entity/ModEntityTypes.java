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
    }
}
