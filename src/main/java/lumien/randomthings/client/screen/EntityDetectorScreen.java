package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.EntityDetectorContainer;
import lumien.randomthings.tileentity.EntityDetectorTileEntity.FILTER;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * EntityDetectorScreen. The original's icon-sprite invert/strong-output
 * toggle buttons are replaced with plain text buttons, matching
 * IgniterScreen's convention.
 */
public class EntityDetectorScreen extends AbstractContainerScreen<EntityDetectorContainer>
{
	private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/entity_detector.png");

	public EntityDetectorScreen(EntityDetectorContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 204;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(0, (pb) -> {
		})).bounds(this.leftPos + 39, this.topPos + 25, 10, 10).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(1, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 25, 10, 10).build());

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(2, (pb) -> {
		})).bounds(this.leftPos + 39, this.topPos + 45, 10, 10).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(3, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 45, 10, 10).build());

		this.addRenderableWidget(Button.builder(Component.literal("-"), (b) -> this.menu.send(4, (pb) -> {
		})).bounds(this.leftPos + 39, this.topPos + 65, 10, 10).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), (b) -> this.menu.send(5, (pb) -> {
		})).bounds(this.leftPos + 119, this.topPos + 65, 10, 10).build());

		// 1.12.2 layout: filter text button 70x16 at (20,95); the two 20x20 sprite toggles
		// (invert at 92, strong output at 115, both y93) come from the GUI texture at (176,0)/(216,0)
		this.addRenderableWidget(Button.builder(Component.literal(""), (b) -> this.menu.send(6, (pb) -> {
		})).bounds(this.leftPos + 20, this.topPos + 95, 70, 16).build());

		this.addRenderableWidget(new ToggleIconButton(this.leftPos + 92, this.topPos + 93, 20, 20, GUI_TEXTURES, 176, 0, () -> this.menu.invert.get() != 0, (b) -> this.menu.send(7, (pb) -> {
		})).setTooltip(on -> I18n.get(on ? "gui.randomthings.entity_detector.inverted" : "gui.randomthings.entity_detector.normal")));

		this.addRenderableWidget(new ToggleIconButton(this.leftPos + 115, this.topPos + 93, 20, 20, GUI_TEXTURES, 216, 0, () -> this.menu.strongOutput.get() != 0, (b) -> this.menu.send(8, (pb) -> {
		})).setTooltip(on -> I18n.get(on ? "gui.randomthings.entity_detector.strong" : "gui.randomthings.entity_detector.weak")));
	}

	private static String filterLabel(FILTER filter)
	{
		return I18n.get("gui.randomthings.entity_detector.filter." + filter.name().toLowerCase());
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, I18n.get("block.randomthings.entity_detector"), 22, 6, 0xFF404040, false);

		String radiusX = I18n.get("gui.randomthings.entity_detector.range_x", this.menu.rangeX.get());
		graphics.drawString(this.font, radiusX, imageWidth / 2 - this.font.width(radiusX) / 2 - 3, 26, 0xFF404040, false);

		String radiusY = I18n.get("gui.randomthings.entity_detector.range_y", this.menu.rangeY.get());
		graphics.drawString(this.font, radiusY, imageWidth / 2 - this.font.width(radiusY) / 2 - 3, 46, 0xFF404040, false);

		String radiusZ = I18n.get("gui.randomthings.entity_detector.range_z", this.menu.rangeZ.get());
		graphics.drawString(this.font, radiusZ, imageWidth / 2 - this.font.width(radiusZ) / 2 - 3, 66, 0xFF404040, false);

		FILTER filter = FILTER.values()[this.menu.filter.get()];
		ScreenWidgets.buttons(this).get(6).setMessage(Component.literal(filterLabel(filter)));

		SpriteStateButton.renderTooltips(graphics, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);

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
