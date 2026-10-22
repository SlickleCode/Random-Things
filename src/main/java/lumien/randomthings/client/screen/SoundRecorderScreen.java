package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.SoundRecorderContainer;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Port of 1.12.2's {@code GuiSoundRecorder}: a scrollable list of the
 * recorded sounds (see {@link SoundNameList}) - clicking one signals the
 * container (id 0, the sound name) to stamp it onto the blank pattern in the
 * input slot.
 */
public class SoundRecorderScreen extends AbstractContainerScreen<SoundRecorderContainer> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/sound_recorder.png");

    private SoundNameList soundList;

    public SoundRecorderScreen(SoundRecorderContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 190;
        this.imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();

        ItemStack recorderStack = this.minecraft.player.getMainHandItem();

        this.soundList = new SoundNameList(this.minecraft, 180, 50, this.topPos + 17, this.topPos + 67, this.leftPos + 5, SoundRecorderItem.getRecordedSounds(recorderStack), selected -> this.menu.send(0, buf -> buf.writeUtf(selected)));

        this.addWidget(this.soundList);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
        graphics.drawString(this.font, net.minecraft.network.chat.Component.translatable("container.inventory").getString(), 15, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight);
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        this.soundList.render(graphics, mouseX, mouseY, partialTicks);

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
