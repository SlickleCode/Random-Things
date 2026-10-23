package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.GlobalChatDetectorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * GlobalChatDetectorScreen
 */
public class GlobalChatDetectorScreen extends AbstractContainerScreen<GlobalChatDetectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/global_chat_detector.png");

	private EditBox messageField;
	private String lastSent;

	public GlobalChatDetectorScreen(GlobalChatDetectorContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 157;
	}

	@Override
	protected void init()
	{
		super.init();

		this.messageField = new EditBox(this.font, this.leftPos + 8, this.topPos + 20, 127, 15, Component.literal(""));
		this.messageField.setMaxLength(256);
		this.messageField.setValue(this.menu.getChatMessage());
		this.lastSent = this.messageField.getValue();
		this.addRenderableWidget(this.messageField);
		this.setInitialFocus(this.messageField);

		this.addRenderableWidget(new ToggleIconButton(this.leftPos + 151, this.topPos + 5, 20, 20, GUI_TEXTURES, 176, 0, () -> this.menu.consume.get() != 0, (button) -> {
			this.menu.send(1, (pb) -> {
			});
		}).setTooltip(on -> I18n.get(on ? "gui.randomthings.chat_detector.consume_yes" : "gui.randomthings.chat_detector.consume_no")));
	}

	@Override
	public void containerTick()
	{
		super.containerTick();

		// Sync on every change; sending only from removed() raced the close-window packet.
		if (!this.messageField.getValue().equals(this.lastSent))
		{
			submitMessage();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (this.messageField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE - still closes normally */)
		{
			if (keyCode == 257 /* GLFW_KEY_ENTER */)
			{
				submitMessage();
				return true;
			}

			// Route straight to the field instead of calling
			// ContainerScreen.keyPressed(): that method treats any keypress its
			// own super.keyPressed() didn't consume as the inventory keybind and
			// closes the screen right there - and TextFieldWidget.keyPressed only
			// consumes *special* keys (backspace, arrows, etc), not plain
			// characters (those go through charTyped instead), so typing a plain
			// "e" was being read as "close the GUI" mid-sentence.
			this.messageField.keyPressed(keyCode, scanCode, modifiers);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed()
	{
		submitMessage();
		super.removed();
	}

	private void submitMessage()
	{
		String text = this.messageField.getValue();
		this.lastSent = text;
		this.menu.send(0, (pb) -> pb.writeUtf(text));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, I18n.get("gui.randomthings.global_chat_detector.message"), 8, 6, 4210752, false);
		graphics.drawString(this.font, I18n.get("container.inventory"), 8, 65, 4210752, false);

		if (ScreenWidgets.buttons(this).get(1).isHoveredOrFocused())
		{
			boolean consume = this.menu.consume.get() != 0;
			graphics.renderTooltip(this.font, Component.literal(I18n.get(consume ? "gui.randomthings.chat_detector.consume_yes" : "gui.randomthings.chat_detector.consume_no")), mouseX - this.leftPos, mouseY - this.topPos);
		}

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}
}
