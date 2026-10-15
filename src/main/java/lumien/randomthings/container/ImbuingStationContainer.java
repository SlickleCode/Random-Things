package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ImbuingStationItemHandler;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Direct port of 1.12.2's {@code ContainerImbuingStation}, restructured to
 * this port's established client/server container-reconstruction split (see
 * {@link PotionVaporizerContainer}) rather than the original's direct
 * tile-entity-reference style.
 */
public class ImbuingStationContainer extends AbstractContainerMenu {
    private final ContainerLevelAccess pos;

    public final DataSlot imbuingProgress = DataSlot.standalone();

    public ImbuingStationContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, new ImbuingStationItemHandler(), ContainerLevelAccess.NULL);
    }

    public ImbuingStationContainer(int windowId, Inventory playerInventory, IItemHandler itemHandler, ContainerLevelAccess pos) {
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

        this.addDataSlot(imbuingProgress);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.IMBUING_STATION);
    }

    @Override
    public void broadcastChanges() {
        this.pos.execute((world, pos) -> {
            BlockEntity te = world.getBlockEntity(pos);

            if (te instanceof ImbuingStationTileEntity) {
                this.imbuingProgress.set(((ImbuingStationTileEntity) te).getImbuingProgress());
            }
        });

        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 5) {
                if (!this.moveItemStackTo(stackInSlot, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 4, false)) {
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
