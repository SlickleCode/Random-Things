package lumien.randomthings.container;

import lumien.randomthings.handler.EnderLetterHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
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
public class EnderMailboxContainer extends AbstractContainerMenu {
    public EnderMailboxContainer(int windowId, Inventory playerInventory, UUID owner) {
        super(ModContainerTypes.ENDER_MAILBOX, windowId);

        IItemHandler inventory = owner != null ? EnderLetterHandler.get(playerInventory.player.level()).getOrCreateInventoryForPlayer(owner) : new ItemStackHandler(9);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        bindPlayerInventory(playerInventory);
    }

    public EnderMailboxContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, (UUID) null);
    }

    private void bindPlayerInventory(Inventory playerInventory) {
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
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }
}
