package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedItemCollectorContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * AdvancedItemCollectorScreen
 */
public class AdvancedItemCollectorScreen extends ContainerScreen<AdvancedItemCollectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_item_collector.png");

	public AdvancedItemCollectorScreen(AdvancedItemCollectorContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 20, 10, 10, "-", (b) -> this.container.send(0, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 20, 10, 10, "+", (b) -> this.container.send(1, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 40, 10, 10, "-", (b) -> this.container.send(2, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 40, 10, 10, "+", (b) -> this.container.send(3, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 60, 10, 10, "-", (b) -> this.container.send(4, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 60, 10, 10, "+", (b) -> this.container.send(5, (pb) -> {
		})));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("block.randomthings.advanced_item_collector"), 8, 6, 0);

		String radiusX = I18n.format("gui.randomthings.advanced_item_collector.range_x", this.container.rangeX.get());
		this.font.drawString(radiusX, xSize / 2 - this.font.getStringWidth(radiusX) / 2 - 3, 21, 0);

		String radiusY = I18n.format("gui.randomthings.advanced_item_collector.range_y", this.container.rangeY.get());
		this.font.drawString(radiusY, xSize / 2 - this.font.getStringWidth(radiusY) / 2 - 3, 41, 0);

		String radiusZ = I18n.format("gui.randomthings.advanced_item_collector.range_z", this.container.rangeZ.get());
		this.font.drawString(radiusZ, xSize / 2 - this.font.getStringWidth(radiusZ) / 2 - 3, 61, 0);

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
