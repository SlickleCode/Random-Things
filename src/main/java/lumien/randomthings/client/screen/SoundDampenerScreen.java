package lumien.randomthings.client.screen;

import lumien.randomthings.container.SoundDampenerContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class SoundDampenerScreen extends AbstractSoundDampenerScreen<SoundDampenerContainer> {
    public SoundDampenerScreen(SoundDampenerContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn, new ResourceLocation("randomthings:textures/gui/sound_dampener.png"));
    }
}
