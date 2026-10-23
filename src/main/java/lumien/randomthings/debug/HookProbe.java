package lumien.randomthings.debug;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts how often a coremod hook really did its job (a dyed item/armor tint applied, a Light Redirector swap, a hidden nameplate,
 * a shielded rain column). Hooks call {@link #hit(String)} unconditionally; it does nothing unless {@link DevMode#ENABLED}. The client
 * scene run ({@code RT_AUTOTEST_SCENES=true}, {@code SceneHookChecks}) asserts per render scene that its hooks were hit, which catches a
 * transformer that is applied to the wrong place - the {@code coremod.applied} test only proves it is applied at all.
 */
public final class HookProbe
{
	private static final ConcurrentHashMap<String, AtomicInteger> COUNTS = new ConcurrentHashMap<>();

	private HookProbe()
	{
	}

	public static void hit(String id)
	{
		if (DevMode.ENABLED)
		{
			COUNTS.computeIfAbsent(id, key -> new AtomicInteger()).incrementAndGet();
		}
	}

	public static int count(String id)
	{
		AtomicInteger n = COUNTS.get(id);
		return n == null ? 0 : n.get();
	}

	public static Map<String, Integer> snapshot()
	{
		Map<String, Integer> copy = new HashMap<>();

		for (Map.Entry<String, AtomicInteger> e : COUNTS.entrySet())
		{
			copy.put(e.getKey(), e.getValue().get());
		}

		return copy;
	}
}
