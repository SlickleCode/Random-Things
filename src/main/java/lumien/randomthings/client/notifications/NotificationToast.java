package lumien.randomthings.client.notifications;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A vanilla-style toast showing a title/description and an item icon,
 * triggered by {@code NotificationInterfaceBlock}. Layout ground-truthed via
 * bytecode disassembly of vanilla's own {@code SystemToast.draw}.
 */
public class NotificationToast implements Toast
{
	private static final net.minecraft.resources.ResourceLocation BACKGROUND_SPRITE = new net.minecraft.resources.ResourceLocation("toast/system");
	private static final long DISPLAY_TIME_MS = 5000L;

	private final String title;
	private final String description;
	private final ItemStack icon;

	private long firstDrawTime;
	private boolean newDisplay = true;

	public NotificationToast(String title, String description, ItemStack icon)
	{
		this.title = title;
		this.description = description;
		this.icon = icon;
	}

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent toastGui, long delta)
	{
		if (newDisplay)
		{
			firstDrawTime = delta;
			newDisplay = false;
		}

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, 160, 32);

		graphics.drawString(toastGui.getMinecraft().font, title, 30, 7, 0xFFFFFF00, false);
		graphics.drawString(toastGui.getMinecraft().font, description, 30, 18, 0xFFFFFFFF, false);

		if (!icon.isEmpty())
		{
			graphics.renderItem(icon, 8, 8);
		}

		return delta - firstDrawTime < DISPLAY_TIME_MS ? Visibility.SHOW : Visibility.HIDE;
	}
}
