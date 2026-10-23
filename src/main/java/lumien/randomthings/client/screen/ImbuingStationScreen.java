package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.ImbuingStationContainer;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Direct port of 1.12.2's {@code GuiImbuingStation}: a filling progress
 * arrow plus a little pixel "bubble" that grows against whichever input
 * slots are occupied while imbuing is in progress, all cropped from the same
 * texture sheet the original used (byte-identical, confirmed).
 */
public class ImbuingStationScreen extends AbstractContainerScreen<ImbuingStationContainer> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/imbuing_station.png");

    private int progressBubble;

    public ImbuingStationScreen(ImbuingStationContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 208;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 3, 6, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int imbuingProgress = this.menu.imbuingProgress.get();

        if (imbuingProgress > 0) {
            int progressArrow = (int) (22F / ImbuingStationTileEntity.IMBUING_LENGTH * imbuingProgress) + 1;
            graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x + 99, y + 54, 189, 13, progressArrow, 16, 256, 256);

            if (this.menu.getSlot(0).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x + 82, y + 28, 176, 0, 12, progressBubble, 256, 256);
            }

            if (this.menu.getSlot(1).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x + 54, y + 56, 189, 0, progressBubble, 12, 256, 256);
            }

            if (this.menu.getSlot(2).hasItem()) {
                graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x + 82, y + 72, 176, 25, 12, 24, 256, 256);
                graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x + 82, y + 72, 176, 50, 12, 24 - progressBubble, 256, 256);
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
