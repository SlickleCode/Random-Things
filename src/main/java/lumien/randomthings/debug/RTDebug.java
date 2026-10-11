package lumien.randomthings.debug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Port-specific developer logging, only active in dev instances (see {@link DevMode}); messages are
 * written at INFO level with a {@code [RT-DEBUG]} prefix so they show up in the normal game log.
 */
public class RTDebug
{
	private static final Logger LOGGER = LogManager.getLogger("randomthings-debug");

	public static boolean isLogging()
	{
		return DevMode.ENABLED;
	}

	public static boolean isGuiScreenshots()
	{
		return DevMode.ENABLED;
	}

	public static void log(String message, Object... args)
	{
		if (DevMode.ENABLED)
		{
			LOGGER.info("[RT-DEBUG] " + message, args);
		}
	}
}
