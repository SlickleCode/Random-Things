package lumien.randomthings.util;

import net.minecraft.core.registries.Registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.core.registries.BuiltInRegistries;

/** 1.19 removed {@code getRegistryName()}; this looks the id up in the matching Forge registry (null if unregistered). */
public final class RegUtil
{
	private RegUtil()
	{
	}

	public static ResourceLocation id(Block b)
	{
		return BuiltInRegistries.BLOCK.getKey(b);
	}

	public static ResourceLocation id(Item i)
	{
		return BuiltInRegistries.ITEM.getKey(i);
	}

	public static ResourceLocation id(Fluid f)
	{
		return BuiltInRegistries.FLUID.getKey(f);
	}

	public static ResourceLocation id(EntityType<?> t)
	{
		return BuiltInRegistries.ENTITY_TYPE.getKey(t);
	}

	public static ResourceLocation id(BlockEntityType<?> t)
	{
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(t);
	}

	public static ResourceLocation id(MobEffect e)
	{
		return BuiltInRegistries.MOB_EFFECT.getKey(e);
	}
}
