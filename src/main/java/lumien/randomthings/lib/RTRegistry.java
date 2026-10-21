package lumien.randomthings.lib;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
		register(new ResourceLocation(ModConstants.MOD_ID, name), value);
	}

	public void register(ResourceLocation name, T value)
	{
		event.register(key, name, () -> value);
	}
}
