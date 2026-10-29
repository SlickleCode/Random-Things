package lumien.randomthings.recipes;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import lumien.randomthings.lib.RTRegistry;

public class ModRecipeSerializers
{
	public static void registerRecipeSerializers(RTRegistry<RecipeSerializer<?>> registry)
	{

		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "golden_compass_set_position"), GoldenCompassSetPositionRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "emerald_compass_set_target"), EmeraldCompassSetTargetRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "portkey_camo"), PortkeyCamoRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre_anchor_combine"), SpectreAnchorCombineRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "diaphanous_set"), DiaphanousSetRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "diaphanous_invert"), DiaphanousInvertRecipe.SERIALIZER);
		registry.register(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "luminous_powder"), LuminousPowderRecipe.SERIALIZER);
	}
}
