package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AbstractSoundDampenerContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Shared plain 9-slot-filter + player-inventory GUI for
 * {@link SoundDampenerScreen} and {@link PortableSoundDampenerScreen} -
 * standard chest-sized background, no widgets beyond the slots themselves.
 */
public abstract class AbstractSoundDampenerScreen<T extends AbstractSoundDampenerContainer> extends AbstractContainerScreen<T> {
    private final ResourceLocation guiTextures;

    protected AbstractSoundDampenerScreen(T screenContainer, Inventory inv, Component titleIn, ResourceLocation guiTextures) {
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
        graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, guiTextures);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, guiTextures, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
