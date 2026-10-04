package lumien.randomthings.client.screen;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;

/** Small helper: clip drawing to a rectangle given in GUI-scaled coordinates (glScissor wants framebuffer pixels, origin bottom-left). */
final class ListClip
{
	private ListClip()
	{
	}

	static void begin(Minecraft mc, int x0, int y0, int x1, int y1)
	{
		double scale = mc.mainWindow.getGuiScaleFactor();
		int framebufferHeight = mc.mainWindow.getFramebufferHeight();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int) (x0 * scale), (int) (framebufferHeight - y1 * scale), (int) ((x1 - x0) * scale), (int) ((y1 - y0) * scale));
	}

	static void end()
	{
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
}
