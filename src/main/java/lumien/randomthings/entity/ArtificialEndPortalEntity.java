package lumien.randomthings.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkHooks;

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
    private static final DataParameter<Integer> ACTION_TIMER = EntityDataManager.createKey(ArtificialEndPortalEntity.class, DataSerializers.VARINT);

    public ArtificialEndPortalEntity(EntityType<? extends ArtificialEndPortalEntity> type, World world) {
        super(type, world);
    }

    public ArtificialEndPortalEntity(World world, double x, double y, double z) {
        this(ModEntityTypes.ARTIFICIAL_END_PORTAL, world);
        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(ACTION_TIMER, 0);
    }

    public int getActionTimer() {
        return this.dataManager.get(ACTION_TIMER);
    }

    @Override
    public void tick() {
        super.tick();

        int actionTimer = getActionTimer();

        if (actionTimer < 200) {
            actionTimer++;
            this.dataManager.set(ACTION_TIMER, actionTimer);

            if (this.world.isRemote && actionTimer > 40) {
                spawnParticles();
            }
        }

        if (!this.world.isRemote) {
            if (this.world.getGameTime() % 40 == 0) {
                if (!isValidPosition(this.world, new BlockPos(this.posX, this.posY, this.posZ), false)) {
                    this.remove();
                }
            }
        } else {
            if (actionTimer == 85) {
                this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.2F, 1, false);
            }
        }
    }

    private void spawnParticles() {
        for (int i = 0; i < 5; i++) {
            double modX = this.rand.nextFloat() * 0.05 - 0.025;
            double modZ = this.rand.nextFloat() * 0.05 - 0.025;

            this.world.addParticle(ParticleTypes.ENCHANT, this.posX + modX, this.posY + 2, this.posZ + modZ, modX * 2, 1, modZ * 2);
        }
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        super.onCollideWithPlayer(entityIn);

        if (!this.world.isRemote && getActionTimer() >= 200 && entityIn.getBoundingBox().intersects(this.getBoundingBox()) && !entityIn.isPassenger() && !entityIn.isBeingRidden()) {
            entityIn.changeDimension(DimensionType.THE_END);
        }
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.dataManager.set(ACTION_TIMER, compound.getInt("actionTimer"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putInt("actionTimer", getActionTimer());
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static boolean isValidPosition(World world, BlockPos center, boolean checkForOtherPortals) {
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!world.isAirBlock(center.add(modX, 0, modZ))) {
                    return false;
                }
            }
        }

        for (int modY = 1; modY < 3; modY++) {
            if (!world.isAirBlock(center.add(0, modY, 0))) {
                return false;
            }
        }

        if (world.getBlockState(center.add(0, 3, 0)).getBlock() != Blocks.END_ROD || world.getBlockState(center.add(0, 4, 0)).getBlock() != Blocks.END_STONE) {
            return false;
        }

        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (world.getBlockState(center.add(modX, -1, modZ)).getBlock() != Blocks.END_STONE) {
                    return false;
                }
            }
        }

        for (int modX = -2; modX < 3; modX++) {
            for (int modZ = -2; modZ < 3; modZ++) {
                if (modX == -2 || modZ == -2 || modX == 2 || modZ == 2) {
                    if (world.getBlockState(center.add(modX, 0, modZ)).getBlock() != Blocks.OBSIDIAN) {
                        return false;
                    }
                }
            }
        }

        if (checkForOtherPortals) {
            List<ArtificialEndPortalEntity> portalList = world.getEntitiesWithinAABB(ArtificialEndPortalEntity.class, new AxisAlignedBB(center, center.add(1, 2, 1)));

            if (!portalList.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
