package lumien.randomthings.debug;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Dev-only {@code /rtdebug manual}: builds a sky platform next to the player with every mod block on show (each with a name sign),
 * a few working set-ups for the manual checklist (Biome Radar with antenna, Fluid Display between solid neighbours, Iron Dropper,
 * Item Collector, beacon pyramid), chests holding every mod item, and fills the hotbar/inventory with the things the checklist needs
 * (Biome Crystals for several biomes, buckets, dyes, armor, imbue ingredients, ...). The player is put in creative mode.
 * Pair it with a fixed-seed normal world (see {@code docs/testing/manual-test-world.md}) so the crystals' biomes are far away.
 */
public final class ManualTestWorld
{
	private static final int FLAGS = 18; // update clients, no neighbour updates: blocks that would pop off (torches, plants) stay for inspection

	private ManualTestWorld()
	{
	}

	public static List<String> build(ServerPlayerEntity player)
	{
		ServerWorld level = player.getServerWorld();
		List<String> report = new ArrayList<>();
		BlockPos spawn = player.getPosition();
		int oy = Math.min(level.getHeight() - 30, Math.max(spawn.getY(), level.getSeaLevel()) + 30);
		BlockPos o = new BlockPos(spawn.getX() - 40, oy, spawn.getZ() - 20);

		// platform 96 x 70 and clear air above it
		for (int x = 0; x < 96; x++)
		{
			for (int z = 0; z < 70; z++)
			{
				level.setBlockState(o.add(x, 0, z), Blocks.SMOOTH_STONE.getDefaultState(), FLAGS);

				for (int y = 1; y <= 14; y++)
				{
					level.setBlockState(o.add(x, y, z), Blocks.AIR.getDefaultState(), FLAGS);
				}
			}
		}

		museum(level, o.add(2, 1, 2));
		setups(level, o.add(2, 1, 52), report);
		itemChests(level, o.add(60, 1, 52));

		giveInventory(player);
		biomeDistances(level, player);
		report.add(churchDirections(level, spawn, o.add(30, 1, 60)));

		player.setGameType(GameType.CREATIVE);
		command(player, "time set day");
		command(player, "weather clear");
		player.teleport(level, o.getX() + 20.5, o.getY() + 1, o.getZ() + 49.5, 180.0F, 10.0F);
		report.add("platform at " + o.getX() + " " + o.getY() + " " + o.getZ() + " (x east, z south); museum in the north half, set-ups and item chests along the south edge");
		return report;
	}

	private static void command(ServerPlayerEntity player, String command)
	{
		player.getServer().getCommandManager().handleCommand(player.getCommandSource(), command);
	}

	// ---- every mod block, 14 per row, name sign in front ------------------------------------------

