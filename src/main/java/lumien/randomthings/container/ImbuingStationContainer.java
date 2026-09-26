package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ImbuingStationItemHandler;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Direct port of 1.12.2's {@code ContainerImbuingStation}, restructured to
 * this port's established client/server container-reconstruction split (see
 * {@link PotionVaporizerContainer}) rather than the original's direct
 * tile-entity-reference style.
 */
public class ImbuingStationContainer extends Container {
    private final IWorldPosCallable pos;

    public final IntReferenceHolder imbuingProgress = IntReferenceHolder.single();

    public ImbuingStationContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory, new ImbuingStationItemHandler(), IWorldPosCallable.DUMMY);
    }

    public ImbuingStationContainer(int windowId, PlayerInventory playerInventory, IItemHandler itemHandler, IWorldPosCallable pos) {
        super(ModContainerTypes.IMBUING_STATION, windowId);

        this.pos = pos;

        this.addSlot(new SlotItemHandler(itemHandler, 0, 80, 9));
        this.addSlot(new SlotItemHandler(itemHandler, 1, 35, 54));
        this.addSlot(new SlotItemHandler(itemHandler, 2, 80, 99));
        this.addSlot(new SlotItemHandler(itemHandler, 3, 80, 54));
        this.addSlot(new SlotItemHandler(itemHandler, 4, 125, 54));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 126 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 184));
        }

        this.trackInt(imbuingProgress);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.pos, playerIn, ModBlocks.IMBUING_STATION);
    }

    @Override
    public void detectAndSendChanges() {
        this.pos.consume((world, pos) -> {
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof ImbuingStationTileEntity) {
                this.imbuingProgress.set(((ImbuingStationTileEntity) te).getImbuingProgress());
            }
        });

        super.detectAndSendChanges();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            result = stackInSlot.copy();

            if (index < 5) {
                if (!this.mergeItemStack(stackInSlot, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stackInSlot, 0, 4, false)) {
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
