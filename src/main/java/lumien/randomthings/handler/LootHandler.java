package lumien.randomthings.handler;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.RTConfig;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.EmptyLootEntry;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.RandomChance;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.util.registry.Registry;
import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;

/**
 * Injects this mod's items into vanilla chest loot, port of 1.12.2's {@code LootHandler}.
 * Lava Charm (removed from this port) is omitted; gated by {@link RTConfig} (the 1.12.2 {@code Worldgen} toggles).
 * 1.12.2's {@code village_blacksmith} table is {@code village/village_weaponsmith}
 * in 1.14.4.
 */
public class LootHandler
{
	public static final ResourceLocation SIMPLE_DUNGEON = new ResourceLocation("minecraft", "chests/simple_dungeon");
	public static final ResourceLocation VILLAGE_WEAPONSMITH = new ResourceLocation("minecraft", "chests/village/village_weaponsmith");
	public static final ResourceLocation STRONGHOLD_CORRIDOR = new ResourceLocation("minecraft", "chests/stronghold_corridor");
	public static final ResourceLocation JUNGLE_TEMPLE = new ResourceLocation("minecraft", "chests/jungle_temple");
	public static final ResourceLocation ABANDONED_MINESHAFT = new ResourceLocation("minecraft", "chests/abandoned_mineshaft");
	public static final ResourceLocation END_CITY_TREASURE = new ResourceLocation("minecraft", "chests/end_city_treasure");

	public static void addLoot(LootTableLoadEvent event)
	{
		LootTable table = event.getTable();
		ResourceLocation name = event.getName();

		if (name.equals(SIMPLE_DUNGEON))
		{
			if (RTConfig.SUMMONING_PENDULUM.get())
				addSingleItemWithChance(table, ModItems.SUMMONING_PENDULUM, 10);
			if (RTConfig.MAGIC_HOOD.get())
				addSingleItemWithChance(table, ModItems.MAGIC_HOOD, 5);
			if (RTConfig.SLIME_CUBE.get())
				addSingleItemWithChance(table, ModBlocks.SLIME_CUBE.asItem(), 10);
			if (RTConfig.NUMBERED_SPECTRE_COILS.get())
				addSingleItemWithChance(table, ModBlocks.SPECTRE_COIL_NUMBER.asItem(), 10);
		}
		else if (name.equals(ABANDONED_MINESHAFT))
		{
			if (RTConfig.NUMBERED_SPECTRE_COILS.get())
				addSingleItemWithChance(table, ModBlocks.SPECTRE_COIL_NUMBER.asItem(), 8);
		}
		else if (name.equals(END_CITY_TREASURE))
		{
			if (RTConfig.NUMBERED_SPECTRE_COILS.get())
				addSingleItemWithChance(table, ModBlocks.SPECTRE_COIL_NUMBER.asItem(), 30);
		}
		else if (name.equals(VILLAGE_WEAPONSMITH))
		{
			if (RTConfig.MAGIC_HOOD.get())
				addSingleItemWithChance(table, ModItems.MAGIC_HOOD, 15);
		}
		else if (name.equals(STRONGHOLD_CORRIDOR))
		{
			if (RTConfig.SUMMONING_PENDULUM.get())
				addSingleItemWithChance(table, ModItems.SUMMONING_PENDULUM, 50);
		}
		else if (name.equals(JUNGLE_TEMPLE))
		{
			if (RTConfig.SLIME_CUBE.get())
				addSingleItemWithChance(table, ModBlocks.SLIME_CUBE.asItem(), 80);
		}

		if (RTConfig.BIOME_CRYSTAL.get() && name.getNamespace().equals("minecraft") && name.getPath().startsWith("chests/"))
		{
			LootPool crystalPool = LootPool.builder().name("randomthings:biome_crystal").rolls(ConstantRange.of(1)).acceptCondition(RandomChance.builder(0.2F))
					.addEntry(ItemLootEntry.builder(ModItems.BIOME_CRYSTAL).acceptFunction(() -> new RandomBiomeFunction())).build();
			table.addPool(crystalPool);
		}
	}

	/** One pool that yields {@code item} with weight {@code chance} out of 100, else nothing. */
	private static void addSingleItemWithChance(LootTable table, IItemProvider item, int chance)
	{
		String itemName = item.asItem().getRegistryName().getPath();

		LootPool pool = LootPool.builder().name("randomthings:" + itemName).rolls(ConstantRange.of(1)).addEntry(ItemLootEntry.builder(item).weight(chance))
				.addEntry(EmptyLootEntry.func_216167_a().weight(100 - chance)).build();
		table.addPool(pool);
	}

	/** 1.16 loot functions carry a registered type; this one is only ever added in code, so it has no JSON form. */
	public static final LootFunctionType RANDOM_BIOME_TYPE = new LootFunctionType(new RandomBiomeFunction.Serializer());

	public static void registerFunctionTypes()
	{
		Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation("randomthings", "random_biome"), RANDOM_BIOME_TYPE);
	}

	/** Tunes the generated Biome Crystal to a uniformly random registered biome. */
	private static class RandomBiomeFunction extends LootFunction
	{
		protected RandomBiomeFunction()
		{
			super(new ILootCondition[0]);
		}

		@Override
		public LootFunctionType getFunctionType()
		{
			return RANDOM_BIOME_TYPE;
		}

		static class Serializer extends LootFunction.Serializer<RandomBiomeFunction>
		{
			@Override
			public RandomBiomeFunction deserialize(JsonObject object, JsonDeserializationContext context, ILootCondition[] conditions)
			{
				return new RandomBiomeFunction();
			}
		}

		@Override
		protected ItemStack doApply(ItemStack stack, LootContext context)
		{
			List<ResourceLocation> biomes = new ArrayList<>(ForgeRegistries.BIOMES.getKeys());
			BiomeCrystalItem.setBiome(stack, ForgeRegistries.BIOMES.getValue(biomes.get(context.getRandom().nextInt(biomes.size()))));
			return stack;
		}
	}
}
