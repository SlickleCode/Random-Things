package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedRedstoneTorchContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * AdvancedRedstoneTorchScreen
 */
public class AdvancedRedstoneTorchScreen extends AbstractContainerScreen<AdvancedRedstoneTorchContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_redstone_torch.png");

	ContainerLevelAccess pos;

	public AdvancedRedstoneTorchScreen(AdvancedRedstoneTorchContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 90;
		this.imageHeight = 56;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
			this.menu.send(0, (pb) -> {
				pb.writeInt(0);
			});
		}).bounds(this.leftPos + 5, this.topPos + 15, 10, 10).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
			this.menu.send(0, (pb) -> {
				pb.writeInt(1);
			});
		}).bounds(this.leftPos + 5 + 70, this.topPos + 15, 10, 10).build());

		this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
			this.menu.send(0, (pb) -> {
				pb.writeInt(2);
			});
		}).bounds(this.leftPos + 5, this.topPos + 39, 10, 10).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
			this.menu.send(0, (pb) -> {
				pb.writeInt(3);
			});
		}).bounds(this.leftPos + 5 + 70, this.topPos + 39, 10, 10).build());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{


		graphics.drawString(this.font, I18n.get("gui.randomthings.advanced_redstone_torch.gs"), 8, 5, 0, false);
		graphics.drawString(this.font, I18n.get("gui.randomthings.advanced_redstone_torch.rs"), 13, 29, 0, false);

		String signalStrengthOffString = this.menu.strengthGreen.get() + "";
		graphics.drawString(this.font, signalStrengthOffString, imageWidth / 2 - this.font.width(signalStrengthOffString) / 2, 16, DyeColor.GREEN.getFireworkColor(), false);

		String signalStrengthOnString = this.menu.strengthRed.get() + "";
		graphics.drawString(this.font, signalStrengthOnString, imageWidth / 2 - this.font.width(signalStrengthOnString) / 2, 40, DyeColor.RED.getFireworkColor(), false);

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
		this.renderBackground(graphics);
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}


}