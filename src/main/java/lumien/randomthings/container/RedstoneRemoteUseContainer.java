package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;

/**
 * No slots at all - the "Use" screen just reads the held Redstone Remote's
 * own NBT directly (always available locally, no server round-trip needed
 * for that part) and renders a row of buttons, one per bound Position
 * Filter; clicking one sends {@link
 * lumien.randomthings.network.messages.RedstoneRemoteActivateMessage} to
 * the server. Matches 1.12.2's own {@code ContainerEmptyContainer} use here.
 */
public class RedstoneRemoteUseContainer extends AbstractContainerMenu {
    private final ItemStack remoteStack;

    public RedstoneRemoteUseContainer(int windowId, Container playerInventory) {
        super(ModContainerTypes.REDSTONE_REMOTE_USE, windowId);

        this.remoteStack = RedstoneRemoteEditContainer.findRemoteStack(((net.minecraft.world.entity.player.Inventory) playerInventory).player);
    }

    public RedstoneRemoteUseContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, playerInventory);
    }

    public ItemStack getRemoteStack() {
        return remoteStack;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return !remoteStack.isEmpty() && remoteStack.getItem() == ModItems.REDSTONE_REMOTE && (playerIn.getMainHandItem() == remoteStack || playerIn.getOffhandItem() == remoteStack);
    }
}
