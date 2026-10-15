package lumien.randomthings.client.notifications;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A vanilla-style toast showing a title/description and an item icon,
 * triggered by {@code NotificationInterfaceBlock}. Layout ground-truthed via
 * bytecode disassembly of vanilla's own {@code SystemToast.draw}.
 */
public class NotificationToast implements Toast
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
	public Visibility render(PoseStack matrixStack, ToastComponent toastGui, long delta)
	{
		if (newDisplay)
		{
			firstDrawTime = delta;
			newDisplay = false;
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		toastGui.blit(matrixStack, 0, 0, 0, 0, 160, 32);

		toastGui.getMinecraft().font.draw(matrixStack, title, 30, 7, 0xFFFFFF00);
		toastGui.getMinecraft().font.draw(matrixStack, description, 30, 18, 0xFFFFFFFF);

		if (!icon.isEmpty())
		{
			toastGui.getMinecraft().getItemRenderer().renderAndDecorateItem(icon, 8, 8);
		}

		return delta - firstDrawTime < DISPLAY_TIME_MS ? Visibility.SHOW : Visibility.HIDE;
	}
}
