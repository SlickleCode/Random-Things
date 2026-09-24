package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedRedstoneRepeaterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * AdvancedRedstoneRepeaterScreen
 */
public class AdvancedRedstoneRepeaterScreen extends ContainerScreen<AdvancedRedstoneRepeaterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_redstone_repeater.png");

	public AdvancedRedstoneRepeaterScreen(AdvancedRedstoneRepeaterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 90;
		this.ySize = 56;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 5, this.guiTop + 15, 10, 10, "-", (button) -> {
			this.container.send(0, (pb) -> pb.writeInt(-1));
		}));
		this.addButton(new Button(this.guiLeft + 5 + 70, this.guiTop + 15, 10, 10, "+", (button) -> {
			this.container.send(0, (pb) -> pb.writeInt(1));
		}));

		this.addButton(new Button(this.guiLeft + 5, this.guiTop + 39, 10, 10, "-", (button) -> {
			this.container.send(1, (pb) -> pb.writeInt(-1));
		}));
		this.addButton(new Button(this.guiLeft + 5 + 70, this.guiTop + 39, 10, 10, "+", (button) -> {
			this.container.send(1, (pb) -> pb.writeInt(1));
		}));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("gui.randomthings.advanced_redstone_repeater.on"), 8, 5, 0);
		this.font.drawString(I18n.format("gui.randomthings.advanced_redstone_repeater.off"), 8, 29, 0);

		String onString = this.container.turnOnDelay.get() + "";
		this.font.drawString(onString, xSize / 2 - this.font.getStringWidth(onString) / 2, 16, 0);

		String offString = this.container.turnOffDelay.get() + "";
		this.font.drawString(offString, xSize / 2 - this.font.getStringWidth(offString) / 2, 40, 0);

		for (Widget widget : this.buttons)
		{
			if (widget.isHovered())
			{
				widget.renderToolTip(mouseX - this.guiLeft, mouseY - this.guiTop);
				break;
			}
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
