package lumien.randomthings.debug;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.util.BiomeUtil;
import lumien.randomthings.util.PotionHelper;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.SignText;

/**
 * Dev-only {@code /rtdebug manual}: builds a sky platform next to the player with every mod block on show (each with a name sign),
 * a few working set-ups for the manual checklist (Biome Radar with antenna, Fluid Display between solid neighbours, Iron Dropper into a chest,
 * Item Collector with items), chests holding every mod item, and fills the hotbar/inventory with the things the checklist needs
 * (Biome Crystals for several biomes, buckets, dyes, armor, imbue ingredients, ...). The player is put in creative mode.
 * Pair it with a fixed-seed normal world (see {@code docs/testing/manual-test-world.md}) so the crystals' biomes are far away.
 */
public final class ManualTestWorld
{
	private static final int FLAGS = 18; // update clients, no neighbour updates: blocks that would pop off (torches, plants) stay for inspection

	private ManualTestWorld()
	{
	}

	public static List<String> build(ServerPlayer player)
	{
		ServerLevel level = player.serverLevel();
		List<String> report = new ArrayList<>();
		BlockPos spawn = player.blockPosition();
		int oy = Math.min(level.getMaxBuildHeight() - 30, Math.max(spawn.getY(), level.getSeaLevel()) + 30);
		BlockPos o = new BlockPos(spawn.getX() - 40, oy, spawn.getZ() - 20);

		// platform 96 x 70 and clear air above it
		for (int x = 0; x < 96; x++)
		{
			for (int z = 0; z < 70; z++)
			{
				level.setBlock(o.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), FLAGS);

				for (int y = 1; y <= 14; y++)
				{
					level.setBlock(o.offset(x, y, z), Blocks.AIR.defaultBlockState(), FLAGS);
				}
			}
		}

		museum(level, o.offset(2, 1, 2));
		setups(level, o.offset(2, 1, 52), report);
		itemChests(level, o.offset(60, 1, 52));

		giveInventory(player);
		report.addAll(biomeDistances(level, spawn));
		report.add(churchDirections(level, spawn, o.offset(30, 1, 60)));

