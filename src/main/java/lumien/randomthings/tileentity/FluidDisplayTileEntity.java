package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.BlockPos;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.fluids.FluidStack;
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

        CompoundTag fluidCompound = new CompoundTag();
        if (fluidStack != null) {
            fluidCompound = (CompoundTag) FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), fluidStack).result().orElse(new CompoundTag());
        }

        compound.put("fluidStack", fluidCompound);
        compound.putBoolean("flowing", flowing);
        compound.putInt("rotation", rotation.ordinal());

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("fluidStack")) {
            // an unset display is written as an empty compound; load it back as "unset" (null) rather than an
            // EMPTY stack so the NBT stays stable across save/load cycles
            FluidStack loaded = FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), compound.getCompoundOrEmpty("fluidStack")).result().orElse(FluidStack.EMPTY);
            this.fluidStack = loaded == null || loaded.isEmpty() ? null : loaded;
        } else {
            this.fluidStack = new FluidStack(Fluids.WATER, 1000);
        }

        this.flowing = compound.getBooleanOr("flowing", false);
        this.rotation = Rotation.values()[compound.getIntOr("rotation", 0)];
    }
}
