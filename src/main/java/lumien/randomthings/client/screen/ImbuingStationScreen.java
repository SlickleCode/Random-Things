package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.ImbuingStationContainer;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Direct port of 1.12.2's {@code GuiImbuingStation}: a filling progress
 * arrow plus a little pixel "bubble" that grows against whichever input
 * slots are occupied while imbuing is in progress, all cropped from the same
 * texture sheet the original used (byte-identical, confirmed).
 */
public class ImbuingStationScreen extends ContainerScreen<ImbuingStationContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/imbuing_station.png");

    private int progressBubble;

    public ImbuingStationScreen(ImbuingStationContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 176;
        this.ySize = 208;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 3, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);

        int imbuingProgress = this.container.imbuingProgress.get();

        if (imbuingProgress > 0) {
            int progressArrow = (int) (22F / ImbuingStationTileEntity.IMBUING_LENGTH * imbuingProgress) + 1;
            this.blit(x + 99, y + 54, 189, 13, progressArrow, 16);

            if (this.container.getSlot(0).getHasStack()) {
                this.blit(x + 82, y + 28, 176, 0, 12, progressBubble);
            }

            if (this.container.getSlot(1).getHasStack()) {
                this.blit(x + 54, y + 56, 189, 0, progressBubble, 12);
            }

            if (this.container.getSlot(2).getHasStack()) {
                this.blit(x + 82, y + 72, 176, 25, 12, 24);
                this.blit(x + 82, y + 72, 176, 50, 12, 24 - progressBubble);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.container.imbuingProgress.get() > 0) {
            progressBubble += 2;

            if (progressBubble > 24) {
                progressBubble = 0;
            }
        } else {
            this.progressBubble = 0;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
