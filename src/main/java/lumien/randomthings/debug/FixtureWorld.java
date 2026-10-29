package lumien.randomthings.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.handler.AnchorChunkLoader;
import lumien.randomthings.handler.EnderLetterHandler;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import lumien.randomthings.tileentity.IronDropperTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Dev-only, for {@code RT_FIXTURE=build|verify} (see {@code docs/testing/fixture-world.md}): the old-world loading check. {@code build} fills a fresh world with
 * the mod data that has to survive a version upgrade (tile entity contents, the Floo network, the Ender Mailbox inboxes, Spectre cubes, chunk-loading anchors)
 * and writes what it put there into {@code rt_fixture_expected.txt} inside the world folder. The world is then opened by the NEXT branch with {@code verify},
 * which reads that file and compares it with what its own code loads from the old save.
 */
public final class FixtureWorld
{
	private static final int FLAGS = 18;
	private static final String FILE = "rt_fixture_expected.txt";
	private static final UUID MAILBOX_OWNER = UUID.fromString("00000000-0000-4000-8000-0000000000f1");
	private static final UUID FIREPLACE_MASTER = UUID.fromString("00000000-0000-4000-8000-0000000000f2");
	private static final UUID FIREPLACE_CREATOR = UUID.fromString("00000000-0000-4000-8000-0000000000f3");
	private static final BlockPos BASE = new BlockPos(4, 120, 4);      // near the world spawn: only loaded, ticking chunks run tile entities (the anchor registers on its first tick)
	private static final BlockPos DROPPER = BASE.add(1, 1, 1);
	private static final BlockPos RADAR = BASE.add(3, 1, 1);
	private static final BlockPos ANCHOR = BASE.add(5, 1, 1);

	private FixtureWorld()
	{
	}

	private static Path file(ServerWorld level)
	{
		return level.getSaveHandler().getWorldDirectory().toPath().resolve(FILE);
	}

