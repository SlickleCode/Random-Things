package lumien.randomthings.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
public class DiaphanousBlockTileEntity extends TileEntity {
    private BlockState displayState = Blocks.STONE.getDefaultState();
    private boolean inverted = false;

    public DiaphanousBlockTileEntity() {
        super(ModTileEntityTypes.DIAPHANOUS_BLOCK);
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

        compound.putString("block", displayState.getBlock().getRegistryName().toString());
        compound.putBoolean("inverted", inverted);

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("block")) {
            this.displayState = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString("block"))).getDefaultState();
        } else {
            this.displayState = Blocks.STONE.getDefaultState();
        }

        this.inverted = compound.getBoolean("inverted");
    }
}
