package lumien.randomthings.container;

import lumien.randomthings.tileentity.SoundDampenerTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemStackHandler;

public class SoundDampenerContainer extends AbstractSoundDampenerContainer {
    public SoundDampenerContainer(int windowId, PlayerInventory playerInventory, SoundDampenerTileEntity te) {
        super(ModContainerTypes.SOUND_DAMPENER, windowId, playerInventory, te.getItemHandler());
    }

    public SoundDampenerContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        super(ModContainerTypes.SOUND_DAMPENER, windowId, (PlayerInventory) playerInventory, new ItemStackHandler(9));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
