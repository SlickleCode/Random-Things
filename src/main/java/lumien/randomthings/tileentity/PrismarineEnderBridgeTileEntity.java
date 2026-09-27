package lumien.randomthings.tileentity;

/**
 * The only difference from {@link EnderBridgeTileEntity}: scans 10 blocks
 * per tick instead of 1. Direct port of 1.12.2's {@code
 * TileEntityPrismarineEnderBridge}, which duplicated the entire state machine
 * for this one difference - unified into the shared base class instead.
 */
public class PrismarineEnderBridgeTileEntity extends EnderBridgeTileEntity {
    public PrismarineEnderBridgeTileEntity() {
        super(ModTileEntityTypes.PRISMARINE_ENDER_BRIDGE);
    }

    @Override
    protected int getScansPerTick() {
        return 10;
    }
}
