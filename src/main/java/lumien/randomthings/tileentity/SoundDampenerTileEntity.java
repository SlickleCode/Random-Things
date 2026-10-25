package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
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
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import lumien.randomthings.itemhandler.ItemStackHandler;

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

    private HashSet<Identifier> mutedSounds = new HashSet<>();

    public SoundDampenerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SOUND_DAMPENER, pos, state);

        loadedDampeners.add(this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public HashSet<Identifier> getMutedSounds() {
        return mutedSounds;
    }

    private void recomputeMutedSounds() {
        mutedSounds = new HashSet<>();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            Identifier sound = SoundPatternItem.getSoundLocation(stack);

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
     * {@code TileEntity.getUpdateTag()}'s default implementation calls the
     * *private* {@code writeInternal()} directly (confirmed via `javap -c`),
     * not the public, overridable {@code write()} - so without this override
     * the sync packet above would carry only the base id/position, never
     * this TE's own inventory. See `RedstoneObserverTileEntity`/
     * `BiomeRadarTileEntity` for the full story on where this was found.
     */
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void syncTE() {
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
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

        compound.put("inventory", itemHandler.serializeNBT(registries));

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        itemHandler.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
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

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.SoundDampenerBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
