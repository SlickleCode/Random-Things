package lumien.randomthings.entity;

import java.util.Random;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Mob;

/**
 * A tiny, near-invulnerable flying wisp with a small chance to appear at the
 * death location of any entity a player kills (see {@link
 * lumien.randomthings.RandomThings}'s {@code LivingDeathEvent} listener for
 * the actual spawn-chance roll) - drifts aimlessly near its spawn point for
 * a while, then despawns; drops Ectoplasm if killed with a Spectre Sword (the
 * only weapon that can actually hurt it). Direct port of 1.12.2's {@code
 * EntitySpirit}.
 */
public class SpiritEntity extends Mob {
    private final Random rng = new Random();

    private BlockPos spawnPosition;
    private int changePositionCounter;
    private int spiritAge;

    public SpiritEntity(EntityType<? extends SpiritEntity> type, Level world) {
        super(type, world);

        this.moveControl = new SpiritMovementController(this);
    }

    public SpiritEntity(Level world, double x, double y, double z) {
        this(ModEntityTypes.SPIRIT, world);

        this.setPos(x, y, z);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.readAdditionalSaveData(rtIn);
    	rtReadAdditionalSaveData(lumien.randomthings.util.TagIO.read(rtIn));
    }

    private void rtReadAdditionalSaveData(CompoundTag compound) {

        this.spiritAge = compound.getIntOr("spiritAge", 0);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.addAdditionalSaveData(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtAddAdditionalSaveData(rtTag);
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtAddAdditionalSaveData(CompoundTag compound) {

        compound.putInt("spiritAge", spiritAge);
    }

    // what FlyingMob did (removed in 1.21.5): no fall damage, not climbable, travels like a Ghast
    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        this.travelFlying(travelVector, 0.02F);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        spiritAge++;

        if (!this.level().isClientSide() && spiritAge > lumien.randomthings.config.RTConfig.SPIRIT_LIFETIME.get()) {
            this.kill((net.minecraft.server.level.ServerLevel) this.level());
        }
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() != null) {
            if (source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK) && source.getEntity() instanceof Player) {
                Player player = (Player) source.getEntity();
                ItemStack equipped = player.getMainHandItem();

                if (!equipped.isEmpty() && equipped.getItem() == ModItems.SPECTRE_SWORD) {
                    return super.hurtServer(level, source, amount);
                }
            }
        }

        if (!source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC) && !source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC) && !source.is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD) && !source.isCreativePlayer()) {
            return false;
        }

        return super.hurtServer(level, source, amount);
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1.0);
    }

    @Override
    public void knockback(double strength, double xRatio, double zRatio) {
    }

    @Override
    protected void doPush(Entity entityIn) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void customServerAiStep(net.minecraft.server.level.ServerLevel level) {
        super.customServerAiStep(level);

        if (spawnPosition == null) {
            this.spawnPosition = this.blockPosition();
        }

        changePositionCounter++;

        if (changePositionCounter >= 60) {
            changePositionCounter = 0;

            int tries = 0;
            BlockPos newTarget = null;

            while (newTarget == null && tries < 10) {
                tries++;

                int modX = rng.nextInt(5) - 2;
                int modY = rng.nextInt(3);
                int modZ = rng.nextInt(5) - 2;

                BlockPos modPos = spawnPosition.offset(modX, modY, modZ);

                if (level().isEmptyBlock(modPos)) {
                    newTarget = modPos;
                }
            }

            if (newTarget != null) {
                this.moveControl.setWantedPosition(newTarget.getX(), newTarget.getY(), newTarget.getZ(), 0.02);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.server.level.ServerLevel level, DamageSource source, boolean wasRecentlyHit) {
        if (!wasRecentlyHit) {
            return;
        }

        int count = 1;

        if (random.nextInt(5) == 0) {
            count++;
        }

        for (int i = 0; i < count; i++) {
            this.spawnAtLocation((net.minecraft.server.level.ServerLevel) this.level(), ModItems.ECTOPLASM);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide() && (this.xOld != this.getX() || this.yOld != this.getY() || this.zOld != this.getZ())) {
            if (Math.random() < 0.5) {
                this.level().addParticle(ParticleTypes.ENCHANT, this.getX(), this.getY(), this.getZ(), Math.random() * 0.02 - 0.01, Math.random() * 0.02, Math.random() * 0.02 - 0.01);
            }
        }
    }

    private static class SpiritMovementController extends MoveControl {
        private final SpiritEntity parentEntity;

        SpiritMovementController(SpiritEntity parentEntity) {
            super(parentEntity);

            this.parentEntity = parentEntity;
        }

        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                double d0 = this.wantedX - this.parentEntity.getX();
                double d1 = this.wantedY - this.parentEntity.getY();
                double d2 = this.wantedZ - this.parentEntity.getZ();
                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                if (this.isNotColliding(this.wantedX, this.wantedY, this.wantedZ, d3) && d3 > 0.2) {
                    this.parentEntity.setDeltaMovement(this.parentEntity.getDeltaMovement().add(d0 / d3 * 0.01D, d1 / d3 * 0.01D, d2 / d3 * 0.01D));
                } else {
                    this.operation = Operation.WAIT;
                }
            }
        }

        private boolean isNotColliding(double targetX, double targetY, double targetZ, double distance) {
            double stepX = (targetX - this.parentEntity.getX()) / distance;
            double stepY = (targetY - this.parentEntity.getY()) / distance;
            double stepZ = (targetZ - this.parentEntity.getZ()) / distance;
            AABB aabb = this.parentEntity.getBoundingBox();

            for (int i = 1; i < distance; ++i) {
                aabb = aabb.move(stepX, stepY, stepZ);

                if (!this.parentEntity.level().noCollision(this.parentEntity, aabb)) {
                    return false;
                }
            }

            return true;
        }
    }
}
