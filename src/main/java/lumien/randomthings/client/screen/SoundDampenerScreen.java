package lumien.randomthings.client.screen;

import lumien.randomthings.container.SoundDampenerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SoundDampenerScreen extends AbstractSoundDampenerScreen<SoundDampenerContainer> {
    public SoundDampenerScreen(SoundDampenerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn, new ResourceLocation("randomthings:textures/gui/sound_dampener.png"));
    }
}
