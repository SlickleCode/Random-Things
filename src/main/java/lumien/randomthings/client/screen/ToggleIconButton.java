package lumien.randomthings.client.screen;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

/**
 * Port of 1.12.2's {@code GuiCustomButton}: a two-state sprite button drawn straight from the
 * GUI's own background texture - the sprite for "off" sits at (u, v), "on" at (u + 20, v), and
 * the hover variant of each is the same sprite 20px lower. The value is read from a supplier
 * (e.g. a synced container field) so it always reflects the server's state.
 */
public class ToggleIconButton extends Button implements TooltipButton
{
	private final ResourceLocation texture;
	private final int u;
	private final int v;
	private final BooleanSupplier value;
	private Function<Boolean, String> tooltip;

	public ToggleIconButton(int x, int y, int width, int height, ResourceLocation texture, int u, int v, BooleanSupplier value, IPressable onPress)
	{
		super(x, y, width, height, "", onPress);

		this.texture = texture;
		this.u = u;
		this.v = v;
		this.value = value;
	}

	public ToggleIconButton setTooltip(Function<Boolean, String> tooltip)
	{
		this.tooltip = tooltip;
		return this;
	}

	@Override
	public String getTooltip()
	{
		return this.tooltip == null ? null : this.tooltip.apply(this.value.getAsBoolean());
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft.getInstance().getTextureManager().bindTexture(this.texture);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.blit(this.x, this.y, this.u + (this.value.getAsBoolean() ? 20 : 0), this.v + (this.isHovered() ? 20 : 0), this.width, this.height);
	}
}
