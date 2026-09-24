package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.RedstoneObserverContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * RedstoneObserverScreen - read-only status display.
 */
public class RedstoneObserverScreen extends ContainerScreen<RedstoneObserverContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/redstone_observer.png");

	public RedstoneObserverScreen(RedstoneObserverContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 136;
		this.ySize = 54;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		if (this.container.hasTarget.get() == 0)
		{
			String center = I18n.format("gui.randomthings.redstone_observer.no_target");
			this.font.drawString(center, xSize / 2 - this.font.getStringWidth(center) / 2, ySize / 2 - this.font.FONT_HEIGHT / 2, 0x960000);
		}
		else
		{
			this.font.drawString(I18n.format("gui.randomthings.redstone_observer.target_x", this.container.targetX.get()), 8, 18, 0x140054);
			this.font.drawString(I18n.format("gui.randomthings.redstone_observer.target_y", this.container.targetY.get()), 8, 28, 0x140054);
			this.font.drawString(I18n.format("gui.randomthings.redstone_observer.target_z", this.container.targetZ.get()), 8, 38, 0x140054);
		}

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
