package lumien.randomthings.entity;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.List;

/**
 * A ritual-built portal: place End Rod + End Stone + Obsidian in the exact
 * arrangement {@link #isValidPosition} checks for, then right-click the End
 * Rod with an Evil Tear ({@link lumien.randomthings.item.EvilTearItem}) to
 * spawn this. After a ~10-second (200-tick) charge-up it lets any player who
 * touches it travel to the End, and de-spawns if its structure gets broken.
 * Direct port of 1.12.2's {@code EntityArtificialEndPortal}.
 * <p>
 * Disclosed simplification: the original tinted each charge-up particle a
 * random shade of purple via a direct {@code Particle.setRBGColorF} call;
 * {@code ParticleTypes.ENCHANT} (the same enchanting-table sparkle family the
 * original built on) doesn't expose per-particle color from {@code
 * World.addParticle}, so this uses the particle's own default look instead.
 */
public class ArtificialEndPortalEntity extends Entity {
    private static final EntityDataAccessor<Integer> ACTION_TIMER = SynchedEntityData.defineId(ArtificialEndPortalEntity.class, EntityDataSerializers.INT);

    public ArtificialEndPortalEntity(EntityType<? extends ArtificialEndPortalEntity> type, Level world) {
        super(type, world);
    }

    public ArtificialEndPortalEntity(Level world, double x, double y, double z) {
        this(ModEntityTypes.ARTIFICIAL_END_PORTAL, world);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        builder.define(ACTION_TIMER, 0);
    }

    public int getActionTimer() {
        return this.entityData.get(ACTION_TIMER);
    }

    @Override
    public void tick() {
        super.tick();

        int actionTimer = getActionTimer();

        if (actionTimer < 200) {
            actionTimer++;
            this.entityData.set(ACTION_TIMER, actionTimer);

            if (this.level().isClientSide && actionTimer > 40) {
                spawnParticles();
            }
        }

        if (!this.level().isClientSide) {
            if (this.level().getGameTime() % 40 == 0) {
                if (!isValidPosition(this.level(), BlockPos.containing(this.getX(), this.getY(), this.getZ()), false)) {
                    this.discard();
                }
            }
        } else {
            if (actionTimer == 85) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.2F, 1, false);
            }
        }
    }

    private void spawnParticles() {
        for (int i = 0; i < 5; i++) {
            double modX = this.random.nextFloat() * 0.05 - 0.025;
            double modZ = this.random.nextFloat() * 0.05 - 0.025;

            this.level().addParticle(ParticleTypes.ENCHANT, this.getX() + modX, this.getY() + 2, this.getZ() + modZ, modX * 2, 1, modZ * 2);
        }
    }

    @Override
    public void playerTouch(Player entityIn) {
        super.playerTouch(entityIn);

        if (!this.level().isClientSide && getActionTimer() >= 200 && entityIn.getBoundingBox().intersects(this.getBoundingBox()) && !entityIn.isPassenger() && !entityIn.isVehicle()) {
            entityIn.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(this.level().getServer().getLevel(Level.END), entityIn, net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(ACTION_TIMER, compound.getInt("actionTimer"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("actionTimer", getActionTimer());
    }


    public static boolean isValidPosition(Level world, BlockPos center, boolean checkForOtherPortals) {
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!world.isEmptyBlock(center.offset(modX, 0, modZ))) {
                    return false;
                }
            }
        }

        for (int modY = 1; modY < 3; modY++) {
            if (!world.isEmptyBlock(center.offset(0, modY, 0))) {
                return false;
            }
        }

        if (world.getBlockState(center.offset(0, 3, 0)).getBlock() != Blocks.END_ROD || world.getBlockState(center.offset(0, 4, 0)).getBlock() != Blocks.END_STONE) {
            return false;
        }

        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (world.getBlockState(center.offset(modX, -1, modZ)).getBlock() != Blocks.END_STONE) {
                    return false;
                }
            }
        }

        for (int modX = -2; modX < 3; modX++) {
            for (int modZ = -2; modZ < 3; modZ++) {
                if (modX == -2 || modZ == -2 || modX == 2 || modZ == 2) {
                    if (world.getBlockState(center.offset(modX, 0, modZ)).getBlock() != Blocks.OBSIDIAN) {
                        return false;
                    }
                }
            }
        }

        if (checkForOtherPortals) {
            List<ArtificialEndPortalEntity> portalList = world.getEntitiesOfClass(ArtificialEndPortalEntity.class, lumien.randomthings.util.AABBs.of(center, center.offset(1, 2, 1)));

            if (!portalList.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
