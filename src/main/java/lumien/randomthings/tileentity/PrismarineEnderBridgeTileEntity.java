package lumien.randomthings.tileentity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.BlockPos;

/**
 * The only difference from {@link EnderBridgeTileEntity}: scans 10 blocks
 * per tick instead of 1. Direct port of 1.12.2's {@code
 * TileEntityPrismarineEnderBridge}, which duplicated the entire state machine
 * for this one difference - unified into the shared base class instead.
 */
public class PrismarineEnderBridgeTileEntity extends EnderBridgeTileEntity {
    public PrismarineEnderBridgeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.PRISMARINE_ENDER_BRIDGE, pos, state);
    }

    @Override
    protected int getScansPerTick() {
        return 10;
    }
}
