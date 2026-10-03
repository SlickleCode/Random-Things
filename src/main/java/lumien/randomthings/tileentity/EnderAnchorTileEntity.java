package lumien.randomthings.tileentity;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.handler.AnchorChunkLoader;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;

/**
 * The teleport destination {@link lumien.randomthings.block.EnderBridgeBlock}
 * scans for. Direct port of 1.12.2's {@code TileEntityEnderAnchor}: on its
 * first server tick it registers with {@link AnchorChunkLoader} to keep its own
 * chunk force-loaded (so a bridge can find it with nobody nearby), or - if the
 * {@code EnderAnchorChunkloading} option is off - releases any chunk it forced
 * before. The matching release on removal is in {@code EnderAnchorBlock#onReplaced}.
 * Done on the first tick, not in {@code onLoad}, since forcing a chunk while
 * that same chunk is still loading would recurse into the chunk loader.
 */
public class EnderAnchorTileEntity extends TileEntity implements ITickableTileEntity {
    private boolean firstTick = true;

    public EnderAnchorTileEntity() {
        super(ModTileEntityTypes.ENDER_ANCHOR);
    }

    @Override
    public void tick() {
        if (!firstTick) {
            return;
        }

        firstTick = false;

        if (this.world instanceof ServerWorld) {
            if (RTConfig.ENDER_ANCHOR_CHUNKLOADING.get()) {
                AnchorChunkLoader.register((ServerWorld) this.world, this.pos);
            } else {
                AnchorChunkLoader.unregister((ServerWorld) this.world, this.pos);
            }
        }
    }
}
