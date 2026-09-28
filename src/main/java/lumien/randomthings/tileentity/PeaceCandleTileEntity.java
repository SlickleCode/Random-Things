package lumien.randomthings.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Direct port of 1.12.2's {@code TileEntityPeaceCandle} - a plain marker with
 * a static registry ({@link #candles}) that {@link
 * lumien.randomthings.asm.AsmHandler#overrideSpawnResult} scans, same
 * "cubes"/"shields"-style pattern already used by {@code SlimeCubeTileEntity}
 * /{@code RainShieldTileEntity}.
 */
public class PeaceCandleTileEntity extends TileEntity {
    public static final Set<PeaceCandleTileEntity> candles = Collections.newSetFromMap(new WeakHashMap<>());

    public PeaceCandleTileEntity() {
        super(ModTileEntityTypes.PEACE_CANDLE);

        candles.add(this);
    }

    /**
     * Chebyshev distance in chunk coordinates, matching the wiki's "3 Chunk
     * Radius" (a square of chunks, not a circle of blocks).
     */
    public boolean isInRange(World world, BlockPos pos, int chunkRadius) {
        if (this.world != world || this.pos == null) {
            return false;
        }

        ChunkPos ours = new ChunkPos(this.pos);
        ChunkPos theirs = new ChunkPos(pos);

        return Math.max(Math.abs(ours.x - theirs.x), Math.abs(ours.z - theirs.z)) <= chunkRadius;
    }

    @Override
    public void remove() {
        super.remove();
        candles.remove(this);
    }
}
