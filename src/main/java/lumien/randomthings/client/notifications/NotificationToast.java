package lumien.randomthings.client.notifications;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.ToastManager;
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
	private static final net.minecraft.resources.ResourceLocation BACKGROUND_SPRITE = net.minecraft.resources.ResourceLocation.parse("toast/system");
	private static final long DISPLAY_TIME_MS = 5000L;

	private final String title;
	private final String description;
	private final ItemStack icon;

	private long firstDrawTime;
	private boolean newDisplay = true;
	private Visibility visibility = Visibility.SHOW;

	public NotificationToast(String title, String description, ItemStack icon)
	{
		this.title = title;
		this.description = description;
		this.icon = icon;
	}

	@Override
	public Visibility getWantedVisibility()
	{
		return visibility;
	}

	@Override
	public void update(ToastManager toastManager, long time)
	{
		if (newDisplay)
		{
			firstDrawTime = time;
			newDisplay = false;
		}

		visibility = time - firstDrawTime < DISPLAY_TIME_MS ? Visibility.SHOW : Visibility.HIDE;
	}

	@Override
	public void render(GuiGraphics graphics, Font font, long time)
	{
		graphics.blitSprite(net.minecraft.client.renderer.RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, 160, 32);

		graphics.drawString(font, title, 30, 7, 0xFFFFFF00, false);
		graphics.drawString(font, description, 30, 18, 0xFFFFFFFF, false);

		if (!icon.isEmpty())
		{
			graphics.renderItem(icon, 8, 8);
		}
	}
}
