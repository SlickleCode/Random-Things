package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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

	public ItemSlotButton(int x, int y, ItemStack stack, String tooltip, IPressable onPress)
	{
		super(x, y, 18, 18, "", onPress);

		this.stack = stack;
		this.tooltip = tooltip;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();

		GlStateManager.disableLighting();
		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		this.blit(this.x, this.y, 0, this.isHovered() ? 18 : 0, 18, 18);

		if (!this.stack.isEmpty())
		{
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableDepthTest();
			mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, this.stack, this.x + 1, this.y + 1);
			RenderHelper.disableStandardItemLighting();
		}
	}

	@Override
	public String getTooltip()
	{
		return this.tooltip;
	}
}
