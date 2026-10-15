package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedRedstoneRepeaterContainer;
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
import net.minecraft.network.chat.TextComponent;

/**
 * AdvancedRedstoneRepeaterScreen
 */
public class AdvancedRedstoneRepeaterScreen extends AbstractContainerScreen<AdvancedRedstoneRepeaterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_redstone_repeater.png");

	public AdvancedRedstoneRepeaterScreen(AdvancedRedstoneRepeaterContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 90;
		this.imageHeight = 56;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(new Button(this.leftPos + 5, this.topPos + 15, 10, 10, new TextComponent("-"), (button) -> {
			this.menu.send(0, (pb) -> pb.writeInt(-1));
		}));
		this.addRenderableWidget(new Button(this.leftPos + 5 + 70, this.topPos + 15, 10, 10, new TextComponent("+"), (button) -> {
			this.menu.send(0, (pb) -> pb.writeInt(1));
		}));

		this.addRenderableWidget(new Button(this.leftPos + 5, this.topPos + 39, 10, 10, new TextComponent("-"), (button) -> {
			this.menu.send(1, (pb) -> pb.writeInt(-1));
		}));
		this.addRenderableWidget(new Button(this.leftPos + 5 + 70, this.topPos + 39, 10, 10, new TextComponent("+"), (button) -> {
			this.menu.send(1, (pb) -> pb.writeInt(1));
		}));
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
	{

		this.font.draw(matrixStack, I18n.get("gui.randomthings.advanced_redstone_repeater.on"), 8, 5, 0);
		this.font.draw(matrixStack, I18n.get("gui.randomthings.advanced_redstone_repeater.off"), 8, 29, 0);

		String onString = this.menu.turnOnDelay.get() + "";
		this.font.draw(matrixStack, onString, imageWidth / 2 - this.font.width(onString) / 2, 16, 0);

		String offString = this.menu.turnOffDelay.get() + "";
		this.font.draw(matrixStack, offString, imageWidth / 2 - this.font.width(offString) / 2, 40, 0);

		for (AbstractWidget widget : ScreenWidgets.buttons(this))
		{
			if (widget.isHoveredOrFocused())
			{
				widget.renderToolTip(matrixStack, mouseX - this.leftPos, mouseY - this.topPos);
				break;
			}
		}

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
