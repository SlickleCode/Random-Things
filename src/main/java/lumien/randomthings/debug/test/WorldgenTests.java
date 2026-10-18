package lumien.randomthings.debug.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import lumien.randomthings.worldgen.GlowingMushroomFeature;
import lumien.randomthings.worldgen.ModFeatures;
import lumien.randomthings.worldgen.PeaceCandleChurchPool;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.structure.VillagePieces;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Deterministic tests for the random-chance world generation (Glowing Mushroom patches and the Peace Candle village church), so
 * they do not have to be verified by flying around new worlds. Every random source is seeded, so a result never changes between runs.
 * <ul>
 * <li>Glowing Mushroom: the feature is wired into every biome with the original rarity; run directly inside a sealed cave
 * it places mushrooms that all have a roof and a valid spot; the config switch stops it.</li>
 * <li>Peace Candle: the plains houses pool really is the wrapper; across thousands of pool rolls about a third of the vanilla
 * churches are swapped; real village assemblies (the same jigsaw assembler a new world uses) contain the Peace Candle church;
 * the NBT template places a Peace Candle block with its tile entity; the config switch stops it.</li>
 * </ul>
 */
final class WorldgenTests
{
	/** Absolute Y of the sealed test cave, far above the flat test world so it cannot touch the other tests' area. */
	private static final int CAVE_Y = 90;

	private static final ResourceLocation HOUSES = new ResourceLocation("village/plains/houses");
	private static final String VANILLA_CHURCH = "Single[minecraft:village/plains/houses/plains_temple_4]";
	private static final String PEACE_CHURCH = "Single[randomthings:peace_candle_church]";

	private WorldgenTests()
	{
	}

