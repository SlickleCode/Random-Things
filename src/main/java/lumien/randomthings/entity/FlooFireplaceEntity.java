package lumien.randomthings.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

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

    public FlooFireplaceEntity(EntityType<FlooFireplaceEntity> type, World world) {
        super(type, world);
    }

    public FlooFireplaceEntity(World world, double x, double y, double z) {
        this(ModEntityTypes.FLOO_FIREPLACE, world);

        this.setPosition(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        age++;

        if (this.world.isRemote) {
            if (age >= 7) {
                spawnParticles();
            }
        } else if (age > 260) {
            this.remove();
        }
    }

    private void spawnParticles() {
        for (float modX = -1; modX <= 1; modX += 0.2F) {
            for (float modZ = -1; modZ <= 1; modZ += 0.2F) {
                double px = posX + modX + (this.rand.nextFloat() * 0.2 - 0.1);
                double pz = posZ + modZ + (this.rand.nextFloat() * 0.1 - 0.05);

                this.world.addParticle(ParticleTypes.FLAME, px, posY + 0.05, pz, 0, this.rand.nextDouble() * 0.01, 0);
            }
        }
    }

    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.age = compound.getInt("age");
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("age", age);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
