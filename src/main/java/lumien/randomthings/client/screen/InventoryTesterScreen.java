package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

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

/**
 * InventoryTesterScreen. The original's icon-sprite toggle button is
 * replaced with a plain text button, matching IgniterScreen's convention.
 */
public class InventoryTesterScreen extends AbstractContainerScreen<InventoryTesterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("randomthings:textures/gui/inventory_tester.png");

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
		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 93, this.topPos + 16, 20, 20, net.minecraft.resources.ResourceLocation.parse("randomthings:textures/gui/inventory_tester/invert_signal.png"), () -> this.menu.invertSignal.get() != 0 ? 1 : 0, state -> I18n.get(state != 0 ? "gui.randomthings.inventory_tester.inverted" : "gui.randomthings.inventory_tester.normal"), (button) -> {
			this.menu.send(0, (pb) -> {
			});
		}));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(this.font, I18n.get("block.randomthings.inventory_tester"), 33, 6, 4210752, false);
		SpriteStateButton.renderTooltips(graphics, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(net.minecraft.client.renderer.RenderType::guiTextured, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}
}