	/** Stone floor + roof around the origin column with air between, 17x17 wide: a cave no sky can reach. */
	private static void buildCave(TestCtx c)
	{
		for (int x = -9; x <= 9; x++)
		{
			for (int z = -9; z <= 9; z++)
			{
				for (int y = 0; y <= 9; y++)
				{
					BlockPos pos = new BlockPos(c.origin.getX() + x, CAVE_Y + y, c.origin.getZ() + z);
					c.world.setBlockState(pos, y == 0 || y == 8 ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState(), 18);
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
					c.world.setBlockState(new BlockPos(c.origin.getX() + x, CAVE_Y + y, c.origin.getZ() + z), Blocks.AIR.getDefaultState(), 18);
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
	 * Generates a real vanilla-overworld patch of 7x7 chunks (terrain, surface, caves, the same steps {@code ChunkStatus} runs) and
	 * decorates the inner 5x5 chunks with the real decorator, so every registered feature - ours included - gets its normal chance.
	 * Nothing is written to the test world. Returns {mushrooms, inner chunks that received mushrooms, mushrooms at or above the surface}.
	 */
	private static int[] generateTerrainAndCount(TestCtx c, int chunkX, int chunkZ)
	{
		ServerWorld world = c.world;
		OverworldGenSettings settings = ChunkGeneratorType.SURFACE.createSettings();
		OverworldBiomeProviderSettings biomeSettings = BiomeProviderType.VANILLA_LAYERED.createSettings().setWorldInfo(world.getWorldInfo()).setGeneratorSettings(settings);
		BiomeProvider provider = BiomeProviderType.VANILLA_LAYERED.create(biomeSettings);
		OverworldChunkGenerator generator = ChunkGeneratorType.SURFACE.create(world, provider, settings);
		final int size = 7;
		ChunkPrimer[][] grid = new ChunkPrimer[size][size];

		for (int z = 0; z < size; z++)
		{
			for (int x = 0; x < size; x++)
			{
				grid[z][x] = new ChunkPrimer(new ChunkPos(chunkX + x, chunkZ + z), UpgradeData.EMPTY);
				generator.generateBiomes(grid[z][x]);
			}
		}

		for (ChunkPrimer[] row : grid)
		{
			for (ChunkPrimer primer : row)
			{
				generator.makeBase(world, primer);
				generator.generateSurface(primer);
				generator.carve(primer, GenerationStage.Carving.AIR);
				generator.carve(primer, GenerationStage.Carving.LIQUID);
				primer.setStatus(ChunkStatus.LIQUID_CARVERS);
				Heightmap.func_222690_a(primer, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
			}
		}

		int mushrooms = 0;
		int chunksWithMushrooms = 0;
		int exposed = 0;

		for (int z = 1; z < size - 1; z++)
		{
			for (int x = 1; x < size - 1; x++)
			{
				List<IChunk> neighbours = new ArrayList<>();

				for (int dz = -1; dz <= 1; dz++)
				{
					for (int dx = -1; dx <= 1; dx++)
					{
						neighbours.add(grid[z + dz][x + dx]);
					}
				}

				generator.decorate(new WorldGenRegion(world, neighbours));
			}
		}

		for (int z = 1; z < size - 1; z++)
		{
			for (int x = 1; x < size - 1; x++)
			{
				ChunkPrimer primer = grid[z][x];
				int here = 0;

				for (int bx = 0; bx < 16; bx++)
				{
					for (int bz = 0; bz < 16; bz++)
					{
						int surface = primer.getTopBlockY(Heightmap.Type.WORLD_SURFACE, bx, bz);

						for (int y = 1; y < 255; y++)
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

	/**
	 * The feature rolls its patch height with the first {@code nextInt(pos.getY() - 4)}; this returns seeds whose first roll lands the
	 * patch centre on {@code wantedY} (so a seeded run is aimed at the prepared cave).
	 */
	private static List<Long> seedsWithCentre(int maxY, int wantedY, int count)
	{
		List<Long> seeds = new ArrayList<>();

		for (long seed = 0; seeds.size() < count && seed < 100000; seed++)
		{
			if (new Random(seed).nextInt(maxY) == wantedY)
			{
				seeds.add(seed);
			}
		}

		return seeds;
	}

	private static JigsawPattern housesPool()
	{
		return JigsawManager.REGISTRY.get(HOUSES);
	}

	private static JigsawPattern vanillaHouses(JigsawPattern wrapper) throws Exception
	{
		Field field = wrapper.getClass().getDeclaredField("vanilla");
		field.setAccessible(true);
		return (JigsawPattern) field.get(wrapper);
	}

	private static List<String> names(List<JigsawPiece> pieces)
	{
		List<String> out = new ArrayList<>();

		for (JigsawPiece piece : pieces)
		{
			out.add(piece.toString());
		}

		return out;
	}

	/** Assembles {@code count} plains villages with the real jigsaw assembler and returns {vanilla churches, Peace Candle churches, villages}. */
	private static int[] assembleVillages(TestCtx c, int count) throws Exception
	{
		TemplateManager templates = c.world.getStructureTemplateManager();
		int vanilla = 0;
		int peace = 0;

		for (int i = 0; i < count; i++)
		{
			SharedSeedRandom rand = new SharedSeedRandom();
			rand.setLargeFeatureSeed(1234567L + i * 31L, i * 7, i * 13);
			List<StructurePiece> pieces = new ArrayList<>();
			VillagePieces.func_214838_a(c.world.getChunkProvider().getChunkGenerator(), templates, new BlockPos(i * 160, 64, 0), pieces, rand, new VillageConfig("village/plains/town_centers", 6));

			for (StructurePiece piece : pieces)
			{
				if (piece instanceof AbstractVillagePiece)
				{
					String name = ((AbstractVillagePiece) piece).getJigsawPiece().toString();

					if (VANILLA_CHURCH.equals(name))
					{
						vanilla++;
					}
					else if (PEACE_CHURCH.equals(name))
					{
						peace++;
					}
				}
			}
		}

		return new int[] { vanilla, peace, count };
	}

	/** Writes {@code biome} into every biome column of the chunks around the test origin. */
	private static void paintBiome(TestCtx c, Biome biome)
	{
		for (int cx = (c.origin.getX() - 24) >> 4; cx <= (c.origin.getX() + 24) >> 4; cx++)
		{
			for (int cz = (c.origin.getZ() - 24) >> 4; cz <= (c.origin.getZ() + 24) >> 4; cz++)
			{
				java.util.Arrays.fill(c.world.getChunk(cx, cz).getBiomes(), biome);
			}
		}
	}

	/** Sends the chunks around the test origin to the real player again, like a fresh chunk load (biomes travel with the chunk). */
	private static void resendAround(TestCtx c)
	{
		for (int cx = (c.origin.getX() - 24) >> 4; cx <= (c.origin.getX() + 24) >> 4; cx++)
		{
			for (int cz = (c.origin.getZ() - 24) >> 4; cz <= (c.origin.getZ() + 24) >> 4; cz++)
			{
				c.player.connection.sendPacket(new net.minecraft.network.play.server.SChunkDataPacket(c.world.getChunk(cx, cz), 65535));
			}
		}
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Glowing Mushroom ----------------------------------------------------------------
		tests.add(RTTest.named("worldgen.glowing_mushroom.is_registered_in_every_biome_with_the_original_rarity").run(c -> {
			int biomes = 0;

			for (Biome biome : ForgeRegistries.BIOMES)
			{
				boolean found = false;

				for (ConfiguredFeature<?> feature : biome.getFeatures(GenerationStage.Decoration.UNDERGROUND_DECORATION))
				{
					if (feature.config instanceof DecoratedFeatureConfig)
					{
						DecoratedFeatureConfig decorated = (DecoratedFeatureConfig) feature.config;

						if (decorated.feature.feature == ModFeatures.GLOWING_MUSHROOM)
						{
							found = true;
							c.check(decorated.decorator.decorator == Placement.CHANCE_HEIGHTMAP, biome.getRegistryName() + ": placement must be CHANCE_HEIGHTMAP");
							c.check(decorated.decorator.config instanceof ChanceConfig && ((ChanceConfig) decorated.decorator.config).chance == 4, biome.getRegistryName() + ": chance must be 1 in 4 chunks like the original");
						}
					}
				}

				c.check(found, "Glowing Mushroom must be decorated into " + biome.getRegistryName());
				biomes++;
			}

			c.check(biomes > 40, "expected the full vanilla biome list, saw " + biomes);
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
				List<Long> seeds = seedsWithCentre(caveMaxY, CAVE_Y + 3, patchesToRun);
				c.check(seeds.size() == patchesToRun, "could not find seeds aiming the patch at the cave");
				BlockPos column = new BlockPos(c.origin.getX(), caveMaxY + 4, c.origin.getZ());
				boolean placed = ModFeatures.GLOWING_MUSHROOM.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(seeds.get(index)), column, IFeatureConfig.NO_FEATURE_CONFIG);
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
					c.check(!c.world.canBlockSeeSky(pos), "mushroom at " + pos + " must not see the sky");
					c.check(Math.abs(pos.getX() - column.getX()) <= 7 && Math.abs(pos.getZ() - column.getZ()) <= 7, "mushroom at " + pos + " outside the +-7 patch radius");
					c.check(Math.abs(pos.getY() - (CAVE_Y + 3)) <= 3, "mushroom at " + pos + " outside the +-3 patch height");
					c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
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
			java.util.function.BooleanSupplier before = GlowingMushroomFeature.enabled;

			try
			{
				GlowingMushroomFeature.enabled = () -> false;
				int maxY = CAVE_Y + 5;
				BlockPos column = new BlockPos(c.origin.getX(), maxY + 4, c.origin.getZ());
				boolean placed = false;

				for (long seed : seedsWithCentre(maxY, CAVE_Y + 3, 6))
				{
					placed |= ModFeatures.GLOWING_MUSHROOM.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(seed), column, IFeatureConfig.NO_FEATURE_CONFIG);
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
					throw new TestCtx.Failure("terrain generation failed: " + root + " at " + (trace.length > 0 ? trace[0] : "?") + " / " + (trace.length > 1 ? trace[1] : "?") + " / " + (trace.length > 2 ? trace[2] : "?"));
				}

				mushrooms += result[0];
				chunks += result[1];
				exposed += result[2];
			}

			RTDebug.log("worldgen test: real terrain generation, {} areas of 5x5 decorated chunks: {} Glowing Mushrooms in {} chunks, {} not under a roof", areas, mushrooms, chunks, exposed);
			c.check(mushrooms > 0, "real terrain generation must place Glowing Mushrooms underground (found none in " + areas * 25 + " chunks)");
			c.check(chunks >= 2, "mushroom patches must show up in several chunks (got " + chunks + ")");
		}));

		// a placed mushroom lights its own block; the block's survival rule must not count that light, or it drops on the next update
		final BlockPos[] planted = new BlockPos[1];
		tests.add(RTTest.named("worldgen.glowing_mushroom.placed_mushroom_survives_updates_in_the_dark").run(c -> buildCave(c)).waitTicks(30).run(c -> {
			planted[0] = new BlockPos(c.origin.getX(), CAVE_Y + 1, c.origin.getZ());
			c.world.setBlockState(planted[0], ModBlocks.GLOWING_MUSHROOM.getDefaultState(), 3);
			c.check(c.world.getBlockState(planted[0]).getBlock() == ModBlocks.GLOWING_MUSHROOM, "the mushroom must be placeable in a dark cave");
		}).waitTicks(40).run(c -> {
			// nudge the neighbours, which is what a drop check listens for
			c.world.setBlockState(planted[0].east(), Blocks.STONE.getDefaultState(), 3);
			c.world.setBlockState(planted[0].east(), Blocks.AIR.getDefaultState(), 3);
		}).waitTicks(10).run(c -> {
			boolean still = c.world.getBlockState(planted[0]).getBlock() == ModBlocks.GLOWING_MUSHROOM;
			clearCave(c);
			c.check(still, "a Glowing Mushroom in a dark cave must not drop itself because of its own light");
		}));

		// ...and it must spread to dark neighbouring spots (the neighbours of a lit mushroom are one light level dimmer than it)
		tests.add(RTTest.named("worldgen.glowing_mushroom.spreads_in_a_dark_cave").run(c -> buildCave(c)).waitTicks(30).run(c -> {
			planted[0] = new BlockPos(c.origin.getX(), CAVE_Y + 1, c.origin.getZ());
			c.world.setBlockState(planted[0], ModBlocks.GLOWING_MUSHROOM.getDefaultState(), 3);
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
			JigsawPattern pool = housesPool();
			c.check(pool != null && pool != JigsawPattern.INVALID, "village/plains/houses must be registered");
			c.eq("PeaceCandleChurchPool", pool.getClass().getSimpleName(), "the plains houses pool must be wrapped");
			c.check(vanillaHouses(pool) != pool, "the wrapper must keep the vanilla pool");
			Template church = c.world.getStructureTemplateManager().getTemplate(new ResourceLocation("randomthings:peace_candle_church"));
			Template vanillaChurch = c.world.getStructureTemplateManager().getTemplate(new ResourceLocation("minecraft:village/plains/houses/plains_temple_4"));
			c.check(church != null && vanillaChurch != null, "both church templates must load");
			c.eq(vanillaChurch.getSize().getX(), church.getSize().getX(), "church width");
			c.eq(vanillaChurch.getSize().getZ(), church.getSize().getZ(), "church depth");
			c.check(church.getSize().getY() >= vanillaChurch.getSize().getY(), "the Peace Candle church is at least as tall as the vanilla one (" + church.getSize() + " vs " + vanillaChurch.getSize() + ")");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.pool_swaps_about_a_third_of_the_vanilla_churches").run(c -> {
			JigsawPattern wrapper = housesPool();
			JigsawPattern vanilla = vanillaHouses(wrapper);
			int vanillaWithChurch = 0;
			int swapped = 0;
			int untouchedOthers = 0;

			for (int seed = 0; seed < 3000; seed++)
			{
				List<String> plain = names(vanilla.getShuffledPieces(new Random(seed)));
				List<String> wrapped = names(wrapper.getShuffledPieces(new Random(seed)));
				c.eq(plain.size(), wrapped.size(), "the swap must not change the number of candidates");

				if (plain.contains(VANILLA_CHURCH))
				{
					vanillaWithChurch++;
					if (wrapped.contains(PEACE_CHURCH))
					{
						swapped++;
						List<String> expected = new ArrayList<>();

						for (String name : plain)
						{
							expected.add(VANILLA_CHURCH.equals(name) ? PEACE_CHURCH : name);
						}

						c.eq(expected, wrapped, "a swap must replace only the church entries");
					}
					else
					{
						c.eq(plain, wrapped, "an unswapped roll must equal the vanilla roll");
					}
				}
				else
				{
					untouchedOthers++;
					c.eq(plain, wrapped, "lists without the church are never changed");
				}
			}

			RTDebug.log("worldgen test: pool swapped {} of {} church candidate lists ({})", swapped, vanillaWithChurch, swapped / (double) vanillaWithChurch);
			c.check(vanillaWithChurch > 500, "the vanilla pool must offer the church often (got " + vanillaWithChurch + ")");
			double share = swapped / (double) vanillaWithChurch;
			c.check(share > 0.28 && share < 0.39, "about a third of church candidates must be Peace Candle churches (got " + swapped + "/" + vanillaWithChurch + " = " + share + ")");
			c.check(untouchedOthers >= 0, "sanity");
		}));

		tests.add(RTTest.named("worldgen.peace_candle.config_switch_stops_the_swap").run(c -> {
			JigsawPattern wrapper = housesPool();
			java.util.function.BooleanSupplier before = PeaceCandleChurchPool.enabled;
			int swapped = 0;

			try
			{
				PeaceCandleChurchPool.enabled = () -> false;

				for (int seed = 0; seed < 1000; seed++)
				{
					if (names(wrapper.getShuffledPieces(new Random(seed))).contains(PEACE_CHURCH))
					{
						swapped++;
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
			java.util.function.BooleanSupplier before = PeaceCandleChurchPool.enabled;
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
			SingleJigsawPiece piece = new SingleJigsawPiece("randomthings:peace_candle_church");
			TemplateManager templates = c.world.getStructureTemplateManager();
			BlockPos base = new BlockPos(c.origin.getX() - 4, CAVE_Y, c.origin.getZ() - 4);
			MutableBoundingBox box = piece.getBoundingBox(templates, base, Rotation.NONE);
			boolean placed = piece.place(templates, c.world, base, Rotation.NONE, box, new Random(1));
			c.check(placed, "the church piece must place");
			int candles = 0;
			PeaceCandleTileEntity found = null;

			for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ)))
			{
				if (c.world.getBlockState(pos).getBlock() == ModBlocks.PEACE_CANDLE)
				{
					candles++;

					if (c.world.getTileEntity(pos) instanceof PeaceCandleTileEntity)
					{
						found = (PeaceCandleTileEntity) c.world.getTileEntity(pos);
					}
				}
			}

			boolean registered = found != null && PeaceCandleTileEntity.candles.contains(found);

			for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(box.minX - 1, box.minY - 1, box.minZ - 1), new BlockPos(box.maxX + 1, box.maxY + 1, box.maxZ + 1)))
			{
				c.world.removeTileEntity(pos);
				c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
			}

			c.eq(1, candles, "the church holds exactly one Peace Candle");
			c.check(found != null, "the Peace Candle block must have its tile entity");
			c.check(registered, "the placed candle must be in the active registry that blocks monster spawns");
		}));

		// ---- Other natural features (registration, placement rules) ---------------------------
		String[][] natural = { { "bean_sprout", "2", "VEGETAL_DECORATION" }, { "lotus", "10", "VEGETAL_DECORATION" }, { "ancient_furnace", "400", "LOCAL_MODIFICATIONS" } };

		for (String[] spec : natural)
		{
			tests.add(RTTest.named("worldgen." + spec[0] + ".is_registered_in_every_biome_in_its_step_with_the_ported_rarity").run(c -> {
				GenerationStage.Decoration step = GenerationStage.Decoration.valueOf(spec[2]);
				int biomes = 0;

				for (Biome biome : ForgeRegistries.BIOMES)
				{
					boolean found = false;

					for (GenerationStage.Decoration stage : GenerationStage.Decoration.values())
					{
						for (ConfiguredFeature<?> feature : biome.getFeatures(stage))
						{
							if (feature.config instanceof DecoratedFeatureConfig)
							{
								DecoratedFeatureConfig decorated = (DecoratedFeatureConfig) feature.config;

								if (decorated.feature.feature.getRegistryName() != null && decorated.feature.feature.getRegistryName().equals(new ResourceLocation("randomthings", spec[0])))
								{
									found = true;
									c.eq(step, stage, spec[0] + " must be in its own decoration step in " + biome.getRegistryName());
									c.check(decorated.decorator.decorator == Placement.CHANCE_HEIGHTMAP, biome.getRegistryName() + ": placement must be CHANCE_HEIGHTMAP");
									c.check(decorated.decorator.config instanceof ChanceConfig && ((ChanceConfig) decorated.decorator.config).chance == Integer.parseInt(spec[1]), biome.getRegistryName() + ": chance must be 1 in " + spec[1] + " chunks");
								}
							}
						}
					}

					c.check(found, spec[0] + " must be decorated into " + biome.getRegistryName());
					biomes++;
				}

				c.check(biomes > 40, "expected the full vanilla biome list, saw " + biomes);
			}));
		}

		tests.add(RTTest.named("worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block").run(c -> {
			BlockPos pos = c.at(2, 1, 2);
			Block[] soils = { Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.FARMLAND };

			for (Block soil : soils)
			{
				c.world.setBlockState(pos.down(), soil.getDefaultState(), 18);
				c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
				boolean placed = ModFeatures.BEAN_SPROUT.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(1), pos, IFeatureConfig.NO_FEATURE_CONFIG);
				Block block = c.world.getBlockState(pos).getBlock();
				c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
				c.check(placed && block == ModBlocks.BEAN_SPROUT, "a sprout must be placed on " + soil);
			}

			c.world.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 18);
			c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
			boolean stone = ModFeatures.BEAN_SPROUT.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(1), pos, IFeatureConfig.NO_FEATURE_CONFIG);
			c.world.setBlockState(pos.down(), Blocks.GRASS_BLOCK.getDefaultState(), 18);
			c.world.setBlockState(pos, Blocks.STONE.getDefaultState(), 18);
			boolean solid = ModFeatures.BEAN_SPROUT.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(1), pos, IFeatureConfig.NO_FEATURE_CONFIG);
			c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
			c.check(!stone, "a sprout must not be placed on stone");
			c.check(!solid, "a sprout must not replace a solid block");
		}));

		tests.add(RTTest.named("worldgen.lotus.snowy_biome_check_and_refuses_in_a_non_snowy_biome").run(c -> {
						c.check(net.minecraftforge.common.BiomeDictionary.hasType(Biomes.SNOWY_TUNDRA, net.minecraftforge.common.BiomeDictionary.Type.SNOWY), "snowy_tundra must count as SNOWY");
			c.check(!net.minecraftforge.common.BiomeDictionary.hasType(Biomes.PLAINS, net.minecraftforge.common.BiomeDictionary.Type.SNOWY), "plains must not count as SNOWY");
			c.check(!net.minecraftforge.common.BiomeDictionary.hasType(Biomes.DESERT, net.minecraftforge.common.BiomeDictionary.Type.SNOWY), "desert must not count as SNOWY");
			BlockPos pos = c.at(2, 1, 2);

			if (!net.minecraftforge.common.BiomeDictionary.hasType(c.world.getBiome(pos), net.minecraftforge.common.BiomeDictionary.Type.SNOWY))
			{
				c.world.setBlockState(pos.down(), Blocks.GRASS_BLOCK.getDefaultState(), 18);
				c.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 18);
				boolean placed = ModFeatures.LOTUS.place(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(1), pos, IFeatureConfig.NO_FEATURE_CONFIG);
				c.check(!placed, "a lotus must not generate in a non-snowy biome");
			}
		}));

		// the test world is warm, so the area is repainted with a cold biome (and restored afterwards); the feature reads the biome through
		// the world's biome manager, which blends the neighbouring cells, hence the whole surrounding chunks are painted
		tests.add(RTTest.named("worldgen.ancient_furnace.generates_only_in_cold_biomes_on_solid_ground").run(c -> {
						BlockPos pos = c.at(2, 1, 2);
			Biome original = c.world.getBiome(pos);
			Biome cold = Biomes.SNOWY_TUNDRA;
			c.check(original.getTemperature(pos.down()) >= 0.15F, "the flat test world is expected to be warm (got " + original.getRegistryName() + ")");
			ServerWorld w = c.world;
			Random random = new Random(1);

			try
			{
				w.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(w, w.getChunkProvider().getChunkGenerator(), random, pos, IFeatureConfig.NO_FEATURE_CONFIG), "no furnace in a warm biome");
				c.eq(Blocks.STONE, w.getBlockState(pos.down()).getBlock(), "the ground must be untouched in a warm biome");

				paintBiome(c, cold);
				c.check(w.getBiome(pos).getTemperature(pos.down()) < 0.15F, "the painted biome must read back as cold");

				w.setBlockState(pos.down(), Blocks.AIR.getDefaultState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(w, w.getChunkProvider().getChunkGenerator(), random, pos, IFeatureConfig.NO_FEATURE_CONFIG), "no furnace over air");
				w.setBlockState(pos.down(), Blocks.WATER.getDefaultState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(w, w.getChunkProvider().getChunkGenerator(), random, pos, IFeatureConfig.NO_FEATURE_CONFIG), "no furnace into water");

				w.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 18);
				c.check(ModFeatures.ANCIENT_FURNACE.place(w, w.getChunkProvider().getChunkGenerator(), random, pos, IFeatureConfig.NO_FEATURE_CONFIG), "a furnace must generate on stone in a cold biome");
				c.eq(ModBlocks.ANCIENT_FURNACE, w.getBlockState(pos.down()).getBlock(), "the furnace replaces the ground block");
				c.check(!ModFeatures.ANCIENT_FURNACE.place(w, w.getChunkProvider().getChunkGenerator(), random, pos, IFeatureConfig.NO_FEATURE_CONFIG), "an existing furnace is not replaced again");
			}
			finally
			{
				paintBiome(c, original);
				w.setBlockState(pos.down(), Blocks.STONE.getDefaultState(), 18);
			}
		}));

