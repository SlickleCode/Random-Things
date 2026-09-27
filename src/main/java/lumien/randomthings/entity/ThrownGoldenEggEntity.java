package lumien.randomthings.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import lumien.randomthings.item.ModItems;

/**
 * Thrown Golden Egg - unlike vanilla's egg (1-in-8 chance of hatching, no
 * damage), this one always hatches a {@link GoldenChickenEntity} on impact
 * and deals 1 damage to whatever it hits directly, matching 1.12.2's {@code
 * EntityGoldenEgg}. Rebased onto vanilla's {@code ProjectileItemEntity} for
 * the same reason as {@code ThrownWeatherEggEntity} - free client item-stack
 * sync and item-based rendering, no custom spawn data needed.
 */
public class ThrownGoldenEggEntity extends net.minecraft.entity.projectile.ProjectileItemEntity {
    public ThrownGoldenEggEntity(EntityType<? extends ThrownGoldenEggEntity> type, World world) {
        super(type, world);
    }

    public ThrownGoldenEggEntity(World world, LivingEntity thrower) {
        super(ModEntityTypes.THROWN_GOLDEN_EGG, thrower, world);
    }

    public ThrownGoldenEggEntity(World world, double x, double y, double z) {
        super(ModEntityTypes.THROWN_GOLDEN_EGG, x, y, z, world);
    }

    @Override
    protected Item func_213885_i() {
        return ModItems.GOLDEN_EGG;
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
            GoldenChickenEntity chicken = new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN, this.world);
            chicken.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
            this.world.addEntity(chicken);

            this.world.setEntityState(this, (byte) 3);
            this.remove();
        }
    }
}
