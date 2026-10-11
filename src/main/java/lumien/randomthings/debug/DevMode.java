package lumien.randomthings.debug;

/**
 * The single switch for every developer-only feature: {@code /rtdebug} commands, the in-game test
 * suite, the resource audit, GUI screenshots/input logging and the hands-free {@code AutoTest}.
 * Enabled only by the JVM property {@code -Drandomthings.debug=true} (the gradle {@code runClient}
 * config sets it; add it to a launcher's JVM arguments to use these tools on a real install).
 * <p>
 * Nothing developer-only is registered, subscribed or even class-loaded unless this is true:
 * {@link DevTools#init()} is the only entry point and is called solely behind this check, so a
 * normal game session pays no tick/event cost for any of it.
 */
public final class DevMode
{
	/** Read once at class load; a constant afterwards so guarded code is trivially cheap. */
	public static final boolean ENABLED = Boolean.getBoolean("randomthings.debug");

	private DevMode()
	{
	}
}
