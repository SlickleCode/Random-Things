package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.IronDropperContainer;
import lumien.randomthings.tileentity.IronDropperTileEntity.EFFECTS;
import lumien.randomthings.tileentity.IronDropperTileEntity.PICKUP_DELAY;
import lumien.randomthings.tileentity.IronDropperTileEntity.REDSTONE_MODE;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * IronDropperScreen. The original's icon-sprite mode buttons are replaced
 * with plain text cycle buttons, matching IgniterScreen's convention (kept
 * consistent across the mod rather than porting a second custom button/
 * tooltip-icon widget system for this one screen).
 */
public class IronDropperScreen extends AbstractContainerScreen<IronDropperContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/iron_dropper.png");

	public IronDropperScreen(IronDropperContainer screenContainer, Inventory inv, Component titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void init()
	{
		super.init();

		// Sprite buttons, positions/sheets from 1.12.2's GuiIronDropper (2x2 grid at x125/150, y16/41)
		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 125, this.topPos + 16, 20, 20, tex("redstone_mode"), () -> this.menu.redstoneMode.get(), state -> redstoneModeLabel(REDSTONE_MODE.values()[state]), (button) -> {
			this.menu.send(0, (pb) -> {
			});
		}));

		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 150, this.topPos + 16, 20, 20, tex("pickup_delay"), () -> this.menu.pickupDelay.get(), state -> pickupDelayLabel(PICKUP_DELAY.values()[state]), (button) -> {
			this.menu.send(1, (pb) -> {
			});
		}));

		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 125, this.topPos + 41, 20, 20, tex("random_motion"), () -> this.menu.randomMotion.get() != 0 ? 1 : 0, state -> I18n.get(state != 0 ? "gui.randomthings.iron_dropper.motion_random" : "gui.randomthings.iron_dropper.motion_straight"), (button) -> {
			this.menu.send(2, (pb) -> {
			});
		}));

		this.addRenderableWidget(new SpriteStateButton(this.leftPos + 150, this.topPos + 41, 20, 20, tex("effects"), () -> this.menu.effects.get(), state -> effectsLabel(EFFECTS.values()[state]), (button) -> {
			this.menu.send(3, (pb) -> {
			});
		}));
	}

	private static ResourceLocation tex(String name)
	{
		return new ResourceLocation("randomthings:textures/gui/iron_dropper/" + name + ".png");
	}

	private static String redstoneModeLabel(REDSTONE_MODE mode)
	{
		switch (mode)
		{
			case PULSE:
				return I18n.get("gui.randomthings.iron_dropper.pulse");
			case REPEAT_POWERED:
			default:
				return I18n.get("gui.randomthings.iron_dropper.repeat_powered");
		}
	}

	private static String pickupDelayLabel(PICKUP_DELAY delay)
	{
		switch (delay)
		{
			case NONE:
				return I18n.get("gui.randomthings.iron_dropper.delay_none");
			case TICKS_20:
				return I18n.get("gui.randomthings.iron_dropper.delay_20");
			case TICKS_5:
			default:
				return I18n.get("gui.randomthings.iron_dropper.delay_5");
		}
	}

	private static String effectsLabel(EFFECTS effects)
	{
		switch (effects)
		{
			case SOUND:
				return I18n.get("gui.randomthings.iron_dropper.effects_sound");
			case PARTICLE:
				return I18n.get("gui.randomthings.iron_dropper.effects_particle");
			case SOUND_PARTICLE:
				return I18n.get("gui.randomthings.iron_dropper.effects_both");
			case NONE:
			default:
				return I18n.get("gui.randomthings.iron_dropper.effects_none");
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{

		graphics.drawString(this.font, this.title.getString(), 8, 6, 4210752, false);
		SpriteStateButton.renderTooltips(graphics, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);

	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		graphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void render(GuiGraphics graphics, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		super.render(graphics, p_render_1_, p_render_2_, p_render_3_);
		this.renderTooltip(graphics, p_render_1_, p_render_2_);
	}
}
