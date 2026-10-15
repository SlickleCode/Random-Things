package lumien.randomthings.entity;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
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

        EntityType.Builder<FlooFireplaceEntity> flooBuilder = EntityType.Builder.of(FlooFireplaceEntity::new, MobCategory.MISC);
        registry.register(flooBuilder.sized(2.0F, 1.0F).noSummon().build("floo_fireplace").setRegistryName("floo_fireplace"));

        EntityType.Builder<EclipsedClockEntity> clockBuilder = EntityType.Builder.of(EclipsedClockEntity::new, MobCategory.MISC);
        registry.register(clockBuilder.sized(0.5F, 0.5F).noSummon().build("eclipsed_clock").setRegistryName("eclipsed_clock"));

        EntityType.Builder<ThrownWeatherEggEntity> eggBuilder = EntityType.Builder.of(ThrownWeatherEggEntity::new, MobCategory.MISC);
        registry.register(eggBuilder.sized(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build("thrown_weather_egg").setRegistryName("thrown_weather_egg"));

        EntityType.Builder<WeatherCloudEntity> cloudBuilder = EntityType.Builder.of(WeatherCloudEntity::new, MobCategory.MISC);
        registry.register(cloudBuilder.sized(0.5F, 0.5F).noSummon().build("weather_cloud").setRegistryName("weather_cloud"));

        EntityType.Builder<TimeAcceleratorEntity> acceleratorBuilder = EntityType.Builder.of(TimeAcceleratorEntity::new, MobCategory.MISC);
        registry.register(acceleratorBuilder.sized(0.1F, 0.1F).noSummon().build("time_accelerator").setRegistryName("time_accelerator"));

        EntityType.Builder<ThrownGoldenEggEntity> goldenEggBuilder = EntityType.Builder.of(ThrownGoldenEggEntity::new, MobCategory.MISC);
        registry.register(goldenEggBuilder.sized(0.25F, 0.25F).setTrackingRange(64).setUpdateInterval(10).build("thrown_golden_egg").setRegistryName("thrown_golden_egg"));

        EntityType.Builder<GoldenChickenEntity> goldenChickenBuilder = EntityType.Builder.of(GoldenChickenEntity::new, MobCategory.CREATURE);
        registry.register(goldenChickenBuilder.sized(0.4F, 0.7F).build("golden_chicken").setRegistryName("golden_chicken"));

        EntityType.Builder<ArtificialEndPortalEntity> endPortalBuilder = EntityType.Builder.of(ArtificialEndPortalEntity::new, MobCategory.MISC);
        registry.register(endPortalBuilder.sized(3.0F, 1.0F).noSummon().build("artificial_end_portal").setRegistryName("artificial_end_portal"));

        EntityType.Builder<SpectreIlluminatorEntity> illuminatorBuilder = EntityType.Builder.of(SpectreIlluminatorEntity::new, MobCategory.MISC);
        registry.register(illuminatorBuilder.sized(0.5F, 0.5F).noSummon().build("spectre_illuminator").setRegistryName("spectre_illuminator"));

        EntityType.Builder<SpiritEntity> spiritBuilder = EntityType.Builder.of(SpiritEntity::new, MobCategory.MISC);
        registry.register(spiritBuilder.sized(0.25F, 0.25F).setTrackingRange(80).setUpdateInterval(1).noSummon().build("spirit").setRegistryName("spirit"));
    }
}
