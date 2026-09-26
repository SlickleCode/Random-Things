package lumien.randomthings.client.screen;

import java.awt.Color;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.PotionVaporizerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Port of 1.12.2's {@code GuiPotionVaporizer}: a shrinking flame (fuel burn)
 * and a draining, potion-colored tank (effect duration), both cropped from
 * the same GUI texture sheet the original used (byte-identical, confirmed -
 * the sprite regions this needs were already there). Replaces this port's
 * earlier placeholder - two plain percentage-text readouts, which the user
 * found confusing and noted didn't show fuel burning the way a furnace does
 * - with the real sprites the original always had.
 */
public class PotionVaporizerScreen extends ContainerScreen<PotionVaporizerContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/potion_vaporizer.png");

	public PotionVaporizerScreen(PotionVaporizerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();
		this.font.drawString(this.title.getString(), 8, 6, 4210752);
		RenderHelper.enableGUIStandardItemLighting();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		this.blit(x, y, 0, 0, this.xSize, this.ySize);

		int duration = this.container.duration.get();

		if (duration != 0)
		{
			int durationLeft = this.container.durationLeft.get();
			int tankProgress = (int) Math.floor(14F - (14F / duration * durationLeft));

			Color c = new Color(this.container.color.get());
			GlStateManager.color4f(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, 1.0F);
			this.blit(x + 81, y + 18 + tankProgress, 176, 30, 14, 14 - tankProgress);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		this.blit(x + 80, y + 17, 176, 14, 16, 16);

		int fuelBurnTime = this.container.fuelBurnTime.get();

		if (fuelBurnTime > 0)
		{
			int fuelBurn = this.container.fuelBurn.get();
			int fuelProgress = 15 - (int) Math.floor(14F - (14F / fuelBurn * fuelBurnTime));
			this.blit(x + 81, y + 50 - fuelProgress, 176, 14 - fuelProgress, 14, fuelProgress);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
}
