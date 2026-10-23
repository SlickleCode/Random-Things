package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.EnderMailboxContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Direct port of 1.12.2's {@code GuiEnderMailbox}: a plain 9-slot display of
 * the viewing player's own inbox.
 */
public class EnderMailboxScreen extends AbstractContainerScreen<EnderMailboxContainer> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/ender_mailbox.png");

    public EnderMailboxScreen(EnderMailboxContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 165;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, I18n.get("block.randomthings.ender_mailbox"), 8, 6, 4210752, false);
        String mode = I18n.get("gui.randomthings.ender_mailbox.receiving");
        graphics.drawString(this.font, mode, this.imageWidth - 8 - this.font.width(mode), 6, 4210752, false);
        graphics.drawString(this.font, I18n.get("container.inventory"), 8, 40, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
