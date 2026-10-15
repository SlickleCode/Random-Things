package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

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
public class ThrownWeatherEggEntity extends net.minecraft.world.entity.projectile.ThrowableItemProjectile {
    public ThrownWeatherEggEntity(EntityType<? extends ThrownWeatherEggEntity> type, Level world) {
        super(type, world);
    }

    public ThrownWeatherEggEntity(Level world, LivingEntity thrower, Item eggItem) {
        super(ModEntityTypes.THROWN_WEATHER_EGG, thrower, world);

        this.setItem(new ItemStack(eggItem));
    }

    public ThrownWeatherEggEntity(Level world, double x, double y, double z, Item eggItem) {
        super(ModEntityTypes.THROWN_WEATHER_EGG, x, y, z, world);

        this.setItem(new ItemStack(eggItem));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WEATHER_EGG_SUN;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = ((EntityHitResult) result).getEntity();
            hitEntity.hurt(DamageSource.thrown(this, this.getOwner()), 1.0F);
        }

        if (!this.level.isClientSide) {
            WeatherCloudEntity cloud = new WeatherCloudEntity(this.level, result.getLocation().x, result.getLocation().y + 0.5, result.getLocation().z, this.getItem().getItem());
            this.level.addFreshEntity(cloud);

            this.level.broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public net.minecraft.network.protocol.Packet<?> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
}
