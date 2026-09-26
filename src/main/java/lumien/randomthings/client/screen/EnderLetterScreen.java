package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.EnderLetterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Direct port of 1.12.2's {@code GuiEnderLetter}: a 9-slot item tray plus a
 * receiver-name field (disabled, once the letter's actually been delivered
 * to you - matches the container's own output-only slot switch).
 */
public class EnderLetterScreen extends ContainerScreen<EnderLetterContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/ender_letter.png");

    private final boolean received;

    private TextFieldWidget receiverField;

    public EnderLetterScreen(EnderLetterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 176;
        this.ySize = 133;

        ItemStack letterStack = inv.player.getHeldItemMainhand();
        this.received = letterStack.hasTag() && letterStack.getTag().getBoolean("received");
    }

    @Override
    protected void init() {
        super.init();

        ItemStack letterStack = this.minecraft.player.getHeldItemMainhand();
        String receiver = letterStack.hasTag() ? letterStack.getTag().getString("receiver") : "";

        this.receiverField = new TextFieldWidget(this.font, this.guiLeft + 92, this.guiTop + 5, 76, 10, "");
        this.receiverField.setMaxStringLength(64);
        this.receiverField.setEnabled(!received);
        this.receiverField.setText(receiver);
        this.addButton(this.receiverField);
    }

    @Override
    public void tick() {
        super.tick();
        this.receiverField.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.receiverField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE */) {
            this.receiverField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        submitReceiver();
        super.removed();
    }

    private void submitReceiver() {
        if (!received) {
            this.container.send(0, (pb) -> pb.writeString(this.receiverField.getText()));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(I18n.format("item.randomthings.ender_letter"), 8, 6, 4210752);
        this.font.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);

        this.receiverField.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
