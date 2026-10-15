package lumien.randomthings.handler;

import lumien.randomthings.util.RegUtil;
import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.RTConfig;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.core.Registry;
import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;

/**
 * Injects this mod's items into vanilla chest loot, port of 1.12.2's {@code LootHandler}.
 * Lava Charm (removed from this port) and Numbered Spectre Coils (not ported) are omitted;
 * gated by {@link RTConfig} (the 1.12.2 {@code Worldgen} toggles).
 * 1.12.2's {@code village_blacksmith} table is {@code village/village_weaponsmith}
 * in 1.14.4.
 */
public class LootHandler
{
	public static final ResourceLocation SIMPLE_DUNGEON = new ResourceLocation("minecraft", "chests/simple_dungeon");
	public static final ResourceLocation VILLAGE_WEAPONSMITH = new ResourceLocation("minecraft", "chests/village/village_weaponsmith");
	public static final ResourceLocation STRONGHOLD_CORRIDOR = new ResourceLocation("minecraft", "chests/stronghold_corridor");
	public static final ResourceLocation JUNGLE_TEMPLE = new ResourceLocation("minecraft", "chests/jungle_temple");

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
			LootPool crystalPool = LootPool.lootPool().name("randomthings:biome_crystal").setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.2F))
					.add(LootItem.lootTableItem(ModItems.BIOME_CRYSTAL).apply(() -> new RandomBiomeFunction())).build();
			table.addPool(crystalPool);
		}
	}

	/** One pool that yields {@code item} with weight {@code chance} out of 100, else nothing. */
	private static void addSingleItemWithChance(LootTable table, ItemLike item, int chance)
	{
		String itemName = RegUtil.id(item.asItem()).getPath();

		LootPool pool = LootPool.lootPool().name("randomthings:" + itemName).setRolls(net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item).setWeight(chance))
				.add(EmptyLootItem.emptyItem().setWeight(100 - chance)).build();
		table.addPool(pool);
	}

	/** 1.16 loot functions carry a registered type; this one is only ever added in code, so it has no JSON form. */
	public static final LootItemFunctionType RANDOM_BIOME_TYPE = new LootItemFunctionType(new RandomBiomeFunction.Serializer());

	public static void registerFunctionTypes()
	{
		Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation("randomthings", "random_biome"), RANDOM_BIOME_TYPE);
	}

	/** Tunes the generated Biome Crystal to a uniformly random registered biome. */
	private static class RandomBiomeFunction extends LootItemConditionalFunction
	{
		protected RandomBiomeFunction()
		{
			super(new LootItemCondition[0]);
		}

		@Override
		public LootItemFunctionType getType()
		{
			return RANDOM_BIOME_TYPE;
		}

		static class Serializer extends LootItemConditionalFunction.Serializer<RandomBiomeFunction>
		{
			@Override
			public RandomBiomeFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditions)
			{
				return new RandomBiomeFunction();
			}
		}

		@Override
		protected ItemStack run(ItemStack stack, LootContext context)
		{
			List<ResourceLocation> biomes = new ArrayList<>(ForgeRegistries.BIOMES.getKeys());
			BiomeCrystalItem.setBiome(stack, ForgeRegistries.BIOMES.getValue(biomes.get(context.getRandom().nextInt(biomes.size()))));
			return stack;
		}
	}
}
