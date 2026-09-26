package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AbstractSoundDampenerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Shared plain 9-slot-filter + player-inventory GUI for
 * {@link SoundDampenerScreen} and {@link PortableSoundDampenerScreen} -
 * standard chest-sized background, no widgets beyond the slots themselves.
 */
public abstract class AbstractSoundDampenerScreen<T extends AbstractSoundDampenerContainer> extends ContainerScreen<T> {
    private final ResourceLocation guiTextures;

    protected AbstractSoundDampenerScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn, ResourceLocation guiTextures) {
        super(screenContainer, inv, titleIn);

        this.guiTextures = guiTextures;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(guiTextures);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }
}
