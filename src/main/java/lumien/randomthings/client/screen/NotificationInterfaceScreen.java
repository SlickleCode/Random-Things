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

	public NotificationInterfaceScreen(NotificationInterfaceContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 182;
	}

	@Override
	protected void init()
	{
		super.init();

		this.titleField = new TextFieldWidget(this.font, this.guiLeft + 8, this.guiTop + 18, 160, 18, "");
		this.titleField.setMaxStringLength(64);
		this.titleField.setText(this.container.getTitle());
		this.addButton(this.titleField);

		this.descriptionField = new TextFieldWidget(this.font, this.guiLeft + 8, this.guiTop + 52, 160, 18, "");
		this.descriptionField.setMaxStringLength(256);
		this.descriptionField.setText(this.container.getDescription());
		this.addButton(this.descriptionField);

		this.setFocusedDefault(this.titleField);
	}

	@Override
	public void tick()
	{
		super.tick();
		this.titleField.tick();
		this.descriptionField.tick();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if ((this.titleField.isFocused() || this.descriptionField.isFocused()) && keyCode == 257 /* GLFW_KEY_ENTER */)
		{
			submit();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose()
	{
		submit();
		super.onClose();
	}

	private void submit()
	{
		String title = this.titleField.getText();
		String description = this.descriptionField.getText();
		this.container.send(0, (pb) -> {
			pb.writeString(title);
			pb.writeString(description);
		});
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("gui.randomthings.notification_interface.title"), 8, 6, 0);
		this.font.drawString(I18n.format("gui.randomthings.notification_interface.description"), 8, 40, 0);

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
