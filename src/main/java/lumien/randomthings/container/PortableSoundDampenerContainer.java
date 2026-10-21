package lumien.randomthings.container;

import lumien.randomthings.util.ItemInventoryHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PortableSoundDampenerContainer extends AbstractSoundDampenerContainer {
    private final ItemStack dampenerStack;

    public PortableSoundDampenerContainer(int windowId, Inventory playerInventory, ItemStack dampenerStack) {
        super(ModContainerTypes.PORTABLE_SOUND_DAMPENER, windowId, playerInventory, new ItemInventoryHandler(dampenerStack, "inventory", 9));

        this.dampenerStack = dampenerStack;
    }

    public PortableSoundDampenerContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        super(ModContainerTypes.PORTABLE_SOUND_DAMPENER, windowId, (Inventory) playerInventory, new ItemStackHandler(9));

        this.dampenerStack = ((Inventory) playerInventory).player.getMainHandItem();
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return ItemStack.isSameItem(dampenerStack, playerIn.getMainHandItem());
    }
}
