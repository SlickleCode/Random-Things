package lumien.randomthings.debug.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import java.util.concurrent.Executor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.mojang.serialization.JsonOps;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import lumien.randomthings.worldgen.GlowingMushroomFeature;
import lumien.randomthings.worldgen.ModFeatures;
import lumien.randomthings.worldgen.PeaceCandleChurchPool;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.feature.JigsawFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.server.level.ServerLevel;

/**
 * Deterministic tests for the random-chance world generation (Glowing Mushroom patches and the Peace Candle village church), so
 * they do not have to be verified by flying around new worlds. Every random source is seeded, so a result never changes between runs.
 * <ul>
 * <li>Glowing Mushroom: the configured feature is in every overworld-style biome with the original rarity (and in no Nether/End
 * biome); run directly inside a sealed cave it places only roofed, valid mushrooms; real vanilla terrain is generated and the real
 * decorator must put patches underground; the switch stops it.</li>
 * <li>Peace Candle: the plains houses pool in this world's dynamic registry really is the wrapper; across thousands of pool rolls about
 * a third of the vanilla churches are swapped; real village assemblies contain the Peace Candle church; the NBT template places a
 * Peace Candle block with its tile entity; the switch stops it.</li>
 * </ul>
 */
final class WorldgenTests
{
	/** Absolute Y of the sealed test cave, far above the flat test world so it cannot touch the other tests' area. */
	private static final int CAVE_Y = 90;

	private static final ResourceLocation HOUSES = new ResourceLocation("village/plains/houses");
	private static final String VANILLA_CHURCH = "village/plains/houses/plains_temple_4";
	private static final String PEACE_CHURCH = "randomthings:peace_candle_church";

	private WorldgenTests()
	{
	}

	/** Stone floor + roof around the origin column with air between, 19x19 wide: a cave no sky can reach. */
	private static void buildCave(TestCtx c)
	{
		for (int x = -9; x <= 9; x++)
		{
			for (int z = -9; z <= 9; z++)
			{
				for (int y = 0; y <= 9; y++)
				{
					BlockPos pos = new BlockPos(c.origin.getX() + x, CAVE_Y + y, c.origin.getZ() + z);
					c.world.setBlock(pos, y == 0 || y == 8 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 18);
				}
			}
		}
	}

	private static void clearCave(TestCtx c)
	{
		for (int x = -9; x <= 9; x++)
		{
			for (int z = -9; z <= 9; z++)
			{
				for (int y = 0; y <= 9; y++)
				{
					c.world.setBlock(new BlockPos(c.origin.getX() + x, CAVE_Y + y, c.origin.getZ() + z), Blocks.AIR.defaultBlockState(), 18);
				}
			}
		}
	}

	private static int countMushrooms(TestCtx c, List<BlockPos> found)
	{
		int count = 0;

		for (int x = -9; x <= 9; x++)
		{
			for (int z = -9; z <= 9; z++)
			{
				for (int y = 1; y <= 7; y++)
				{
					BlockPos pos = new BlockPos(c.origin.getX() + x, CAVE_Y + y, c.origin.getZ() + z);

					if (c.world.getBlockState(pos).getBlock() == ModBlocks.GLOWING_MUSHROOM)
					{
						count++;

						if (found != null)
						{
							found.add(pos);
						}
					}
				}
			}
		}

		return count;
	}

