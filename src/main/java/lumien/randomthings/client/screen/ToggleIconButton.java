package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

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

	public ToggleIconButton(int x, int y, int width, int height, ResourceLocation texture, int u, int v, BooleanSupplier value, OnPress onPress)
	{
		super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);

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
	public String getTooltipText()
	{
		return this.tooltip == null ? null : this.tooltip.apply(this.value.getAsBoolean());
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
	{
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.texture);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(this.texture, this.getX(), this.getY(), this.u + (this.value.getAsBoolean() ? 20 : 0), this.v + (this.isHovered ? 20 : 0), this.width, this.height);
	}
}
