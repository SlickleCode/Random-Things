package lumien.randomthings.client;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Debug feature ({@code debug.DebugGuiScreenshots}): screenshots every Random Things GUI a few
 * frames after it opens (so widgets/lists that lay out on the first frames are present), saved to
 * {@code <game dir>/screenshots/randomthings_debug/<ScreenClass>_<timestamp>.png}. One shot per
 * opened screen instance.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GuiDebugScreenshots
{
	private static final int FRAMES_BEFORE_SHOT = 5;

	private static WeakReference<Screen> current = new WeakReference<>(null);
	private static int frames;
	private static boolean shot;

	@SubscribeEvent
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event)
	{
		Screen screen = event.getGui();

		if (!screen.getClass().getName().startsWith("lumien.randomthings."))
		{
			return;
		}

		if (current.get() != screen)
		{
			current = new WeakReference<>(screen);
			frames = 0;
			shot = false;
			RTDebug.log("Opened GUI {} (scaled size {}x{}); recent input: {}", screen.getClass().getSimpleName(), screen.width, screen.height, GuiOpenTriggerLog.recentInputs());
		}

		if (shot || !RTDebug.isGuiScreenshots() || ++frames < FRAMES_BEFORE_SHOT)
		{
			return;
		}

		shot = true;
		Minecraft mc = Minecraft.getInstance();

		try
		{
			File dir = new File(mc.gameDir, "screenshots/randomthings_debug");
			dir.mkdirs();
			File out = new File(dir, screen.getClass().getSimpleName() + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".png");

			try (NativeImage image = ScreenShotHelper.createScreenshot(mc.mainWindow.getFramebufferWidth(), mc.mainWindow.getFramebufferHeight(), mc.getFramebuffer()))
			{
				image.write(out);
			}

			RTDebug.log("Saved GUI screenshot {}", out.getAbsolutePath());
		}
		catch (Exception e)
		{
			RTDebug.log("GUI screenshot failed for {}: {}", screen.getClass().getSimpleName(), e.toString());
		}
	}
}