	/**
	 * Generates a real vanilla-overworld patch of 7x7 chunks (terrain, surface, caves - the same steps {@code ChunkStatus} runs) and
	 * decorates the inner 5x5 chunks with the real decorator, so every registered feature - ours included - gets its normal chance.
	 * Nothing is written to the test world. Returns {mushrooms, inner chunks that received mushrooms, mushrooms at or above the surface}.
	 */
	private static int[] generateTerrainAndCount(TestCtx c, int chunkX, int chunkZ)
	{
		ServerLevel world = c.world;
		RegistryAccess registries = world.registryAccess();
		Registry<Biome> biomes = registries.registryOrThrow(Registry.BIOME_REGISTRY);
		long seed = world.getSeed();
		NoiseBasedChunkGenerator generator = WorldGenSettings.makeDefaultOverworld(registries, seed);
				BiomeManager biomeManager = new BiomeManager(generator, BiomeManager.obfuscateSeed(seed));
		StructureFeatureManager featureManager = NoStructures.of(world);
		Executor sameThread = Runnable::run;
		// 9x9 chunks: the outer ring only exists so every generated chunk has its 3x3 neighbourhood (structure references, carvers)
		final int size = 9;
		ProtoChunk[][] grid = new ProtoChunk[size][size];

		// 1.18 runs the generator steps as futures; they are driven synchronously here, in the same order ChunkStatus runs them.
		// Everything sees the world only through regions over the grid: the world's own structure manager would block on the
		// server thread's chunk source from the generator's worker threads (this runs on the server thread) and deadlock.
		for (int z = 0; z < size; z++)
		{
			for (int x = 0; x < size; x++)
			{
				grid[z][x] = new ProtoChunk(new ChunkPos(chunkX + x, chunkZ + z), UpgradeData.EMPTY, world, biomes, null);
				grid[z][x].setStatus(ChunkStatus.STRUCTURE_REFERENCES);
			}
		}

		// Surface building (blending data up to 7 chunks out) and the carver pass (every chunk within 8) need a much wider region than
		// the patch itself: the missing chunks are blank placeholders, the patch chunks are the real ones.
		final int wide = 8;
		Map<Long, ProtoChunk> placeholders = new HashMap<>();

		for (int z = 1; z < size - 1; z++)
		{
			for (int x = 1; x < size - 1; x++)
			{
				WorldGenRegion region = new WorldGenRegion(world, neighbourhood(grid, x, z), ChunkStatus.STRUCTURE_REFERENCES, 0);
				StructureFeatureManager regionManager = featureManager.forWorldGenRegion(region);
				generator.createBiomes(biomes, sameThread, Blender.empty(), regionManager, grid[z][x]).join();
				generator.fillFromNoise(sameThread, Blender.empty(), regionManager, grid[z][x]).join();
			}
		}

		for (int z = 1; z < size - 1; z++)
		{
			for (int x = 1; x < size - 1; x++)
			{
				WorldGenRegion surfaceRegion = new WorldGenRegion(world, square(grid, x, z, wide, chunkX, chunkZ, placeholders, world, biomes), ChunkStatus.SURFACE, 0);
				generator.buildSurface(surfaceRegion, featureManager.forWorldGenRegion(surfaceRegion), grid[z][x]);
			}
		}

		for (int z = 1; z < size - 1; z++)
		{
			for (int x = 1; x < size - 1; x++)
			{
				WorldGenRegion carveRegion = new WorldGenRegion(world, square(grid, x, z, wide, chunkX, chunkZ, placeholders, world, biomes), ChunkStatus.CARVERS, 0);
				StructureFeatureManager regionManager = featureManager.forWorldGenRegion(carveRegion);
				generator.applyCarvers(carveRegion, seed, biomeManager, regionManager, grid[z][x], GenerationStep.Carving.AIR);
				generator.applyCarvers(carveRegion, seed, biomeManager, regionManager, grid[z][x], GenerationStep.Carving.LIQUID);
			}
		}

		for (ProtoChunk[] row : grid)
		{
			for (ProtoChunk primer : row)
			{
				primer.setStatus(ChunkStatus.LIQUID_CARVERS);
				Heightmap.primeHeightmaps(primer, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
			}
		}

		for (int z = 2; z < size - 2; z++)
		{
			for (int x = 2; x < size - 2; x++)
			{
				WorldGenRegion region = new WorldGenRegion(world, neighbourhood(grid, x, z), ChunkStatus.FEATURES, 1);
				generator.applyBiomeDecoration(region, grid[z][x], featureManager.forWorldGenRegion(region));
			}
		}

		int mushrooms = 0;
		int chunksWithMushrooms = 0;
		int exposed = 0;

		for (int z = 2; z < size - 2; z++)
		{
			for (int x = 2; x < size - 2; x++)
			{
				ProtoChunk primer = grid[z][x];
				int here = 0;

				for (int bx = 0; bx < 16; bx++)
				{
					for (int bz = 0; bz < 16; bz++)
					{
						int surface = primer.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);

						for (int y = primer.getMinBuildHeight() + 1; y < primer.getMaxBuildHeight() - 1; y++)
						{
							if (primer.getBlockState(new BlockPos(bx, y, bz)).getBlock() == ModBlocks.GLOWING_MUSHROOM)
							{
								here++;

								if (y >= surface)
								{
									exposed++;
								}
							}
						}
					}
				}

				mushrooms += here;

				if (here > 0)
				{
					chunksWithMushrooms++;
				}
			}
		}

		return new int[] { mushrooms, chunksWithMushrooms, exposed };
	}

