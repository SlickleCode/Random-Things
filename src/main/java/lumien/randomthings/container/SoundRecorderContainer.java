package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Two temporary slots (a blank {@link SoundPatternItem} in, a stamped one
 * out) plus the player's own inventory, with whichever hotbar slot is
 * holding the {@link SoundRecorderItem} itself shown read-only (matching the
 * original's "you can see but not touch the item whose GUI you have open"
 * treatment). Direct port of 1.12.2's {@code ContainerSoundRecorder}; the
 * recorded-sounds list selection itself is client-side UI
 * ({@code SoundRecorderScreen}) reusing {@link ISignalContainer} instead of
 * a bespoke network message.
 */
public class SoundRecorderContainer extends Container implements ISignalContainer {
    private final PlayerEntity player;
    private final ItemStackHandler patternHandler;

    public SoundRecorderContainer(int windowId, PlayerInventory playerInventory) {
        super(ModContainerTypes.SOUND_RECORDER, windowId);

        this.player = playerInventory.player;

        this.patternHandler = new ItemStackHandler(2) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot == 0 && stack.getItem() == ModItems.SOUND_PATTERN;
            }
        };

        this.addSlot(new SlotItemHandler(patternHandler, 0, 65, 73));
        this.addSlot(new SlotItemHandler(patternHandler, 1, 108, 73));

        bindPlayerInventory(playerInventory);
    }

    public SoundRecorderContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(windowId, playerInventory);
    }

    private void bindPlayerInventory(PlayerInventory playerInventory) {
        ItemStack heldRecorder = playerInventory.player.getHeldItemMainhand();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 15 + j * 18, 105 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (playerInventory.getStackInSlot(i) == heldRecorder) {
                this.addSlot(new Slot(playerInventory, i, 15 + i * 18, 163) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeStack(PlayerEntity playerIn) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, i, 15 + i * 18, 163));
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        if (!playerIn.world.isRemote) {
            for (int i = 0; i < patternHandler.getSlots(); i++) {
                ItemStack stack = patternHandler.extractItem(i, patternHandler.getSlotLimit(i), false);

                if (!stack.isEmpty() && !playerIn.inventory.addItemStackToInventory(stack)) {
                    playerIn.dropItem(stack, false);
                }
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (index < 2) {
                if (!this.mergeItemStack(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }

    @Override
    public void handle(int id, PacketBuffer data) {
        if (id == 0) {
            outputSound(data.readString());
        }
    }

    private void outputSound(String selectedSound) {
        ItemStack patternIn = patternHandler.getStackInSlot(0);

        if (patternIn.isEmpty()) {
            return;
        }

        ItemStack patternOut = patternHandler.getStackInSlot(1);

        if (patternOut.isEmpty()) {
            ItemStack stamped = new ItemStack(ModItems.SOUND_PATTERN);
            SoundPatternItem.setSoundLocation(stamped, selectedSound);

            patternHandler.setStackInSlot(1, stamped);
            patternHandler.extractItem(0, 1, false);
        } else if (ItemStack.areItemsEqual(patternOut, new ItemStack(ModItems.SOUND_PATTERN)) && SoundPatternItem.getSoundLocation(patternOut) != null && SoundPatternItem.getSoundLocation(patternOut).toString().equals(selectedSound) && patternOut.getCount() < patternOut.getMaxStackSize()) {
            patternOut.grow(1);
            patternHandler.extractItem(0, 1, false);
        }
    }
}
