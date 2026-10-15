package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.InventoryTesterContainer;
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
 * InventoryTesterScreen. The original's icon-sprite toggle button is
 * replaced with a plain text button, matching IgniterScreen's convention.
 */
public class InventoryTesterScreen extends AbstractContainerScreen<InventoryTesterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/inventory_tester.png");

	public InventoryTesterScreen(InventoryTesterContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 136;
	}

	@Override
	protected void init()
	{
		super.init();

		// 20x20 sprite toggle at the original position (93,16)
		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 93, this.topPos + 16, 20, 20, new net.minecraft.resources.ResourceLocation("randomthings:textures/gui/inventory_tester/invert_signal.png"), () -> this.menu.invertSignal.get() != 0 ? 1 : 0, state -> I18n.get(state != 0 ? "gui.randomthings.inventory_tester.inverted" : "gui.randomthings.inventory_tester.normal"), (button) -> {
			this.menu.send(0, (pb) -> {
			});
		}));
	}

	@Override
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
	{
		this.font.draw(matrixStack, I18n.get("block.randomthings.inventory_tester"), 33, 6, 4210752);
		SpriteStateButton.renderTooltips(matrixStack, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);

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
