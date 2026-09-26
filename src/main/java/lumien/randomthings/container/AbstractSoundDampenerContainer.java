package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Shared 9-slot "filter of stamped Sound Patterns" layout used by both
 * {@link SoundDampenerContainer} (a Sound Dampener block's own
 * {@code IItemHandler}) and {@link PortableSoundDampenerContainer} (a
 * carried item's {@link lumien.randomthings.util.ItemInventoryHandler}) -
 * 1.12.2 had these as two near-identical, independently hand-written
 * containers ({@code ContainerSoundDampener}/{@code ContainerPortableSoundDampener});
 * merged here since 1.14.4's base {@code Container} already provides
 * {@code mergeItemStack}, leaving nothing left to duplicate.
 */
public abstract class AbstractSoundDampenerContainer extends Container {
    protected AbstractSoundDampenerContainer(ContainerType<?> type, int windowId, PlayerInventory playerInventory, IItemHandler filterHandler) {
        super(type, windowId);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotItemHandler(filterHandler, i, 8 + i * 18, 18) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.getItem() == ModItems.SOUND_PATTERN && SoundPatternItem.getSoundLocation(stack) != null;
                }
            });
        }

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
        }

        return result;
    }
}
