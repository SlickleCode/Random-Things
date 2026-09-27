package lumien.randomthings.block;

import lumien.randomthings.tileentity.CreativePlayerInterfaceTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A cosmetic/creative-menu-only variant of {@link PlayerInterfaceBlock} - no
 * survival crafting recipe existed for it in 1.12.2 either. Matches 1.12.2's
 * {@code BlockCreativePlayerInterface}, which likewise only overrode {@code
 * createTileEntity} on top of the plain interface block.
 */
public class CreativePlayerInterfaceBlock extends PlayerInterfaceBlock {
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CreativePlayerInterfaceTileEntity();
    }
}