	public static List<String> build(ServerPlayerEntity player)
	{
		ServerWorld level = player.getServerWorld();
		List<String> report = new ArrayList<>();
		Map<String, String> expected = new LinkedHashMap<>();

		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				level.setBlockState(BASE.add(x, 0, z), Blocks.SMOOTH_STONE.getDefaultState(), FLAGS);
			}
		}

		// tile entity contents
		level.setBlockState(DROPPER, ModBlocks.IRON_DROPPER.getDefaultState(), FLAGS);
		IronDropperTileEntity dropper = (IronDropperTileEntity) level.getTileEntity(DROPPER);
		dropper.itemHandler().insertItem(0, new ItemStack(Items.DIAMOND, 7), false);
		dropper.itemHandler().insertItem(1, new ItemStack(Items.STONE, 3), false);
		dropper.rotateRedstoneMode();
		dropper.markDirty();
		expected.put("dropper.diamonds", "7");
		expected.put("dropper.stone", "3");
		expected.put("dropper.redstone_mode", dropper.redstoneMode().name());

		level.setBlockState(RADAR, ModBlocks.BIOME_RADAR.getDefaultState(), FLAGS);
		ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
		BiomeCrystalItem.setBiome(crystal, ForgeRegistries.BIOMES.getValue(new ResourceLocation("minecraft:desert")));
		((BiomeRadarTileEntity) level.getTileEntity(RADAR)).setCrystal(crystal);
		level.getTileEntity(RADAR).markDirty();
		expected.put("radar.crystal_biome", crystal.getTag().getString("biomeName"));

		// saved data: Ender Mailbox inbox, Floo network, Spectre cube
		ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
		letter.getOrCreateTag().putString("sender", "Fixture");
		letter.getOrCreateTag().putBoolean("received", true);
		EnderLetterHandler.get(level).getOrCreateInventoryForPlayer(MAILBOX_OWNER).insertItem(0, letter, false);
		EnderLetterHandler.get(level).markDirty();
		expected.put("inbox.letters", "1");
		expected.put("inbox.sender", "Fixture");

		FlooNetworkHandler.get(level).addFirePlace(FIREPLACE_CREATOR, FIREPLACE_MASTER, "fixture_hearth", BASE.add(8, 1, 8));
		expected.put("floo.name", "fixture_hearth");

		SpectreHandler spectre = SpectreHandler.getInstance(level.getServer());
		spectre.teleportPlayerToSpectreCube(player);
		spectre.teleportPlayerBack(player);
		expected.put("spectre.cube_owners", String.valueOf(spectreOwners(spectre).size()));
		expected.put("spectre.owner", player.getGameProfile().getId().toString());

		// a chunk-loading anchor: its chunk stays forced across a restart, and the loader still knows the anchor
		level.setBlockState(ANCHOR, ModBlocks.ENDER_ANCHOR.getDefaultState(), 3);
		expected.put("anchor.pos", String.valueOf(ANCHOR.toLong()));

		try
		{
			StringBuilder text = new StringBuilder();
			expected.forEach((k, v) -> text.append(k).append('=').append(v).append('\n'));
			Files.write(file(level), text.toString().getBytes(StandardCharsets.UTF_8));
		}
		catch (IOException e)
		{
			report.add("FAIL could not write the expected file: " + e);
		}

		expected.forEach((k, v) -> report.add("built " + k + "=" + v));
		return report;
	}

	public static List<String> verify(ServerWorld level)
	{
		List<String> report = new ArrayList<>();
		Map<String, String> expected = new LinkedHashMap<>();

		try
		{
			for (String line : Files.readAllLines(file(level), StandardCharsets.UTF_8))
			{
				int eq = line.indexOf('=');

				if (eq > 0)
				{
					expected.put(line.substring(0, eq), line.substring(eq + 1));
				}
			}
		}
		catch (IOException e)
		{
			report.add("FAIL no fixture marker in this world (" + e + "): it was not made by RT_FIXTURE=build");
			return report;
		}

		// the area is only loaded while something needs it
		level.getChunk(BASE.getX() >> 4, BASE.getZ() >> 4);

		TileEntity dropperEntity = level.getTileEntity(DROPPER);
		int diamonds = 0;
		int stone = 0;
		String mode = "?";

		if (dropperEntity instanceof IronDropperTileEntity)
		{
			IronDropperTileEntity dropper = (IronDropperTileEntity) dropperEntity;

			for (int slot = 0; slot < dropper.itemHandler().getSlots(); slot++)
			{
				ItemStack stack = dropper.itemHandler().getStackInSlot(slot);
				diamonds += stack.getItem() == Items.DIAMOND ? stack.getCount() : 0;
				stone += stack.getItem() == Items.STONE ? stack.getCount() : 0;
			}

			mode = dropper.redstoneMode().name();
		}

		check(report, expected, "dropper.diamonds", String.valueOf(diamonds));
		check(report, expected, "dropper.stone", String.valueOf(stone));
		check(report, expected, "dropper.redstone_mode", mode);

		String biome = "?";

		if (level.getTileEntity(RADAR) instanceof BiomeRadarTileEntity)
		{
			ItemStack stack = ((BiomeRadarTileEntity) level.getTileEntity(RADAR)).getCurrentCrystal();
			biome = stack.hasTag() ? stack.getTag().getString("biomeName") : "none";
		}

		check(report, expected, "radar.crystal_biome", biome);

		IItemHandler inbox = EnderLetterHandler.get(level).getOrCreateInventoryForPlayer(MAILBOX_OWNER);
		int letters = 0;
		String sender = "?";

		for (int slot = 0; slot < inbox.getSlots(); slot++)
		{
			ItemStack stack = inbox.getStackInSlot(slot);

			if (stack.getItem() == ModItems.ENDER_LETTER)
			{
				letters++;
				sender = stack.hasTag() ? stack.getTag().getString("sender") : "?";
			}
		}

		check(report, expected, "inbox.letters", String.valueOf(letters));
		check(report, expected, "inbox.sender", sender);

		String name = FlooNetworkHandler.get(level).getNameFromUUID(FIREPLACE_MASTER);
		check(report, expected, "floo.name", String.valueOf(name));

		SpectreHandler spectre = SpectreHandler.getInstance(level.getServer());
		List<String> owners = spectreOwners(spectre);
		check(report, expected, "spectre.cube_owners", String.valueOf(owners.size()));
		check(report, expected, "spectre.owner", owners.isEmpty() ? "none" : owners.get(0));

		boolean registered = anchorRegistered(level, ANCHOR);
		boolean forced = level.getForcedChunks().contains(new ChunkPos(ANCHOR).asLong());
		report.add((registered ? "OK   " : "FAIL ") + "anchor.registered: the chunk loader still knows the anchor at " + ANCHOR);
		report.add((forced ? "OK   " : "FAIL ") + "anchor.forced: the anchor's chunk is still force-loaded");
		return report;
	}

	private static void check(List<String> report, Map<String, String> expected, String key, String actual)
	{
		String want = expected.get(key);
		report.add((actual.equals(want) ? "OK   " : "FAIL ") + key + ": expected " + want + ", loaded " + actual);
	}

	private static List<String> spectreOwners(SpectreHandler handler)
	{
		List<String> owners = new ArrayList<>();

		if (handler == null)
		{
			return owners;
		}

		try
		{
			java.lang.reflect.Field field = SpectreHandler.class.getDeclaredField("cubes");
			field.setAccessible(true);

			for (Object key : ((Map<?, ?>) field.get(handler)).keySet())
			{
				owners.add(String.valueOf(key));
			}

			return owners;
		}
		catch (ReflectiveOperationException e)
		{
			owners.add("unreadable: " + e);
			return owners;
		}
	}

	private static boolean anchorRegistered(ServerWorld level, BlockPos pos)
	{
		try
		{
			Object data = level.getSavedData().getOrCreate(AnchorChunkLoader::new, "randomthings_anchor_chunks");
			java.lang.reflect.Field field = AnchorChunkLoader.class.getDeclaredField("anchors");
			field.setAccessible(true);
			return ((java.util.Set<?>) field.get(data)).contains(pos.toLong());
		}
		catch (ReflectiveOperationException e)
		{
			return false;
		}
	}
}
