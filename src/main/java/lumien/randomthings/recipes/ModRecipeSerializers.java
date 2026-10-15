package lumien.randomthings.recipes;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipeSerializers
{
	public static void registerRecipeSerializers(IForgeRegistry<RecipeSerializer<?>> registry)
	{

		registry.register(new ResourceLocation(ModConstants.MOD_ID, "golden_compass_set_position"), GoldenCompassSetPositionRecipe.SERIALIZER);
		registry.register(new ResourceLocation(ModConstants.MOD_ID, "emerald_compass_set_target"), EmeraldCompassSetTargetRecipe.SERIALIZER);
		registry.register(new ResourceLocation(ModConstants.MOD_ID, "portkey_camo"), PortkeyCamoRecipe.SERIALIZER);
		registry.register(new ResourceLocation(ModConstants.MOD_ID, "spectre_anchor_combine"), SpectreAnchorCombineRecipe.SERIALIZER);
		registry.register(new ResourceLocation(ModConstants.MOD_ID, "diaphanous_set"), DiaphanousSetRecipe.SERIALIZER);
		registry.register(new ResourceLocation(ModConstants.MOD_ID, "diaphanous_invert"), DiaphanousInvertRecipe.SERIALIZER);
	}
}