		player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
		level.setDayTime(6000);
		level.setWeatherParameters(6000, 0, false, false);
		player.teleportTo(level, o.getX() + 20.5, o.getY() + 1, o.getZ() + 49.5, 180.0F, 10.0F);
		report.add("platform at " + o.getX() + " " + o.getY() + " " + o.getZ() + " (x east, z south); museum in the north half, set-ups and item chests along the south edge");
		return report;
	}

	// ---- every mod block, 14 per row, name sign in front ------------------------------------------

	private static void museum(ServerLevel level, BlockPos origin)
	{
		int index = 0;

		for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet())
		{
			if (!ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				continue;
			}

			Block block = BuiltInRegistries.BLOCK.get(id);
			BlockPos pos = origin.offset((index % 14) * 6, 0, (index / 14) * 4);
			level.setBlock(pos.above(0), block.defaultBlockState(), FLAGS);
			sign(level, pos.offset(0, 0, 2), id.getPath());
			index++;
		}
	}

	private static void sign(ServerLevel level, BlockPos pos, String... lines)
	{
		level.setBlock(pos, Blocks.OAK_SIGN.defaultBlockState(), FLAGS);
		BlockEntity be = level.getBlockEntity(pos);

		if (be instanceof SignBlockEntity sign)
		{
			SignText text = new SignText();

			for (int i = 0; i < lines.length && i < 4; i++)
			{
				text = text.setMessage(i, Component.literal(lines[i]));
			}

			sign.setText(text, true);
			sign.setChanged();
			level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
		}
	}

	// ---- working set-ups ----------------------------------------------------------------------------

	private static void setups(ServerLevel level, BlockPos o, List<String> report)
	{
		// Biome Radar with its iron-bar antenna (breaking a bar sends it back to idle); power it with the redstone block east of it
		BlockPos radar = o;
		set(level, radar, ModBlocks.BIOME_RADAR);
		int[][] bars = { { 0, 1, 0 }, { 0, 2, 0 }, { 1, 2, 0 }, { -1, 2, 0 }, { 1, 3, 0 }, { -1, 3, 0 }, { 0, 2, 1 }, { 0, 2, -1 }, { 0, 3, 1 }, { 0, 3, -1 } };

		for (int[] b : bars)
		{
			set(level, radar.offset(b[0], b[1], b[2]), Blocks.IRON_BARS);
		}

		set(level, radar.offset(1, 0, 0), Blocks.REDSTONE_BLOCK);
		sign(level, radar.offset(0, 0, 3), "Biome Radar", "crystal in hand,", "right-click it in,", "power: redstone block");

		// Fluid Display between solid neighbours (no holes in the neighbours' faces), plus one on its own
		BlockPos display = o.offset(10, 0, 0);
		set(level, display, ModBlocks.FLUID_DISPLAY);
		set(level, display.offset(-1, 0, 0), Blocks.SAND);
		set(level, display.offset(1, 0, 0), Blocks.STONE);
		set(level, display.offset(0, 1, 0), Blocks.GLASS);
		set(level, display.offset(5, 0, 0), ModBlocks.FLUID_DISPLAY);
		sign(level, display.offset(0, 0, 3), "Fluid Display", "right-click with a", "bucket; sneak-click", "rotates");

		// Iron Dropper into a chest, powered by a lever on top
		BlockPos dropper = o.offset(22, 0, 0);
		set(level, dropper, ModBlocks.IRON_DROPPER);
		BlockState facing = ModBlocks.IRON_DROPPER.defaultBlockState();
		sign(level, dropper.offset(0, 0, 3), "Iron Dropper", "fill it, then", "power it");

		// Item Collector next to a chest with some loose items to collect
		BlockPos collector = o.offset(32, 0, 0);
		set(level, collector, ModBlocks.ITEM_COLLECTOR);
		set(level, collector.offset(-1, 0, 0), Blocks.CHEST);
		sign(level, collector.offset(0, 0, 3), "Item Collector", "drop items near it");

		// Beacon pyramid for the Spectre Lens (pick the effects in the beacon screen, then put the lens on top)
		BlockPos beacon = o.offset(45, 1, 1);

		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				set(level, beacon.offset(x, -1, z), Blocks.IRON_BLOCK);
			}
		}

		set(level, beacon, Blocks.BEACON);
		sign(level, beacon.offset(0, -1, 3), "Beacon + Spectre Lens", "choose effects,", "lens goes on top");

		report.add("set-ups: Biome Radar (with antenna), Fluid Display (+neighbours), Iron Dropper, Item Collector, Beacon pyramid - along z " + o.getZ());
	}

	private static void set(ServerLevel level, BlockPos pos, Block block)
	{
		level.setBlock(pos, block.defaultBlockState(), FLAGS);
	}

	// ---- chests with every mod item ---------------------------------------------------------------------

	private static void itemChests(ServerLevel level, BlockPos origin)
	{
		List<Item> items = new ArrayList<>();

		for (ResourceLocation id : BuiltInRegistries.ITEM.keySet())
		{
			if (ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				items.add(BuiltInRegistries.ITEM.get(id));
			}
		}

		for (int chest = 0; chest * 27 < items.size(); chest++)
		{
			BlockPos pos = origin.offset(chest * 2, 0, 0);
			level.setBlock(pos, Blocks.CHEST.defaultBlockState(), FLAGS);

			if (level.getBlockEntity(pos) instanceof ChestBlockEntity be)
			{
				for (int slot = 0; slot < 27 && chest * 27 + slot < items.size(); slot++)
				{
					be.setItem(slot, new ItemStack(items.get(chest * 27 + slot)));
				}
			}
		}

		sign(level, origin.offset(0, 0, 2), "Every mod item", "(" + items.size() + " items,", "one chest per 27)");
	}

	// ---- the player's inventory ---------------------------------------------------------------------------

	private static void giveInventory(ServerPlayer player)
	{
		Inventory inv = player.getInventory();
		inv.clearContent();
		List<ItemStack> stacks = new ArrayList<>();

		// hotbar first: the things most checklist rows start from
		stacks.add(crystal("minecraft:desert"));
		stacks.add(crystal("minecraft:snowy_plains"));
		stacks.add(crystal("minecraft:jungle"));
		stacks.add(crystal("minecraft:badlands"));
		stacks.add(crystal("minecraft:mushroom_fields"));
		stacks.add(new ItemStack(Items.PAPER, 16));
		stacks.add(new ItemStack(ModItems.PORTKEY));
		stacks.add(new ItemStack(Items.WATER_BUCKET));
		stacks.add(new ItemStack(Items.LAVA_BUCKET));

		// tools/gear
		stacks.add(new ItemStack(ModItems.ENDER_BUCKET));
		stacks.add(new ItemStack(ModItems.REINFORCED_ENDER_BUCKET));
		stacks.add(new ItemStack(ModItems.REDSTONE_REMOTE));
		stacks.add(mod("redstone_tool", 1));
		stacks.add(new ItemStack(ModItems.POSITION_FILTER, 4));
		stacks.add(new ItemStack(ModItems.ID_CARD));
		stacks.add(mod("entity_filter", 1));
		stacks.add(new ItemStack(ModItems.TIME_IN_A_BOTTLE));
		stacks.add(new ItemStack(ModItems.MAGIC_HOOD));
		stacks.add(new ItemStack(ModItems.SUPER_LUBRICENT_BOOTS));
		stacks.add(new ItemStack(ModItems.SPECTRE_PICKAXE));
		stacks.add(new ItemStack(ModItems.SPECTRE_AXE));
		stacks.add(new ItemStack(ModItems.SPECTRE_SHOVEL));
		stacks.add(new ItemStack(ModItems.SPECTRE_SWORD));
		stacks.add(new ItemStack(ModItems.SPECTRE_KEY));
		stacks.add(new ItemStack(ModItems.SPECTRE_ANCHOR));
		stacks.add(mod("spectre_charger_normal", 1));
		stacks.add(new ItemStack(ModItems.ENDER_LETTER, 4));
		stacks.add(new ItemStack(ModItems.SOUND_RECORDER));
		stacks.add(new ItemStack(ModItems.SOUND_PATTERN, 2));
		stacks.add(new ItemStack(ModItems.SUMMONING_PENDULUM));
		stacks.add(new ItemStack(ModItems.GOLDEN_COMPASS));
		stacks.add(new ItemStack(ModItems.EMERALD_COMPASS));
		stacks.add(new ItemStack(ModItems.FLOO_POWDER, 16));
		stacks.add(new ItemStack(ModItems.GOLDEN_EGG, 16));
		stacks.add(mod("weather_egg_sun", 8));
		stacks.add(mod("weather_egg_rain", 8));
		stacks.add(mod("weather_egg_storm", 8));

		// imbuing / dyeing / potions / enchanting ingredients
		stacks.add(PotionHelper.potion(Potions.WATER));
		stacks.add(PotionHelper.potion(Potions.WATER));
		stacks.add(new ItemStack(Items.COBBLESTONE, 16));
		stacks.add(new ItemStack(Items.VINE, 16));
		stacks.add(new ItemStack(Items.BONE_MEAL, 16));
		stacks.add(new ItemStack(Items.COAL, 16));
		stacks.add(new ItemStack(Items.FLINT, 16));
		stacks.add(new ItemStack(Items.BLAZE_POWDER, 16));
		stacks.add(new ItemStack(Items.NETHER_STAR));
		stacks.add(new ItemStack(Items.IRON_HELMET));
		stacks.add(new ItemStack(Items.IRON_CHESTPLATE));
		stacks.add(new ItemStack(Items.IRON_LEGGINGS));
		stacks.add(new ItemStack(Items.IRON_BOOTS));
		ItemStack enchanted = new ItemStack(Items.IRON_HELMET);
		enchanted.enchant(player.serverLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PROTECTION), 1);
		stacks.add(enchanted);
		ItemStack magnetic = new ItemStack(Items.DIAMOND_PICKAXE);
		magnetic.enchant(lumien.randomthings.enchantment.ModEnchantments.holder(player.serverLevel().registryAccess(), lumien.randomthings.enchantment.ModEnchantments.MAGNETIC), 1);
		stacks.add(magnetic);

		for (net.minecraft.world.item.DyeColor color : net.minecraft.world.item.DyeColor.values())
		{
			stacks.add(new ItemStack(net.minecraft.world.item.DyeItem.byColor(color), 8));
		}

		stacks.add(new ItemStack(Items.IRON_INGOT, 32));
		stacks.add(new ItemStack(Items.REDSTONE_BLOCK, 8));
		stacks.add(new ItemStack(Items.LEVER, 8));
		stacks.add(new ItemStack(Items.IRON_BARS, 32));
		stacks.add(new ItemStack(Items.STONE, 64));

		for (ItemStack stack : stacks)
		{
			inv.add(stack);
		}

		// the hotbar gets exactly the first nine entries (Inventory.add fills the hotbar first)
	}

	private static ItemStack mod(String path, int count)
	{
		return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, path)), count);
	}

	private static ItemStack crystal(String biomeId)
	{
		ItemStack stack = new ItemStack(ModItems.BIOME_CRYSTAL);
		Biome biome = BiomeUtil.byId(ResourceLocation.parse(biomeId));

		if (biome != null)
		{
			BiomeCrystalItem.setBiome(stack, biome);
		}
		else
		{
			StackTag.edit(stack, tag -> tag.putString("biomeName", biomeId));
		}

		return stack;
	}

	// ---- the closest plains village whose church holds a Peace Candle (a third of them do) ----------------------

	private static String churchDirections(ServerLevel level, BlockPos spawn, BlockPos signPos)
	{
		java.util.Set<Long> seen = new java.util.HashSet<>();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;

		// the nearest-structure search only returns one village per origin: probe a grid so that every village within ~3000 blocks is considered
		for (int gx = -2; gx <= 2; gx++)
		{
			for (int gz = -2; gz <= 2; gz++)
			{
				BlockPos found = level.findNearestMapStructure(net.minecraft.tags.StructureTags.ON_PLAINS_VILLAGE_MAPS, spawn.offset(gx * 1000, 0, gz * 1000), 64, false);

				if (found == null || !seen.add(net.minecraft.world.level.ChunkPos.asLong(found.getX() >> 4, found.getZ() >> 4)))
				{
					continue;
				}

				BlockPos church = peaceCandleChurchIn(level, found);

				if (church != null)
				{
					double dx = church.getX() - spawn.getX();
					double dz = church.getZ() - spawn.getZ();

					if (dx * dx + dz * dz < bestDistance)
					{
						bestDistance = dx * dx + dz * dz;
						best = church;
					}
				}
			}
		}

		if (best == null)
		{
			sign(level, signPos, "Peace Candle", "church village:", "none within", "~3000 blocks");
			return "peace candle church: no plains village with one within ~3000 blocks";
		}

		int dx = best.getX() - spawn.getX();
		int dz = best.getZ() - spawn.getZ();
		String[] names = { "east", "south-east", "south", "south-west", "west", "north-west", "north", "north-east" };
		String direction = names[(int) Math.floorMod(Math.round(Math.atan2(dz, dx) / (Math.PI / 4)), 8L)];
		int distance = (int) Math.sqrt(bestDistance);
		sign(level, signPos, "Peace Candle", "church: " + distance + " m " + direction, "x " + best.getX() + " z " + best.getZ(), "(/tp there)");
		return "peace candle church: " + distance + " blocks " + direction + " (x " + best.getX() + ", z " + best.getZ() + ")";
	}

	private static BlockPos peaceCandleChurchIn(ServerLevel level, BlockPos villagePos)
	{
		net.minecraft.world.level.chunk.ChunkAccess chunk = level.getChunkSource().getChunk(villagePos.getX() >> 4, villagePos.getZ() >> 4, net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS, true);

		for (net.minecraft.world.level.levelgen.structure.StructureStart start : chunk.getAllStarts().values())
		{
			for (net.minecraft.world.level.levelgen.structure.StructurePiece piece : start.getPieces())
			{
				if (piece instanceof net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece pool && pool.getElement().toString().contains("peace_candle_church"))
				{
					return piece.getBoundingBox().getCenter();
				}
			}
		}

		return null;
	}

	// ---- how far each crystal's biome is from the spawn ---------------------------------------------------

	private static List<String> biomeDistances(ServerLevel level, BlockPos spawn)
	{
		List<String> lines = new ArrayList<>();
		ResourceKey<Biome>[] keys = new ResourceKey[] { Biomes.DESERT, Biomes.SNOWY_PLAINS, Biomes.JUNGLE, Biomes.BADLANDS, Biomes.MUSHROOM_FIELDS };

		for (ResourceKey<Biome> key : keys)
		{
			Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(holder -> holder.is(key), spawn, 4000, 32, 64);

			if (found == null)
			{
				lines.add(key.location().getPath() + ": none within 4000 blocks");
			}
			else
			{
				int dx = found.getFirst().getX() - spawn.getX();
				int dz = found.getFirst().getZ() - spawn.getZ();
				lines.add(key.location().getPath() + ": " + (int) Math.sqrt((double) dx * dx + (double) dz * dz) + " blocks away (x " + found.getFirst().getX() + ", z " + found.getFirst().getZ() + ")");
			}
		}

		return lines;
	}
}
