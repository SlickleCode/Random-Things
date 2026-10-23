package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.Random;

/**
 * Rises from where a {@link ThrownWeatherEggEntity} lands, then floats up to
 * the world's height limit and sets the weather to match its egg type (Sun =
 * clear skies, Rain, or Storm = rain + thunder) for 300-900 real seconds.
 * Direct port of 1.12.2's {@code EntityWeatherCloud}.
 * <p>
 * Disclosed simplification: the original's decorative particles used a
 * custom tinted-smoke particle class ({@code EntityColoredSmokeFX}) for the
 * Storm/Sun variants. This substitutes the already-established
 * {@code RedstoneParticleData} (a tintable dust particle - see {@code
 * PotionVaporizerTileEntity}/{@code FlooNetworkHandler} elsewhere in this
 * port) instead of porting a whole separate custom particle type, and
 * additionally drops the Sun variant's Tessellator-drawn sunburst overlay
 * ({@code RenderWeatherCloud}, SUN-only, purely decorative on top of the
 * particle cloud) - the particle cloud itself already carries the visual
 * identity, kept faithfully.
 */
public class WeatherCloudEntity extends Entity {
    private static final EntityDataAccessor<Integer> EGG_TYPE = SynchedEntityData.defineId(WeatherCloudEntity.class, EntityDataSerializers.INT);

    private int age;

    public WeatherCloudEntity(EntityType<WeatherCloudEntity> type, net.minecraft.world.level.Level world) {
        super(type, world);

        this.noPhysics = true;
    }

    public WeatherCloudEntity(net.minecraft.world.level.Level world, double x, double y, double z, Item eggItem) {
        this(ModEntityTypes.WEATHER_CLOUD, world);

        this.setPos(x, y, z);
        this.entityData.set(EGG_TYPE, typeIndex(eggItem));
    }

    private static int typeIndex(Item eggItem) {
        if (eggItem == ModItems.WEATHER_EGG_RAIN) {
            return 1;
        } else if (eggItem == ModItems.WEATHER_EGG_STORM) {
            return 2;
        }

        return 0;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(EGG_TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < 200) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.007, this.getDeltaMovement().z);
        } else if (this.getY() < this.level().getMaxY() + 1) {
            this.setDeltaMovement(this.getDeltaMovement().x, (this.getDeltaMovement().y + 0.001) * 1.02, this.getDeltaMovement().z);
        } else if (!this.level().isClientSide) {
            int duration = (300 + new Random().nextInt(600)) * 20;
            ServerLevelData info = (ServerLevelData) this.level().getLevelData();

            int type = this.entityData.get(EGG_TYPE);

            if (type == 1) {
                info.setClearWeatherTime(0);
                info.setRainTime(duration);
                info.setThunderTime(duration);
                info.setRaining(true);
                info.setThundering(false);
            } else if (type == 2) {
                info.setClearWeatherTime(0);
                info.setRainTime(duration);
                info.setThunderTime(duration);
                info.setRaining(true);
                info.setThundering(true);
            } else {
                info.setClearWeatherTime(duration);
                info.setRainTime(0);
                info.setThunderTime(0);
                info.setRaining(false);
                info.setThundering(false);
            }

            this.discard();
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.level().isClientSide) {
            spawnParticles();
        }

        age++;
    }

    private void spawnParticles() {
        int type = this.entityData.get(EGG_TYPE);

        spawnCloudShape(type == 0 ? 0.95F : 1F);

        if (type == 1) {
            for (int i = 0; i < 2; i++) {
                double t = Math.PI * 2 * Math.random();
                double a = 0.25 / (1.5 + Math.random());
                double b = 0.35 / (1.5 + Math.random());

                this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + a * Math.cos(t), this.getY() - 0.2, this.getZ() + b * Math.sin(t), 0, -0.05, 0);
            }
        } else if (type == 2) {
            double t = Math.PI * 2 * Math.random();
            double a = 0.25 / (1.5 + Math.random());
            double b = 0.35 / (1.5 + Math.random());

            this.level().addParticle(new DustParticleOptions(net.minecraft.util.ARGB.colorFromFloat(1F, 1F, 1F, 0F), 1F), this.getX() + a * Math.cos(t), this.getY(), this.getZ() + b * Math.sin(t), Math.random() * 0.1 - 0.05, Math.random() * 0.2 - 0.1, Math.random() * 0.1 - 0.05);
        }
    }

    private void spawnCloudShape(float shade) {
        for (double y = -1; y <= 1; y += 1) {
            for (double t = 0; t < Math.PI * 2; t += Math.PI / 5) {
                double a = 0.25 / (Math.abs(y) * 0.5 + 1);
                double b = 0.35 / (Math.abs(y) * 0.5 + 1);

                this.level().addParticle(ParticleTypes.CLOUD, this.getX() + a * Math.cos(t), this.getY() + y / 8, this.getZ() + b * Math.sin(t), 0, -0.03, 0);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(EGG_TYPE, compound.getInt("eggType"));
        this.age = compound.getInt("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("eggType", this.entityData.get(EGG_TYPE));
        compound.putInt("age", age);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
}
