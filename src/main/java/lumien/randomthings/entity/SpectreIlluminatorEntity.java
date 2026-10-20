package lumien.randomthings.entity;

import lumien.randomthings.util.WeakSets;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.network.NetworkHooks;

import java.util.Set;
import net.minecraft.world.InteractionResult;

/**
 * A floating orb, placed by right-clicking with a {@link
 * lumien.randomthings.item.SpectreIlluminatorItem}, that drifts to hover
 * above the tallest block in its chunk and centers itself there, then makes
 * every block in that chunk report as lit. Right-click it to collect it back
 * (turning the light off). Direct port of 1.12.2's {@code
 * EntitySpectreIlluminator}.
 * <p>
 * Disclosed simplifications from 1.12.2's version:
 * <ul>
 * <li>1.12.2 tracked "is this chunk lit" as a separate persisted {@code
 * WorldSavedData} registry, mirrored to clients via its own network message
 * so the client's independent light rendering could see it too. Collapsed
 * here into the same static-registry-of-live-instances pattern this port
 * already uses for {@link lumien.randomthings.tileentity.SlimeCubeTileEntity}
 * /{@link lumien.randomthings.tileentity.RainShieldTileEntity} ({@link
 * #ILLUMINATORS}, scanned by {@link
 * lumien.randomthings.asm.AsmHandler#overrideLightValue}) - the entity's own
 * ordinary position sync already gives the client everything it needs, so no
 * separate persisted flag or custom packet is needed at all.</li>
 * <li>1.12.2's light override ASM-patched {@code Block.getLightValue(state,
 * world, pos)} directly. That 3-argument overload no longer exists in this
 * Forge version - {@code Block.getLightValue} now takes only a {@code
 * BlockState} (confirmed via {@code javap -p}). Ground-truthed via {@code
 * javap -c} where block-light computation actually reads a light value in
 * this version instead: {@code BlockLightEngine.getLightValue(long)} calls
 * {@code IBlockReader.getLightValue(BlockPos)}, a default interface method
 * ({@code getBlockState(pos).getLightValue()}) inherited by every {@code
 * IBlockReader} implementor ({@code Chunk}, {@code World}, ...) with no
 * override of its own - patched there instead, once, at that one shared
 * choke point.</li>
 * <li>1.12.2's magic-circle visual ({@code RenderSpectreIlluminator}) was
 * built on this mod's bespoke immediate-mode-GL {@code MKRRenderUtil}
 * framework (not otherwise needed anywhere in this port - see {@link
 * EclipsedClockEntity}'s own disclosed simplification for the same
 * framework). Replaced with a plain vanilla particle effect instead. This
 * entity originally shipped with no visible model at all (matching 1.12.2's
 * own renderer, whose {@code getEntityTexture} also returned {@code null})
 * - per explicit user request, 2026-09-27, now also implements {@link
 * IRendersAsItem} and renders via vanilla's own {@code SpriteRenderer} (the
 * same billboard-icon mechanism already used for this port's {@link
 * ThrownGoldenEggEntity}/{@link ThrownWeatherEggEntity}), so the item icon
 * itself is now the visible marker - since it already tracks this entity's
 * real position every frame, it automatically drifts from the clicked spot
 * to the chunk center along with the existing centering logic below, with
 * no extra animation code needed.</li>
 * </ul>
 * <p>
 * Real bug found and fixed, 2026-09-27 (reported by user: no visual, no
 * lighting): {@code illuminated} being a plain field synced only through
 * {@code readAdditional}/{@code writeAdditional} (save/load NBT) meant the
 * client's own copy of this entity never learned the server had set it -
 * the entity spawn packet doesn't carry that data. It's a real {@code
 * DataParameter} now. That alone still isn't enough, though - ground-
 * truthing 1.12.2's real client-side handling (its own network message
 * handler, {@code SpectreIlluminationClientHandler#setIlluminated}) shows
 * it *also* explicitly re-ran the light-recheck helper client-side upon
 * receiving the toggle, exactly mirroring the server's own call - learning
 * the boolean alone was never enough there either. {@link #relight()} is
 * now called from both sides: the server's own existing call, plus {@link
 * #notifyDataManagerChange} reacting to the synced value arriving on the
 * client. {@link #onRemovedFromWorld()} relights too (it's a generic Entity
 * lifecycle hook, not server-only) so picking the orb back up turns the
 * chunk dark again instead of leaving it stuck lit - the original 1.12.2
 * code paired this symmetrically (its {@code toggleChunk} always called the
 * recheck helper for both the on and off transition) but this port's first
 * pass only handled the on case.
 * <p>
 * Real crash found and fixed, 2026-09-27, same day (reported by user):
 * {@link #relight()} calling {@code checkBlock} for all ~83,000 positions in
 * its column synchronously - matching 1.12.2's own exhaustive sweep - itself
 * crashed the client with an {@code ArrayIndexOutOfBoundsException} deep in
 * this version's rewritten light engine. See {@link SpectreIlluminatorRelight}
 * for the fix (bounded per-tick draining instead of one big burst).
 */
@net.minecraftforge.api.distmarker.OnlyIn(value = net.minecraftforge.api.distmarker.Dist.CLIENT, _interface = ItemSupplier.class)
public class SpectreIlluminatorEntity extends Entity implements ItemSupplier {

    public static final Set<SpectreIlluminatorEntity> ILLUMINATORS = WeakSets.create();

