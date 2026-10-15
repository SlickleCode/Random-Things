package lumien.randomthings.client.screen;

import lumien.randomthings.container.PortableSoundDampenerContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class PortableSoundDampenerScreen extends AbstractSoundDampenerScreen<PortableSoundDampenerContainer> {
    public PortableSoundDampenerScreen(PortableSoundDampenerContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn, new ResourceLocation("randomthings:textures/gui/portable_sound_dampener.png"));
    }
}
