package lumien.randomthings.client.screen;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.EnderLetterContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.TextComponent;

/**
 * Direct port of 1.12.2's {@code GuiEnderLetter}: a 9-slot item tray plus a
 * receiver-name field (disabled, once the letter's actually been delivered
 * to you - matches the container's own output-only slot switch).
 */
public class EnderLetterScreen extends AbstractContainerScreen<EnderLetterContainer> {
    public static final int MAX_MESSAGE_LENGTH = 200;

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/ender_letter.png");

    private final boolean received;

    private EditBox receiverField;
    private String lastSentReceiver = "";
    private MultilineTextFieldWidget messageField;
    private String lastSentMessage = "";

    public EnderLetterScreen(EnderLetterContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 176;
        this.imageHeight = 183;

        ItemStack letterStack = inv.player.getMainHandItem();
        this.received = letterStack.hasTag() && letterStack.getTag().getBoolean("received");
    }

    @Override
    protected void init() {
        super.init();

        ItemStack letterStack = this.minecraft.player.getMainHandItem();
        String receiver = letterStack.hasTag() ? letterStack.getTag().getString("receiver") : "";

        this.receiverField = new EditBox(this.font, this.leftPos + 92, this.topPos + 5, 76, 10, new TextComponent(""));
        this.receiverField.setMaxLength(64);
        this.receiverField.setEditable(!received);
        this.receiverField.setValue(receiver);
        this.addRenderableWidget(this.receiverField);
        this.lastSentReceiver = receiver;

        String message = letterStack.hasTag() ? letterStack.getTag().getString("message") : "";

        this.messageField = new MultilineTextFieldWidget(this.font, this.leftPos + 8, this.topPos + 40, 162, 44, MAX_MESSAGE_LENGTH);
        this.messageField.setEditable(!received);
        this.messageField.setText(message);
        this.addRenderableWidget(this.messageField);
        this.lastSentMessage = this.messageField.getText();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.receiverField.tick();

        // Sending only from removed() raced the vanilla close-window packet - the server could
        // already have swapped player.openContainer back to the plain inventory container by the
        // time this signal arrived, silently dropping it (matches the reported "does not save,
        // even after hitting enter" - closing the GUI is the only thing that ever sent it at
        // all). Syncing on every text change instead means the server already has the latest
        // value long before the GUI ever starts closing.
        if (!received && !this.receiverField.getValue().equals(this.lastSentReceiver)) {
            this.lastSentReceiver = this.receiverField.getValue();
            submitReceiver();
        }

        if (!received && !this.messageField.getText().equals(this.lastSentMessage)) {
            this.lastSentMessage = this.messageField.getText();
            submitMessage();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // only one of the two text inputs may hold focus; the clicked one claims it
        if (this.messageField.isMouseOver(mouseX, mouseY)) {
            this.receiverField.setFocus(false);
        } else {
            this.messageField.setFocusedState(false);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.messageField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE */) {
            this.messageField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        if (this.receiverField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE */) {
            this.receiverField.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        submitReceiver();
        submitMessage();
        super.removed();
    }

    private void submitReceiver() {
        if (!received) {
            this.menu.send(0, (pb) -> pb.writeUtf(this.receiverField.getValue()));
        }
    }

    private void submitMessage() {
        if (!received) {
            this.menu.send(1, (pb) -> pb.writeUtf(this.messageField.getText()));
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, I18n.get("item.randomthings.ender_letter"), 8, 6, 4210752);
        this.font.draw(matrixStack, I18n.get("container.inventory"), 8, this.imageHeight - 96 + 2, 4210752);

        if (!received) {
            String counter = this.messageField.getText().length() + "/" + MAX_MESSAGE_LENGTH;
            this.font.draw(matrixStack, counter, this.imageWidth - 8 - this.font.width(counter), this.imageHeight - 96 + 2, 0x707070);
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, x, y, 0, 0, this.imageWidth, this.imageHeight);

        this.receiverField.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
}
