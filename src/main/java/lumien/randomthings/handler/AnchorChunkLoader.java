package lumien.randomthings.handler;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Keeps each Ender Anchor's chunk force-loaded, replacing 1.12.2's per-anchor
 * {@code ForgeChunkManager} ticket (see {@code EnderAnchorTileEntity}). 1.14.4's
 * only persistent equivalent is {@link ServerWorld#forceChunk} - the same
 * mechanism as {@code /forceload}, saved with the world and reloaded on startup,
 * so no restart callback is needed.
 * <p>
 * Per-dimension saved data tracks (a) every registered anchor position and (b)
 * the chunks <i>this mod</i> forced. Registering is idempotent (anchors re-register on
 * their first tick after every world load), a chunk is released only when its last anchor
 * goes, and a chunk that was already {@code /forceload}ed when the first anchor
 * arrived is never recorded as ours, so breaking the anchor won't unload it.
 */
public class AnchorChunkLoader extends WorldSavedData {
    private static final String ID = "randomthings_anchor_chunks";

    private final Set<Long> anchors = new HashSet<>();
    private final Set<Long> ownedChunks = new HashSet<>();

    public AnchorChunkLoader() {
        super(ID);
    }

    private static AnchorChunkLoader get(ServerWorld world) {
        return world.getSavedData().getOrCreate(AnchorChunkLoader::new, ID);
    }

    /** Start keeping the anchor's chunk loaded. Safe to call repeatedly for the same anchor. */
    public static void register(ServerWorld world, BlockPos pos) {
        AnchorChunkLoader data = get(world);

        if (!data.anchors.add(pos.toLong())) {
            return;
        }

        data.markDirty();

        ChunkPos chunk = new ChunkPos(pos);
        long chunkKey = chunk.asLong();

        if (!data.ownedChunks.contains(chunkKey) && !world.getForcedChunks().contains(chunkKey)) {
            data.ownedChunks.add(chunkKey);
            world.forceChunk(chunk.x, chunk.z, true);
        }
    }

    /** Stop keeping the anchor's chunk loaded (unless another anchor still needs it). */
    public static void unregister(ServerWorld world, BlockPos pos) {
        AnchorChunkLoader data = get(world);

        if (!data.anchors.remove(pos.toLong())) {
            return;
        }

        data.markDirty();

        ChunkPos chunk = new ChunkPos(pos);

        for (long other : data.anchors) {
            if (new ChunkPos(BlockPos.fromLong(other)).equals(chunk)) {
                return;
            }
        }

        if (data.ownedChunks.remove(chunk.asLong())) {
            world.forceChunk(chunk.x, chunk.z, false);
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        anchors.clear();
        ownedChunks.clear();

        for (long l : nbt.getLongArray("anchors")) {
            anchors.add(l);
        }

        for (long l : nbt.getLongArray("ownedChunks")) {
            ownedChunks.add(l);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLongArray("anchors", anchors.stream().mapToLong(Long::longValue).toArray());
        compound.putLongArray("ownedChunks", ownedChunks.stream().mapToLong(Long::longValue).toArray());
        return compound;
    }
}
