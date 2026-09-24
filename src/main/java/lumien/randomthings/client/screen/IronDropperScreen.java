package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.IronDropperContainer;
import lumien.randomthings.tileentity.IronDropperTileEntity.EFFECTS;
import lumien.randomthings.tileentity.IronDropperTileEntity.PICKUP_DELAY;
import lumien.randomthings.tileentity.IronDropperTileEntity.REDSTONE_MODE;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * IronDropperScreen. The original's icon-sprite mode buttons are replaced
 * with plain text cycle buttons, matching IgniterScreen's convention (kept
 * consistent across the mod rather than porting a second custom button/
 * tooltip-icon widget system for this one screen).
 */
public class IronDropperScreen extends ContainerScreen<IronDropperContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/iron_dropper.png");

	public IronDropperScreen(IronDropperContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 176;
		this.ySize = 166;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 120, this.guiTop + 5, 50, 20, "", (button) -> {
			this.container.send(0, (pb) -> {
			});
		}));

		this.addButton(new Button(this.guiLeft + 120, this.guiTop + 27, 50, 20, "", (button) -> {
			this.container.send(1, (pb) -> {
			});
		}));

		this.addButton(new Button(this.guiLeft + 120, this.guiTop + 49, 50, 20, "", (button) -> {
			this.container.send(2, (pb) -> {
			});
		}));

		this.addButton(new Button(this.guiLeft + 120, this.guiTop + 71, 50, 20, "", (button) -> {
			this.container.send(3, (pb) -> {
			});
		}));
	}

	private static String redstoneModeLabel(REDSTONE_MODE mode)
	{
		switch (mode)
		{
			case PULSE:
				return I18n.format("gui.randomthings.iron_dropper.pulse");
			case REPEAT_POWERED:
			default:
				return I18n.format("gui.randomthings.iron_dropper.repeat_powered");
		}
	}

	private static String pickupDelayLabel(PICKUP_DELAY delay)
	{
		switch (delay)
		{
			case NONE:
				return I18n.format("gui.randomthings.iron_dropper.delay_none");
			case TICKS_20:
				return I18n.format("gui.randomthings.iron_dropper.delay_20");
			case TICKS_5:
			default:
				return I18n.format("gui.randomthings.iron_dropper.delay_5");
		}
	}

	private static String effectsLabel(EFFECTS effects)
	{
		switch (effects)
		{
			case SOUND:
				return I18n.format("gui.randomthings.iron_dropper.effects_sound");
			case PARTICLE:
				return I18n.format("gui.randomthings.iron_dropper.effects_particle");
			case SOUND_PARTICLE:
				return I18n.format("gui.randomthings.iron_dropper.effects_both");
			case NONE:
			default:
				return I18n.format("gui.randomthings.iron_dropper.effects_none");
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		RenderHelper.disableStandardItemLighting();

		REDSTONE_MODE mode = REDSTONE_MODE.values()[this.container.redstoneMode.get()];
		this.buttons.get(0).setMessage(redstoneModeLabel(mode));

		PICKUP_DELAY delay = PICKUP_DELAY.values()[this.container.pickupDelay.get()];
		this.buttons.get(1).setMessage(pickupDelayLabel(delay));

		this.buttons.get(2).setMessage(this.container.randomMotion.get() != 0 ? I18n.format("gui.randomthings.iron_dropper.motion_random") : I18n.format("gui.randomthings.iron_dropper.motion_straight"));

		EFFECTS effects = EFFECTS.values()[this.container.effects.get()];
		this.buttons.get(3).setMessage(effectsLabel(effects));

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
