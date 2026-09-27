package lumien.randomthings.tileentity;

import net.minecraft.tileentity.TileEntity;

/**
 * The teleport destination {@link lumien.randomthings.block.EnderBridgeBlock}
 * scans for - a plain marker block with no behavior of its own. Direct port
 * of 1.12.2's {@code TileEntityEnderAnchor}, minus its optional Forge
 * chunk-loading ticket request (see {@link EnderBridgeTileEntity}'s javadoc
 * for why that's not ported).
 */
public class EnderAnchorTileEntity extends TileEntity {
    public EnderAnchorTileEntity() {
        super(ModTileEntityTypes.ENDER_ANCHOR);
    }
}
