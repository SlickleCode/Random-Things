package lumien.randomthings.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
@net.neoforged.api.distmarker.OnlyIn(value = net.neoforged.api.distmarker.Dist.CLIENT, _interface = ItemSupplier.class)
public class TimeAcceleratorEntity extends Entity implements ItemSupplier {
    private static final EntityDataAccessor<Integer> TIME_RATE = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.BLOCK_POS);

    private int remainingTime;

    public TimeAcceleratorEntity(EntityType<TimeAcceleratorEntity> type, Level world) {
        super(type, world);

        this.noPhysics = true;
    }

    public TimeAcceleratorEntity(Level world, BlockPos target, double x, double y, double z) {
        this(ModEntityTypes.TIME_ACCELERATOR, world);

        this.entityData.set(TARGET, target);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(TIME_RATE, 1);
        builder.define(TARGET, BlockPos.ZERO);
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getTimeRate() {
        return this.entityData.get(TIME_RATE);
    }

    public void setTimeRate(int timeRate) {
        this.entityData.set(TIME_RATE, timeRate);
    }

    public BlockPos getTarget() {
        return this.entityData.get(TARGET);
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
            BlockEntity targetTE = this.level().getBlockEntity(target);

            BlockState targetState = this.level().getBlockState(target);

            if (targetTE != null && targetState.getBlock() instanceof net.minecraft.world.level.block.EntityBlock) {
                @SuppressWarnings("unchecked")
                net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity> ticker = ((net.minecraft.world.level.block.EntityBlock) targetState.getBlock())
                        .getTicker(this.level(), targetState, (net.minecraft.world.level.block.entity.BlockEntityType<BlockEntity>) targetTE.getType());

                if (ticker != null) {
                    ticker.tick(this.level(), target, targetState, targetTE);
                }
            }

            if (this.level().random.nextInt(1365) == 0) {
                BlockState targetBlock = level().getBlockState(target);

                if (targetBlock.isRandomlyTicking()) {
                    targetBlock.randomTick((net.minecraft.server.level.ServerLevel) level(), target, level().random);
                }
            }
        }

        this.remainingTime -= 1;

        if (this.remainingTime <= 0 && !this.level().isClientSide) {
            this.discard();
        }

        if (this.level().isClientSide) {
            spawnParticles();
        }
    }

    private void spawnParticles() {
        int rate = getTimeRate();
        int count = Math.max(1, rate / 2);

        for (int i = 0; i < count; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 1.05;
            double dy = (this.random.nextDouble() - 0.5) * 1.05;
            double dz = (this.random.nextDouble() - 0.5) * 1.05;

            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT, this.getX() + dx, this.getY() + dy, this.getZ() + dz, 0, 0, 0);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(TARGET, new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ")));
        this.remainingTime = compound.getInt("remainingTime");
        setTimeRate(compound.getInt("timeRate"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        BlockPos target = getTarget();

        compound.putInt("targetX", target.getX());
        compound.putInt("targetY", target.getY());
        compound.putInt("targetZ", target.getZ());
        compound.putInt("remainingTime", remainingTime);
        compound.putInt("timeRate", getTimeRate());
    }

}
