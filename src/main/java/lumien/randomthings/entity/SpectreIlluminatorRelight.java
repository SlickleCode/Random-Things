package lumien.randomthings.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Drains {@link SpectreIlluminatorEntity#relight()}'s work over many ticks
 * instead of all at once. Real bug found and fixed, 2026-09-27 (reported by
 * user: crashed the client with an {@code ArrayIndexOutOfBoundsException} in
 * {@code SectionLightStorage.cancelSectionUpdates}/{@code
 * LevelBasedGraph.bulkCancel}): calling {@code WorldLightManager#checkBlock}
 * for every position in an 18x18x256 column (~83,000 calls) synchronously in
 * one method call - matching 1.12.2's own exhaustive sweep byte-for-byte -
 * overwhelms this Forge version's rewritten light engine, which schedules
 * updates into a fixed-capacity internal structure rather than processing
 * them immediately the way the old (pre-1.14) engine did. 1.12.2 never hit
 * this because its lighting engine was architecturally different.
 * <p>
 * Fixed two ways: (1) the scanned height range is now bounded to just above
 * the chunk's own tallest block instead of the full 0-255 world height
 * (most of that range is empty air no build ever reaches, and touching it
 * means allocating/touching far more chunk sections than actually matter);
 * (2) the remaining positions are queued here and drained a bounded number
 * per tick (see the {@code WorldTickEvent}/{@code ClientTickEvent} listeners
 * in {@code RandomThings}'s constructor - both needed, since the client and
 * server are separate {@code World} instances even in singleplayer, and
 * only draining server-side left the client stuck showing stale light until
 * a relog forced a full chunk resync - another real bug, also found and
 * fixed 2026-09-27) instead of all in one burst, matching how real vanilla
 * code never enqueues anywhere near this many light updates in a single
 * tick either. See {@link #PADDING}'s own javadoc for a third real bug
 * (found the same day) in how far this padded scan needs to reach.
 * <p>
 * Real bug found and fixed, 2026-09-28 (reported by user: many-illuminator
 * worlds still stall the whole server for multiple seconds - "Can't keep up!
 * ... 44 ticks behind" - right when a batch of them settle/load together,
 * e.g. on world join): the per-tick draining above fixed the *processing*
 * cost, but {@link #queue(World, BlockPos)} itself still built the entire
 * padded column as real materialized {@code BlockPos} objects - up to ~185,000
 * {@code new BlockPos(...)} allocations plus queue insertions in one
 * synchronous call, done once per illuminator. A world-join burst of just
 * 14 illuminators settling within the same second (an ordinary amount for a
 * "lit up the whole base" setup, and exactly what the reported log showed)
 * is therefore ~2 million object allocations dumped into one or two ticks -
 * that allocation/GC burst, not the bounded per-tick drain, is what actually
 * blocked the server thread. Fixed by replacing the materialized {@code
 * Queue<BlockPos>} with a queue of lightweight {@link Job} bounds
 * descriptors (six {@code int}s each) - {@link #queue} now only computes and
 * stores those bounds (O(1) allocation, no loop), and {@link #tick} lazily
 * generates the next {@code PER_TICK} {@code BlockPos} values on demand from
 * whichever job is at the front of the queue, saving its cursor for the next
 * tick. Total positions checked, their order, and the per-tick rate are all
 * unchanged - this only moves *when* each position's {@code BlockPos} gets
 * allocated, from "all up front" to "the tick it's actually used."
 */
public class SpectreIlluminatorRelight {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Briefly split into separate server/client rates (server-side {@code
     * checkBlock} calls are pure light-data bookkeeping, no rendering
     * involved, so a much higher rate is free there than on the client, where
     * each one that actually changes a value can trigger a chunk render-mesh
     * rebuild) - reverted to one shared rate per user request, 2026-09-27,
     * for consistency, then raised from 48 to 256 per a follow-up user
     * request the same day. Tune down further if the client is choppy at this
     * rate (spreads the same total work over more ticks, smoother frame times
     * but slower to finish); tune up for a faster finish at the cost of
     * smoothness. Still ~325x below the ~83,000-positions-in-one-synchronous-
     * call threshold that caused the original crash, so no regression risk.
     */
    private static final int PER_TICK = 256;

    private static final int HEIGHT_BUFFER = 8;

    /**
     * Real bug found and fixed, 2026-09-27 (reported by user: after picking the
     * orb back up, part of the chunk - right where it originally settled and
     * started forcing light 14 - stayed stuck lit instead of going dark). The
     * override only forces light 14 for positions *inside* the illuminated
     * chunk, but vanilla's own light propagation doesn't stop at the chunk
     * boundary - once a boundary cell computes 14, its normal neighbor-cascade
     * (confirmed via {@code javap -c} on {@code LightEngine.checkLight}, which
     * reschedules a checked position's 6 neighbors too) keeps carrying reduced
     * values outward for up to 15 more blocks, vanilla's own max light-level
     * falloff distance. The old 1-block pad only re-checked far enough to catch
     * the override itself, not everywhere that bled light might have reached -
     * so positions just past that 1-block boundary could still be relying on a
     * stale, still-bright value that nothing ever told to re-derive. Padding by
     * the full 15-block falloff distance instead guarantees every position that
     * could possibly have received bled light also gets re-derived.
     */
    private static final int PADDING = 15;

    private static final Map<World, WorldQueue> PENDING = new WeakHashMap<>();

    public static void queue(World world, BlockPos center) {
        Chunk chunk = world.getChunk(center.getX() >> 4, center.getZ() >> 4);

        int highest = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING, x, z) + 1;

                if (height > highest) {
                    highest = height;
                }
            }
        }

        int maxY = Math.min(255, highest + HEIGHT_BUFFER);

        ChunkPos chunkPos = new ChunkPos(center);
        Job job = new Job(chunkPos.getXStart() - PADDING, chunkPos.getXEnd() + PADDING, chunkPos.getZStart() - PADDING, chunkPos.getZEnd() + PADDING, maxY);

        WorldQueue queue = PENDING.computeIfAbsent(world, w -> new WorldQueue());
        long before = queue.totalRemaining;
        queue.jobs.add(job);
        queue.totalRemaining += job.remaining();

        LOGGER.info("[SpectreIlluminator] queued {} relight positions for chunk {} (world.isRemote={}, maxY={}, queue size {} -> {})", job.remaining(), chunkPos, world.isRemote, maxY, before, queue.totalRemaining);
    }

    public static void tick(World world) {
        WorldQueue queue = PENDING.get(world);

        if (queue == null || queue.jobs.isEmpty()) {
            return;
        }

        int drained = 0;

        for (int i = 0; i < PER_TICK; i++) {
            Job job = queue.jobs.peek();

            if (job == null) {
                break;
            }

            world.getChunkProvider().getLightManager().checkBlock(job.next());
            drained++;
            queue.totalRemaining--;

            if (job.isDone()) {
                queue.jobs.poll();
            }
        }

        if (queue.jobs.isEmpty()) {
            LOGGER.info("[SpectreIlluminator] relight queue for world {} drained (drained {} this tick)", world.getDimension().getType(), drained);
        }
    }

    /** A world's pending relight jobs, plus a running total kept for logging (see {@link #queue}). */
    private static final class WorldQueue {
        final Deque<Job> jobs = new ArrayDeque<>();
        long totalRemaining;
    }

    /**
     * A padded column's bounds plus a cursor tracking how far {@link #tick}
     * has worked through it - replaces what used to be millions of
     * pre-allocated {@code BlockPos} objects sitting in a queue at once (see
     * this class's own javadoc for why). {@code next()} walks the same x/z/y
     * nesting order the old eager loop used, generating one {@code BlockPos}
     * at a time only when it's actually about to be checked.
     */
    private static final class Job {
        private final int xMin, xMax, zMin, zMax, yMax;
        private int x, z, y;

        Job(int xMin, int xMax, int zMin, int zMax, int yMax) {
            this.xMin = xMin;
            this.xMax = xMax;
            this.zMin = zMin;
            this.zMax = zMax;
            this.yMax = yMax;
            this.x = xMin;
            this.z = zMin;
            this.y = 0;
        }

        int remaining() {
            long width = (long) (xMax - xMin + 1) * (zMax - zMin + 1) * (yMax + 1);
            long done = (long) (x - xMin) * (zMax - zMin + 1) * (yMax + 1) + (long) (z - zMin) * (yMax + 1) + y;
            return (int) Math.max(0, width - done);
        }

        boolean isDone() {
            return x > xMax;
        }

        BlockPos next() {
            BlockPos pos = new BlockPos(x, y, z);

            y++;
            if (y > yMax) {
                y = 0;
                z++;
                if (z > zMax) {
                    z = zMin;
                    x++;
                }
            }

            return pos;
        }
    }
}
