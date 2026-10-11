package lumien.randomthings.client.screen;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.StringTextComponent;
import com.mojang.blaze3d.matrix.MatrixStack;

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

	public SpriteStateButton(int x, int y, int width, int height, ResourceLocation texture, IntSupplier state, IntFunction<String> tooltip, IPressable onPress)
	{
		super(x, y, width, height, new StringTextComponent(""), onPress);

		this.texture = texture;
		this.state = state;
		this.tooltip = tooltip;
	}

	@Override
	public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(this.texture);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		this.blit(matrixStack, this.x, this.y, this.state.getAsInt() * this.width, this.isHovered() ? this.height : 0, this.width, this.height);
	}

	@Override
	public String getTooltip()
	{
		return this.tooltip == null ? null : this.tooltip.apply(this.state.getAsInt());
	}

	/**
	 * Call from {@code drawGuiContainerForegroundLayer} (coordinates there are relative to the
	 * GUI's top-left corner) to show the tooltip of whichever {@link SpriteStateButton} is hovered.
	 */
	public static void renderTooltips(MatrixStack matrixStack, Screen screen, List<? extends Widget> buttons, int mouseX, int mouseY, int guiLeft, int guiTop)
	{
		for (Widget widget : buttons)
		{
			if (widget instanceof TooltipButton && widget.isHovered())
			{
				String text = ((TooltipButton) widget).getTooltip();

				if (text != null)
				{
					screen.renderTooltip(matrixStack, new StringTextComponent(text), mouseX - guiLeft, mouseY - guiTop);
				}

				return;
			}
		}
	}
}
