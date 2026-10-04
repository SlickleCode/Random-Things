package lumien.randomthings.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lumien.randomthings.config.RTConfig;

/**
 * Port-specific developer logging, toggled by {@code debug.DebugLogging} in
 * {@code randomthings-common.toml} (or the {@code -Drandomthings.debug=true} system property, which also
 * enables GUI screenshots and is set for {@code runClient} in build.gradle). Off by default; messages are written at INFO level with a
 * {@code [RT-DEBUG]} prefix so they show up in the normal game log without needing to raise
 * any log4j level.
 */
public class RTDebug
{
	private static final Logger LOGGER = LogManager.getLogger("randomthings-debug");

	public static boolean isLogging()
	{
		return get(true);
	}

	public static boolean isGuiScreenshots()
	{
		return get(false);
	}

	private static boolean get(boolean logging)
	{
		// -Drandomthings.debug=true (set by the gradle runClient config) forces both options on
		if (Boolean.getBoolean("randomthings.debug"))
		{
			return true;
		}

		try
		{
			return logging ? RTConfig.DEBUG_LOGGING.get() : RTConfig.DEBUG_GUI_SCREENSHOTS.get();
		}
		catch (IllegalStateException e)
		{
			// Config not loaded yet (very early lifecycle) - treat as off.
			return false;
		}
	}

	public static void log(String message, Object... args)
	{
		if (isLogging())
		{
			LOGGER.info("[RT-DEBUG] " + message, args);
		}
	}
}
