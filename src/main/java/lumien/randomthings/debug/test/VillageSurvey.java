package lumien.randomthings.debug.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.worldgen.PeaceCandleChurchPool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Developer-only survey of the REAL villages of a normal world ({@code RT_VILLAGE_SURVEY=true ./gradlew runServer}, seed 424242): probes a
 * grid for the nearest villages, reads each village's pieces from the generated structure starts, and counts per village type how many
 * churches there are and how many hold a Peace Candle. Then it loads the nearest Peace Candle church of each type in full and checks that
 * the candle block is really standing in it. Writes {@code run/randomthings_villages.txt}. One probe per tick so the server never stalls.
 */
public final class VillageSurvey
{
	private static final String[] TYPES = { "plains", "desert", "savanna", "snowy", "taiga" };
	private static final int STEP = 1000;
	private static final int RADIUS = Integer.getInteger("randomthings.survey.radius", 4000);

	private static int ticks;
	private static boolean done;
	private static List<BlockPos> probes;
	private static int next;
	private static final Set<Long> seen = new HashSet<>();
	private static final List<String> villageLines = new ArrayList<>();
	private static final Map<String, int[]> stats = new LinkedHashMap<>();
	private static final Map<String, BlockPos> nearest = new LinkedHashMap<>();
	private static final Map<String, Double> nearestDistance = new LinkedHashMap<>();

	private VillageSurvey()
	{
	}

