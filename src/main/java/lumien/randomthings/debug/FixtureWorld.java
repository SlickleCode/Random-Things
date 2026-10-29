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
import lumien.randomthings.util.BiomeUtil;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;

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
	private static final BlockPos BASE = new BlockPos(4, 120, 4);      // near the world spawn: only loaded, ticking chunks run block entities (the anchor registers on its first tick)
	private static final BlockPos DROPPER = BASE.offset(1, 1, 1);
	private static final BlockPos RADAR = BASE.offset(3, 1, 1);
	private static final BlockPos ANCHOR = BASE.offset(5, 1, 1);

	private FixtureWorld()
	{
	}

	private static Path file(net.minecraft.server.MinecraftServer server)
	{
		return server.getWorldPath(LevelResource.ROOT).resolve(FILE);
	}

	public static List<String> build(ServerPlayer player)
	{
		ServerLevel level = player.level();
		List<String> report = new ArrayList<>();
		Map<String, String> expected = new LinkedHashMap<>();

		for (int x = 0; x < 16; x++)
		{
			for (int z = 0; z < 16; z++)
			{
				level.setBlock(BASE.offset(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState(), FLAGS);
			}
		}

		// tile entity contents
		level.setBlock(DROPPER, ModBlocks.IRON_DROPPER.defaultBlockState(), FLAGS);
		IronDropperTileEntity dropper = (IronDropperTileEntity) level.getBlockEntity(DROPPER);
		dropper.itemHandler().insertItem(0, new ItemStack(Items.DIAMOND, 7), false);
		dropper.itemHandler().insertItem(1, new ItemStack(Items.STONE, 3), false);
		dropper.rotateRedstoneMode();
		dropper.setChanged();
		expected.put("dropper.diamonds", "7");
		expected.put("dropper.stone", "3");
		expected.put("dropper.redstone_mode", dropper.redstoneMode().name());

		level.setBlock(RADAR, ModBlocks.BIOME_RADAR.defaultBlockState(), FLAGS);
		ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
		BiomeCrystalItem.setBiome(crystal, BiomeUtil.byId(Identifier.parse("minecraft:desert")));
		((BiomeRadarTileEntity) level.getBlockEntity(RADAR)).setCrystal(crystal);
		level.getBlockEntity(RADAR).setChanged();
		expected.put("radar.crystal_biome", StackTag.get(crystal).getStringOr("biomeName", "?"));

		// saved data: Ender Mailbox inbox, Floo network, Spectre cube
		ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
		StackTag.edit(letter, tag -> {
			tag.putString("sender", "Fixture");
			tag.putBoolean("received", true);
		});
		EnderLetterHandler.get(level).getOrCreateInventoryForPlayer(MAILBOX_OWNER).insertItem(0, letter, false);
		EnderLetterHandler.get(level).setDirty();
		expected.put("inbox.letters", "1");
		expected.put("inbox.sender", "Fixture");

		FlooNetworkHandler.get(level).addFirePlace(FIREPLACE_CREATOR, FIREPLACE_MASTER, "fixture_hearth", BASE.offset(8, 1, 8));
		expected.put("floo.name", "fixture_hearth");

		SpectreHandler spectre = SpectreHandler.getInstance(level.getServer());
		spectre.teleportPlayerToSpectreCube(player);
		spectre.teleportPlayerBack(player);
		expected.put("spectre.cube_owners", String.valueOf(spectreOwners(spectre).size()));
		expected.put("spectre.owner", player.getGameProfile().id().toString());

		// a chunk-loading anchor: its chunk stays forced across a restart, and the loader still knows the anchor
		level.setBlock(ANCHOR, ModBlocks.ENDER_ANCHOR.defaultBlockState(), 3);
		expected.put("anchor.pos", String.valueOf(ANCHOR.asLong()));

		try
		{
			StringBuilder text = new StringBuilder();
			expected.forEach((k, v) -> text.append(k).append('=').append(v).append('\n'));
			Files.writeString(file(level.getServer()), text.toString(), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			report.add("FAIL could not write the expected file: " + e);
		}

		expected.forEach((k, v) -> report.add("built " + k + "=" + v));
		return report;
	}

	public static List<String> verify(ServerLevel level)
	{
		List<String> report = new ArrayList<>();
		Map<String, String> expected = new LinkedHashMap<>();

		try
		{
			for (String line : Files.readAllLines(file(level.getServer()), StandardCharsets.UTF_8))
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

		BlockEntity dropperEntity = level.getBlockEntity(DROPPER);
		int diamonds = 0;
		int stone = 0;
		String mode = "?";

		if (dropperEntity instanceof IronDropperTileEntity dropper)
		{
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

		if (level.getBlockEntity(RADAR) instanceof BiomeRadarTileEntity radar)
		{
			ItemStack stack = radar.getCurrentCrystal();
			biome = StackTag.has(stack) ? StackTag.get(stack).getStringOr("biomeName", "?") : "none";
		}

		check(report, expected, "radar.crystal_biome", biome);

		var inbox = EnderLetterHandler.get(level).getOrCreateInventoryForPlayer(MAILBOX_OWNER);
		int letters = 0;
		String sender = "?";

		for (int slot = 0; slot < inbox.getSlots(); slot++)
		{
			if (inbox.getStackInSlot(slot).getItem() == ModItems.ENDER_LETTER)
			{
				letters++;
				sender = StackTag.has(inbox.getStackInSlot(slot)) ? StackTag.get(inbox.getStackInSlot(slot)).getStringOr("sender", "?") : "?";
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
		boolean forced = level.getForceLoadedChunks().contains(new ChunkPos(ANCHOR).toLong());
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
		try
		{
			java.lang.reflect.Field field = SpectreHandler.class.getDeclaredField("cubes");
			field.setAccessible(true);
			List<String> owners = new ArrayList<>();

			for (Object key : ((Map<?, ?>) field.get(handler)).keySet())
			{
				owners.add(String.valueOf(key));
			}

			return owners;
		}
		catch (ReflectiveOperationException e)
		{
			return List.of("unreadable: " + e);
		}
	}

	private static boolean anchorRegistered(ServerLevel level, BlockPos pos)
	{
		try
		{
			Object data = level.getDataStorage().computeIfAbsent(AnchorChunkLoader.TYPE);
			java.lang.reflect.Field field = AnchorChunkLoader.class.getDeclaredField("anchors");
			field.setAccessible(true);
			return ((java.util.Set<?>) field.get(data)).contains(pos.asLong());
		}
		catch (ReflectiveOperationException e)
		{
			return false;
		}
	}
}
