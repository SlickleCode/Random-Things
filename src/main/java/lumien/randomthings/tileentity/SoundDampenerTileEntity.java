package lumien.randomthings.tileentity;

import lumien.randomthings.container.SoundDampenerContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A 9-slot inventory of {@link lumien.randomthings.item.SoundPatternItem}s;
 * every sound stamped into one of them gets muted for anyone within 20
 * blocks (checked by the client-side {@code PlaySoundEvent} listener in
 * {@code RandomThings}, which walks {@link #loadedDampeners} - same
 * weak-reference-tracked-set convention as
 * {@link RedstoneObserverTileEntity#loadedObservers}). Direct port of
 * 1.12.2's {@code TileEntitySoundDampener}.
 */
public class SoundDampenerTileEntity extends TileEntity implements INamedContainerProvider {
    public static final Set<SoundDampenerTileEntity> loadedDampeners = Collections.newSetFromMap(new WeakHashMap<>());

    private final ItemStackHandler itemHandler = new ItemStackHandler(9) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == ModItems.SOUND_PATTERN;
        }

        @Override
        protected void onContentsChanged(int slot) {
            recomputeMutedSounds();
            syncTE();
        }
    };

    private HashSet<ResourceLocation> mutedSounds = new HashSet<>();

    public SoundDampenerTileEntity() {
        super(ModTileEntityTypes.SOUND_DAMPENER);

        loadedDampeners.add(this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public HashSet<ResourceLocation> getMutedSounds() {
        return mutedSounds;
    }

    private void recomputeMutedSounds() {
        mutedSounds = new HashSet<>();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            ResourceLocation sound = SoundPatternItem.getSoundLocation(stack);

            if (sound != null) {
                mutedSounds.add(sound);
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    /**
     * {@code TileEntity.getUpdateTag()}'s default implementation calls the
     * *private* {@code writeInternal()} directly (confirmed via `javap -c`),
     * not the public, overridable {@code write()} - so without this override
     * the sync packet above would carry only the base id/position, never
     * this TE's own inventory. See `RedstoneObserverTileEntity`/
     * `BiomeRadarTileEntity` for the full story on where this was found.
     */
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    private void syncTE() {
        this.markDirty();

        if (this.world != null) {
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.put("inventory", itemHandler.serializeNBT());

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        itemHandler.deserializeNBT(compound.getCompound("inventory"));
        recomputeMutedSounds();
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SoundDampenerContainer(windowId, playerInventory, this);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.sound_dampener");
    }
}
