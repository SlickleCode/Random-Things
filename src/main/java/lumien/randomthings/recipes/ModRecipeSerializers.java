package lumien.randomthings.recipes;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipeSerializers
{
	public static void registerRecipeSerializers(Register<RecipeSerializer<?>> recipeSerializerRegistryEvent)
	{
		IForgeRegistry<RecipeSerializer<?>> registry = recipeSerializerRegistryEvent.getRegistry();

		registry.register(GoldenCompassSetPositionRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "golden_compass_set_position")));
		registry.register(EmeraldCompassSetTargetRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "emerald_compass_set_target")));
		registry.register(PortkeyCamoRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "portkey_camo")));
		registry.register(SpectreAnchorCombineRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "spectre_anchor_combine")));
		registry.register(DiaphanousSetRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "diaphanous_set")));
		registry.register(DiaphanousInvertRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ModConstants.MOD_ID, "diaphanous_invert")));
	}
}
