package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import java.awt.Color;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.PotionVaporizerContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Port of 1.12.2's {@code GuiPotionVaporizer}: a shrinking flame (fuel burn)
 * and a draining, potion-colored tank (effect duration), both cropped from
 * the same GUI texture sheet the original used (byte-identical, confirmed -
 * the sprite regions this needs were already there). Replaces this port's
 * earlier placeholder - two plain percentage-text readouts, which the user
 * found confusing and noted didn't show fuel burning the way a furnace does
 * - with the real sprites the original always had.
 */
public class PotionVaporizerScreen extends AbstractContainerScreen<PotionVaporizerContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/potion_vaporizer.png");

	public PotionVaporizerScreen(PotionVaporizerContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;
		graphics.blit(GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight);

		int duration = this.menu.duration.get();

		if (duration != 0)
		{
			int durationLeft = this.menu.durationLeft.get();
			int tankProgress = (int) Math.floor(14F - (14F / duration * durationLeft));

			Color c = new Color(this.menu.color.get());
			RenderSystem.setShaderColor(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, 1.0F);
			graphics.blit(GUI_TEXTURES, x + 81, y + 18 + tankProgress, 176, 30, 14, 14 - tankProgress);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		graphics.blit(GUI_TEXTURES, x + 80, y + 17, 176, 14, 16, 16);

		int fuelBurnTime = this.menu.fuelBurnTime.get();

		if (fuelBurnTime > 0)
		{
			int fuelBurn = this.menu.fuelBurn.get();
			int fuelProgress = 15 - (int) Math.floor(14F - (14F / fuelBurn * fuelBurnTime));
			graphics.blit(GUI_TEXTURES, x + 81, y + 50 - fuelProgress, 176, 14 - fuelProgress, 14, fuelProgress);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}
}
