package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.SoundRecorderContainer;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Port of 1.12.2's {@code GuiSoundRecorder}: a scrollable list of the
 * recorded sounds (see {@link SoundNameList}) - clicking one signals the
 * container (id 0, the sound name) to stamp it onto the blank pattern in the
 * input slot.
 */
public class SoundRecorderScreen extends ContainerScreen<SoundRecorderContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/sound_recorder.png");

    private SoundNameList soundList;

    public SoundRecorderScreen(SoundRecorderContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 190;
        this.ySize = 186;
    }

    @Override
    protected void init() {
        super.init();

        ItemStack recorderStack = this.playerInventory.player.getHeldItemMainhand();

        this.soundList = new SoundNameList(this.minecraft, 180, 50, this.guiTop + 17, this.guiTop + 67, this.guiLeft + 5, SoundRecorderItem.getRecordedSounds(recorderStack), selected -> this.container.send(0, buf -> buf.writeString(selected)));

        this.children.add(this.soundList);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.drawString(matrixStack, this.title.getString(), 8, 6, 4210752);
        this.font.drawString(matrixStack, new net.minecraft.util.text.TranslationTextComponent("container.inventory").getString(), 15, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
    }

    // ContainerScreen's mouseDragged/mouseReleased don't forward to child widgets, so the list's
    // scrollbar drag never arrived
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return this.soundList.mouseDragged(mouseX, mouseY, button, dx, dy) || super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.soundList.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        this.soundList.render(matrixStack, mouseX, mouseY, partialTicks);

        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
}