		// the heat-up's finish (warm the surroundings, then explode) is split: only the warming is run, the explosion would wreck the test area
		tests.add(RTTest.named("worldgen.ancient_furnace.heating_converts_cold_biomes_melts_snow_and_ice_and_leaves_warm_biomes_alone").run(c -> {
						Biome original = c.world.getBiome(c.at(2, 1, 2));
			BlockPos snow = c.at(2, 1, 2);
			BlockPos ice = c.at(4, 1, 2);
			ServerWorld w = c.world;
			java.lang.reflect.Method warm;

			try
			{
				warm = lumien.randomthings.tileentity.AncientFurnaceTileEntity.class.getDeclaredMethod("warmSurroundings", net.minecraft.world.World.class, BlockPos.class);
				warm.setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach warmSurroundings: " + e);
			}

			try
			{
				// a warm (unconvertible) area first: neither the biome nor the snow may change
				w.setBlockState(snow, Blocks.SNOW_BLOCK.getDefaultState(), 18);
				warm.invoke(null, w, c.at(0, 1, 0));
				c.eq(original.getRegistryName(), w.getBiome(snow).getRegistryName(), "a warm biome is not converted");
				c.eq(Blocks.SNOW_BLOCK, w.getBlockState(snow).getBlock(), "snow in a biome without a warm counterpart stays");

				// then a snowy area: biome becomes plains, snow disappears, ice melts
				paintBiome(c, Biomes.SNOWY_TUNDRA);
				w.setBlockState(ice, Blocks.PACKED_ICE.getDefaultState(), 18);
				warm.invoke(null, w, c.at(0, 1, 0));
				c.eq(Biomes.PLAINS.getRegistryName(), w.getBiome(snow).getRegistryName(), "snowy tundra must become plains");
				c.eq(Biomes.PLAINS.getRegistryName(), w.getBiome(ice).getRegistryName(), "the whole radius is converted");
				c.eq(Blocks.AIR, w.getBlockState(snow).getBlock(), "snow blocks are removed");
				c.eq(Blocks.WATER, w.getBlockState(ice).getBlock(), "packed ice becomes water");
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Failure("warmSurroundings threw: " + e.getCause());
			}
			finally
			{
				paintBiome(c, original);
				w.setBlockState(snow, Blocks.AIR.getDefaultState(), 18);
				w.setBlockState(ice, Blocks.AIR.getDefaultState(), 18);
			}
		}));

