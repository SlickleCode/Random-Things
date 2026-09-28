package lumien.randomthings.client.screen;

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

        this.addButton(new Button(this.guiLeft + 3, this.guiTop + 7, 26, 20, "", (button) -> {
            this.container.send(0, (pb) -> {
            });
        }));

        this.addButton(new Button(this.guiLeft + 30, this.guiTop + 7, 26, 20, "", (button) -> {
            this.container.send(1, (pb) -> {
            });
        }));

        this.addButton(new Button(this.guiLeft + 57, this.guiTop + 7, 26, 20, I18n.format("gui.block_destabilizer.reset"), (button) -> {
            this.container.send(2, (pb) -> {
            });
        }));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();

        this.buttons.get(0).setMessage(I18n.format(this.container.lazy.get() != 0 ? "gui.block_destabilizer.lazy_on" : "gui.block_destabilizer.lazy_off"));
        this.buttons.get(1).setMessage(I18n.format(this.container.fuzzy.get() != 0 ? "gui.block_destabilizer.fuzzy_on" : "gui.block_destabilizer.fuzzy_off"));

        RenderHelper.enableGUIStandardItemLighting();
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
