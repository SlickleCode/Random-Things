package lumien.randomthings.container;

import lumien.randomthings.util.ItemInventoryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

public class PortableSoundDampenerContainer extends AbstractSoundDampenerContainer {
    private final ItemStack dampenerStack;

    public PortableSoundDampenerContainer(int windowId, PlayerInventory playerInventory, ItemStack dampenerStack) {
        super(ModContainerTypes.PORTABLE_SOUND_DAMPENER, windowId, playerInventory, new ItemInventoryHandler(dampenerStack, "inventory", 9));

        this.dampenerStack = dampenerStack;
    }

    public PortableSoundDampenerContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        super(ModContainerTypes.PORTABLE_SOUND_DAMPENER, windowId, (PlayerInventory) playerInventory, new ItemStackHandler(9));

        this.dampenerStack = ((PlayerInventory) playerInventory).player.getHeldItemMainhand();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return ItemStack.areItemsEqual(dampenerStack, playerIn.getHeldItemMainhand());
    }
}
