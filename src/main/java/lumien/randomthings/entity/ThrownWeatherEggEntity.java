package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * A thrown Weather Egg (Sun/Rain/Storm - three separate registered items,
 * matching this port's convention, rather than 1.12.2's single metadata
 * item). Direct port of 1.12.2's {@code EntityThrownWeatherEgg}, rebased
 * onto vanilla's {@code ProjectileItemEntity} (used by vanilla's own
 * {@code EggEntity}) instead of the older {@code EntityThrowable} +
 * {@code IEntityAdditionalSpawnData} combination - {@code ProjectileItemEntity}
 * already syncs its held {@link ItemStack} (egg type included) to clients on
 * its own, so no custom spawn-data packet is needed here at all.
 */
public class ThrownWeatherEggEntity extends net.minecraft.entity.projectile.ProjectileItemEntity {
    public ThrownWeatherEggEntity(EntityType<? extends ThrownWeatherEggEntity> type, World world) {
        super(type, world);
    }

    public ThrownWeatherEggEntity(World world, LivingEntity thrower, Item eggItem) {
        super(ModEntityTypes.THROWN_WEATHER_EGG, thrower, world);

        this.func_213884_b(new ItemStack(eggItem));
    }

    public ThrownWeatherEggEntity(World world, double x, double y, double z, Item eggItem) {
        super(ModEntityTypes.THROWN_WEATHER_EGG, x, y, z, world);

        this.func_213884_b(new ItemStack(eggItem));
    }

    @Override
    protected Item func_213885_i() {
        return ModItems.WEATHER_EGG_SUN;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.world.addParticle(new ItemParticleData(ParticleTypes.ITEM, this.getItem()), this.posX, this.posY, this.posZ, ((double) this.rand.nextFloat() - 0.5D) * 0.08D, ((double) this.rand.nextFloat() - 0.5D) * 0.08D, ((double) this.rand.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.getType() == RayTraceResult.Type.ENTITY) {
            Entity hitEntity = ((EntityRayTraceResult) result).getEntity();
            hitEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 1.0F);
        }

        if (!this.world.isRemote) {
            WeatherCloudEntity cloud = new WeatherCloudEntity(this.world, result.getHitVec().x, result.getHitVec().y + 0.5, result.getHitVec().z, this.getItem().getItem());
            this.world.addEntity(cloud);

            this.world.setEntityState(this, (byte) 3);
            this.remove();
        }
    }
}
