package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.core.BlockPos;

import lumien.randomthings.tileentity.CreativePlayerInterfaceTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * A cosmetic/creative-menu-only variant of {@link PlayerInterfaceBlock} - no
 * survival crafting recipe existed for it in 1.12.2 either. Matches 1.12.2's
 * {@code BlockCreativePlayerInterface}, which likewise only overrode {@code
 * createTileEntity} on top of the plain interface block.
 */
public class CreativePlayerInterfaceBlock extends PlayerInterfaceBlock implements EntityBlock {
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new CreativePlayerInterfaceTileEntity(pos, state);
    }
}
