package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.InventoryTesterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * InventoryTesterScreen. The original's icon-sprite toggle button is
 * replaced with a plain text button, matching IgniterScreen's convention.
 */
public class InventoryTesterScreen extends ContainerScreen<InventoryTesterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/inventory_tester.png");

	public InventoryTesterScreen(InventoryTesterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 136;
	}

	@Override
	protected void init()
	{
		super.init();

		// 20x20 sprite toggle at the original position (93,16)
		this.addButton(new SpriteStateButton(this.guiLeft + 93, this.guiTop + 16, 20, 20, new net.minecraft.util.ResourceLocation("randomthings:textures/gui/inventory_tester/invert_signal.png"), () -> this.container.invertSignal.get() != 0 ? 1 : 0, state -> I18n.format(state != 0 ? "gui.randomthings.inventory_tester.inverted" : "gui.randomthings.inventory_tester.normal"), (button) -> {
			this.container.send(0, (pb) -> {
			});
		}));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.font.drawString(I18n.format("block.randomthings.inventory_tester"), 33, 6, 4210752);
		SpriteStateButton.renderTooltips(this, this.buttons, mouseX, mouseY, this.guiLeft, this.guiTop);

		RenderHelper.disableStandardItemLighting();
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
