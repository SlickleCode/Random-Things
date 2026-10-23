package lumien.randomthings.lib;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/** Fills the {@link ObjectHolder} fields of one registry; called at the end of that registry's {@code RegisterEvent}. */
public class ObjectHolderInjector
{
	private static final Class<?>[] HOLDERS = {
			lumien.randomthings.block.ModBlocks.class,
			lumien.randomthings.item.ModItems.class,
			lumien.randomthings.tileentity.ModTileEntityTypes.class,
			lumien.randomthings.container.ModContainerTypes.class,
			lumien.randomthings.entity.ModEntityTypes.class,
			lumien.randomthings.potion.ModEffects.class,
			lumien.randomthings.worldgen.ModFeatures.class };

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void inject(ResourceKey<? extends Registry<?>> registryKey)
	{
		Registry registry = (Registry) BuiltInRegistries.REGISTRY.getValue(registryKey.location());
		for (Class<?> holder : HOLDERS)
		{
			for (Field field : holder.getDeclaredFields())
			{
				ObjectHolder annotation = field.getAnnotation(ObjectHolder.class);
				if (annotation == null || !Modifier.isStatic(field.getModifiers()) || !ResourceLocation.parse(annotation.registryName()).equals(registryKey.location()))
				{
					continue;
				}
				ResourceLocation id = ResourceLocation.parse(annotation.value());
				Object value = field.getType() == net.minecraft.core.Holder.class ? registry.get(id).orElseThrow() : registry.getValue(id);
				try
				{
					field.set(null, value);
				}
				catch (IllegalAccessException e)
				{
					throw new IllegalStateException("Cannot fill " + field, e);
				}
			}
		}
	}
}
