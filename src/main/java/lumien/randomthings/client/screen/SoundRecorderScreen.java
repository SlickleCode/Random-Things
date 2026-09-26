package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.SoundRecorderContainer;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 8, 6, 4210752);
        this.font.drawString(new net.minecraft.util.text.TranslationTextComponent("container.inventory").getString(), 15, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        this.soundList.render(mouseX, mouseY, partialTicks);

        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
