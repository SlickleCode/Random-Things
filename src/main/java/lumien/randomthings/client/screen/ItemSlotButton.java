package lumien.randomthings.client.screen;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Port of 1.12.2's {@code GuiSlotButton}: an 18x18 slot-shaped button (sprite at (0,0), hover
 * variant 18px lower in {@code slot_button.png}) that draws an item icon inside it and shows an
 * optional tooltip (the Position Filter's custom name for the Redstone Remote's buttons).
 */
public class ItemSlotButton extends Button implements TooltipButton
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("randomthings:textures/gui/slot_button.png");

	private final ItemStack stack;
	private final String tooltip;

	public ItemSlotButton(int x, int y, ItemStack stack, String tooltip, OnPress onPress)
	{
		super(x, y, 18, 18, Component.literal(""), onPress);

		this.stack = stack;
		this.tooltip = tooltip;
	}

	@Override
	public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();
		this.blit(matrixStack, this.x, this.y, 0, this.isHovered ? 18 : 0, 18, 18);

		if (!this.stack.isEmpty())
		{
			RenderSystem.enableDepthTest();
			mc.getItemRenderer().renderAndDecorateItem(mc.player, this.stack, this.x + 1, this.y + 1, 0);
		}
	}

	@Override
	public String getTooltip()
	{
		return this.tooltip;
	}
}