	/** The (2r+1)^2 chunks around grid chunk (x, z): patch chunks where the patch has them, blank placeholders elsewhere. */
	private static List<ChunkAccess> square(ProtoChunk[][] grid, int x, int z, int radius, int originX, int originZ, Map<Long, ProtoChunk> placeholders, ServerLevel world, Registry<Biome> biomes)
	{
		List<ChunkAccess> chunks = new ArrayList<>();

		for (int dz = -radius; dz <= radius; dz++)
		{
			for (int dx = -radius; dx <= radius; dx++)
			{
				int gx = x + dx;
				int gz = z + dz;

				if (gx >= 0 && gz >= 0 && gx < grid.length && gz < grid.length)
				{
					chunks.add(grid[gz][gx]);
				}
				else
				{
					ChunkPos pos = new ChunkPos(originX + gx, originZ + gz);
					chunks.add(placeholders.computeIfAbsent(pos.toLong(), key -> new ProtoChunk(pos, UpgradeData.EMPTY, world, biomes, null)));
				}
			}
		}

		return chunks;
	}

	private static List<ChunkAccess> neighbourhood(ProtoChunk[][] grid, int x, int z)
	{
		List<ChunkAccess> neighbours = new ArrayList<>();

		for (int dz = -1; dz <= 1; dz++)
		{
			for (int dx = -1; dx <= 1; dx++)
			{
				neighbours.add(grid[z + dz][x + dx]);
			}
		}

		return neighbours;
	}

	/**
	 * The feature rolls its patch height with the first {@code nextInt(pos.getY() - 4)}; this returns seeds whose first roll lands the
	 * patch centre on {@code wantedY} (so a seeded run is aimed at the prepared cave).
	 */
	private static List<Long> seedsWithCentre(int minY, int maxY, int wantedY, int count)
	{
		List<Long> seeds = new ArrayList<>();

		for (long seed = 0; seeds.size() < count && seed < 100000; seed++)
		{
			if (minY + new Random(seed).nextInt(maxY - minY) == wantedY)
			{
				seeds.add(seed);
			}
		}

		return seeds;
	}

