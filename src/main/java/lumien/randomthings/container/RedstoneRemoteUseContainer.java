package lumien.randomthings.container;

import lumien.randomthings.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * No slots at all - the "Use" screen just reads the held Redstone Remote's
 * own NBT directly (always available locally, no server round-trip needed
 * for that part) and renders a row of buttons, one per bound Position
 * Filter; clicking one sends {@link
 * lumien.randomthings.network.messages.RedstoneRemoteActivateMessage} to
 * the server. Matches 1.12.2's own {@code ContainerEmptyContainer} use here.
 */
public class RedstoneRemoteUseContainer extends Container {
    private final ItemStack remoteStack;

    public RedstoneRemoteUseContainer(int windowId, IInventory playerInventory) {
        super(ModContainerTypes.REDSTONE_REMOTE_USE, windowId);

        this.remoteStack = RedstoneRemoteEditContainer.findRemoteStack(((net.minecraft.entity.player.PlayerInventory) playerInventory).player);
    }

    public RedstoneRemoteUseContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, playerInventory);
    }

    public ItemStack getRemoteStack() {
        return remoteStack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return !remoteStack.isEmpty() && remoteStack.getItem() == ModItems.REDSTONE_REMOTE && (playerIn.getHeldItemMainhand() == remoteStack || playerIn.getHeldItemOffhand() == remoteStack);
    }
}
