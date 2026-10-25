package lumien.randomthings.client;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.network.chat.Component;

/**
 * Debug feature ({@code debug.DebugGuiScreenshots}): screenshots every Random Things GUI a few
 * frames after it opens (so widgets/lists that lay out on the first frames are present), saved to
 * {@code <game dir>/screenshots/randomthings_debug/<ScreenClass>_<timestamp>.png}. One shot per
 * opened screen instance.
 */
public class GuiDebugScreenshots
{
	private static final int FRAMES_BEFORE_SHOT = 5;

	private static WeakReference<Screen> current = new WeakReference<>(null);
	private static int frames;
	private static boolean shot;

	@SubscribeEvent
	public static void onDrawScreen(ScreenEvent.Render.Post event)
	{
		Screen screen = event.getScreen();

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
			File dir = new File(mc.gameDirectory, "screenshots/randomthings_debug");
			dir.mkdirs();
			File out = new File(dir, screen.getClass().getSimpleName() + "_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".png");

			Screenshot.takeScreenshot(mc.getMainRenderTarget(), image ->
			{
				try (NativeImage shot = image)
				{
					shot.writeToFile(out);
				}
				catch (Exception e)
				{
					RTDebug.log("GUI screenshot failed: {}", e.toString());
				}
			});

			RTDebug.log("Saved GUI screenshot {}", out.getAbsolutePath());
		}
		catch (Exception e)
		{
			RTDebug.log("GUI screenshot failed for {}: {}", screen.getClass().getSimpleName(), e.toString());
		}
	}
}
