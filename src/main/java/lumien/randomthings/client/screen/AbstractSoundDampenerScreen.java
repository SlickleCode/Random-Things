package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.AbstractSoundDampenerContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Shared plain 9-slot-filter + player-inventory GUI for
 * {@link SoundDampenerScreen} and {@link PortableSoundDampenerScreen} -
 * standard chest-sized background, no widgets beyond the slots themselves.
 */
public abstract class AbstractSoundDampenerScreen<T extends AbstractSoundDampenerContainer> extends AbstractContainerScreen<T> {
    private final Identifier guiTextures;

    protected AbstractSoundDampenerScreen(T screenContainer, Inventory inv, Component titleIn, Identifier guiTextures) {
        super(screenContainer, inv, titleIn);

        this.guiTextures = guiTextures;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        // 1.12.2's GuiContainerBase did this for every container GUI: without it the Sound Pattern tooltips never show
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 8, 6, 0xFF404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, guiTextures, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
