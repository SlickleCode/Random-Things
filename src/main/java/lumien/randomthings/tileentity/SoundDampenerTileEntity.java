package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.util.WeakSets;
import lumien.randomthings.container.SoundDampenerContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A 9-slot inventory of {@link lumien.randomthings.item.SoundPatternItem}s;
 * every sound stamped into one of them gets muted for anyone within 20
 * blocks (checked by the client-side {@code PlaySoundEvent} listener in
 * {@code RandomThings}, which walks {@link #loadedDampeners} - same
 * weak-reference-tracked-set convention as
 * {@link RedstoneObserverTileEntity#loadedObservers}). Direct port of
 * 1.12.2's {@code TileEntitySoundDampener}.
 */
public class SoundDampenerTileEntity extends BlockEntity implements MenuProvider {
    public static final Set<SoundDampenerTileEntity> loadedDampeners = WeakSets.create();

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

    public SoundDampenerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SOUND_DAMPENER, pos, state);

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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Real bug, found 2026-09-27 (see {@code RuneBaseTileEntity} for the full
     * story): {@code TileEntity.onDataPacket} doesn't exist in this Forge
     * version - it's a Forge-added default method on {@code
     * IForgeTileEntity} whose default is an empty no-op, unlike {@code
     * handleUpdateTag}'s default (initial chunk-load sync only), which
     * already calls {@code read(tag)}. Without this override, every live
     * update sent via {@code getUpdatePacket} above silently did nothing on
     * arrival until the next relog/rejoin.
     */
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        // 1.18 delivers an empty update tag as null
        this.load(pkt.getTag() != null ? pkt.getTag() : new CompoundTag());
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private void syncTE() {
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.put("inventory", itemHandler.serializeNBT());

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        itemHandler.deserializeNBT(compound.getCompound("inventory"));
        recomputeMutedSounds();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new SoundDampenerContainer(windowId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.sound_dampener");
    }
}
