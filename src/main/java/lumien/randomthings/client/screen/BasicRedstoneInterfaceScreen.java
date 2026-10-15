package lumien.randomthings.client.screen;

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
import net.minecraft.network.chat.TextComponent;

/**
 * BasicRedstoneInterfaceScreen - read-only status display, same layout as
 * {@link RedstoneObserverScreen}.
 */
public class BasicRedstoneInterfaceScreen extends AbstractContainerScreen<BasicRedstoneInterfaceContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/redstone_interface/basic.png");

    public BasicRedstoneInterfaceScreen(BasicRedstoneInterfaceContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 136;
        this.imageHeight = 54;
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {

        if (this.menu.hasTarget.get() == 0) {
            String center = I18n.get("gui.randomthings.basic_redstone_interface.no_target");
            this.font.draw(matrixStack, center, imageWidth / 2 - this.font.width(center) / 2, imageHeight / 2 - this.font.lineHeight / 2, 0x960000);
        } else {
            this.font.draw(matrixStack, I18n.get("gui.randomthings.basic_redstone_interface.target"), 8, 6, 4210752);
            this.font.draw(matrixStack, I18n.get("gui.randomthings.basic_redstone_interface.target_x", this.menu.targetX.get()), 8, 18, 0x140054);
            this.font.draw(matrixStack, I18n.get("gui.randomthings.basic_redstone_interface.target_y", this.menu.targetY.get()), 8, 28, 0x140054);
            this.font.draw(matrixStack, I18n.get("gui.randomthings.basic_redstone_interface.target_z", this.menu.targetZ.get()), 8, 38, 0x140054);
        }

    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
        this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
    }
}
