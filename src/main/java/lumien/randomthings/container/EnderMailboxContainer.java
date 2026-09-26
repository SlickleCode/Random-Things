package lumien.randomthings.container;

import lumien.randomthings.handler.EnderLetterHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code ContainerEnderMailbox}: 9 output-only slots
 * reading directly from the viewing player's own {@link EnderLetterHandler}
 * inbox (server-authoritative; the client-side reconstruction below just
 * needs the right shape - real contents arrive through the normal slot-sync
 * packets every container gets, same as {@code PotionVaporizerContainer}'s
 * dummy handler). Manual placement is blocked entirely - the only way mail
 * arrives is via {@code EnderMailboxBlock}'s delivery logic.
 */
public class EnderMailboxContainer extends Container {
    public EnderMailboxContainer(int windowId, PlayerInventory playerInventory, UUID owner) {
        super(ModContainerTypes.ENDER_MAILBOX, windowId);

        IItemHandler inventory = owner != null ? EnderLetterHandler.get(playerInventory.player.world).getOrCreateInventoryForPlayer(owner) : new ItemStackHandler(9);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }
            });
        }

        bindPlayerInventory(playerInventory);
    }

    public EnderMailboxContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory, (UUID) null);
    }

    private void bindPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
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
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
