package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.RedstoneRemoteEditContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * RedstoneRemoteEditScreen - a row of 9 Position Filter slots, a row of 9 ghost camo-icon slots
 * beneath it, and the player inventory (1.12.2's redstoneremoteedit.png layout).
 */
public class RedstoneRemoteEditScreen extends AbstractContainerScreen<RedstoneRemoteEditContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/redstone_remote_edit.png");

    public RedstoneRemoteEditScreen(RedstoneRemoteEditContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 150;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    /**
     * Real bug, found 2026-09-28 (same missing-title gap as {@code
     * RedstoneRemoteUseScreen}, see its own javadoc for the full story) -
     * this screen never drew its title either.
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
        graphics.drawString(this.font, I18n.get("container.inventory"), 8, this.imageHeight - 95 + 2, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(graphics);
        super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
        this.renderTooltip(graphics, p_render_1_, p_render_2_);
    }
}
