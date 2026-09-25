package lumien.randomthings.recipes;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipeSerializers
{
	public static void registerRecipeSerializers(Register<IRecipeSerializer<?>> recipeSerializerRegistryEvent)
	{
		IForgeRegistry<IRecipeSerializer<?>> registry = recipeSerializerRegistryEvent.getRegistry();

		registry.register(GoldenCompassSetPositionRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "golden_compass_set_position")));
		registry.register(EmeraldCompassSetTargetRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "emerald_compass_set_target")));
	}
}
