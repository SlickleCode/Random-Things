package lumien.randomthings.container;

import lumien.randomthings.tileentity.SoundDampenerTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.network.FriendlyByteBuf;
import lumien.randomthings.itemhandler.ItemStackHandler;

public class SoundDampenerContainer extends AbstractSoundDampenerContainer {
    public SoundDampenerContainer(int windowId, Inventory playerInventory, SoundDampenerTileEntity te) {
        super(ModContainerTypes.SOUND_DAMPENER, windowId, playerInventory, te.getItemHandler());
    }

    public SoundDampenerContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        super(ModContainerTypes.SOUND_DAMPENER, windowId, (Inventory) playerInventory, new ItemStackHandler(9));
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }
}
