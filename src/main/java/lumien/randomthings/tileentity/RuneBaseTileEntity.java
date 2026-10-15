package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A 4x4 grid of colored rune pixels ({@code null} = empty). Direct port of
 * 1.12.2's {@code TileEntityRuneBase}, storing {@link DyeColor} directly
 * instead of a raw int index into a 1.12.2-era ordinal enum.
 */
public class RuneBaseTileEntity extends BlockEntity {
    private final DyeColor[][] runeData = new DyeColor[4][4];

    public RuneBaseTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.RUNE_BASE, pos, state);
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
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        // 1.18 delivers an empty update tag as null
        this.load(pkt.getTag() != null ? pkt.getTag() : new CompoundTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        byte[] flat = new byte[16];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                flat[x * 4 + y] = (byte) (runeData[x][y] != null ? runeData[x][y].getId() : -1);
            }
        }

        compound.putByteArray("runeData", flat);

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

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
