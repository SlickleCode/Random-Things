package lumien.randomthings.tileentity;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
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
