package lumien.randomthings.block;

import lumien.randomthings.tileentity.PrismarineEnderBridgeTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

/**
 * A faster reskin of {@link EnderBridgeBlock} - scans 10 blocks per tick
 * instead of 1 (see {@link PrismarineEnderBridgeTileEntity}). Direct port of
 * 1.12.2's {@code BlockPrismarineEnderBridge}.
 */
public class PrismarineEnderBridgeBlock extends EnderBridgeBlockBase {
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PrismarineEnderBridgeTileEntity();
    }
}
