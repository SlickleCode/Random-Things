package lumien.randomthings.lib;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** The data components this mod defines. Most item data lives in {@code minecraft:custom_data} (see {@code StackTag}); only the fluid buckets need a typed one. */
public class ModDataComponents
{
	/** The fluid held by the Ender Bucket and the Reinforced Ender Bucket. */
	public static final DataComponentType<SimpleFluidContent> FLUID = DataComponentType.<SimpleFluidContent>builder().persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC).cacheEncoding().build();

	public static void register(RegisterEvent event)
	{
		event.register(Registries.DATA_COMPONENT_TYPE, ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "fluid"), () -> FLUID);
	}
}
