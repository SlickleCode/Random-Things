package lumien.randomthings.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import lumien.randomthings.item.ModItems;

/**
 * Thrown Golden Egg - unlike vanilla's egg (1-in-8 chance of hatching, no
 * damage), this one always hatches a {@link GoldenChickenEntity} on impact
 * and deals 1 damage to whatever it hits directly, matching 1.12.2's {@code
 * EntityGoldenEgg}. Rebased onto vanilla's {@code ProjectileItemEntity} for
 * the same reason as {@code ThrownWeatherEggEntity} - free client item-stack
 * sync and item-based rendering, no custom spawn data needed.
 */
public class ThrownGoldenEggEntity extends net.minecraft.world.entity.projectile.ThrowableItemProjectile {
    public ThrownGoldenEggEntity(EntityType<? extends ThrownGoldenEggEntity> type, Level world) {
        super(type, world);
    }

    public ThrownGoldenEggEntity(Level world, LivingEntity thrower) {
        super(ModEntityTypes.THROWN_GOLDEN_EGG, thrower, world);
    }

    public ThrownGoldenEggEntity(Level world, double x, double y, double z) {
        super(ModEntityTypes.THROWN_GOLDEN_EGG, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.GOLDEN_EGG;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D, ((double) this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = ((EntityHitResult) result).getEntity();
            hitEntity.hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
        }

        if (!this.level().isClientSide) {
            GoldenChickenEntity chicken = new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN, this.level());
            chicken.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            this.level().addFreshEntity(chicken);

            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
}
