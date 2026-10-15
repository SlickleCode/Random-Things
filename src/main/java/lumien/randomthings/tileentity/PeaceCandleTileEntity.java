package lumien.randomthings.tileentity;

import net.minecraft.world.level.block.state.BlockState;

import lumien.randomthings.util.WeakSets;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Set;

/**
 * Direct port of 1.12.2's {@code TileEntityPeaceCandle} - a plain marker with
 * a static registry ({@link #candles}) that {@link
 * lumien.randomthings.asm.AsmHandler#overrideSpawnResult} scans, same
 * "cubes"/"shields"-style pattern already used by {@code SlimeCubeTileEntity}
 * /{@code RainShieldTileEntity}.
 */
public class PeaceCandleTileEntity extends BlockEntity {
    public static final Set<PeaceCandleTileEntity> candles = WeakSets.create();

    public PeaceCandleTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.PEACE_CANDLE, pos, state);

        candles.add(this);
    }

    /**
     * Chebyshev distance in chunk coordinates, matching the wiki's "3 Chunk
     * Radius" (a square of chunks, not a circle of blocks).
     */
    public boolean isInRange(Level world, BlockPos pos, int chunkRadius) {
        if (this.level != world || this.worldPosition == null) {
            return false;
        }

        ChunkPos ours = new ChunkPos(this.worldPosition);
        ChunkPos theirs = new ChunkPos(pos);

        return Math.max(Math.abs(ours.x - theirs.x), Math.abs(ours.z - theirs.z)) <= chunkRadius;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        candles.remove(this);
    }
}
