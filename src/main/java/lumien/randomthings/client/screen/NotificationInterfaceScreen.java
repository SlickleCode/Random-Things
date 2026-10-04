package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.NotificationInterfaceContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * NotificationInterfaceScreen
 */
public class NotificationInterfaceScreen extends ContainerScreen<NotificationInterfaceContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/notification_interface.png");

	private TextFieldWidget titleField;
	private TextFieldWidget descriptionField;
	private String lastTitle;
	private String lastDescription;

	public NotificationInterfaceScreen(NotificationInterfaceContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 146;
	}

	@Override
	protected void init()
	{
		super.init();

		this.titleField = new TextFieldWidget(this.font, this.guiLeft + 34, this.guiTop + 18, 130, 15, "");
		this.titleField.setMaxStringLength(64);
		this.titleField.setText(this.container.getTitle());
		this.addButton(this.titleField);

		this.descriptionField = new TextFieldWidget(this.font, this.guiLeft + 34, this.guiTop + 40, 130, 15, "");
		this.descriptionField.setMaxStringLength(256);
		this.descriptionField.setText(this.container.getDescription());
		this.addButton(this.descriptionField);
		this.lastTitle = this.titleField.getText();
		this.lastDescription = this.descriptionField.getText();

		this.setFocusedDefault(this.titleField);
	}

	@Override
	public void tick()
	{
		super.tick();
		this.titleField.tick();
		this.descriptionField.tick();

		// Sync on every change; sending only from removed() raced the close-window packet.
		if (!this.titleField.getText().equals(this.lastTitle) || !this.descriptionField.getText().equals(this.lastDescription))
		{
			submit();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		TextFieldWidget focused = this.titleField.isFocused() ? this.titleField : this.descriptionField.isFocused() ? this.descriptionField : null;

		if (focused != null && keyCode != 256 /* GLFW_KEY_ESCAPE - still closes normally */)
		{
			if (keyCode == 257 /* GLFW_KEY_ENTER */)
			{
				submit();
				return true;
			}

			// Route straight to the focused field instead of calling
			// ContainerScreen.keyPressed(): that method treats any keypress its
			// own super.keyPressed() didn't consume as the inventory keybind and
			// closes the screen right there - and TextFieldWidget.keyPressed only
			// consumes *special* keys (backspace, arrows, etc), not plain
			// characters (those go through charTyped instead), so typing a plain
			// "e" was being read as "close the GUI" mid-sentence.
			focused.keyPressed(keyCode, scanCode, modifiers);
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed()
	{
		submit();
		super.removed();
	}

	private void submit()
	{
		String title = this.titleField.getText();
		String description = this.descriptionField.getText();
		this.lastTitle = title;
		this.lastDescription = description;
		this.container.send(0, (pb) -> {
			pb.writeString(title);
			pb.writeString(description);
		});
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(this.title.getString(), 8, 6, 4210752);
		// label for the icon slot (x8,y31): the item shown in the notification toast
		this.font.drawString(I18n.format("gui.randomthings.notification_interface.icon"), 8, 21, 4210752);

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
