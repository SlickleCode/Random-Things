package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Direct port of 1.12.2's {@code TileEntityBlockDiaphanous}: holds which
 * block's appearance to display and whether the block currently has a real
 * (full-cube) collision box - see {@link lumien.randomthings.block.DiaphanousBlock}
 * for the collision/render-layer side and {@link
 * lumien.randomthings.client.renderer.DiaphanousBlockTileEntityRenderer} for
 * the actual ghost-fade rendering. Disclosed simplification: 1.12.2 stored
 * both a block registry name AND a numeric {@code meta} (metadata no longer
 * exists in this version - every meaningfully different vanilla block
 * variant, e.g. each wool color, is its own registry entry now) and a
 * per-face {@code renderMap} computed in {@code neighborChanged} (dropped
 * here - {@link lumien.randomthings.client.renderer.DiaphanousBlockTileEntityRenderer}
 * gets the same "don't draw a face touching a solid neighbor" culling for
 * free from {@code BlockModelRenderer.renderModel}'s own {@code checkSides}
 * parameter instead, recomputed live every frame against this TE's *real*
 * position rather than needing to be tracked and synced by hand).
 */
public class DiaphanousBlockTileEntity extends BlockEntity {
    private BlockState displayState = Blocks.STONE.defaultBlockState();
    private boolean inverted = false;

    public DiaphanousBlockTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.DIAPHANOUS_BLOCK, pos, state);
    }

    public BlockState getDisplayState() {
        return displayState;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setDisplayState(BlockState displayState, boolean inverted) {
        this.displayState = displayState;
        this.inverted = inverted;
        syncTE();
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

        compound.putString("block", RegUtil.id(displayState.getBlock()).toString());
        compound.putBoolean("inverted", inverted);

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("block")) {
            this.displayState = BuiltInRegistries.BLOCK.getValue(Identifier.parse(compound.getStringOr("block", ""))).defaultBlockState();
        } else {
            this.displayState = Blocks.STONE.defaultBlockState();
        }

        this.inverted = compound.getBooleanOr("inverted", false);
    }

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.DiaphanousBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
