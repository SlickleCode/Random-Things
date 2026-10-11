package lumien.randomthings.debug.test;

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
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.FuzzedBiomeMagnifier;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.JigsawStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;

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
	 * Generates a real vanilla-overworld patch of 7x7 chunks (terrain, surface, caves - the same steps {@code ChunkStatus} runs) and
	 * decorates the inner 5x5 chunks with the real decorator, so every registered feature - ours included - gets its normal chance.
	 * Nothing is written to the test world. Returns {mushrooms, inner chunks that received mushrooms, mushrooms at or above the surface}.
	 */
	private static int[] generateTerrainAndCount(TestCtx c, int chunkX, int chunkZ)
	{
		ServerWorld world = c.world;
		DynamicRegistries registries = world.func_241828_r();
		Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
		long seed = world.getSeed();
		OverworldBiomeProvider provider = new OverworldBiomeProvider(seed, false, false, biomes);
		NoiseChunkGenerator generator = new NoiseChunkGenerator(provider, seed, () -> DimensionSettings.getDefaultDimensionSettings());
		BiomeManager biomeManager = new BiomeManager(provider, BiomeManager.getHashedSeed(seed), FuzzedBiomeMagnifier.INSTANCE);
		final int size = 7;
		ChunkPrimer[][] grid = new ChunkPrimer[size][size];

		for (int z = 0; z < size; z++)
		{
			for (int x = 0; x < size; x++)
			{
				grid[z][x] = new ChunkPrimer(new ChunkPos(chunkX + x, chunkZ + z), UpgradeData.EMPTY);
				grid[z][x].setStatus(ChunkStatus.STRUCTURE_REFERENCES);
				generator.func_242706_a(biomes, grid[z][x]);
			}
		}

		for (ChunkPrimer[] row : grid)
		{
			for (ChunkPrimer primer : row)
			{
				WorldGenRegion own = new WorldGenRegion(world, Collections.<IChunk>singletonList(primer));
				generator.func_230352_b_(own, world.getStructureManager().getStructureManager(own), primer);
				generator.generateSurface(new WorldGenRegion(world, Collections.<IChunk>singletonList(primer)), primer);
				generator.func_230350_a_(seed, biomeManager, primer, GenerationStage.Carving.AIR);
				generator.func_230350_a_(seed, biomeManager, primer, GenerationStage.Carving.LIQUID);
				primer.setStatus(ChunkStatus.LIQUID_CARVERS);
				Heightmap.updateChunkHeightmaps(primer, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
			}
		}

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

				WorldGenRegion region = new WorldGenRegion(world, neighbours);
				generator.func_230351_a_(region, world.getStructureManager().getStructureManager(region));
			}
		}

		int mushrooms = 0;
		int chunksWithMushrooms = 0;
		int exposed = 0;

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

	private static JigsawPattern housesPool(TestCtx c)
	{
		return c.world.func_241828_r().getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(HOUSES);
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
		DynamicRegistries registries = c.world.func_241828_r();
		TemplateManager templates = c.world.getStructureTemplateManager();
		Biome plains = registries.getRegistry(Registry.BIOME_KEY).getOrDefault(Biomes.PLAINS.getLocation());
		JigsawPattern centers = registries.getRegistry(Registry.JIGSAW_POOL_KEY).getOrDefault(new ResourceLocation("village/plains/town_centers"));
		int vanilla = 0;
		int peace = 0;

		for (int i = 0; i < count; i++)
		{
			StructureStart<VillageConfig> start = new JigsawStructure.Start((JigsawStructure) Structure.VILLAGE, i * 11, i * 17, MutableBoundingBox.getNewBoundingBox(), 0, 1234567L + i * 31L);
			start.func_230364_a_(registries, c.world.getChunkProvider().getChunkGenerator(), templates, i * 11, i * 17, plains, new VillageConfig(() -> centers, 6));

			for (StructurePiece piece : start.getComponents())
			{
				if (piece instanceof AbstractVillagePiece)
				{
					String name = ((AbstractVillagePiece) piece).getJigsawPiece().toString();

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
			Registry<Biome> biomes = c.world.func_241828_r().getRegistry(Registry.BIOME_KEY);
			int overworldStyle = 0;

			for (Biome biome : biomes)
			{
				boolean found = false;
				boolean found4 = false;
				List<List<Supplier<ConfiguredFeature<?, ?>>>> stages = biome.getGenerationSettings().getFeatures();
				List<Supplier<ConfiguredFeature<?, ?>>> stage = stages.size() > GenerationStage.Decoration.UNDERGROUND_DECORATION.ordinal() ? stages.get(GenerationStage.Decoration.UNDERGROUND_DECORATION.ordinal()) : Collections.<Supplier<ConfiguredFeature<?, ?>>>emptyList();

				for (Supplier<ConfiguredFeature<?, ?>> supplier : stage)
				{
					String json = ConfiguredFeature.CODEC.encodeStart(JsonOps.INSTANCE, supplier.get()).result().map(Object::toString).orElse("");

					if (json.contains("randomthings:glowing_mushroom"))
					{
						found = true;
						found4 = json.contains("\"chance\":4") && json.contains("minecraft:heightmap") && json.contains("minecraft:square");
					}
				}

				Biome.Category category = biome.getCategory();
				boolean overworld = category != Biome.Category.NETHER && category != Biome.Category.THEEND && category != Biome.Category.NONE;
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
				List<Long> seeds = seedsWithCentre(caveMaxY, CAVE_Y + 3, patchesToRun);
				c.check(seeds.size() == patchesToRun, "could not find seeds aiming the patch at the cave");
				BlockPos column = new BlockPos(c.origin.getX(), caveMaxY + 4, c.origin.getZ());
				boolean placed = ModFeatures.GLOWING_MUSHROOM.generate(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(seeds.get(index)), column, NoFeatureConfig.INSTANCE);
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
			BooleanSupplier before = GlowingMushroomFeature.enabled;

			try
			{
				GlowingMushroomFeature.enabled = () -> false;
				int maxY = CAVE_Y + 5;
				BlockPos column = new BlockPos(c.origin.getX(), maxY + 4, c.origin.getZ());
				boolean placed = false;

				for (long seed : seedsWithCentre(maxY, CAVE_Y + 3, 6))
				{
					placed |= ModFeatures.GLOWING_MUSHROOM.generate(c.world, c.world.getChunkProvider().getChunkGenerator(), new Random(seed), column, NoFeatureConfig.INSTANCE);
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
					throw new TestCtx.Failure("terrain generation failed: " + root + " at " + (trace.length > 0 ? trace[0] : "?") + " / " + (trace.length > 1 ? trace[1] : "?") + " / " + (trace.length > 2 ? trace[2] : "?"));
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
			JigsawPattern pool = housesPool(c);
			c.check(pool != null, "village/plains/houses must be in the world's pool registry");
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
			JigsawPattern wrapper = housesPool(c);
			JigsawPattern vanilla = vanillaHouses(wrapper);
			int vanillaWithChurch = 0;
			int swapped = 0;

			for (int seed = 0; seed < 3000; seed++)
			{
				List<String> plain = names(vanilla.getShuffledPieces(new Random(seed)));
				List<String> wrapped = names(wrapper.getShuffledPieces(new Random(seed)));
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
			JigsawPattern wrapper = housesPool(c);
			BooleanSupplier before = PeaceCandleChurchPool.enabled;
			int swapped = 0;

			try
			{
				PeaceCandleChurchPool.enabled = () -> false;

				for (int seed = 0; seed < 1000; seed++)
				{
					for (String name : names(wrapper.getShuffledPieces(new Random(seed))))
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
			SingleJigsawPiece piece = JigsawPiece.func_242859_b("randomthings:peace_candle_church").apply(JigsawPattern.PlacementBehaviour.RIGID);
			TemplateManager templates = c.world.getStructureTemplateManager();
			BlockPos base = new BlockPos(c.origin.getX() - 4, CAVE_Y, c.origin.getZ() - 4);
			MutableBoundingBox box = piece.getBoundingBox(templates, base, Rotation.NONE);
			boolean placed = piece.func_230378_a_(templates, c.world, c.world.getStructureManager(), c.world.getChunkProvider().getChunkGenerator(), base, base, Rotation.NONE, box, new Random(1), false);
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

		return tests;
	}
}
