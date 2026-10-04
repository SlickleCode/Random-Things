package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.GlobalChatDetectorContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * GlobalChatDetectorScreen
 */
public class GlobalChatDetectorScreen extends ContainerScreen<GlobalChatDetectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/global_chat_detector.png");

	private TextFieldWidget messageField;
	private String lastSent;

	public GlobalChatDetectorScreen(GlobalChatDetectorContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 157;
	}

	@Override
	protected void init()
	{
		super.init();

		this.messageField = new TextFieldWidget(this.font, this.guiLeft + 8, this.guiTop + 20, 127, 15, "");
		this.messageField.setMaxStringLength(256);
		this.messageField.setText(this.container.getChatMessage());
		this.lastSent = this.messageField.getText();
		this.addButton(this.messageField);
		this.setFocusedDefault(this.messageField);

		this.addButton(new ToggleIconButton(this.guiLeft + 151, this.guiTop + 5, 20, 20, GUI_TEXTURES, 176, 0, () -> this.container.consume.get() != 0, (button) -> {
			this.container.send(1, (pb) -> {
			});
		}).setTooltip(on -> I18n.format(on ? "gui.randomthings.chat_detector.consume_yes" : "gui.randomthings.chat_detector.consume_no")));
	}

	@Override
	public void tick()
	{
		super.tick();
		this.messageField.tick();

		// Sync on every change; sending only from removed() raced the close-window packet.
		if (!this.messageField.getText().equals(this.lastSent))
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
		String text = this.messageField.getText();
		this.lastSent = text;
		this.container.send(0, (pb) -> pb.writeString(text));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("gui.randomthings.global_chat_detector.message"), 8, 6, 4210752);
		this.font.drawString(I18n.format("container.inventory"), 8, 65, 4210752);

		if (this.buttons.get(1).isHovered())
		{
			boolean consume = this.container.consume.get() != 0;
			this.renderTooltip(I18n.format(consume ? "gui.randomthings.chat_detector.consume_yes" : "gui.randomthings.chat_detector.consume_no"), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		RenderHelper.enableGUIStandardItemLighting();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(i, j, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground();
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
	}
}
