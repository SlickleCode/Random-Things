package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

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
public class SoundRecorderContainer extends AbstractContainerMenu implements ISignalContainer {
    private final Player player;
    private final ItemStackHandler patternHandler;

    public SoundRecorderContainer(int windowId, Inventory playerInventory) {
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

    public SoundRecorderContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    private void bindPlayerInventory(Inventory playerInventory) {
        ItemStack heldRecorder = playerInventory.player.getMainHandItem();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 15 + j * 18, 105 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (playerInventory.getItem(i) == heldRecorder) {
                this.addSlot(new Slot(playerInventory, i, 15 + i * 18, 163) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player playerIn) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, i, 15 + i * 18, 163));
            }
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        if (!playerIn.level().isClientSide) {
            for (int i = 0; i < patternHandler.getSlots(); i++) {
                ItemStack stack = patternHandler.extractItem(i, patternHandler.getSlotLimit(i), false);

                if (!stack.isEmpty() && !playerIn.getInventory().add(stack)) {
                    playerIn.drop(stack, false);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        if (id == 0) {
            outputSound(data.readUtf(32767));
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
        } else if (ItemStack.isSameItem(patternOut, new ItemStack(ModItems.SOUND_PATTERN)) && SoundPatternItem.getSoundLocation(patternOut) != null && SoundPatternItem.getSoundLocation(patternOut).toString().equals(selectedSound) && patternOut.getCount() < patternOut.getMaxStackSize()) {
            patternOut.grow(1);
            patternHandler.extractItem(0, 1, false);
        }
    }
}
