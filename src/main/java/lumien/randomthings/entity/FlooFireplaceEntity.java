package lumien.randomthings.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

/**
 * A brief (260-tick), invisible marker entity a dropped {@link
 * lumien.randomthings.item.FlooTokenItem} spawns once it's rested on the
 * ground long enough - purely decorative (a little flame-particle fireplace
 * effect at that spot) and also doubles as the "is there already one of
 * these nearby" check the token uses to avoid stacking several at once.
 * Direct port of 1.12.2's {@code EntityTemporaryFlooFireplace}, with its
 * custom {@code ParticleFlooFlame} class dropped in favor of vanilla's own
 * {@code ParticleTypes.FLAME} - visually the same rising-flame-flicker
 * effect, and this port already has precedent (see {@code
 * PotionVaporizerTileEntity}) for preferring a plain vanilla particle over
 * porting a whole bespoke one where the two look the same.
 */
public class FlooFireplaceEntity extends Entity {
    private int age;

    public FlooFireplaceEntity(EntityType<FlooFireplaceEntity> type, Level world) {
        super(type, world);
    }

    public FlooFireplaceEntity(Level world, double x, double y, double z) {
        this(ModEntityTypes.FLOO_FIREPLACE, world);

        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        age++;

        if (this.level().isClientSide) {
            if (age >= 7) {
                spawnParticles();
            }
        } else if (age > 260) {
            this.discard();
        }
    }

    private void spawnParticles() {
        for (float modX = -1; modX <= 1; modX += 0.2F) {
            for (float modZ = -1; modZ <= 1; modZ += 0.2F) {
                double px = getX() + modX + (this.random.nextFloat() * 0.2 - 0.1);
                double pz = getZ() + modZ + (this.random.nextFloat() * 0.1 - 0.05);

                this.level().addParticle(ParticleTypes.FLAME, px, getY() + 0.05, pz, 0, this.random.nextDouble() * 0.01, 0);
            }
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.age = compound.getInt("age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("age", age);
    }

}
