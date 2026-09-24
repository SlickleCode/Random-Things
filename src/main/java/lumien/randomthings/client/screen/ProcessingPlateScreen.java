package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.ProcessingPlateContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * ProcessingPlateScreen
 */
public class ProcessingPlateScreen extends ContainerScreen<ProcessingPlateContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/processing_plate.png");

	public ProcessingPlateScreen(ProcessingPlateContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 140;
		this.ySize = 60;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 10, this.guiTop + 15, 120, 20, "", (button) -> {
			this.container.send(0, (pb) -> {
			});
		}));
		this.addButton(new Button(this.guiLeft + 10, this.guiTop + 37, 120, 20, "", (button) -> {
			this.container.send(1, (pb) -> {
			});
		}));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		Direction insert = Direction.byIndex(this.container.insertFacing.get());
		Direction extract = Direction.byIndex(this.container.extractFacing.get());

		this.buttons.get(0).setMessage(I18n.format("gui.randomthings.plate_processing.insert") + ": " + insert.getName());
		this.buttons.get(1).setMessage(I18n.format("gui.randomthings.plate_processing.extract") + ": " + extract.getName());

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
