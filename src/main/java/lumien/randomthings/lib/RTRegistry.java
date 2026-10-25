package lumien.randomthings.lib;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import java.util.function.Supplier;

import net.neoforged.neoforge.registries.RegisterEvent;

/** Registration handle for one registry during {@link RegisterEvent}; replaces the old {@code IForgeRegistry} parameter of the {@code ModXxx.registerXxx} methods. */
public class RTRegistry<T>
{
	private final RegisterEvent event;
	private final ResourceKey<? extends Registry<T>> key;

	public RTRegistry(RegisterEvent event, ResourceKey<? extends Registry<T>> key)
	{
		this.event = event;
		this.key = key;
	}

	public void register(String name, T value)
	{
		register(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, name), value);
	}

	public void register(Identifier name, T value)
	{
		event.register(key, name, () -> value);
	}

	/** Builds the object lazily with its registry id published to {@link RTIds}; use this for blocks and items (1.21.2+ needs the id inside their properties). */
	public void register(String name, Supplier<? extends T> factory)
	{
		register(Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, name), factory);
	}

	public void register(Identifier name, Supplier<? extends T> factory)
	{
		Identifier previous = RTIds.swap(name);

		try
		{
			T value = factory.get();
			event.register(key, name, () -> value);
		}
		finally
		{
			RTIds.swap(previous);
		}
	}
}
