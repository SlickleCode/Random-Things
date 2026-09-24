package lumien.randomthings.client.notifications;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.item.ItemStack;

/**
 * A vanilla-style toast showing a title/description and an item icon,
 * triggered by {@code NotificationInterfaceBlock}. Layout ground-truthed via
 * bytecode disassembly of vanilla's own {@code SystemToast.draw}.
 */
public class NotificationToast implements IToast
{
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
	public Visibility draw(ToastGui toastGui, long delta)
	{
		if (newDisplay)
		{
			firstDrawTime = delta;
			newDisplay = false;
		}

		toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		toastGui.blit(0, 0, 0, 0, 160, 32);

		toastGui.getMinecraft().fontRenderer.drawString(title, 30, 7, 0xFFFFFF00);
		toastGui.getMinecraft().fontRenderer.drawString(description, 30, 18, 0xFFFFFFFF);

		if (!icon.isEmpty())
		{
			toastGui.getMinecraft().getItemRenderer().renderItemAndEffectIntoGUI(icon, 8, 8);
		}

		return delta - firstDrawTime < DISPLAY_TIME_MS ? Visibility.SHOW : Visibility.HIDE;
	}
}
