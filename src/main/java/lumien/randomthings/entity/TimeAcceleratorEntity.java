package lumien.randomthings.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * An invisible, immobile marker placed by {@link
 * lumien.randomthings.item.TimeInABottleItem} that force-ticks one target
 * {@link TileEntity} extra times per game tick (up to 32x, upgraded by
 * feeding it more stored time) for a limited duration, then disappears.
 * Direct port of 1.12.2's {@code EntityTimeAccelerator}.
 * <p>
 * Disclosed simplification: dropped the original's reflection-based check
 * for {@code cofh.core.block.TileCore} (a dead Thermal Expansion/CoFH
 * compatibility hook - that mod isn't present and third-party compat is
 * already dropped project-wide) and its elaborate rotating-ring decorative
 * renderer ({@code RenderTimeAccelerator}, built on the same {@code
 * MKRRenderUtil} custom rendering framework as {@code EclipsedClockEntity}'s
 * dropped burst effect) in favor of a plain particle effect that scales with
 * the current time rate.
 */
public class TimeAcceleratorEntity extends Entity {
    private static final DataParameter<Integer> TIME_RATE = EntityDataManager.createKey(TimeAcceleratorEntity.class, DataSerializers.VARINT);

    private int remainingTime;
    private BlockPos target;

    public TimeAcceleratorEntity(EntityType<TimeAcceleratorEntity> type, World world) {
        super(type, world);

        this.noClip = true;
    }

    public TimeAcceleratorEntity(World world, BlockPos target, double x, double y, double z) {
        this(ModEntityTypes.TIME_ACCELERATOR, world);

        this.target = target;
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(TIME_RATE, 1);
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getTimeRate() {
        return this.dataManager.get(TIME_RATE);
    }

    public void setTimeRate(int timeRate) {
        this.dataManager.set(TIME_RATE, timeRate);
    }

    public BlockPos getTarget() {
        return target;
    }

    @Override
    public void tick() {
        super.tick();

        for (int i = 0; i < getTimeRate(); i++) {
            TileEntity targetTE = this.world.getTileEntity(target);

            if (targetTE instanceof ITickableTileEntity) {
                ((ITickableTileEntity) targetTE).tick();
            }

            if (this.world.rand.nextInt(1365) == 0) {
                BlockState targetBlock = world.getBlockState(target);

                if (targetBlock.ticksRandomly()) {
                    targetBlock.randomTick(world, target, world.rand);
                }
            }
        }

        this.remainingTime -= 1;

        if (this.remainingTime <= 0 && !this.world.isRemote) {
            this.remove();
        }

        if (this.world.isRemote) {
            spawnParticles();
        }
    }

    private void spawnParticles() {
        int rate = getTimeRate();
        int count = Math.max(1, rate / 2);

        for (int i = 0; i < count; i++) {
            double angle = this.rand.nextDouble() * Math.PI * 2;
            double radius = 0.3 + this.rand.nextDouble() * 0.2;

            this.world.addParticle(net.minecraft.particles.ParticleTypes.ENCHANT, this.posX + Math.cos(angle) * radius, this.posY + this.rand.nextDouble() * 0.4 - 0.2, this.posZ + Math.sin(angle) * radius, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.target = new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ"));
        this.remainingTime = compound.getInt("remainingTime");
        setTimeRate(compound.getInt("timeRate"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("targetX", target.getX());
        compound.putInt("targetY", target.getY());
        compound.putInt("targetZ", target.getZ());
        compound.putInt("remainingTime", remainingTime);
        compound.putInt("timeRate", getTimeRate());
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
