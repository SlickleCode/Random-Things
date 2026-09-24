package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.EntityDetectorContainer;
import lumien.randomthings.tileentity.EntityDetectorTileEntity.FILTER;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * EntityDetectorScreen. The original's icon-sprite invert/strong-output
 * toggle buttons are replaced with plain text buttons, matching
 * IgniterScreen's convention.
 */
public class EntityDetectorScreen extends ContainerScreen<EntityDetectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/entity_detector.png");

	public EntityDetectorScreen(EntityDetectorContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 204;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 25, 10, 10, "-", (b) -> this.container.send(0, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 25, 10, 10, "+", (b) -> this.container.send(1, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 45, 10, 10, "-", (b) -> this.container.send(2, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 45, 10, 10, "+", (b) -> this.container.send(3, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 39, this.guiTop + 65, 10, 10, "-", (b) -> this.container.send(4, (pb) -> {
		})));
		this.addButton(new Button(this.guiLeft + 119, this.guiTop + 65, 10, 10, "+", (b) -> this.container.send(5, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 20, this.guiTop + 93, 70, 16, "", (b) -> this.container.send(6, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 92, this.guiTop + 93, 30, 20, "", (b) -> this.container.send(7, (pb) -> {
		})));

		this.addButton(new Button(this.guiLeft + 115, this.guiTop + 93, 30, 20, "", (b) -> this.container.send(8, (pb) -> {
		})));
	}

	private static String filterLabel(FILTER filter)
	{
		return I18n.format("gui.randomthings.entity_detector.filter." + filter.name().toLowerCase());
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		this.font.drawString(I18n.format("block.randomthings.entity_detector"), 22, 6, 0);

		String radiusX = I18n.format("gui.randomthings.entity_detector.range_x", this.container.rangeX.get());
		this.font.drawString(radiusX, xSize / 2 - this.font.getStringWidth(radiusX) / 2 - 3, 26, 0);

		String radiusY = I18n.format("gui.randomthings.entity_detector.range_y", this.container.rangeY.get());
		this.font.drawString(radiusY, xSize / 2 - this.font.getStringWidth(radiusY) / 2 - 3, 46, 0);

		String radiusZ = I18n.format("gui.randomthings.entity_detector.range_z", this.container.rangeZ.get());
		this.font.drawString(radiusZ, xSize / 2 - this.font.getStringWidth(radiusZ) / 2 - 3, 66, 0);

		FILTER filter = FILTER.values()[this.container.filter.get()];
		this.buttons.get(6).setMessage(filterLabel(filter));

		boolean invert = this.container.invert.get() != 0;
		this.buttons.get(7).setMessage(I18n.format(invert ? "gui.randomthings.entity_detector.inverted" : "gui.randomthings.entity_detector.normal"));

		boolean strong = this.container.strongOutput.get() != 0;
		this.buttons.get(8).setMessage(I18n.format(strong ? "gui.randomthings.entity_detector.strong" : "gui.randomthings.entity_detector.weak"));

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
