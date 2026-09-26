package lumien.randomthings.client.screen;

import lumien.randomthings.container.PortableSoundDampenerContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class PortableSoundDampenerScreen extends AbstractSoundDampenerScreen<PortableSoundDampenerContainer> {
    public PortableSoundDampenerScreen(PortableSoundDampenerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn, new ResourceLocation("randomthings:textures/gui/portable_sound_dampener.png"));
    }
}
