package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.container.BlockDestabilizerContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Ported from 1.12.2's {@code GuiBlockDestabilizer}. The original used custom
 * image-toggle buttons for Lazy/Fuzzy; this port uses plain text buttons
 * (matching {@code IgniterScreen}'s own proven pattern) instead of porting a
 * bespoke image-button widget for two on/off toggles.
 */
public class BlockDestabilizerScreen extends AbstractContainerScreen<BlockDestabilizerContainer> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/block_destabilizer.png");

    public BlockDestabilizerScreen(BlockDestabilizerContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 85;
        this.imageHeight = 35;
    }

    @Override
    protected void init() {
        super.init();

        // Sprite buttons at the original GuiBlockDestabilizer's positions (7/33/58, y7)
        this.addRenderableWidget(new SpriteStateButton(this.leftPos + 7, this.topPos + 7, 20, 20, ResourceLocation.parse("randomthings:textures/gui/block_destabilizer/lazy.png"), () -> this.menu.lazy.get() != 0 ? 1 : 0, state -> I18n.get(state != 0 ? "gui.block_destabilizer.lazy_on" : "gui.block_destabilizer.lazy_off"), (button) -> {
            this.menu.send(0, (pb) -> {
            });
        }));

        this.addRenderableWidget(new SpriteStateButton(this.leftPos + 33, this.topPos + 7, 20, 20, ResourceLocation.parse("randomthings:textures/gui/block_destabilizer/fuzzy.png"), () -> this.menu.fuzzy.get() != 0 ? 1 : 0, state -> I18n.get(state != 0 ? "gui.block_destabilizer.fuzzy_on" : "gui.block_destabilizer.fuzzy_off"), (button) -> {
            this.menu.send(1, (pb) -> {
            });
        }));

        // Reset: the 20x20 sprite at (85,0) of the GUI texture, hover variant 20px lower
        this.addRenderableWidget(new SpriteStateButton(this.leftPos + 58, this.topPos + 7, 20, 20, GUI_TEXTURES, () -> 0, state -> I18n.get("gui.block_destabilizer.reset"), (button) -> {
            this.menu.send(2, (pb) -> {
            });
        }) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, GUI_TEXTURES);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                graphics.blit(GUI_TEXTURES, this.getX(), this.getY(), 85, this.isHovered ? 20 : 0, 20, 20);
            }
        });
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        SpriteStateButton.renderTooltips(graphics, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
