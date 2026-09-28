package lumien.randomthings.tileentity;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

/**
 * A 4x4 grid of colored rune pixels ({@code null} = empty). Direct port of
 * 1.12.2's {@code TileEntityRuneBase}, storing {@link DyeColor} directly
 * instead of a raw int index into a 1.12.2-era ordinal enum.
 */
public class RuneBaseTileEntity extends TileEntity {
    private final DyeColor[][] runeData = new DyeColor[4][4];

    public RuneBaseTileEntity() {
        super(ModTileEntityTypes.RUNE_BASE);
    }

    public DyeColor[][] getRuneData() {
        return runeData;
    }

    public void setRuneData(DyeColor[][] newData) {
        for (int x = 0; x < 4; x++) {
            System.arraycopy(newData[x], 0, this.runeData[x], 0, 4);
        }
    }

    public boolean isEmpty() {
        for (DyeColor[] row : runeData) {
            for (DyeColor color : row) {
                if (color != null) {
                    return false;
                }
            }
        }

        return true;
    }

    public void syncTE() {
        this.markDirty();

        if (this.world != null) {
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    /**
     * Real bug, found 2026-09-27: without this override, a live update (this
     * tile entity's own {@link #getUpdatePacket}, sent via {@code
     * syncTE}/{@code notifyBlockUpdate}) silently did nothing on arrival.
     * {@code TileEntity}'s own {@code onDataPacket} doesn't exist in this
     * Forge version at all - it's a Forge-added default method on {@code
     * IForgeTileEntity}, and that default is an EMPTY no-op (confirmed by
     * reading its source), unlike {@code handleUpdateTag}'s default (used
     * only for the initial chunk-load sync, e.g. on rejoin), which already
     * calls {@code read(tag)} by default. That's exactly why a freshly
     * placed/modified pixel didn't render until a relog - relog goes through
     * {@code handleUpdateTag} (works by default), but a live update while
     * already in the world goes through {@code onDataPacket} (silently did
     * nothing without this override).
     */
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        byte[] flat = new byte[16];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                flat[x * 4 + y] = (byte) (runeData[x][y] != null ? runeData[x][y].getId() : -1);
            }
        }

        compound.putByteArray("runeData", flat);

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("runeData")) {
            byte[] flat = compound.getByteArray("runeData");

            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    byte id = flat[x * 4 + y];
                    runeData[x][y] = id >= 0 ? DyeColor.byId(id) : null;
                }
            }
        }
    }
}
