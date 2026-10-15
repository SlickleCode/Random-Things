package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

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

        compound.putString("block", displayState.getBlock().getRegistryName().toString());
        compound.putBoolean("inverted", inverted);

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("block")) {
            this.displayState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString("block"))).defaultBlockState();
        } else {
            this.displayState = Blocks.STONE.defaultBlockState();
        }

        this.inverted = compound.getBoolean("inverted");
    }
}
