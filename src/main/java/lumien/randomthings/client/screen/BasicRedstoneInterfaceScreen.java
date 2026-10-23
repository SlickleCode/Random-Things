package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.BasicRedstoneInterfaceContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * BasicRedstoneInterfaceScreen - read-only status display, same layout as
 * {@link RedstoneObserverScreen}.
 */
public class BasicRedstoneInterfaceScreen extends AbstractContainerScreen<BasicRedstoneInterfaceContainer> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/redstone_interface/basic.png");

    public BasicRedstoneInterfaceScreen(BasicRedstoneInterfaceContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 136;
        this.imageHeight = 54;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {

        if (this.menu.hasTarget.get() == 0) {
            String center = I18n.get("gui.randomthings.basic_redstone_interface.no_target");
            graphics.drawString(this.font, center, imageWidth / 2 - this.font.width(center) / 2, imageHeight / 2 - this.font.lineHeight / 2, 0x960000, false);
        } else {
            graphics.drawString(this.font, I18n.get("gui.randomthings.basic_redstone_interface.target"), 8, 6, 4210752, false);
            graphics.drawString(this.font, I18n.get("gui.randomthings.basic_redstone_interface.target_x", this.menu.targetX.get()), 8, 18, 0x140054, false);
            graphics.drawString(this.font, I18n.get("gui.randomthings.basic_redstone_interface.target_y", this.menu.targetY.get()), 8, 28, 0x140054, false);
            graphics.drawString(this.font, I18n.get("gui.randomthings.basic_redstone_interface.target_z", this.menu.targetZ.get()), 8, 38, 0x140054, false);
        }

    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
        this.renderTooltip(graphics, p_render_1_, p_render_2_);
    }
}