		// client side of the heat-up: the server rewrites the biome, the client only learns about it from the chunk resend (checklist N6)
		final java.lang.reflect.Method[] warmClient = new java.lang.reflect.Method[1];
		final Biome[] originalClient = new Biome[1];
		tests.add(RTTest.named("worldgen.ancient_furnace.client_sees_the_biome_change_after_heating").run(c -> {
			if (net.minecraftforge.fml.loading.FMLEnvironment.dist != net.minecraftforge.api.distmarker.Dist.CLIENT)
			{
				throw new TestCtx.Skip("needs a client run (the dedicated server has no client to look at)");
			}

			try
			{
				warmClient[0] = lumien.randomthings.tileentity.AncientFurnaceTileEntity.class.getDeclaredMethod("warmSurroundings", net.minecraft.world.World.class, BlockPos.class);
				warmClient[0].setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach warmSurroundings: " + e);
			}

			originalClient[0] = c.world.getBiome(c.at(2, 1, 2));
			paintBiome(c, Biomes.SNOWY_TUNDRA);
			resendAround(c);
		}).waitTicks(20).run(c -> {
			if (warmClient[0] == null)
			{
				return;
			}

			c.eq(Biomes.SNOWY_TUNDRA.getRegistryName(), ClientProbe.biomeAt(c.at(2, 1, 2)), "precondition: the client must show the repainted biome after the resend");

			try
			{
				warmClient[0].invoke(null, c.world, c.at(0, 1, 0));
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Failure("warmSurroundings threw: " + e.getCause());
			}
		}).waitTicks(20).run(c -> {
			if (warmClient[0] == null)
			{
				return;
			}

			ResourceLocation client = ClientProbe.biomeAt(c.at(2, 1, 2));
			paintBiome(c, originalClient[0]);
			resendAround(c);
			c.eq(Biomes.PLAINS.getRegistryName(), client, "the client must see the converted biome without relogging");
		}));

