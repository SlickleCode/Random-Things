package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Port of 1.12.2's {@code GuiEnumButton}/{@code GuiBoolButton}: a button drawn from its own
 * sprite sheet, one column per state ({@code state * width}) and a second row ({@code height}
 * lower) for the hovered look. The current state is read from a supplier (a synced container
 * field) and the hover tooltip comes from a per-state function.
 */
public class SpriteStateButton extends Button implements TooltipButton
{
	private final ResourceLocation texture;
	private final IntSupplier state;
	private final IntFunction<String> tooltip;

	public SpriteStateButton(int x, int y, int width, int height, ResourceLocation texture, IntSupplier state, IntFunction<String> tooltip, OnPress onPress)
	{
		super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);

		this.texture = texture;
		this.state = state;
		this.tooltip = tooltip;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.texture);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		graphics.blit(this.texture, this.getX(), this.getY(), this.state.getAsInt() * this.width, this.isHovered ? this.height : 0, this.width, this.height);
	}

	@Override
	public String getTooltipText()
	{
		return this.tooltip == null ? null : this.tooltip.apply(this.state.getAsInt());
	}

	/**
	 * Call from {@code drawGuiContainerForegroundLayer} (coordinates there are relative to the
	 * GUI's top-left corner) to show the tooltip of whichever {@link SpriteStateButton} is hovered.
	 */
	public static void renderTooltips(GuiGraphics graphics, Screen screen, List<? extends AbstractWidget> buttons, int mouseX, int mouseY, int guiLeft, int guiTop)
	{
		for (AbstractWidget widget : buttons)
		{
			if (widget instanceof TooltipButton && widget.isHoveredOrFocused())
			{
				String text = ((TooltipButton) widget).getTooltipText();

				if (text != null)
				{
					graphics.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, Component.literal(text), mouseX - guiLeft, mouseY - guiTop);
				}

				return;
			}
		}
	}
}
