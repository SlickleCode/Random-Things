package lumien.randomthings.block;

import lumien.randomthings.tileentity.EnderBridgeTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class EnderBridgeBlock extends EnderBridgeBlockBase {
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnderBridgeTileEntity();
    }
}
