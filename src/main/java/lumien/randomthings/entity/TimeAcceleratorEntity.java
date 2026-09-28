package lumien.randomthings.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.item.ItemStack;
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
 * An immobile marker placed by {@link
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
 * the current time rate, now spread across the whole target block's volume
 * (see {@link #spawnParticles()}) rather than a tight radius around one
 * point. Originally shipped with no visible model at all on top of that
 * (matching how {@link SpectreIlluminatorEntity} originally shipped) - per
 * user request, 2026-09-27, now implements {@link IRendersAsItem} so {@link
 * lumien.randomthings.client.renderer.TimeAcceleratorEntityRenderer} can
 * draw the bottle icon as a billboard on all 6 faces of the target block,
 * close to each face's own surface, rather than the single fixed-position
 * billboard vanilla's own {@code SpriteRenderer} (used for a brief first
 * pass) can only place at one spot.
 * <p>
 * Real crash found and fixed, 2026-09-27 (reported by user): {@code target}
 * was a plain field, set only by the position-taking constructor (used
 * server-side when {@code TimeInABottleItem} spawns this). The client
 * constructs its own copy via the other constructor (the one {@code
 * EntityType.Builder}'s factory always uses for the spawn packet), which
 * never set it - so it stayed {@code null} client-side, and {@code tick()}
 * calling {@code world.getTileEntity(null)} unconditionally (not gated to
 * server-only) NPE'd immediately, crashing the client. 1.12.2's version
 * avoided this via {@code IEntityAdditionalSpawnData}'s {@code
 * writeSpawnData}/{@code readSpawnData}, explicitly sending {@code target}
 * as part of the spawn packet - dropped when porting with nothing to
 * replace it. Fixed the same way this port already solved the identical
 * problem for {@link EclipsedClockEntity}'s {@code HANGING_POS}: a real
 * synced {@code DataParameter<BlockPos>} instead of a plain field.
 */
public class TimeAcceleratorEntity extends Entity implements IRendersAsItem {
    private static final DataParameter<Integer> TIME_RATE = EntityDataManager.createKey(TimeAcceleratorEntity.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> TARGET = EntityDataManager.createKey(TimeAcceleratorEntity.class, DataSerializers.BLOCK_POS);

    private int remainingTime;

    public TimeAcceleratorEntity(EntityType<TimeAcceleratorEntity> type, World world) {
        super(type, world);

        this.noClip = true;
    }

    public TimeAcceleratorEntity(World world, BlockPos target, double x, double y, double z) {
        this(ModEntityTypes.TIME_ACCELERATOR, world);

        this.dataManager.set(TARGET, target);
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(TIME_RATE, 1);
        this.dataManager.register(TARGET, BlockPos.ZERO);
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
        return this.dataManager.get(TARGET);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(lumien.randomthings.item.ModItems.TIME_IN_A_BOTTLE);
    }

    @Override
    public void tick() {
        super.tick();

        BlockPos target = getTarget();

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
            double dx = (this.rand.nextDouble() - 0.5) * 1.05;
            double dy = (this.rand.nextDouble() - 0.5) * 1.05;
            double dz = (this.rand.nextDouble() - 0.5) * 1.05;

            this.world.addParticle(net.minecraft.particles.ParticleTypes.ENCHANT, this.posX + dx, this.posY + dy, this.posZ + dz, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.dataManager.set(TARGET, new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ")));
        this.remainingTime = compound.getInt("remainingTime");
        setTimeRate(compound.getInt("timeRate"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        BlockPos target = getTarget();

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
