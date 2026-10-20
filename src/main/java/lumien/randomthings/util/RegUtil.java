package lumien.randomthings.util;

import net.minecraft.core.registries.Registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

/** 1.19 removed {@code getRegistryName()}; this looks the id up in the matching Forge registry (null if unregistered). */
public final class RegUtil
{
	private RegUtil()
	{
	}

	public static ResourceLocation id(Block b)
	{
		return ForgeRegistries.BLOCKS.getKey(b);
	}

	public static ResourceLocation id(Item i)
	{
		return ForgeRegistries.ITEMS.getKey(i);
	}

	public static ResourceLocation id(Fluid f)
	{
		return ForgeRegistries.FLUIDS.getKey(f);
	}

	public static ResourceLocation id(EntityType<?> t)
	{
		return ForgeRegistries.ENTITY_TYPES.getKey(t);
	}

	public static ResourceLocation id(BlockEntityType<?> t)
	{
		return ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(t);
	}

	public static ResourceLocation id(MobEffect e)
	{
		return ForgeRegistries.MOB_EFFECTS.getKey(e);
	}

	public static ResourceLocation id(Enchantment e)
	{
		return ForgeRegistries.ENCHANTMENTS.getKey(e);
	}
}
