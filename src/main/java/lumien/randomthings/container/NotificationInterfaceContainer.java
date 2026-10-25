package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.NotificationInterfaceTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ItemStackHandler;
import lumien.randomthings.itemhandler.SlotItemHandler;

/**
 * NotificationInterfaceContainer
 */
public class NotificationInterfaceContainer extends AbstractContainerMenu implements ISignalContainer {
    ContainerLevelAccess pos;
    String title;
    String description;

    public NotificationInterfaceContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, new ItemStackHandler(1), resolvePos(playerInventory, extraData.readBlockPos()), extraData.readUtf(32767), extraData.readUtf(32767));
    }

    public NotificationInterfaceContainer(int windowId, Inventory playerInventory, IItemHandler iconInventory, ContainerLevelAccess pos, String title, String description) {
        super(ModContainerTypes.NOTIFICATION_INTERFACE, windowId);

        this.pos = pos;
        this.title = title;
        this.description = description;

        // ghost slot (like the Redstone Remote's camo row): never holds a real item, see slotClick
        this.addSlot(new SlotItemHandler(iconInventory, 0, 11, 31) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
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

    private static ContainerLevelAccess resolvePos(Container playerInventory, BlockPos blockPos) {
        Inventory inv = (Inventory) playerInventory;
        return ContainerLevelAccess.create(inv.player.level(), blockPos);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.NOTIFICATION_INTERFACE);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId == 0) {
            // clicking with a stack in hand copies one of it as the icon, clicking empty-handed clears it
            Slot slot = this.slots.get(0);
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
        // the icon slot is ghost-only, so there is nowhere to shift-click to or from
        return ItemStack.EMPTY;
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        if (id == 0) {
            String newTitle = data.readUtf(32767);
            String newDescription = data.readUtf(32767);

            this.title = newTitle;
            this.description = newDescription;

            this.pos.execute((world, pos) -> {
                BlockEntity te = world.getBlockEntity(pos);

                if (te instanceof NotificationInterfaceTileEntity) {
                    ((NotificationInterfaceTileEntity) te).setData(newTitle, newDescription);
                }
            });
        }
    }
}
