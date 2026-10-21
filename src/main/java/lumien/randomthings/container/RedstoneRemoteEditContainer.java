package lumien.randomthings.container;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * 9 Position Filter slots plus a second row of 9 ghost "camo" slots (stored in the held
 * Redstone Remote's own NBT under {@code positions} / {@code camo}, not a tile entity) and the
 * usual player inventory. Direct port of 1.12.2's {@code ContainerRedstoneRemote} in its "edit"
 * mode. A camo slot never holds a real item: clicking it with a stack in hand copies one of it
 * as the button's icon, clicking it empty-handed clears it (1.12.2's {@code SlotGhost}).
 */
public class RedstoneRemoteEditContainer extends AbstractContainerMenu {
    private final ItemStack remoteStack;
    private final ItemStackHandler positionInventory;
    private final ItemStackHandler camoInventory;

    public RedstoneRemoteEditContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory);
    }

    public RedstoneRemoteEditContainer(int windowId, Inventory playerInventory) {
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
                    StackTag.edit(remoteStack, t -> t.put("positions", serializeNBT(StackTag.provider())));
                }
            }
        };

        if (!remoteStack.isEmpty() && StackTag.contains(remoteStack, "positions")) {
            positionInventory.deserializeNBT(StackTag.provider(), StackTag.get(remoteStack).getCompound("positions"));
        }

        this.camoInventory = new ItemStackHandler(9) {
            @Override
            protected void onContentsChanged(int slot) {
                if (!remoteStack.isEmpty()) {
                    StackTag.edit(remoteStack, t -> t.put("camo", serializeNBT(StackTag.provider())));
                }
            }
        };

        if (!remoteStack.isEmpty() && StackTag.contains(remoteStack, "camo")) {
            camoInventory.deserializeNBT(StackTag.provider(), StackTag.get(remoteStack).getCompound("camo"));
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
            if (playerInventory.getItem(col) == remoteStack) {
                // the remote being edited is shown but can't be picked up (1.12.2 SlotDisplay)
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 126) {
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
                this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 126));
            }
        }
    }

    public static ItemStack findRemoteStack(Player player) {
        ItemStack mainhand = player.getMainHandItem();

        if (mainhand.getItem() == ModItems.REDSTONE_REMOTE) {
            return mainhand;
        }

        ItemStack offhand = player.getOffhandItem();

        if (offhand.getItem() == ModItems.REDSTONE_REMOTE) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return !remoteStack.isEmpty() && (playerIn.getMainHandItem() == remoteStack || playerIn.getOffhandItem() == remoteStack);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 9 && slotId < 18) {
            // ghost camo row: copy the held stack (count 1) into the slot, or clear it if the hand is empty
            Slot slot = this.slots.get(slotId);
            ItemStack holding = this.getCarried();

            if (clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE || clickTypeIn == ClickType.SWAP) {
                if (holding.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    ItemStack copy = holding.copy();
                    copy.setCount(1);
                    slot.set(copy);
                }
            }

            return;
        }

        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index >= 9 && index < 18) {
                return ItemStack.EMPTY;
            }

            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 18, 54, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
