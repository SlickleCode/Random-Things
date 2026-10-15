package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.EntityDetectorContainer;
import lumien.randomthings.tileentity.EntityDetectorTileEntity.FILTER;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.TextComponent;

/**
 * EntityDetectorScreen. The original's icon-sprite invert/strong-output
 * toggle buttons are replaced with plain text buttons, matching
 * IgniterScreen's convention.
 */
public class EntityDetectorScreen extends AbstractContainerScreen<EntityDetectorContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/entity_detector.png");

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

		this.addRenderableWidget(new Button(this.leftPos + 39, this.topPos + 25, 10, 10, new TextComponent("-"), (b) -> this.menu.send(0, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 25, 10, 10, new TextComponent("+"), (b) -> this.menu.send(1, (pb) -> {
		})));

		this.addRenderableWidget(new Button(this.leftPos + 39, this.topPos + 45, 10, 10, new TextComponent("-"), (b) -> this.menu.send(2, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 45, 10, 10, new TextComponent("+"), (b) -> this.menu.send(3, (pb) -> {
		})));

		this.addRenderableWidget(new Button(this.leftPos + 39, this.topPos + 65, 10, 10, new TextComponent("-"), (b) -> this.menu.send(4, (pb) -> {
		})));
		this.addRenderableWidget(new Button(this.leftPos + 119, this.topPos + 65, 10, 10, new TextComponent("+"), (b) -> this.menu.send(5, (pb) -> {
		})));

		// 1.12.2 layout: filter text button 70x16 at (20,95); the two 20x20 sprite toggles
		// (invert at 92, strong output at 115, both y93) come from the GUI texture at (176,0)/(216,0)
		this.addRenderableWidget(new Button(this.leftPos + 20, this.topPos + 95, 70, 16, new TextComponent(""), (b) -> this.menu.send(6, (pb) -> {
		})));

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
	protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
	{

		this.font.draw(matrixStack, I18n.get("block.randomthings.entity_detector"), 22, 6, 4210752);

		String radiusX = I18n.get("gui.randomthings.entity_detector.range_x", this.menu.rangeX.get());
		this.font.draw(matrixStack, radiusX, imageWidth / 2 - this.font.width(radiusX) / 2 - 3, 26, 4210752);

		String radiusY = I18n.get("gui.randomthings.entity_detector.range_y", this.menu.rangeY.get());
		this.font.draw(matrixStack, radiusY, imageWidth / 2 - this.font.width(radiusY) / 2 - 3, 46, 4210752);

		String radiusZ = I18n.get("gui.randomthings.entity_detector.range_z", this.menu.rangeZ.get());
		this.font.draw(matrixStack, radiusZ, imageWidth / 2 - this.font.width(radiusZ) / 2 - 3, 66, 4210752);

		FILTER filter = FILTER.values()[this.menu.filter.get()];
		ScreenWidgets.buttons(this).get(6).setMessage(new TextComponent(filterLabel(filter)));

		SpriteStateButton.renderTooltips(matrixStack, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);

	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
	}
}
