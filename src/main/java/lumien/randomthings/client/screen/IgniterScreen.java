package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.IgniterContainer;
import lumien.randomthings.tileentity.IgniterTileEntity.MODE;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * IgniterScreen
 */
public class IgniterScreen extends ContainerScreen<IgniterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/igniter.png");

	public IgniterScreen(IgniterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 100;
		this.ySize = 46;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 10, this.guiTop + 15, 80, 20, "", (button) -> {
			this.container.send(0, (pb) -> {
			});
		}));
	}

	private static String modeLabel(MODE mode)
	{
		switch (mode)
		{
			case IGNITE:
				return I18n.format("gui.igniter.ignite");
			case KEEP_IGNITED:
				return I18n.format("gui.igniter.keepIgnited");
			case TOGGLE:
			default:
				return I18n.format("gui.igniter.toggle");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		MODE mode = MODE.values()[this.container.mode.get()];
		this.buttons.get(0).setMessage(modeLabel(mode));

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
