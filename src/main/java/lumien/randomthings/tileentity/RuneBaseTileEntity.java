package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
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

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.saveAdditional(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        byte[] flat = new byte[16];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                flat[x * 4 + y] = (byte) (runeData[x][y] != null ? runeData[x][y].getId() : -1);
            }
        }

        compound.putByteArray("runeData", flat);

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("runeData")) {
            byte[] flat = compound.getByteArray("runeData").orElse(new byte[0]);

            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    byte id = flat[x * 4 + y];
                    runeData[x][y] = id >= 0 ? DyeColor.byId(id) : null;
                }
            }
        }
    }

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.RuneBaseBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
