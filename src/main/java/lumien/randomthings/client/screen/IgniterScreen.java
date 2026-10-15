package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.IgniterContainer;
import lumien.randomthings.tileentity.IgniterTileEntity.MODE;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;

/**
 * IgniterScreen
 */
public class IgniterScreen extends AbstractContainerScreen<IgniterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/igniter.png");

	public IgniterScreen(IgniterContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 78;
		this.imageHeight = 50;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(new Button(this.leftPos + 5, this.topPos + 4, 68, 20, Component.literal(""), (button) -> {
			this.menu.send(0, (pb) -> {
			});
		}));
	}

	private static String modeLabel(MODE mode)
	{
		switch (mode)
		{
			case IGNITE:
				return I18n.get("gui.igniter.ignite");
			case TOGGLE:
			default:
				return I18n.get("gui.igniter.toggle");
		}
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
	{

		MODE mode = MODE.values()[this.menu.mode.get()];
		ScreenWidgets.buttons(this).get(0).setMessage(Component.literal(modeLabel(mode)));

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
