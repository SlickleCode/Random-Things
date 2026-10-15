package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.core.BlockPos;

import net.minecraft.world.level.Level;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.EnderBridgeTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;

public class EnderBridgeBlock extends EnderBridgeBlockBase implements EntityBlock {
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new EnderBridgeTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.ENDER_BRIDGE, (l, p, s2, be) -> be.tick());
    }
}
