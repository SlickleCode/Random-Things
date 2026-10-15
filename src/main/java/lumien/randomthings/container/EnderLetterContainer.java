package lumien.randomthings.container;

import lumien.randomthings.util.ItemInventoryHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
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
public class EnderLetterContainer extends net.minecraft.world.inventory.AbstractContainerMenu implements ISignalContainer {
    public static final int MAX_MESSAGE_LENGTH = 200;

    private final ItemStack letterStack;
    private final boolean received;

    public EnderLetterContainer(int windowId, Inventory playerInventory, ItemStack letterStack) {
        super(ModContainerTypes.ENDER_LETTER, windowId);

        this.letterStack = letterStack;
        this.received = letterStack.hasTag() && letterStack.getTag().getBoolean("received");

        ItemInventoryHandler content = new ItemInventoryHandler(letterStack, "content", 9);

        for (int i = 0; i < 9; i++) {
            if (received) {
                this.addSlot(new SlotItemHandler(content, i, 8 + i * 18, 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new SlotItemHandler(content, i, 8 + i * 18, 18));
            }
        }

        bindPlayerInventory(playerInventory);
    }

    public EnderLetterContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, ((Inventory) playerInventory).player.getMainHandItem());
    }

    private void bindPlayerInventory(Inventory playerInventory) {
        ItemStack heldLetter = playerInventory.player.getMainHandItem();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 101 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (playerInventory.getItem(i) == heldLetter) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 159) {
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
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 159));
            }
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return ItemStack.isSame(letterStack, playerIn.getMainHandItem());
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        if (received && !playerIn.level.isClientSide && isLetterEmpty()) {
            playerIn.getInventory().setItem(playerIn.getInventory().selected, ItemStack.EMPTY);
        }
    }

    private boolean isLetterEmpty() {
        for (int i = 0; i < 9; i++) {
            if (!this.getSlot(i).getItem().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        if (id == 0 && !received) {
            String receiver = data.readUtf(32767);
            letterStack.getOrCreateTag().putString("receiver", receiver);
        } else if (id == 1 && !received) {
            String message = data.readUtf(1024);

            if (message.length() > MAX_MESSAGE_LENGTH) {
                message = message.substring(0, MAX_MESSAGE_LENGTH);
            }

            if (message.isEmpty()) {
                letterStack.getOrCreateTag().remove("message");
            } else {
                letterStack.getOrCreateTag().putString("message", message);
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

            if (index < 9) {
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (received || !this.moveItemStackTo(stackInSlot, 0, 9, false)) {
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
