package lumien.randomthings.block;

import lumien.randomthings.tileentity.EnderAnchorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

/**
 * Direct port of 1.12.2's {@code BlockEnderAnchor}. Its original {@code
 * breakBlock} override just released the tile entity's optional chunk-loading
 * ticket - not ported since the ticket request itself isn't (see {@link
 * lumien.randomthings.tileentity.EnderBridgeTileEntity}'s javadoc).
 */
public class EnderAnchorBlock extends Block {
    public EnderAnchorBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnderAnchorTileEntity();
    }
}
