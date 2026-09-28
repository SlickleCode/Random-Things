package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 9 Position Filter slots (stored in the held Redstone Remote's own NBT, not
 * a tile entity) plus the usual player inventory. Direct port of 1.12.2's
 * {@code ContainerRedstoneRemote} in its "edit" mode - minus its cosmetic
 * ghost/camo-icon row (see {@link lumien.randomthings.item.RedstoneRemoteItem}'s
 * javadoc for that disclosed simplification).
 */
public class RedstoneRemoteEditContainer extends Container {
    private final ItemStack remoteStack;
    private final ItemStackHandler positionInventory;

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

        for (int col = 0; col < 9; col++) {
            this.addSlot(new SlotItemHandler(positionInventory, col, 8 + col * 18, 20));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
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
