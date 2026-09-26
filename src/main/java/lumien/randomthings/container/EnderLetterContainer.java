package lumien.randomthings.container;

import lumien.randomthings.util.ItemInventoryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

/**
 * The "write/read a letter" GUI's container: 9 slots backed directly by the
 * letter item's own NBT (via {@link ItemInventoryHandler}), plus the
 * receiver-name text field (sent as a signal, id 0, rather than a dedicated
 * network message - see {@code EnderLetterScreen}). A delivered ({@code
 * received}) letter's slots go output-only, matching {@link
 * lumien.randomthings.item.EnderLetterItem}'s "unwrap and it disappears once
 * empty" behavior. Direct port of 1.12.2's {@code ContainerEnderLetter}.
 */
public class EnderLetterContainer extends net.minecraft.inventory.container.Container implements ISignalContainer {
    private final ItemStack letterStack;
    private final boolean received;

    public EnderLetterContainer(int windowId, PlayerInventory playerInventory, ItemStack letterStack) {
        super(ModContainerTypes.ENDER_LETTER, windowId);

        this.letterStack = letterStack;
        this.received = letterStack.hasTag() && letterStack.getTag().getBoolean("received");

        ItemInventoryHandler content = new ItemInventoryHandler(letterStack, "content", 9);

        for (int i = 0; i < 9; i++) {
            if (received) {
                this.addSlot(new SlotItemHandler(content, i, 8 + i * 18, 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new SlotItemHandler(content, i, 8 + i * 18, 18));
            }
        }

        bindPlayerInventory(playerInventory);
    }

    public EnderLetterContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory, ((PlayerInventory) playerInventory).player.getHeldItemMainhand());
    }

    private void bindPlayerInventory(PlayerInventory playerInventory) {
        ItemStack heldLetter = playerInventory.player.getHeldItemMainhand();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (playerInventory.getStackInSlot(i) == heldLetter) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109) {
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
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return ItemStack.areItemsEqual(letterStack, playerIn.getHeldItemMainhand());
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);

        if (received && !playerIn.world.isRemote && isLetterEmpty()) {
            playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);
        }
    }

    private boolean isLetterEmpty() {
        for (int i = 0; i < 9; i++) {
            if (!this.getSlot(i).getStack().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void handle(int id, PacketBuffer data) {
        if (id == 0 && !received) {
            String receiver = data.readString();
            letterStack.getOrCreateTag().putString("receiver", receiver);
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (index < 9) {
                if (!this.mergeItemStack(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (received || !this.mergeItemStack(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
