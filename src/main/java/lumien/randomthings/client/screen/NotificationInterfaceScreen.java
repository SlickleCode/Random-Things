package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.NotificationInterfaceContainer;
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
 * NotificationInterfaceScreen
 */
public class NotificationInterfaceScreen extends AbstractContainerScreen<NotificationInterfaceContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/notification_interface.png");

	private EditBox titleField;
	private EditBox descriptionField;
	private String lastTitle;
	private String lastDescription;

	public NotificationInterfaceScreen(NotificationInterfaceContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 146;
	}

	@Override
	protected void init()
	{
		super.init();

		this.titleField = new EditBox(this.font, this.leftPos + 34, this.topPos + 18, 130, 15, Component.literal(""));
		this.titleField.setMaxLength(64);
		this.titleField.setValue(this.menu.getTitle());
		this.addRenderableWidget(this.titleField);

		this.descriptionField = new EditBox(this.font, this.leftPos + 34, this.topPos + 40, 130, 15, Component.literal(""));
		this.descriptionField.setMaxLength(256);
		this.descriptionField.setValue(this.menu.getDescription());
		this.addRenderableWidget(this.descriptionField);
		this.lastTitle = this.titleField.getValue();
		this.lastDescription = this.descriptionField.getValue();

		this.setInitialFocus(this.titleField);
	}

	@Override
	public void containerTick()
	{
		super.containerTick();

		// Sync on every change; sending only from removed() raced the close-window packet.
		if (!this.titleField.getValue().equals(this.lastTitle) || !this.descriptionField.getValue().equals(this.lastDescription))
		{
			submit();
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		EditBox focused = this.titleField.isFocused() ? this.titleField : this.descriptionField.isFocused() ? this.descriptionField : null;

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
		String title = this.titleField.getValue();
		String description = this.descriptionField.getValue();
		this.lastTitle = title;
		this.lastDescription = description;
		this.menu.send(0, (pb) -> {
			pb.writeUtf(title);
			pb.writeUtf(description);
		});
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
		// label for the icon slot (x8,y31): the item shown in the notification toast
		graphics.drawString(this.font, I18n.get("gui.randomthings.notification_interface.icon"), 8, 21, 4210752, false);

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}
}