	public static void init()
	{
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && Boolean.getBoolean("randomthings.survey"))
		{
			// RT_VILLAGE_SURVEY_VANILLA=true: the same survey with the Peace Candle swap switched off, to compare the vanilla church counts
			if (Boolean.getBoolean("randomthings.survey.vanilla"))
			{
				PeaceCandleChurchPool.enabled = () -> false;
			}

			NeoForge.EVENT_BUS.addListener(VillageSurvey::onTick);
		}
	}

	private static void onTick(ServerTickEvent.Post event)
	{
		if (done)
		{
			return;
		}

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

		if (++ticks < 100)
		{
			return;
		}

		ServerLevel world = server.getLevel(Level.OVERWORLD);
		BlockPos spawn = world.getSharedSpawnPos();

		if (probes == null)
		{
			probes = new ArrayList<>();

			for (int gx = -RADIUS / STEP; gx <= RADIUS / STEP; gx++)
			{
				for (int gz = -RADIUS / STEP; gz <= RADIUS / STEP; gz++)
				{
					probes.add(spawn.offset(gx * STEP, 0, gz * STEP));
				}
			}

			for (String type : TYPES)
			{
				stats.put(type, new int[4]);
				stats.put("zombie " + type, new int[4]);
			}

			RTDebug.log("Village survey: {} probes around {}", probes.size(), spawn);
		}

		if (next < probes.size())
		{
			probe(world, spawn, probes.get(next++));
			return;
		}

		finish(world, spawn);
		done = true;
		server.halt(false);
	}

	private static List<BlockPos> villagesNear(ServerLevel world, BlockPos from)
	{
		List<BlockPos> out = new ArrayList<>();
		for (String t : TYPES)
		{
			net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.Structure> holder = world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE, net.minecraft.resources.ResourceLocation.parse("village_" + t)));
			com.mojang.datafixers.util.Pair<BlockPos, net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.Structure>> found = world.getChunkSource().getGenerator().findNearestMapStructure(world, net.minecraft.core.HolderSet.direct(holder), from, 100, false);

			if (found != null)
			{
				out.add(found.getFirst());
			}
		}

		return out;
	}

	private static void probe(ServerLevel world, BlockPos spawn, BlockPos from)
	{
		for (BlockPos found : villagesNear(world, from))
		{
			probeVillage(world, spawn, found);
		}
	}

	private static void probeVillage(ServerLevel world, BlockPos spawn, BlockPos found)
	{
		if (!seen.add(ChunkPos.asLong(found.getX() >> 4, found.getZ() >> 4)))
		{
			return;
		}

		ChunkAccess chunk = world.getChunkSource().getChunk(found.getX() >> 4, found.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS, true);

		for (StructureStart start : chunk.getAllStarts().values())
		{
			String type = null;
			boolean zombie = false;
			int churches = 0;
			int candles = 0;
			BlockPos candleChurch = null;

			for (StructurePiece piece : start.getPieces())
			{
				if (!(piece instanceof PoolElementStructurePiece pool))
				{
					continue;
				}

				String name = pool.getElement().toString();

				for (String t : TYPES)
				{
					if (type == null && name.contains("village/" + t + "/"))
					{
						type = t;
					}
				}

				zombie |= name.contains("/zombie/");

				if (name.contains("_temple_"))
				{
					churches++;

					if (PeaceCandleChurchPool.hasCandle(pool.getElement()))
					{
						candles++;
						candleChurch = piece.getBoundingBox().getCenter();
					}
				}
			}

			if (type == null)
			{
				continue;
			}

			String key = (zombie ? "zombie " : "") + type;
			int[] s = stats.get(key);
			s[0]++;
			s[1] += churches;
			s[2] += candles;
			s[3] += candles > 0 ? 1 : 0;
			villageLines.add(String.format("%-14s at %6d %6d: %d church(es), %d with a Peace Candle", key, found.getX(), found.getZ(), churches, candles));

			if (candleChurch != null)
			{
				double dx = candleChurch.getX() - spawn.getX();
				double dz = candleChurch.getZ() - spawn.getZ();
				double d = Math.sqrt(dx * dx + dz * dz);

				if (!nearestDistance.containsKey(key) || d < nearestDistance.get(key))
				{
					nearestDistance.put(key, d);
					nearest.put(key, candleChurch);
				}
			}
		}
	}

	private static void finish(ServerLevel world, BlockPos spawn)
	{
		List<String> out = new ArrayList<>();
		out.add("Village survey, seed " + world.getSeed() + ", spawn " + spawn.getX() + " " + spawn.getZ() + ", probes " + probes.size() + " (grid " + STEP + ", radius " + RADIUS + "), Peace Candle config " + PeaceCandleChurchPool.enabled.getAsBoolean());
		out.add("");
		out.add("type            villages  churches  candle-churches  villages-with-candle  candle share");

		int allChurches = 0;
		int allCandles = 0;

		for (Map.Entry<String, int[]> e : stats.entrySet())
		{
			int[] s = e.getValue();
			allChurches += s[1];
			allCandles += s[2];
			out.add(String.format("%-15s %8d %9d %16d %21d  %s", e.getKey(), s[0], s[1], s[2], s[3], s[1] == 0 ? "-" : Math.round(100.0 * s[2] / s[1]) + "%"));
		}

		out.add(String.format("%-15s %8d %9d %16d", "all", villageLines.size(), allChurches, allCandles));
		out.add("");
		out.add("nearest Peace Candle church per type, and whether the candle block is really standing there once the chunks are fully generated:");

		for (Map.Entry<String, BlockPos> e : nearest.entrySet())
		{
			BlockPos p = e.getValue();
			int blocks = candleBlocksAround(world, p);
			out.add(String.format("  %-14s %5d blocks from spawn at x %d z %d: %d Peace Candle block(s) found -> %s", e.getKey(), Math.round(nearestDistance.get(e.getKey())), p.getX(), p.getZ(), blocks, blocks > 0 ? "OK" : "MISSING"));
		}

		for (String key : stats.keySet())
		{
			if (!nearest.containsKey(key))
			{
				out.add("  " + key + ": no Peace Candle church found" + (stats.get(key)[0] == 0 ? " (no village of this type found at all)" : ""));
			}
		}

		out.add("");
		out.add("every village found:");
		out.addAll(villageLines);

		try
		{
			Files.write(new File("randomthings_villages.txt").toPath(), out, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			RTDebug.log("Village survey: could not write the report: {}", e);
		}

		RTDebug.log("Village survey done: {} villages", villageLines.size());
	}

	/** Loads the chunks around the church in full and counts Peace Candle blocks in a 25 x 17 x 25 area around it. */
	private static int candleBlocksAround(ServerLevel world, BlockPos centre)
	{
		int count = 0;

		for (int cx = (centre.getX() - 12) >> 4; cx <= (centre.getX() + 12) >> 4; cx++)
		{
			for (int cz = (centre.getZ() - 12) >> 4; cz <= (centre.getZ() + 12) >> 4; cz++)
			{
				world.getChunk(cx, cz);
			}
		}

		for (int x = centre.getX() - 12; x <= centre.getX() + 12; x++)
		{
			for (int z = centre.getZ() - 12; z <= centre.getZ() + 12; z++)
			{
				for (int y = centre.getY() - 8; y <= centre.getY() + 8; y++)
				{
					if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == ModBlocks.PEACE_CANDLE)
					{
						count++;
					}
				}
			}
		}

		return count;
	}
}
