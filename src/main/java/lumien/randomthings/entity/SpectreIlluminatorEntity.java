package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

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
public class SpectreIlluminatorEntity extends Entity implements IRendersAsItem {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final Set<SpectreIlluminatorEntity> ILLUMINATORS = Collections.newSetFromMap(new WeakHashMap<>());

    private static final DataParameter<Boolean> ILLUMINATED = EntityDataManager.createKey(SpectreIlluminatorEntity.class, DataSerializers.BOOLEAN);

    public SpectreIlluminatorEntity(EntityType<? extends SpectreIlluminatorEntity> type, World world) {
        super(type, world);

        this.noClip = true;

        ILLUMINATORS.add(this);
    }

    public SpectreIlluminatorEntity(World world, double x, double y, double z) {
        this(ModEntityTypes.SPECTRE_ILLUMINATOR, world);

        this.setPosition(x, y, z);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(ILLUMINATED, false);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (this.world.isRemote && key.equals(ILLUMINATED) && this.dataManager.get(ILLUMINATED)) {
            LOGGER.info("[SpectreIlluminator] entity {} learned illuminated=true on the client, relighting", this.getEntityId());
            relight();
        }
    }

    @Override
    public void onRemovedFromWorld() {
        boolean wasIlluminated = isIlluminated();

        super.onRemovedFromWorld();

        ILLUMINATORS.remove(this);

        LOGGER.info("[SpectreIlluminator] entity {} removed from world (remote={}, wasIlluminated={})", this.getEntityId(), this.world.isRemote, wasIlluminated);

        if (wasIlluminated) {
            relight();
        }
    }

    public boolean isIlluminated() {
        return this.dataManager.get(ILLUMINATED);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ModItems.SPECTRE_ILLUMINATOR);
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand) {
        if (!this.world.isRemote) {
            this.remove();

            this.entityDropItem(new net.minecraft.item.ItemStack(ModItems.SPECTRE_ILLUMINATOR), 0.0F);
        }

        return true;
    }

    public boolean isInChunk(World world, ChunkPos chunkPos) {
        if (!isIlluminated() || this.world != world) {
            return false;
        }

        BlockPos p = this.getPosition();
        return (p.getX() >> 4) == chunkPos.x && (p.getZ() >> 4) == chunkPos.z;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isRemote) {
            Chunk chunk = this.world.getChunk(((int) this.posX) >> 4, ((int) this.posZ) >> 4);

            int highestBlockInChunk = 0;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING, x, z) + 1;

                    if (height > highestBlockInChunk) {
                        highestBlockInChunk = height;
                    }
                }
            }

            if (Math.abs(highestBlockInChunk - this.posY) > 0.05) {
                this.setMotion(this.getMotion().x, highestBlockInChunk < this.posY ? -0.05 : 0.05, this.getMotion().z);
            } else {
                this.posY = highestBlockInChunk;
                this.setMotion(this.getMotion().x, 0, this.getMotion().z);
            }

            ChunkPos chunkPos = new ChunkPos(this.getPosition());

            double chunkX = (chunkPos.getXStart() + chunkPos.getXEnd()) / 2D;
            double chunkZ = (chunkPos.getZStart() + chunkPos.getZEnd()) / 2D;

            if (Math.abs(posX - chunkX) > 0.03) {
                this.setMotion(chunkX > posX ? 0.03 : -0.03, this.getMotion().y, this.getMotion().z);
            } else {
                this.posX = chunkX;
                this.setMotion(0, this.getMotion().y, this.getMotion().z);
            }

            if (Math.abs(posZ - chunkZ) > 0.03) {
                this.setMotion(this.getMotion().x, this.getMotion().y, chunkZ > posZ ? 0.03 : -0.03);
            } else {
                this.posZ = chunkZ;
                this.setMotion(this.getMotion().x, this.getMotion().y, 0);
            }

            if (!isIlluminated()) {
                if (this.world.getGameTime() % 100 == 0) {
                    LOGGER.info("[SpectreIlluminator] entity {} settling: pos=({}, {}, {}) target=({}, {}, {}) highest={}", this.getEntityId(), posX, posY, posZ, chunkX, highestBlockInChunk, chunkZ, highestBlockInChunk);
                }

                if (posX == chunkX && posZ == chunkZ) {
                    LOGGER.info("[SpectreIlluminator] entity {} settled at ({}, {}, {}), illuminating chunk {}", this.getEntityId(), posX, posY, posZ, chunkPos);

                    this.dataManager.set(ILLUMINATED, true);

                    relight();
                }
            }
        } else if (isIlluminated()) {
            if (this.world.getGameTime() % 5 == 0) {
                this.world.addParticle(ParticleTypes.END_ROD, this.posX + (this.rand.nextDouble() - 0.5) * 0.3, this.posY + this.rand.nextDouble() * 0.3, this.posZ + (this.rand.nextDouble() - 0.5) * 0.3, 0, 0.01, 0);
            }
        }

        this.move(MoverType.SELF, this.getMotion());
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
        SpectreIlluminatorRelight.queue(this.world, this.getPosition());
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.dataManager.set(ILLUMINATED, compound.getBoolean("illuminated"));
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putBoolean("illuminated", isIlluminated());
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
