package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.OnlineDetectorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * OnlineDetectorScreen
 */
public class OnlineDetectorScreen extends AbstractContainerScreen<OnlineDetectorContainer>
{
	private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/online_detector.png");

	private EditBox usernameField;
	private String lastSent;

	public OnlineDetectorScreen(OnlineDetectorContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 136;
		this.imageHeight = 52;
	}

	@Override
	protected void init()
	{
		super.init();

		this.usernameField = new EditBox(this.font, this.leftPos + 5, this.topPos + 26, 127, 20, Component.literal(""));
		this.usernameField.setMaxLength(16);
		this.usernameField.setValue(this.menu.getUsername());
		this.lastSent = this.usernameField.getValue();
		this.addRenderableWidget(this.usernameField);
		this.setInitialFocus(this.usernameField);
		// Real bug, found 2026-09-28: setFocusedDefault only tells the Screen which
		// widget should receive routed input - it does NOT set the widget's own
		// internal isFocused() flag (a separate boolean on Widget, normally only set
		// by mouseClicked). TextFieldWidget#keyPressed/#charTyped both gate on that
		// same internal flag, and so does this class's own keyPressed override below,
		// so typing immediately after opening the GUI (without first clicking into
		// the field) fell straight through to ContainerScreen's "unhandled key =
		// close on inventory keybind" check - explaining both "hitting the inventory
		// button still closes the GUI" and "doesn't save unless you hit Enter" (the
		// field never actually received any of the typed text to begin with).
		this.usernameField.setFocused(true);
	}

	// setFocusedDefault(IGuiEventListener) is inherited from INestedGuiEventHandler

	@Override
	public void containerTick()
	{
		super.containerTick();

		// Sync on every change rather than only from removed(): sending only on close
		// raced the vanilla close-window packet, so Escape after clicking out of the
		// field could drop the text (same fix as EnderLetterScreen).
		if (!this.usernameField.getValue().equals(this.lastSent))
		{
			submitUsername();
		}
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent rtEvent)
	{
		int keyCode = rtEvent.key();
		int scanCode = rtEvent.scancode();
		int modifiers = rtEvent.modifiers();
		if (this.usernameField.isFocused() && keyCode != 256 /* GLFW_KEY_ESCAPE - still closes normally */)
		{
			if (keyCode == 257 /* GLFW_KEY_ENTER */)
			{
				submitUsername();
				return true;
			}

			// Route straight to the field instead of calling
			// ContainerScreen.keyPressed(): that method treats any keypress its
			// own super.keyPressed() didn't consume as the inventory keybind and
			// closes the screen right there - and TextFieldWidget.keyPressed only
			// consumes *special* keys (backspace, arrows, etc), not plain
			// characters (those go through charTyped instead), so typing a plain
			// "e" was being read as "close the GUI" mid-sentence.
			this.usernameField.keyPressed(rtEvent);
			return true;
		}

		return super.keyPressed(rtEvent);
	}

	@Override
	public void removed()
	{
		submitUsername();
		super.removed();
	}

	private void submitUsername()
	{
		String text = this.usernameField.getValue();
		this.lastSent = text;
		this.menu.send(0, (pb) -> pb.writeUtf(text));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, I18n.get("gui.randomthings.online_detector.username"), 8, 6, 0xFF404040, false);

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}
}
