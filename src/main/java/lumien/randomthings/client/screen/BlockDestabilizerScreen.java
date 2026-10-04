package lumien.randomthings.client.screen;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.container.BlockDestabilizerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Ported from 1.12.2's {@code GuiBlockDestabilizer}. The original used custom
 * image-toggle buttons for Lazy/Fuzzy; this port uses plain text buttons
 * (matching {@code IgniterScreen}'s own proven pattern) instead of porting a
 * bespoke image-button widget for two on/off toggles.
 */
public class BlockDestabilizerScreen extends ContainerScreen<BlockDestabilizerContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/block_destabilizer.png");

    public BlockDestabilizerScreen(BlockDestabilizerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 85;
        this.ySize = 35;
    }

    @Override
    protected void init() {
        super.init();

        // Sprite buttons at the original GuiBlockDestabilizer's positions (7/33/58, y7)
        this.addButton(new SpriteStateButton(this.guiLeft + 7, this.guiTop + 7, 20, 20, new ResourceLocation("randomthings:textures/gui/block_destabilizer/lazy.png"), () -> this.container.lazy.get() != 0 ? 1 : 0, state -> I18n.format(state != 0 ? "gui.block_destabilizer.lazy_on" : "gui.block_destabilizer.lazy_off"), (button) -> {
            this.container.send(0, (pb) -> {
            });
        }));

        this.addButton(new SpriteStateButton(this.guiLeft + 33, this.guiTop + 7, 20, 20, new ResourceLocation("randomthings:textures/gui/block_destabilizer/fuzzy.png"), () -> this.container.fuzzy.get() != 0 ? 1 : 0, state -> I18n.format(state != 0 ? "gui.block_destabilizer.fuzzy_on" : "gui.block_destabilizer.fuzzy_off"), (button) -> {
            this.container.send(1, (pb) -> {
            });
        }));

        // Reset: the 20x20 sprite at (85,0) of the GUI texture, hover variant 20px lower
        this.addButton(new SpriteStateButton(this.guiLeft + 58, this.guiTop + 7, 20, 20, GUI_TEXTURES, () -> 0, state -> I18n.format("gui.block_destabilizer.reset"), (button) -> {
            this.container.send(2, (pb) -> {
            });
        }) {
            @Override
            public void renderButton(int mouseX, int mouseY, float partialTicks) {
                Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURES);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.blit(this.x, this.y, 85, this.isHovered() ? 20 : 0, 20, 20);
            }
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        SpriteStateButton.renderTooltips(this, this.buttons, mouseX, mouseY, this.guiLeft, this.guiTop);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
