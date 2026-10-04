package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 9 Position Filter slots plus a second row of 9 ghost "camo" slots (stored in the held
 * Redstone Remote's own NBT under {@code positions} / {@code camo}, not a tile entity) and the
 * usual player inventory. Direct port of 1.12.2's {@code ContainerRedstoneRemote} in its "edit"
 * mode. A camo slot never holds a real item: clicking it with a stack in hand copies one of it
 * as the button's icon, clicking it empty-handed clears it (1.12.2's {@code SlotGhost}).
 */
public class RedstoneRemoteEditContainer extends Container {
    private final ItemStack remoteStack;
    private final ItemStackHandler positionInventory;
    private final ItemStackHandler camoInventory;

    public RedstoneRemoteEditContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory);
    }

    public RedstoneRemoteEditContainer(int windowId, PlayerInventory playerInventory) {
        super(ModContainerTypes.REDSTONE_REMOTE_EDIT, windowId);

        this.remoteStack = findRemoteStack(playerInventory.player);

        this.positionInventory = new ItemStackHandler(9) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() == ModItems.POSITION_FILTER;
            }

            @Override
            protected void onContentsChanged(int slot) {
                if (!remoteStack.isEmpty()) {
                    remoteStack.getOrCreateTag().put("positions", serializeNBT());
                }
            }
        };

        if (!remoteStack.isEmpty() && remoteStack.hasTag() && remoteStack.getTag().contains("positions")) {
            positionInventory.deserializeNBT(remoteStack.getTag().getCompound("positions"));
        }

        this.camoInventory = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                if (!remoteStack.isEmpty()) {
                    remoteStack.getOrCreateTag().put("camo", serializeNBT());
                }
            }
        };

        if (!remoteStack.isEmpty() && remoteStack.hasTag() && remoteStack.getTag().contains("camo")) {
            camoInventory.deserializeNBT(remoteStack.getTag().getCompound("camo"));
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(positionInventory, col, 8 + col * 18, 18));
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(camoInventory, col, 8 + col * 18, 36));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 68 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            if (playerInventory.getStackInSlot(col) == remoteStack) {
                // the remote being edited is shown but can't be picked up (1.12.2 SlotDisplay)
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 126) {
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
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 126));
            }
        }
    }

    public static ItemStack findRemoteStack(PlayerEntity player) {
        ItemStack mainhand = player.getHeldItemMainhand();

        if (mainhand.getItem() == ModItems.REDSTONE_REMOTE) {
            return mainhand;
        }

        ItemStack offhand = player.getHeldItemOffhand();

        if (offhand.getItem() == ModItems.REDSTONE_REMOTE) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return !remoteStack.isEmpty() && (playerIn.getHeldItemMainhand() == remoteStack || playerIn.getHeldItemOffhand() == remoteStack);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId >= 9 && slotId < 18) {
            // ghost camo row: copy the held stack (count 1) into the slot, or clear it if the hand is empty
            Slot slot = this.inventorySlots.get(slotId);
            ItemStack holding = player.inventory.getItemStack();

            if (clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE || clickTypeIn == ClickType.SWAP) {
                if (holding.isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    ItemStack copy = holding.copy();
                    copy.setCount(1);
                    slot.putStack(copy);
                }
            }

            return ItemStack.EMPTY;
        }

        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (index >= 9 && index < 18) {
                return ItemStack.EMPTY;
            }

            if (index < 9) {
                if (!this.mergeItemStack(stackInSlot, 18, 54, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stackInSlot, 0, 9, false)) {
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