	private static void museum(ServerWorld level, BlockPos origin)
	{
		int index = 0;

		for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys())
		{
			if (!ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				continue;
			}

			Block block = ForgeRegistries.BLOCKS.getValue(id);
			BlockPos pos = origin.add((index % 14) * 6, 0, (index / 14) * 4);
			level.setBlockState(pos, block.getDefaultState(), FLAGS);
			sign(level, pos.add(0, 0, 2), id.getPath());
			index++;
		}
	}

	private static void sign(ServerWorld level, BlockPos pos, String... lines)
	{
		level.setBlockState(pos, SIGN_BLOCK().getDefaultState(), FLAGS);
		TileEntity be = level.getTileEntity(pos);

		if (be instanceof SignTileEntity)
		{
			SignTileEntity sign = (SignTileEntity) be;

			for (int i = 0; i < lines.length && i < 4; i++)
			{
				sign.setText(i, new StringTextComponent(lines[i]));
			}

			sign.markDirty();
			level.notifyBlockUpdate(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
		}
	}

	private static Block SIGN_BLOCK()
	{
		return Blocks.OAK_SIGN;
	}

	// ---- working set-ups ----------------------------------------------------------------------------

	private static void setups(ServerWorld level, BlockPos o, List<String> report)
	{
		// Biome Radar with its iron-bar antenna (breaking a bar sends it back to idle); power it with the redstone block east of it
		BlockPos radar = o;
		set(level, radar, block("biome_radar"));
		int[][] bars = { { 0, 1, 0 }, { 0, 2, 0 }, { 1, 2, 0 }, { -1, 2, 0 }, { 1, 3, 0 }, { -1, 3, 0 }, { 0, 2, 1 }, { 0, 2, -1 }, { 0, 3, 1 }, { 0, 3, -1 } };

		for (int[] b : bars)
		{
			set(level, radar.add(b[0], b[1], b[2]), Blocks.IRON_BARS);
		}

		set(level, radar.add(1, 0, 0), Blocks.REDSTONE_BLOCK);
		sign(level, radar.add(0, 0, 3), "Biome Radar", "crystal in hand,", "right-click it in,", "power: redstone block");

		// Fluid Display between solid neighbours (no holes in the neighbours' faces), plus one on its own
		BlockPos display = o.add(10, 0, 0);
		set(level, display, block("fluid_display"));
		set(level, display.add(-1, 0, 0), Blocks.SAND);
		set(level, display.add(1, 0, 0), Blocks.STONE);
		set(level, display.add(0, 1, 0), Blocks.GLASS);
		set(level, display.add(5, 0, 0), block("fluid_display"));
		sign(level, display.add(0, 0, 3), "Fluid Display", "right-click with a", "bucket; sneak-click", "rotates");

		BlockPos dropper = o.add(22, 0, 0);
		set(level, dropper, block("iron_dropper"));
		sign(level, dropper.add(0, 0, 3), "Iron Dropper", "fill it, then", "power it");

		BlockPos collector = o.add(32, 0, 0);
		set(level, collector, block("item_collector"));
		set(level, collector.add(-1, 0, 0), Blocks.CHEST);
		sign(level, collector.add(0, 0, 3), "Item Collector", "drop items near it");

		// Beacon pyramid for the Spectre Lens (pick the effects in the beacon screen, then put the lens on top)
		BlockPos beacon = o.add(45, 1, 1);

		for (int x = -1; x <= 1; x++)
		{
			for (int z = -1; z <= 1; z++)
			{
				set(level, beacon.add(x, -1, z), Blocks.IRON_BLOCK);
			}
		}

		set(level, beacon, Blocks.BEACON);
		sign(level, beacon.add(0, -1, 3), "Beacon + Spectre Lens", "choose effects,", "lens goes on top");

		report.add("set-ups: Biome Radar (with antenna), Fluid Display (+neighbours), Iron Dropper, Item Collector, Beacon pyramid - along z " + o.getZ());
	}

	private static Block block(String path)
	{
		return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ModConstants.MOD_ID, path));
	}

	private static void set(ServerWorld level, BlockPos pos, Block block)
	{
		level.setBlockState(pos, block.getDefaultState(), FLAGS);
	}

	// ---- chests with every mod item ---------------------------------------------------------------------

	private static void itemChests(ServerWorld level, BlockPos origin)
	{
		List<Item> items = new ArrayList<>();

		for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys())
		{
			if (ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				items.add(ForgeRegistries.ITEMS.getValue(id));
			}
		}

		for (int chest = 0; chest * 27 < items.size(); chest++)
		{
			BlockPos pos = origin.add(chest * 2, 0, 0);
			level.setBlockState(pos, Blocks.CHEST.getDefaultState(), FLAGS);
			TileEntity be = level.getTileEntity(pos);

			if (be instanceof ChestTileEntity)
			{
				for (int slot = 0; slot < 27 && chest * 27 + slot < items.size(); slot++)
				{
					((ChestTileEntity) be).setInventorySlotContents(slot, new ItemStack(items.get(chest * 27 + slot)));
				}
			}
		}

		sign(level, origin.add(0, 0, 2), "Every mod item", "(" + items.size() + " items,", "one chest per 27)");
	}

	// ---- the player's inventory ---------------------------------------------------------------------------

	private static ItemStack mod(String path, int count)
	{
		return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ModConstants.MOD_ID, path)), count);
	}

	private static ItemStack crystal(String biomeId)
	{
		ItemStack stack = mod("biome_crystal", 1);
		stack.getOrCreateTag().putString("biomeName", biomeId);
		return stack;
	}

	private static void giveInventory(ServerPlayerEntity player)
	{
		PlayerInventory inv = player.inventory;
		inv.clear();
		List<ItemStack> stacks = new ArrayList<>();

		// hotbar first (the inventory fills the hotbar before the main slots): the things most checklist rows start from
		stacks.add(crystal("minecraft:desert"));
		stacks.add(crystal("minecraft:snowy_tundra"));
		stacks.add(crystal("minecraft:jungle"));
		stacks.add(crystal("minecraft:badlands"));
		stacks.add(crystal("minecraft:mushroom_fields"));
		stacks.add(new ItemStack(Items.PAPER, 16));
		stacks.add(mod("portkey", 1));
		stacks.add(new ItemStack(Items.WATER_BUCKET));
		stacks.add(new ItemStack(Items.LAVA_BUCKET));

		String[][] modItems = { { "ender_bucket", "1" }, { "reinforced_ender_bucket", "1" }, { "redstone_remote", "1" }, { "redstone_tool", "1" }, { "position_filter", "4" }, { "id_card", "1" },
				{ "entity_filter", "1" }, { "time_in_a_bottle", "1" }, { "magic_hood", "1" }, { "super_lubricent_boots", "1" }, { "spectre_pickaxe", "1" }, { "spectre_axe", "1" },
				{ "spectre_shovel", "1" }, { "spectre_sword", "1" }, { "spectre_key", "1" }, { "spectre_anchor", "1" }, { "spectre_charger_normal", "1" }, { "ender_letter", "4" },
				{ "sound_recorder", "1" }, { "sound_pattern", "2" }, { "summoning_pendulum", "1" }, { "golden_compass", "1" }, { "emerald_compass", "1" }, { "floo_powder", "16" },
				{ "golden_egg", "16" }, { "weather_egg_sun", "8" }, { "weather_egg_rain", "8" }, { "weather_egg_storm", "8" } };

		for (String[] entry : modItems)
		{
			stacks.add(mod(entry[0], Integer.parseInt(entry[1])));
		}

		// imbuing / dyeing / potions / enchanting ingredients
		stacks.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER));
		stacks.add(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER));
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
		enchanted.addEnchantment(Enchantments.PROTECTION, 1);
		stacks.add(enchanted);
		ItemStack magnetic = new ItemStack(Items.DIAMOND_PICKAXE);
		magnetic.addEnchantment(ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(ModConstants.MOD_ID, "magnetic")), 1);
		stacks.add(magnetic);

		for (DyeColor color : DyeColor.values())
		{
			stacks.add(new ItemStack(DyeItem.getItem(color), 8));
		}

		stacks.add(new ItemStack(Items.IRON_INGOT, 32));
		stacks.add(new ItemStack(Items.REDSTONE_BLOCK, 8));
		stacks.add(new ItemStack(Items.LEVER, 8));
		stacks.add(new ItemStack(Items.IRON_BARS, 32));
		stacks.add(new ItemStack(Items.STONE, 64));

		for (ItemStack stack : stacks)
		{
			if (!stack.isEmpty())
			{
				inv.addItemStackToInventory(stack);
			}
		}
	}

	// ---- the closest plains village whose church holds a Peace Candle (a third of them do) ----------------------

	private static String churchDirections(ServerWorld level, BlockPos spawn, BlockPos signPos)
	{
		java.util.Set<Long> seen = new java.util.HashSet<>();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;

		// the nearest-structure search only returns one village per origin: probe a grid so that every village within ~3000 blocks is considered
		for (int gx = -2; gx <= 2; gx++)
		{
			for (int gz = -2; gz <= 2; gz++)
			{
				BlockPos found = level.getStructureLocation(net.minecraft.world.gen.feature.structure.Structure.VILLAGE, spawn.add(gx * 1000, 0, gz * 1000), 64, false);

				if (found == null || !seen.add(net.minecraft.util.math.ChunkPos.asLong(found.getX() >> 4, found.getZ() >> 4)))
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

	private static BlockPos peaceCandleChurchIn(ServerWorld level, BlockPos villagePos)
	{
		net.minecraft.world.chunk.IChunk chunk = level.getChunkProvider().getChunk(villagePos.getX() >> 4, villagePos.getZ() >> 4, net.minecraft.world.chunk.ChunkStatus.STRUCTURE_STARTS, true);

		for (net.minecraft.world.gen.feature.structure.StructureStart<?> start : chunk.getStructureStarts().values())
		{
			for (net.minecraft.world.gen.feature.structure.StructurePiece piece : start.getComponents())
			{
				if (piece instanceof net.minecraft.world.gen.feature.structure.AbstractVillagePiece && ((net.minecraft.world.gen.feature.structure.AbstractVillagePiece) piece).getJigsawPiece().toString().contains("peace_candle_church"))
				{
					net.minecraft.util.math.MutableBoundingBox box = piece.getBoundingBox();
					return new BlockPos((box.minX + box.maxX) / 2, (box.minY + box.maxY) / 2, (box.minZ + box.maxZ) / 2);
				}
			}
		}

		return null;
	}

	// ---- how far each crystal's biome is from the spawn: the game's own locate command prints it into the chat ----

	private static void biomeDistances(ServerWorld level, ServerPlayerEntity player)
	{
		for (String biome : new String[] { "desert", "snowy_tundra", "jungle", "badlands", "mushroom_fields" })
		{
			command(player, "locatebiome minecraft:" + biome);
		}
	}
}
