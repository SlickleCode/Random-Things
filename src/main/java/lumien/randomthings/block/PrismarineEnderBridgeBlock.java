package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.core.BlockPos;

import lumien.randomthings.tileentity.PrismarineEnderBridgeTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;

/**
 * A faster reskin of {@link EnderBridgeBlock} - scans 10 blocks per tick
 * instead of 1 (see {@link PrismarineEnderBridgeTileEntity}). Direct port of
 * 1.12.2's {@code BlockPrismarineEnderBridge}.
 */
public class PrismarineEnderBridgeBlock extends EnderBridgeBlockBase implements EntityBlock {
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new PrismarineEnderBridgeTileEntity(pos, state);
    }
}
