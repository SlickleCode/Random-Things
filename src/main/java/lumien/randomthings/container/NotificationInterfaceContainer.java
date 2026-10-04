package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.NotificationInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * NotificationInterfaceContainer
 */
public class NotificationInterfaceContainer extends Container implements ISignalContainer {
    IWorldPosCallable pos;
    String title;
    String description;

    public NotificationInterfaceContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(1), resolvePos(playerInventory, extraData.readBlockPos()), extraData.readString(), extraData.readString());
    }

    public NotificationInterfaceContainer(int windowId, PlayerInventory playerInventory, IItemHandler iconInventory, IWorldPosCallable pos, String title, String description) {
        super(ModContainerTypes.NOTIFICATION_INTERFACE, windowId);

        this.pos = pos;
        this.title = title;
        this.description = description;

        // ghost slot (like the Redstone Remote's camo row): never holds a real item, see slotClick
        this.addSlot(new SlotItemHandler(iconInventory, 0, 11, 31) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(PlayerEntity playerIn) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 64 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 122));
        }
    }

    private static IWorldPosCallable resolvePos(IInventory playerInventory, BlockPos blockPos) {
        PlayerInventory inv = (PlayerInventory) playerInventory;
        return IWorldPosCallable.of(inv.player.world, blockPos);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.pos, playerIn, ModBlocks.NOTIFICATION_INTERFACE);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId == 0) {
            // clicking with a stack in hand copies one of it as the icon, clicking empty-handed clears it
            Slot slot = this.inventorySlots.get(0);
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
        // the icon slot is ghost-only, so there is nowhere to shift-click to or from
        return ItemStack.EMPTY;
    }

    @Override
    public void handle(int id, PacketBuffer data) {
        if (id == 0) {
            String newTitle = data.readString();
            String newDescription = data.readString();

            this.title = newTitle;
            this.description = newDescription;

            this.pos.consume((world, pos) -> {
                TileEntity te = world.getTileEntity(pos);

                if (te instanceof NotificationInterfaceTileEntity) {
                    ((NotificationInterfaceTileEntity) te).setData(newTitle, newDescription);
                }
            });
        }
    }
}
