package lumien.randomthings.entity;

import java.util.Random;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * A tiny, near-invulnerable flying wisp with a small chance to appear at the
 * death location of any entity a player kills (see {@link
 * lumien.randomthings.RandomThings}'s {@code LivingDeathEvent} listener for
 * the actual spawn-chance roll) - drifts aimlessly near its spawn point for
 * a while, then despawns; drops Ectoplasm if killed with a Spectre Sword (the
 * only weapon that can actually hurt it). Direct port of 1.12.2's {@code
 * EntitySpirit}.
 */
public class SpiritEntity extends FlyingEntity {
    private final Random rng = new Random();

    private BlockPos spawnPosition;
    private int changePositionCounter;
    private int spiritAge;

    public SpiritEntity(EntityType<? extends SpiritEntity> type, World world) {
        super(type, world);

        this.moveController = new SpiritMovementController(this);
    }

    public SpiritEntity(World world, double x, double y, double z) {
        this(ModEntityTypes.SPIRIT, world);

        this.setPosition(x, y, z);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);

        this.spiritAge = compound.getInt("spiritAge");
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);

        compound.putInt("spiritAge", spiritAge);
    }

    @Override
    public void livingTick() {
        super.livingTick();

        spiritAge++;

        if (!this.world.isRemote && spiritAge > lumien.randomthings.config.RTConfig.SPIRIT_LIFETIME.get()) {
            this.onKillCommand();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source instanceof EntityDamageSource) {
            EntityDamageSource eds = (EntityDamageSource) source;

            if (eds.getDamageType().equals("player") && eds.getTrueSource() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) eds.getTrueSource();
                ItemStack equipped = player.getHeldItemMainhand();

                if (!equipped.isEmpty() && equipped.getItem() == ModItems.SPECTRE_SWORD) {
                    return super.attackEntityFrom(source, amount);
                }
            }
        }

        if (!source.isMagicDamage() && source != DamageSource.OUT_OF_WORLD && !source.isCreativePlayer()) {
            return false;
        }

        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();

        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0);
    }

    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (spawnPosition == null) {
            this.spawnPosition = this.getPosition();
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

                BlockPos modPos = spawnPosition.add(modX, modY, modZ);

                if (world.isAirBlock(modPos)) {
                    newTarget = modPos;
                }
            }

            if (newTarget != null) {
                this.moveController.setMoveTo(newTarget.getX(), newTarget.getY(), newTarget.getZ(), 0.02);
            }
        }
    }

    @Override
    protected void dropSpecialItems(DamageSource source, int lootingModifier, boolean wasRecentlyHit) {
        if (!wasRecentlyHit) {
            return;
        }

        int count = 1;

        if (rand.nextInt(5) == 0) {
            count++;
        }

        for (int i = 0; i < count; i++) {
            this.entityDropItem(ModItems.ECTOPLASM);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isRemote && (this.lastTickPosX != this.posX || this.lastTickPosY != this.posY || this.lastTickPosZ != this.posZ)) {
            if (Math.random() < 0.5) {
                this.world.addParticle(ParticleTypes.ENCHANT, this.posX, this.posY, this.posZ, Math.random() * 0.02 - 0.01, Math.random() * 0.02, Math.random() * 0.02 - 0.01);
            }
        }
    }

    private static class SpiritMovementController extends MovementController {
        private final SpiritEntity parentEntity;

        SpiritMovementController(SpiritEntity parentEntity) {
            super(parentEntity);

            this.parentEntity = parentEntity;
        }

        @Override
        public void tick() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                if (this.isNotColliding(this.posX, this.posY, this.posZ, d3) && d3 > 0.2) {
                    this.parentEntity.setMotion(this.parentEntity.getMotion().add(d0 / d3 * 0.01D, d1 / d3 * 0.01D, d2 / d3 * 0.01D));
                } else {
                    this.action = Action.WAIT;
                }
            }
        }

        private boolean isNotColliding(double targetX, double targetY, double targetZ, double distance) {
            double stepX = (targetX - this.parentEntity.posX) / distance;
            double stepY = (targetY - this.parentEntity.posY) / distance;
            double stepZ = (targetZ - this.parentEntity.posZ) / distance;
            AxisAlignedBB aabb = this.parentEntity.getBoundingBox();

            for (int i = 1; i < distance; ++i) {
                aabb = aabb.offset(stepX, stepY, stepZ);

                if (!this.parentEntity.world.isCollisionBoxesEmpty(this.parentEntity, aabb)) {
                    return false;
                }
            }

            return true;
        }
    }
}
