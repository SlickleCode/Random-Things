package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.AdvancedItemCollectorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * AdvancedItemCollectorScreen
 */
public class AdvancedItemCollectorScreen extends AbstractContainerScreen<AdvancedItemCollectorContainer>
{
	private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/advanced_item_collector.png");

	public AdvancedItemCollectorScreen(AdvancedItemCollectorContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 211;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(0, (pb) -> {
		})).bounds(this.leftPos + 29, this.topPos + 20, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(1, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 20, 20, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(2, (pb) -> {
		})).bounds(this.leftPos + 29, this.topPos + 45, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(3, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 45, 20, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(4, (pb) -> {
		})).bounds(this.leftPos + 29, this.topPos + 70, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(5, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 70, 20, 20).build());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, I18n.get("block.randomthings.advanced_item_collector"), 22, 6, 0xFF404040, false);

		String radiusX = I18n.get("gui.randomthings.advanced_item_collector.range_x", this.menu.rangeX.get());
		graphics.drawString(this.font, radiusX, imageWidth / 2 - this.font.width(radiusX) / 2 - 3, 26, 0xFF404040, false);

		String radiusY = I18n.get("gui.randomthings.advanced_item_collector.range_y", this.menu.rangeY.get());
		graphics.drawString(this.font, radiusY, imageWidth / 2 - this.font.width(radiusY) / 2 - 3, 51, 0xFF404040, false);

		String radiusZ = I18n.get("gui.randomthings.advanced_item_collector.range_z", this.menu.rangeZ.get());
		graphics.drawString(this.font, radiusZ, imageWidth / 2 - this.font.width(radiusZ) / 2 - 3, 76, 0xFF404040, false);

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
