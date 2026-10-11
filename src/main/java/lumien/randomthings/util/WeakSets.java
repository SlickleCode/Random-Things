package lumien.randomthings.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Static registries of loaded block entities/entities are written by the server thread and read by the client render
 * thread (and, in singleplayer, both worlds register their copies). A plain weak set throws
 * {@link java.util.ConcurrentModificationException} when that happens, so every such registry is created here and
 * iterated through {@link #snapshot(Set)}.
 */
public final class WeakSets
{
	private WeakSets()
	{
	}

	/** A weak, synchronized set: members disappear once nothing else references them. */
	public static <T> Set<T> create()
	{
		return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
	}

	/** A copy that is safe to iterate while the set is changed on another thread. */
	public static <T> List<T> snapshot(Set<T> set)
	{
		synchronized (set)
		{
			return set.isEmpty() ? Collections.<T>emptyList() : new ArrayList<>(set);
		}
	}
}