		// ---- Spectre dimension generator -------------------------------------------------------
		tests.add(RTTest.named("worldgen.spectre.dimension_uses_the_void_generator").run(c -> {
			lumien.randomthings.handler.spectre.SpectreHandler handler = lumien.randomthings.handler.spectre.SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			ServerWorld spectre = (ServerWorld) handler.getWorld();
			c.check(spectre.getChunkProvider().getChunkGenerator() instanceof lumien.randomthings.handler.spectre.SpectreChunkGenerator, "the Spectre dimension must use SpectreChunkGenerator, got " + spectre.getChunkProvider().getChunkGenerator().getClass());
			c.check(!spectre.getDimension().hasSkyLight(), "no skylight");
		}));

		tests.add(RTTest.named("worldgen.spectre.a_generated_chunk_far_from_the_cubes_is_empty_air").run(c -> {
			lumien.randomthings.handler.spectre.SpectreHandler handler = lumien.randomthings.handler.spectre.SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			IChunk chunk = handler.getWorld().getChunk(5000, 5000);

			for (int x = 0; x < 16; x += 5)
			{
				for (int z = 0; z < 16; z += 5)
				{
					for (int y = 0; y < 256; y += 17)
					{
						c.check(chunk.getBlockState(new BlockPos(x, y, z)).isAir(), "void chunk must be air at " + x + "," + y + "," + z);
					}
				}
			}
		}));

		return tests;
	}
}
