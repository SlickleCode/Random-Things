package lumien.randomthings.tileentity;

/**
 * A cosmetic/creative-menu-only variant with no unique behavior of its own -
 * matches 1.12.2's {@code TileEntityCreativePlayerInterface}, itself an empty
 * subclass of {@code TileEntityPlayerInterface}.
 */
public class CreativePlayerInterfaceTileEntity extends PlayerInterfaceTileEntity {
    public CreativePlayerInterfaceTileEntity() {
        super(ModTileEntityTypes.CREATIVE_PLAYER_INTERFACE);
    }
}
