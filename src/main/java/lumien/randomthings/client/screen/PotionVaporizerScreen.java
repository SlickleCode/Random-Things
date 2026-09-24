package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.PotionVaporizerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * PotionVaporizerScreen
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

		int fuelBurn = this.container.fuelBurn.get();
		int fuelBurnTime = this.container.fuelBurnTime.get();
		String fuelText = fuelBurn > 0 ? (fuelBurnTime * 100 / fuelBurn) + "%" : "";
		this.font.drawString(fuelText, 8, 62, 0);

		int duration = this.container.duration.get();
		int durationLeft = this.container.durationLeft.get();
		String durationText = duration > 0 ? (durationLeft * 100 / duration) + "%" : I18n.format("gui.randomthings.potion_vaporizer.empty");
		this.font.drawString(durationText, 60, 38, this.container.color.get() != 0 ? (this.container.color.get() | 0xFF000000) : 0x404040);

		RenderHelper.enableGUIStandardItemLighting();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(i, j, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground();
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
	}
}
