package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.FilteredSuperLubricentPlatformContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * FilteredSuperLubricentPlatformScreen
 */
public class FilteredSuperLubricentPlatformScreen extends AbstractContainerScreen<FilteredSuperLubricentPlatformContainer>
{
	private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/filtered_super_lubricent_platform.png");

	public FilteredSuperLubricentPlatformScreen(FilteredSuperLubricentPlatformContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 129;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(this.font, net.minecraft.client.resources.language.I18n.get("container.inventory"), 8, 37, 0xFF404040, false);
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
