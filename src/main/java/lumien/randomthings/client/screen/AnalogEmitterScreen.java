package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AnalogEmitterContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * AnalogEmitterScreen
 */
public class AnalogEmitterScreen extends AbstractContainerScreen<AnalogEmitterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/analog_emitter.png");

	public AnalogEmitterScreen(AnalogEmitterContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 78;
		this.imageHeight = 50;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(Button.builder(Component.literal("<"), (button) -> {
			this.menu.send(0, (pb) -> pb.writeInt(0));
		}).bounds(this.leftPos + 5, this.topPos + 20, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal(">"), (button) -> {
			this.menu.send(0, (pb) -> pb.writeInt(1));
		}).bounds(this.leftPos + 55, this.topPos + 20, 20, 20).build());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, I18n.get("block.randomthings.analog_emitter"), 4, 6, 4210752, false);

		String levelString = this.menu.emitLevel.get() + "";
		graphics.drawString(this.font, levelString, imageWidth / 2 - this.font.width(levelString) / 2 + 3, 26, 0xFF0000, false);

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
