package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.ImbuingStationContainer;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Direct port of 1.12.2's {@code GuiImbuingStation}: a filling progress
 * arrow plus a little pixel "bubble" that grows against whichever input
 * slots are occupied while imbuing is in progress, all cropped from the same
 * texture sheet the original used (byte-identical, confirmed).
 */
public class ImbuingStationScreen extends AbstractContainerScreen<ImbuingStationContainer> {
    private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/imbuing_station.png");

    private int progressBubble;

    public ImbuingStationScreen(ImbuingStationContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 208;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 3, 6, 0xFF404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int imbuingProgress = this.menu.imbuingProgress.get();

        if (imbuingProgress > 0) {
            int progressArrow = (int) (22F / ImbuingStationTileEntity.IMBUING_LENGTH * imbuingProgress) + 1;
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 99, y + 54, 189, 13, progressArrow, 16, 256, 256);

            if (this.menu.getSlot(0).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 82, y + 28, 176, 0, 12, progressBubble, 256, 256);
            }

            if (this.menu.getSlot(1).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 54, y + 56, 189, 0, progressBubble, 12, 256, 256);
            }

            if (this.menu.getSlot(2).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 82, y + 72, 176, 25, 12, 24, 256, 256);
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x + 82, y + 72, 176, 50, 12, 24 - progressBubble, 256, 256);
            }
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (this.menu.imbuingProgress.get() > 0) {
            progressBubble += 2;

            if (progressBubble > 24) {
                progressBubble = 0;
            }
        } else {
            this.progressBubble = 0;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
