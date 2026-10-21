package lumien.randomthings.handler;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

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
public class AnchorChunkLoader extends SavedData {
    private static final String ID = "randomthings_anchor_chunks";

    private final Set<Long> anchors = new HashSet<>();
    private final Set<Long> ownedChunks = new HashSet<>();

    public AnchorChunkLoader() {
    }

    public static AnchorChunkLoader loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        AnchorChunkLoader data = new AnchorChunkLoader();
        data.readData(nbt);
        return data;
    }

    private static AnchorChunkLoader get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(AnchorChunkLoader::new, AnchorChunkLoader::loadFrom, null), ID);
    }

    /** Start keeping the anchor's chunk loaded. Safe to call repeatedly for the same anchor. */
    public static void register(ServerLevel world, BlockPos pos) {
        AnchorChunkLoader data = get(world);

        if (!data.anchors.add(pos.asLong())) {
            return;
        }

        data.setDirty();

        ChunkPos chunk = new ChunkPos(pos);
        long chunkKey = chunk.toLong();

        if (!data.ownedChunks.contains(chunkKey) && !world.getForcedChunks().contains(chunkKey)) {
            data.ownedChunks.add(chunkKey);
            world.setChunkForced(chunk.x, chunk.z, true);
        }
    }

    /** Stop keeping the anchor's chunk loaded (unless another anchor still needs it). */
    public static void unregister(ServerLevel world, BlockPos pos) {
        AnchorChunkLoader data = get(world);

        if (!data.anchors.remove(pos.asLong())) {
            return;
        }

        data.setDirty();

        ChunkPos chunk = new ChunkPos(pos);

        for (long other : data.anchors) {
            if (new ChunkPos(BlockPos.of(other)).equals(chunk)) {
                return;
            }
        }

        if (data.ownedChunks.remove(chunk.toLong())) {
            world.setChunkForced(chunk.x, chunk.z, false);
        }
    }

    public void readData(CompoundTag nbt) {
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
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        compound.putLongArray("anchors", anchors.stream().mapToLong(Long::longValue).toArray());
        compound.putLongArray("ownedChunks", ownedChunks.stream().mapToLong(Long::longValue).toArray());
        return compound;
    }
}