	private static StructureTemplatePool housesPool(TestCtx c)
	{
		return c.world.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).get(HOUSES);
	}

	private static StructureTemplatePool vanillaHouses(StructureTemplatePool wrapper) throws Exception
	{
		Field field = wrapper.getClass().getDeclaredField("vanilla");
		field.setAccessible(true);
		return (StructureTemplatePool) field.get(wrapper);
	}

	private static List<String> names(List<StructurePoolElement> pieces)
	{
		List<String> out = new ArrayList<>();

		for (StructurePoolElement piece : pieces)
		{
			out.add(piece.toString());
		}

		return out;
	}

	private static boolean isVanillaChurch(String name)
	{
		return name.contains(VANILLA_CHURCH);
	}

	private static boolean isPeaceChurch(String name)
	{
		return name.contains(PEACE_CHURCH);
	}

	/** Assembles {@code count} plains villages with the real village start (the code a new world runs) and returns {vanilla churches, Peace Candle churches, villages}. */
	private static int[] assembleVillages(TestCtx c, int count)
	{
		RegistryAccess registries = c.world.registryAccess();
		StructureManager templates = c.world.getStructureManager();
		Holder<StructureTemplatePool> centers = registries.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY).getHolderOrThrow(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("village/plains/town_centers")));
		ChunkGenerator generator = c.world.getChunkSource().getGenerator();
		int vanilla = 0;
		int peace = 0;

		for (int i = 0; i < count; i++)
		{
			ChunkPos chunkPos = new ChunkPos(i * 11, i * 17);
			ConfiguredStructureFeature<?, ?> village = registries.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).get(new ResourceLocation("village_plains"));
			// vanilla's own village start (a hand-built JigsawPlacement call in this harness produced streets but never any house)
			net.minecraft.world.level.levelgen.structure.StructureStart start = village.generate(registries, generator, generator.getBiomeSource(), templates, c.world.getSeed(), chunkPos, 0, c.world, biome -> true);
			for (StructurePiece piece : start.getPieces())
			{
				if (piece instanceof PoolElementStructurePiece)
				{
					String name = ((PoolElementStructurePiece) piece).getElement().toString();

					if (isVanillaChurch(name))
					{
						vanilla++;
					}
					else if (isPeaceChurch(name))
					{
						peace++;
					}
				}
			}
		}

		return new int[] { vanilla, peace, count };
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Glowing Mushroom ----------------------------------------------------------------
		tests.add(RTTest.named("worldgen.glowing_mushroom.is_registered_in_every_overworld_biome_with_the_original_rarity").run(c -> {
			Registry<Biome> biomes = c.world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
			int overworldStyle = 0;

			for (Biome biome : biomes)
			{
				boolean found = false;
				boolean found4 = false;
				List<HolderSet<PlacedFeature>> stages = biome.getGenerationSettings().features();
				List<PlacedFeature> stage = new ArrayList<>();

				if (stages.size() > GenerationStep.Decoration.UNDERGROUND_DECORATION.ordinal())
				{
					stages.get(GenerationStep.Decoration.UNDERGROUND_DECORATION.ordinal()).forEach(holder -> stage.add(holder.value()));
				}

				for (PlacedFeature placedFeature : stage)
				{
					String json = PlacedFeature.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, placedFeature).result().map(Object::toString).orElse("");

					if (json.contains("randomthings:glowing_mushroom"))
					{
						found = true;
						found4 = json.contains("\"chance\":4") && json.contains("minecraft:heightmap") && json.contains("minecraft:in_square");
					}
				}

				Biome.BiomeCategory category = Biome.getBiomeCategory(biomes.getHolderOrThrow(biomes.getResourceKey(biome).get()));
				boolean overworld = category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE;
				ResourceLocation name = biomes.getKey(biome);

				if (overworld)
				{
					overworldStyle++;
					c.check(found, "Glowing Mushroom must be decorated into " + name);
					c.check(found4, name + ": placement must be 1 in 4 chunks, random column, on the heightmap (like the original)");
				}
				else
				{
					c.check(!found, "Glowing Mushroom must not be decorated into the Nether/End biome " + name);
				}
			}

			c.check(overworldStyle > 40, "expected the full vanilla biome list, saw " + overworldStyle);
		}));

		// the lighting engine runs asynchronously, so the cave is built, given time to light up, then used one patch per step
		final int caveMaxY = CAVE_Y + 5;
		final int patchesToRun = 6;
		final int[] stats = new int[2]; // patches with mushrooms, total mushrooms
		RTTest cave = RTTest.named("worldgen.glowing_mushroom.patch_in_a_sealed_cave_places_only_roofed_valid_mushrooms").run(c -> buildCave(c)).waitTicks(30);

		for (int run = 0; run < patchesToRun; run++)
		{
			final int index = run;
			cave.run(c -> {
				List<Long> seeds = seedsWithCentre(Math.max(0, c.world.getMinBuildHeight()), caveMaxY, CAVE_Y + 3, patchesToRun);
				c.check(seeds.size() == patchesToRun, "could not find seeds aiming the patch at the cave");
				BlockPos column = new BlockPos(c.origin.getX(), caveMaxY + 4, c.origin.getZ());
				boolean placed = ModFeatures.GLOWING_MUSHROOM.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new Random(seeds.get(index)), column);
				c.check(placed, "the patch feature reports success for seed " + seeds.get(index));
				List<BlockPos> found = new ArrayList<>();
				int count = countMushrooms(c, found);

				if (count > 0)
				{
					stats[0]++;
				}

				stats[1] += count;

				for (BlockPos pos : found)
				{
					c.check(!c.world.canSeeSkyFromBelowWater(pos), "mushroom at " + pos + " must not see the sky");
					c.check(Math.abs(pos.getX() - column.getX()) <= 7 && Math.abs(pos.getZ() - column.getZ()) <= 7, "mushroom at " + pos + " outside the +-7 patch radius");
					c.check(Math.abs(pos.getY() - (CAVE_Y + 3)) <= 3, "mushroom at " + pos + " outside the +-3 patch height");
					c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
				}
			}).waitTicks(12);
		}

		tests.add(cave.run(c -> {
			clearCave(c);
			RTDebug.log("worldgen test: {} of {} aimed mushroom patches produced mushrooms, {} mushrooms in total", stats[0], patchesToRun, stats[1]);
			c.check(stats[0] >= 4, "most aimed patches must produce mushrooms (got " + stats[0] + " of " + patchesToRun + ")");
			c.check(stats[1] >= 20, "expected a few dozen mushrooms over " + patchesToRun + " patches, got " + stats[1]);
		}));

		tests.add(RTTest.named("worldgen.glowing_mushroom.config_switch_stops_the_patches").run(c -> buildCave(c)).waitTicks(30).run(c -> {
			BooleanSupplier before = GlowingMushroomFeature.enabled;

			try
			{
				GlowingMushroomFeature.enabled = () -> false;
				int maxY = CAVE_Y + 5;
				BlockPos column = new BlockPos(c.origin.getX(), maxY + 4, c.origin.getZ());
				boolean placed = false;

				for (long seed : seedsWithCentre(Math.max(0, c.world.getMinBuildHeight()), maxY, CAVE_Y + 3, 6))
				{
					placed |= ModFeatures.GLOWING_MUSHROOM.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new Random(seed), column);
				}

				c.check(!placed, "the feature must refuse to place with GlowingMushrooms = false");
				c.eq(0, countMushrooms(c, null), "mushrooms placed with the config switch off");
			}
			finally
			{
				GlowingMushroomFeature.enabled = before;
				clearCave(c);
			}
		}));

		tests.add(RTTest.named("worldgen.glowing_mushroom.real_terrain_generation_places_patches_underground").run(c -> {
			int mushrooms = 0;
			int chunks = 0;
			int exposed = 0;
			int areas = 12;
			final int[] calls = new int[1];
			BooleanSupplier before = GlowingMushroomFeature.enabled;
			GlowingMushroomFeature.enabled = () -> {
				calls[0]++;
				return before.getAsBoolean();
			};

			for (int area = 0; area < areas; area++)
			{
				int[] result;

				try
				{
					result = generateTerrainAndCount(c, 400 + area * 37, -300 - area * 41);
				}
				catch (RuntimeException e)
				{
					Throwable root = e;

					while (root.getCause() != null)
					{
						root = root.getCause();
					}

					StackTraceElement[] trace = root.getStackTrace();
					throw new TestCtx.Failure("terrain generation failed: " + root + " at " + java.util.Arrays.stream(trace).filter(t -> !t.getClassName().startsWith("java.")).limit(10).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining(" / ")));
				}

				mushrooms += result[0];
				chunks += result[1];
				exposed += result[2];
			}

			GlowingMushroomFeature.enabled = before;
			RTDebug.log("worldgen test: the patch feature was asked to generate {} times", calls[0]);
			RTDebug.log("worldgen test: real terrain generation, {} areas of 5x5 decorated chunks: {} Glowing Mushrooms in {} chunks, {} not under a roof", areas, mushrooms, chunks, exposed);
			c.check(mushrooms > 0, "real terrain generation must place Glowing Mushrooms underground (found none in " + areas * 25 + " chunks)");
			c.check(chunks >= 2, "mushroom patches must show up in several chunks (got " + chunks + ")");
		}));

		// a placed mushroom lights its own block; the block's survival rule must not count that light, or it drops on the next update
		final BlockPos[] planted = new BlockPos[1];
		tests.add(RTTest.named("worldgen.glowing_mushroom.placed_mushroom_survives_updates_in_the_dark").run(c -> buildCave(c)).waitTicks(30).run(c -> {
			planted[0] = new BlockPos(c.origin.getX(), CAVE_Y + 1, c.origin.getZ());
			c.world.setBlock(planted[0], ModBlocks.GLOWING_MUSHROOM.defaultBlockState(), 3);
			c.check(c.world.getBlockState(planted[0]).getBlock() == ModBlocks.GLOWING_MUSHROOM, "the mushroom must be placeable in a dark cave");
		}).waitTicks(40).run(c -> {
			// nudge the neighbours, which is what a drop check listens for
			c.world.setBlock(planted[0].east(), Blocks.STONE.defaultBlockState(), 3);
			c.world.setBlock(planted[0].east(), Blocks.AIR.defaultBlockState(), 3);
		}).waitTicks(10).run(c -> {
			boolean still = c.world.getBlockState(planted[0]).getBlock() == ModBlocks.GLOWING_MUSHROOM;
			clearCave(c);
			c.check(still, "a Glowing Mushroom in a dark cave must not drop itself because of its own light");
		}));

		// ...and it must spread to dark neighbouring spots (the neighbours of a lit mushroom are one light level dimmer than it)
		tests.add(RTTest.named("worldgen.glowing_mushroom.spreads_in_a_dark_cave").run(c -> buildCave(c)).waitTicks(30).run(c -> {
			planted[0] = new BlockPos(c.origin.getX(), CAVE_Y + 1, c.origin.getZ());
			c.world.setBlock(planted[0], ModBlocks.GLOWING_MUSHROOM.defaultBlockState(), 3);
		}).waitTicks(40).run(c -> {
			Random random = new Random(99);

			for (int i = 0; i < 4000; i++)
			{
				BlockPos source = planted[0];
				List<BlockPos> found = new ArrayList<>();

				if (countMushrooms(c, found) == 0)
				{
					break;
				}

				source = found.get(random.nextInt(found.size()));
				ModBlocks.GLOWING_MUSHROOM.tick(c.world.getBlockState(source), c.world, source, random);
			}

			int count = countMushrooms(c, null);
			clearCave(c);
			c.check(count > 1, "a lone Glowing Mushroom must spread to neighbouring dark spots after many random ticks (got " + count + ")");
		}));

		// ---- Peace Candle church -------------------------------------------------------------
		tests.add(RTTest.named("worldgen.peace_candle.plains_houses_pool_is_the_church_wrapper").run(c -> {
			StructureTemplatePool pool = housesPool(c);
			c.check(pool != null, "village/plains/houses must be in the world's pool registry");
			c.eq("PeaceCandleChurchPool", pool.getClass().getSimpleName(), "the plains houses pool must be wrapped");
			c.check(vanillaHouses(pool) != pool, "the wrapper must keep the vanilla pool");
			StructureTemplate church = c.world.getStructureManager().get(new ResourceLocation("randomthings:peace_candle_church")).orElse(null);
			StructureTemplate vanillaChurch = c.world.getStructureManager().get(new ResourceLocation("minecraft:village/plains/houses/plains_temple_4")).orElse(null);
			c.check(church != null && vanillaChurch != null, "both church templates must load");
			c.eq(vanillaChurch.getSize().getX(), church.getSize().getX(), "church width");
			c.eq(vanillaChurch.getSize().getZ(), church.getSize().getZ(), "church depth");
			c.check(church.getSize().getY() >= vanillaChurch.getSize().getY(), "the Peace Candle church is at least as tall as the vanilla one (" + church.getSize() + " vs " + vanillaChurch.getSize() + ")");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.pool_swaps_about_a_third_of_the_vanilla_churches").run(c -> {
			StructureTemplatePool wrapper = housesPool(c);
			StructureTemplatePool vanilla = vanillaHouses(wrapper);
			int vanillaWithChurch = 0;
			int swapped = 0;

			for (int seed = 0; seed < 3000; seed++)
			{
				List<String> plain = names(vanilla.getShuffledTemplates(new Random(seed)));
				List<String> wrapped = names(wrapper.getShuffledTemplates(new Random(seed)));
				c.eq(plain.size(), wrapped.size(), "the swap must not change the number of candidates");
				boolean hasChurch = false;

				for (String name : plain)
				{
					hasChurch |= isVanillaChurch(name);
				}

				if (hasChurch)
				{
					vanillaWithChurch++;
					boolean anyPeace = false;

					for (String name : wrapped)
					{
						anyPeace |= isPeaceChurch(name);
					}

					if (anyPeace)
					{
						swapped++;

						for (int i = 0; i < plain.size(); i++)
						{
							if (isVanillaChurch(plain.get(i)))
							{
								c.check(isPeaceChurch(wrapped.get(i)), "a swap must replace every church entry");
							}
							else
							{
								c.eq(plain.get(i), wrapped.get(i), "a swap must leave every other entry alone");
							}
						}
					}
					else
					{
						c.eq(plain, wrapped, "an unswapped roll must equal the vanilla roll");
					}
				}
				else
				{
					c.eq(plain, wrapped, "lists without the church are never changed");
				}
			}

			RTDebug.log("worldgen test: pool swapped {} of {} church candidate lists ({})", swapped, vanillaWithChurch, swapped / (double) vanillaWithChurch);
			c.check(vanillaWithChurch > 500, "the vanilla pool must offer the church often (got " + vanillaWithChurch + ")");
			double share = swapped / (double) vanillaWithChurch;
			c.check(share > 0.28 && share < 0.39, "about a third of church candidates must be Peace Candle churches (got " + swapped + "/" + vanillaWithChurch + " = " + share + ")");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.config_switch_stops_the_swap").run(c -> {
			StructureTemplatePool wrapper = housesPool(c);
			BooleanSupplier before = PeaceCandleChurchPool.enabled;
			int swapped = 0;

			try
			{
				PeaceCandleChurchPool.enabled = () -> false;

				for (int seed = 0; seed < 1000; seed++)
				{
					for (String name : names(wrapper.getShuffledTemplates(new Random(seed))))
					{
						if (isPeaceChurch(name))
						{
							swapped++;
						}
					}
				}
			}
			finally
			{
				PeaceCandleChurchPool.enabled = before;
			}

			c.eq(0, swapped, "Peace Candle churches with PeaceCandle = false");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.real_village_assemblies_contain_the_church_about_a_third_of_the_time").run(c -> {
			int[] result = assembleVillages(c, 60);
			int vanilla = result[0];
			int peace = result[1];
			int churches = vanilla + peace;
			RTDebug.log("worldgen test: 60 assembled plains villages hold {} churches: {} vanilla, {} Peace Candle", churches, vanilla, peace);
			c.check(churches >= 20, "60 plains villages must contain plenty of churches (got " + churches + ")");
			c.check(peace > 0 && vanilla > 0, "both church kinds must show up (vanilla " + vanilla + ", Peace Candle " + peace + ")");
			double share = peace / (double) churches;
			c.check(share > 0.12 && share < 0.60, "Peace Candle share of churches should be near a third (got " + peace + "/" + churches + " = " + share + ")");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.assembler_builds_no_peace_candle_churches_with_the_config_off").run(c -> {
			BooleanSupplier before = PeaceCandleChurchPool.enabled;
			int[] result;

			try
			{
				PeaceCandleChurchPool.enabled = () -> false;
				result = assembleVillages(c, 40);
			}
			finally
			{
				PeaceCandleChurchPool.enabled = before;
			}

			c.eq(0, result[1], "Peace Candle churches in villages with PeaceCandle = false");
			c.check(result[0] > 0, "vanilla churches still generate (got " + result[0] + ")");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.church_piece_places_a_working_peace_candle").run(c -> {
			SinglePoolElement piece = StructurePoolElement.single("randomthings:peace_candle_church").apply(StructureTemplatePool.Projection.RIGID);
			StructureManager templates = c.world.getStructureManager();
			BlockPos base = new BlockPos(c.origin.getX() - 4, CAVE_Y, c.origin.getZ() - 4);
			BoundingBox box = piece.getBoundingBox(templates, base, Rotation.NONE);
			boolean placed = piece.place(templates, c.world, c.world.structureFeatureManager(), c.world.getChunkSource().getGenerator(), base, base, Rotation.NONE, box, new Random(1), false);
			c.check(placed, "the church piece must place");
			int candles = 0;
			PeaceCandleTileEntity found = null;

			for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(box.minX(), box.minY(), box.minZ()), new BlockPos(box.maxX(), box.maxY(), box.maxZ())))
			{
				if (c.world.getBlockState(pos).getBlock() == ModBlocks.PEACE_CANDLE)
				{
					candles++;

					if (c.world.getBlockEntity(pos) instanceof PeaceCandleTileEntity)
					{
						found = (PeaceCandleTileEntity) c.world.getBlockEntity(pos);
					}
				}
			}

			boolean registered = found != null && PeaceCandleTileEntity.candles.contains(found);

			for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(box.minX() - 1, box.minY() - 1, box.minZ() - 1), new BlockPos(box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)))
			{
				c.world.removeBlockEntity(pos);
				c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
			}

			c.eq(1, candles, "the church holds exactly one Peace Candle");
			c.check(found != null, "the Peace Candle block must have its tile entity");
			c.check(registered, "the placed candle must be in the active registry that blocks monster spawns");
		}));

		return tests;
	}

	/**
	 * A structure manager that knows no structures. The generator asks it about structure starts in a wide chunk range around
	 * every chunk it generates; this test only has a small patch of chunks in hand, so the answer is "none" instead of a
	 * region lookup out of bounds (the real one would also block on the server thread's own chunk source).
	 */
	private static class NoStructures extends StructureFeatureManager
	{
		private final ServerLevel world;

		private NoStructures(ServerLevel world, net.minecraft.world.level.LevelAccessor level, net.minecraft.world.level.levelgen.WorldGenSettings settings, net.minecraft.world.level.levelgen.structure.StructureCheck check)
		{
			super(level, settings, check);
			this.world = world;
		}

		static NoStructures of(ServerLevel world)
		{
			try
			{
				StructureFeatureManager real = world.structureFeatureManager();
				net.minecraft.world.level.levelgen.WorldGenSettings settings = (net.minecraft.world.level.levelgen.WorldGenSettings) net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(StructureFeatureManager.class, "f_47267_").get(real);
				net.minecraft.world.level.levelgen.structure.StructureCheck check = (net.minecraft.world.level.levelgen.structure.StructureCheck) net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(StructureFeatureManager.class, "f_196665_").get(real);
				return new NoStructures(world, world, settings, check);
			}
			catch (IllegalAccessException e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		public StructureFeatureManager forWorldGenRegion(WorldGenRegion region)
		{
			return new NoStructures(world, region, world.getServer().getWorldData().worldGenSettings(), net.minecraftforge.fml.util.ObfuscationReflectionHelper.<net.minecraft.world.level.levelgen.structure.StructureCheck, StructureFeatureManager>getPrivateValue(StructureFeatureManager.class, this, "f_196665_"));
		}

		@Override
		public List<net.minecraft.world.level.levelgen.structure.StructureStart> startsForFeature(net.minecraft.core.SectionPos pos, java.util.function.Predicate<ConfiguredStructureFeature<?, ?>> predicate)
		{
			return Collections.emptyList();
		}

		@Override
		public List<net.minecraft.world.level.levelgen.structure.StructureStart> startsForFeature(net.minecraft.core.SectionPos pos, ConfiguredStructureFeature<?, ?> feature)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean hasAnyStructureAt(BlockPos pos)
		{
			return false;
		}

		@Override
		public net.minecraft.world.level.levelgen.structure.StructureStart getStructureAt(BlockPos pos, ConfiguredStructureFeature<?, ?> feature)
		{
			return net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START;
		}

		@Override
		public net.minecraft.world.level.levelgen.structure.StructureStart getStructureWithPieceAt(BlockPos pos, ConfiguredStructureFeature<?, ?> feature)
		{
			return net.minecraft.world.level.levelgen.structure.StructureStart.INVALID_START;
		}
	}
}
