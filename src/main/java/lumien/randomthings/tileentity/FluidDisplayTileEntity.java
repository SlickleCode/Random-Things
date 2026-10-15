package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds the fluid it's displaying, whether it shows the still or flowing
 * texture, and a 4-step rotation. Direct port of 1.12.2's {@code
 * TileEntityFluidDisplay} (itself a plain data holder - all rendering lived
 * in the block's custom baked model).
 */
public class FluidDisplayTileEntity extends BlockEntity {
    private FluidStack fluidStack;
    private boolean flowing;
    private Rotation rotation = Rotation.NONE;

    public FluidDisplayTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.FLUID_DISPLAY, pos, state);
    }

    public FluidStack getFluidStack() {
        return fluidStack;
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public boolean flowing() {
        return flowing;
    }

    public void toggleFlowing() {
        flowing = !flowing;
        syncTE();
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void cycleRotation() {
        rotation = rotation.getRotated(Rotation.CLOCKWISE_90);
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

    /**
     * Real bug, found 2026-09-27 (see {@code RuneBaseTileEntity}'s identical
     * fix for the full story): {@code TileEntity.onDataPacket} doesn't exist
     * in this Forge version - it's a Forge-added default method on {@code
     * IForgeTileEntity} whose default is an empty no-op, unlike {@code
     * handleUpdateTag}'s default (initial chunk-load sync only), which
     * already calls {@code read(tag)}. Without this override, every live
     * update sent via {@code getUpdatePacket} above (rotation/flowing toggle)
     * silently did nothing on arrival until the next relog/rejoin.
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

        CompoundTag fluidCompound = new CompoundTag();
        if (fluidStack != null) {
            fluidStack.writeToNBT(fluidCompound);
        }

        compound.put("fluidStack", fluidCompound);
        compound.putBoolean("flowing", flowing);
        compound.putInt("rotation", rotation.ordinal());

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("fluidStack")) {
            // an unset display is written as an empty compound; load it back as "unset" (null) rather than an
            // EMPTY stack so the NBT stays stable across save/load cycles
            FluidStack loaded = FluidStack.loadFluidStackFromNBT(compound.getCompound("fluidStack"));
            this.fluidStack = loaded == null || loaded.isEmpty() ? null : loaded;
        } else {
            this.fluidStack = new FluidStack(Fluids.WATER, 1000);
        }

        this.flowing = compound.getBoolean("flowing");
        this.rotation = Rotation.values()[compound.getInt("rotation")];
    }
}
