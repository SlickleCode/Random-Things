package lumien.randomthings.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Rotation;
import net.minecraftforge.fluids.FluidStack;

/**
 * Holds the fluid it's displaying, whether it shows the still or flowing
 * texture, and a 4-step rotation. Direct port of 1.12.2's {@code
 * TileEntityFluidDisplay} (itself a plain data holder - all rendering lived
 * in the block's custom baked model).
 */
public class FluidDisplayTileEntity extends TileEntity {
    private FluidStack fluidStack;
    private boolean flowing;
    private Rotation rotation = Rotation.NONE;

    public FluidDisplayTileEntity() {
        super(ModTileEntityTypes.FLUID_DISPLAY);
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
        rotation = rotation.add(Rotation.CLOCKWISE_90);
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

        CompoundNBT fluidCompound = new CompoundNBT();
        if (fluidStack != null) {
            fluidStack.writeToNBT(fluidCompound);
        }

        compound.put("fluidStack", fluidCompound);
        compound.putBoolean("flowing", flowing);
        compound.putInt("rotation", rotation.ordinal());

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("fluidStack")) {
            this.fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompound("fluidStack"));
        } else {
            this.fluidStack = new FluidStack(Fluids.WATER, 1000);
        }

        this.flowing = compound.getBoolean("flowing");
        this.rotation = Rotation.values()[compound.getInt("rotation")];
    }
}
