package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.OnlineDetectorContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * OnlineDetectorScreen
 */
public class OnlineDetectorScreen extends ContainerScreen<OnlineDetectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/online_detector.png");

	private TextFieldWidget usernameField;

	public OnlineDetectorScreen(OnlineDetectorContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 50;
	}

	@Override
	protected void init()
	{
		super.init();

		this.usernameField = new TextFieldWidget(this.font, this.guiLeft + 8, this.guiTop + 20, 160, 18, "");
		this.usernameField.setMaxStringLength(16);
		this.usernameField.setText(this.container.getUsername());
		this.addButton(this.usernameField);
		this.setFocusedDefault(this.usernameField);
	}

	// setFocusedDefault(IGuiEventListener) is inherited from INestedGuiEventHandler

	@Override
	public void tick()
	{
		super.tick();
		this.usernameField.tick();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (this.usernameField.isFocused() && keyCode == 257 /* GLFW_KEY_ENTER */)
		{
			submitUsername();
			return true;
		}

		boolean consumed = super.keyPressed(keyCode, scanCode, modifiers);

		// TextFieldWidget.keyPressed only handles special keys (backspace, arrows,
		// etc.) - plain characters are consumed separately via charTyped, so an
		// unconsumed keyPressed here (e.g. plain "e") would otherwise fall through
		// to global keybinds (opening the inventory) while still typing normally.
		return this.usernameField.isFocused() || consumed;
	}

	@Override
	public void removed()
	{
		submitUsername();
		super.removed();
	}

	private void submitUsername()
	{
		String text = this.usernameField.getText();
		this.container.send(0, (pb) -> pb.writeString(text));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("gui.randomthings.online_detector.username"), 8, 8, 0);

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