    private static final EntityDataAccessor<Boolean> ILLUMINATED = SynchedEntityData.defineId(SpectreIlluminatorEntity.class, EntityDataSerializers.BOOLEAN);

    public SpectreIlluminatorEntity(EntityType<? extends SpectreIlluminatorEntity> type, Level world) {
        super(type, world);

        this.noPhysics = true;

        ILLUMINATORS.add(this);
    }

    public SpectreIlluminatorEntity(Level world, double x, double y, double z) {
        this(ModEntityTypes.SPECTRE_ILLUMINATOR, world);

        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ILLUMINATED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);

        if (this.level().isClientSide && key.equals(ILLUMINATED) && this.entityData.get(ILLUMINATED)) {
            RTDebug.log("[SpectreIlluminator] entity {} learned illuminated=true on the client, relighting", this.getId());
            relight();
        }
    }

    @Override
    public void onRemovedFromWorld() {
        boolean wasIlluminated = isIlluminated();

        super.onRemovedFromWorld();

        ILLUMINATORS.remove(this);

        RTDebug.log("[SpectreIlluminator] entity {} removed from world (remote={}, wasIlluminated={})", this.getId(), this.level().isClientSide, wasIlluminated);

        if (wasIlluminated) {
            relight();
        }
    }

    public boolean isIlluminated() {
        return this.entityData.get(ILLUMINATED);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.SPECTRE_ILLUMINATOR);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            this.discard();

            this.spawnAtLocation(new net.minecraft.world.item.ItemStack(ModItems.SPECTRE_ILLUMINATOR), 0.0F);
        }

        return InteractionResult.SUCCESS;
    }

    public boolean isInChunk(Level world, ChunkPos chunkPos) {
        if (!isIlluminated() || this.level() != world) {
            return false;
        }

        BlockPos p = this.blockPosition();
        return (p.getX() >> 4) == chunkPos.x && (p.getZ() >> 4) == chunkPos.z;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            LevelChunk chunk = this.level().getChunk(((int) this.getX()) >> 4, ((int) this.getZ()) >> 4);

            int highestBlockInChunk = 0;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1;

                    if (height > highestBlockInChunk) {
                        highestBlockInChunk = height;
                    }
                }
            }

            if (Math.abs(highestBlockInChunk - this.getY()) > 0.05) {
                this.setDeltaMovement(this.getDeltaMovement().x, highestBlockInChunk < this.getY() ? -0.05 : 0.05, this.getDeltaMovement().z);
            } else {
                this.setPosRaw(this.getX(), highestBlockInChunk, this.getZ());
                this.setDeltaMovement(this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
            }

            ChunkPos chunkPos = new ChunkPos(this.blockPosition());

            double chunkX = (chunkPos.getMinBlockX() + chunkPos.getMaxBlockX()) / 2D;
            double chunkZ = (chunkPos.getMinBlockZ() + chunkPos.getMaxBlockZ()) / 2D;

            if (Math.abs(getX() - chunkX) > 0.03) {
                this.setDeltaMovement(chunkX > getX() ? 0.03 : -0.03, this.getDeltaMovement().y, this.getDeltaMovement().z);
            } else {
                this.setPosRaw(chunkX, this.getY(), this.getZ());
                this.setDeltaMovement(0, this.getDeltaMovement().y, this.getDeltaMovement().z);
            }

            if (Math.abs(getZ() - chunkZ) > 0.03) {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, chunkZ > getZ() ? 0.03 : -0.03);
            } else {
                this.setPosRaw(this.getX(), this.getY(), chunkZ);
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, 0);
            }

            if (!isIlluminated()) {
                if (this.level().getGameTime() % 100 == 0) {
                    RTDebug.log("[SpectreIlluminator] entity {} settling: pos=({}, {}, {}) target=({}, {}, {}) highest={}", this.getId(), getX(), getY(), getZ(), chunkX, highestBlockInChunk, chunkZ, highestBlockInChunk);
                }

                if (getX() == chunkX && getZ() == chunkZ) {
                    RTDebug.log("[SpectreIlluminator] entity {} settled at ({}, {}, {}), illuminating chunk {}", this.getId(), getX(), getY(), getZ(), chunkPos);

                    this.entityData.set(ILLUMINATED, true);

                    relight();
                }
            }
        } else if (isIlluminated()) {
            if (this.level().getGameTime() % 5 == 0) {
                this.level().addParticle(ParticleTypes.END_ROD, this.getX() + (this.random.nextDouble() - 0.5) * 0.3, this.getY() + this.random.nextDouble() * 0.3, this.getZ() + (this.random.nextDouble() - 0.5) * 0.3, 0, 0.01, 0);
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    /**
     * Queues the light engine to re-check every position this chunk's own
     * newly-changed light value could affect, matching 1.12.2's {@code
     * SpectreIlluminationHelper.lightUpdateChunk} ({@code
     * world.checkLightFor(EnumSkyBlock.BLOCK, pos)} for every position in a
     * padded column over the chunk). See {@link SpectreIlluminatorRelight}'s
     * own javadoc for why this is queued and drained gradually rather than
     * done synchronously here (the way 1.12.2, and this port's own first
     * attempt, did it), and for how wide that padding actually needs to be.
     */
    private void relight() {
        SpectreIlluminatorRelight.queue(this.level(), this.blockPosition());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(ILLUMINATED, compound.getBoolean("illuminated"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putBoolean("illuminated", isIlluminated());
    }

    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
