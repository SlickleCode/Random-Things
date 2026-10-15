package lumien.randomthings.client.screen;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedItemCollectorContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.TextComponent;

/**
 * AdvancedItemCollectorScreen
 */
public class AdvancedItemCollectorScreen extends AbstractContainerScreen<AdvancedItemCollectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_item_collector.png");

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

		this.addRenderableWidget(new Button(this.leftPos + 29, this.topPos + 20, 20, 20, new TextComponent("-"), (b) -> this.menu.send(0, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 20, 20, 20, new TextComponent("+"), (b) -> this.menu.send(1, (pb) -> {
		})));

		this.addRenderableWidget(new Button(this.leftPos + 29, this.topPos + 45, 20, 20, new TextComponent("-"), (b) -> this.menu.send(2, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 45, 20, 20, new TextComponent("+"), (b) -> this.menu.send(3, (pb) -> {
		})));

		this.addRenderableWidget(new Button(this.leftPos + 29, this.topPos + 70, 20, 20, new TextComponent("-"), (b) -> this.menu.send(4, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 70, 20, 20, new TextComponent("+"), (b) -> this.menu.send(5, (pb) -> {
		})));
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
	{

		this.font.draw(matrixStack, I18n.get("block.randomthings.advanced_item_collector"), 22, 6, 4210752);

		String radiusX = I18n.get("gui.randomthings.advanced_item_collector.range_x", this.menu.rangeX.get());
		this.font.draw(matrixStack, radiusX, imageWidth / 2 - this.font.width(radiusX) / 2 - 3, 26, 4210752);

		String radiusY = I18n.get("gui.randomthings.advanced_item_collector.range_y", this.menu.rangeY.get());
		this.font.draw(matrixStack, radiusY, imageWidth / 2 - this.font.width(radiusY) / 2 - 3, 51, 4210752);

		String radiusZ = I18n.get("gui.randomthings.advanced_item_collector.range_z", this.menu.rangeZ.get());
		this.font.draw(matrixStack, radiusZ, imageWidth / 2 - this.font.width(radiusZ) / 2 - 3, 76, 4210752);

	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
	}
}
