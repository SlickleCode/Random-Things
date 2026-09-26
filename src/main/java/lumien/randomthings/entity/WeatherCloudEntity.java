package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.network.NetworkHooks;

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
    private static final DataParameter<Integer> EGG_TYPE = EntityDataManager.createKey(WeatherCloudEntity.class, DataSerializers.VARINT);

    private int age;

    public WeatherCloudEntity(EntityType<WeatherCloudEntity> type, net.minecraft.world.World world) {
        super(type, world);

        this.noClip = true;
    }

    public WeatherCloudEntity(net.minecraft.world.World world, double x, double y, double z, Item eggItem) {
        this(ModEntityTypes.WEATHER_CLOUD, world);

        this.setPosition(x, y, z);
        this.dataManager.set(EGG_TYPE, typeIndex(eggItem));
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
    protected void registerData() {
        this.dataManager.register(EGG_TYPE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age < 200) {
            this.setMotion(this.getMotion().x, 0.007, this.getMotion().z);
        } else if (this.posY < this.world.getHeight()) {
            this.setMotion(this.getMotion().x, (this.getMotion().y + 0.001) * 1.02, this.getMotion().z);
        } else if (!this.world.isRemote) {
            int duration = (300 + new Random().nextInt(600)) * 20;
            WorldInfo info = this.world.getWorldInfo();

            int type = this.dataManager.get(EGG_TYPE);

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

            this.remove();
        }

        this.move(MoverType.SELF, this.getMotion());

        if (this.world.isRemote) {
            spawnParticles();
        }

        age++;
    }

    private void spawnParticles() {
        int type = this.dataManager.get(EGG_TYPE);

        spawnCloudShape(type == 0 ? 0.95F : 1F);

        if (type == 1) {
            for (int i = 0; i < 2; i++) {
                double t = Math.PI * 2 * Math.random();
                double a = 0.25 / (1.5 + Math.random());
                double b = 0.35 / (1.5 + Math.random());

                this.world.addParticle(ParticleTypes.BUBBLE, this.posX + a * Math.cos(t), this.posY - 0.2, this.posZ + b * Math.sin(t), 0, -0.05, 0);
            }
        } else if (type == 2) {
            double t = Math.PI * 2 * Math.random();
            double a = 0.25 / (1.5 + Math.random());
            double b = 0.35 / (1.5 + Math.random());

            this.world.addParticle(new RedstoneParticleData(1F, 1F, 0F, 1F), this.posX + a * Math.cos(t), this.posY, this.posZ + b * Math.sin(t), Math.random() * 0.1 - 0.05, Math.random() * 0.2 - 0.1, Math.random() * 0.1 - 0.05);
        }
    }

    private void spawnCloudShape(float shade) {
        for (double y = -1; y <= 1; y += 1) {
            for (double t = 0; t < Math.PI * 2; t += Math.PI / 5) {
                double a = 0.25 / (Math.abs(y) * 0.5 + 1);
                double b = 0.35 / (Math.abs(y) * 0.5 + 1);

                this.world.addParticle(ParticleTypes.CLOUD, this.posX + a * Math.cos(t), this.posY + y / 8, this.posZ + b * Math.sin(t), 0, -0.03, 0);
            }
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.dataManager.set(EGG_TYPE, compound.getInt("eggType"));
        this.age = compound.getInt("age");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("eggType", this.dataManager.get(EGG_TYPE));
        compound.putInt("age", age);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
