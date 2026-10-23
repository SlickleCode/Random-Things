package lumien.randomthings.debug.test;

import net.minecraft.core.registries.Registries;

import java.util.HashMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.chunk.status.ChunkStatus;
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
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
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

	private static final ResourceLocation HOUSES = ResourceLocation.parse("village/plains/houses");
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

	private static final int INNER = 3;

	/**
	 * Loads real vanilla-overworld chunks (a 3x3 patch the real generator fully decorates, so every registered feature - ours included -
	 * gets its normal chance) and counts the Glowing Mushrooms in them. Needs a normal-terrain overworld, which the default (flat) suite
	 * world is not: it skips there, and the second suite pass ({@code docs/testing/normal-world-pass.txt}, run by {@code suite-all.sh})
	 * runs it in a normal-terrain server world. Returns {mushrooms, chunks that received mushrooms, mushrooms at or above the surface}.
	 */
	private static int[] generateTerrainAndCount(TestCtx c, int chunkX, int chunkZ)
	{
		int mushrooms = 0;
		int chunksWithMushrooms = 0;
		int exposed = 0;

		for (int z = 0; z < INNER; z++)
		{
			for (int x = 0; x < INNER; x++)
			{
				ChunkAccess chunk = c.world.getChunk(chunkX + x, chunkZ + z, ChunkStatus.FULL, true);
				int here = 0;

				for (int bx = 0; bx < 16; bx++)
				{
					for (int bz = 0; bz < 16; bz++)
					{
						int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, bx, bz);

						for (int y = chunk.getMinY() + 1; y < chunk.getMaxY() + 1 - 1; y++)
						{
							if (chunk.getBlockState(new BlockPos(bx, y, bz)).getBlock() == ModBlocks.GLOWING_MUSHROOM)
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

	private static StructureTemplatePool housesPool(TestCtx c)
	{
		return c.world.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL).getValue(HOUSES);
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
		StructureTemplateManager templates = c.world.getStructureManager();
		Holder<StructureTemplatePool> centers = registries.lookupOrThrow(Registries.TEMPLATE_POOL).getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse("village/plains/town_centers")));
		ChunkGenerator generator = c.world.getChunkSource().getGenerator();
		int vanilla = 0;
		int peace = 0;
		for (int i = 0; i < count; i++)
		{
			ChunkPos chunkPos = new ChunkPos(i * 11, i * 17);
			Holder<Structure> village = registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse("village_plains")));
			// vanilla's own village start (a hand-built JigsawPlacement call in this harness produced streets but never any house)
			net.minecraft.world.level.levelgen.structure.StructureStart start = village.value().generate(village, c.world.dimension(), registries, generator, generator.getBiomeSource(), c.world.getChunkSource().randomState(), templates, c.world.getSeed(), chunkPos, 0, c.world, biome -> true);
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

	/** Writes {@code biome} into every biome cell of the chunks around the test origin. */
	private static void paintBiome(TestCtx c, Holder<Biome> biome)
	{
		for (int cx = (c.origin.getX() - 24) >> 4; cx <= (c.origin.getX() + 24) >> 4; cx++)
		{
			for (int cz = (c.origin.getZ() - 24) >> 4; cz <= (c.origin.getZ() + 24) >> 4; cz++)
			{
				net.minecraft.world.level.chunk.LevelChunk chunk = c.world.getChunk(cx, cz);

				for (net.minecraft.world.level.chunk.LevelChunkSection section : chunk.getSections())
				{
					net.minecraft.world.level.chunk.PalettedContainer<Holder<Biome>> cells = (net.minecraft.world.level.chunk.PalettedContainer<Holder<Biome>>) section.getBiomes();

					for (int x = 0; x < 4; x++)
					{
						for (int y = 0; y < 4; y++)
						{
							for (int z = 0; z < 4; z++)
							{
								cells.set(x, y, z, biome);
							}
						}
					}
				}
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
				net.minecraft.world.level.chunk.LevelChunk chunk = c.world.getChunk(cx, cz);
				c.player.connection.send(new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(chunk, c.world.getChunkSource().getLightEngine(), null, null));
			}
		}
	}

	/**
	 * A world the autotest creates fresh on the client is built from the built-in registries, so datapack biome modifiers never ran
	 * for it (every later run reopens the saved world and has them); the registration checks are meaningless there.
	 */
	private static void skipWithoutBiomeModifiers(TestCtx c)
	{
		Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);

		for (Biome biome : biomes)
		{
			for (HolderSet<PlacedFeature> stage : biome.getGenerationSettings().features())
			{
				for (Holder<PlacedFeature> holder : stage)
				{
					if (PlacedFeature.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, holder.value()).result().map(Object::toString).orElse("").contains("randomthings:"))
					{
						return;
					}
				}
			}
		}

		throw new TestCtx.Skip("no biome has any Random Things feature: this world was created from the built-in registries, the biome modifiers only apply to datapack-loaded worlds");
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Glowing Mushroom ----------------------------------------------------------------
		tests.add(RTTest.named("worldgen.glowing_mushroom.is_registered_in_every_overworld_biome_with_the_original_rarity").run(c -> {
				skipWithoutBiomeModifiers(c);
			Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);
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

				boolean overworld = biomes.getOrThrow(biomes.getResourceKey(biome).get()).is(net.minecraft.tags.BiomeTags.IS_OVERWORLD);
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
				List<Long> seeds = seedsWithCentre(Math.max(0, c.world.getMinY()), caveMaxY, CAVE_Y + 3, patchesToRun);
				c.check(seeds.size() == patchesToRun, "could not find seeds aiming the patch at the cave");
				BlockPos column = new BlockPos(c.origin.getX(), caveMaxY + 4, c.origin.getZ());
				boolean placed = ModFeatures.GLOWING_MUSHROOM.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new net.minecraft.world.level.levelgen.LegacyRandomSource(seeds.get(index)), column);
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

				for (long seed : seedsWithCentre(Math.max(0, c.world.getMinY()), maxY, CAVE_Y + 3, 6))
				{
					placed |= ModFeatures.GLOWING_MUSHROOM.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new net.minecraft.world.level.levelgen.LegacyRandomSource(seed), column);
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
			net.minecraft.util.RandomSource random = new net.minecraft.world.level.levelgen.LegacyRandomSource(99);

			for (int i = 0; i < 4000; i++)
			{
				BlockPos source = planted[0];
				List<BlockPos> found = new ArrayList<>();

				if (countMushrooms(c, found) == 0)
				{
					break;
				}

				source = found.get(random.nextInt(found.size()));
				c.world.getBlockState(source).tick(c.world, source, random);
			}

			int count = countMushrooms(c, null);
			clearCave(c);
			c.check(count > 1, "a lone Glowing Mushroom must spread to neighbouring dark spots after many random ticks (got " + count + ")");
		}));



		tests.add(RTTest.named("worldgen.glowing_mushroom.real_terrain_generation_places_patches_underground").run(c -> {
			skipWithoutBiomeModifiers(c);

			if (!(c.world.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator))
			{
				throw new TestCtx.Skip("needs a normal-terrain overworld (the default suite world is flat): runs in the second suite pass, see docs/testing/normal-world-pass.txt");
			}

			int mushrooms = 0;
			int chunks = 0;
			int exposed = 0;
			int areas = 16;
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
				catch (TestCtx.Skip e)
				{
					throw e;
				}
				catch (RuntimeException e)
				{
					GlowingMushroomFeature.enabled = before;
					Throwable root = e;
					while (root.getCause() != null)
					{
						root = root.getCause();
					}
					StackTraceElement[] trace = root.getStackTrace();
					throw new TestCtx.Failure("terrain generation failed: " + root + " at " + java.util.Arrays.stream(trace).filter(t -> !t.getClassName().startsWith("java.")).limit(10).map(StackTraceElement::toString).collect(java.util.stream.Collectors.joining(" / ")));
				}
				RTDebug.log("worldgen test: area {} ({}, {}): {} mushrooms in {} chunks", area, 400 + area * 37, -300 - area * 41, result[0], result[1]);
				mushrooms += result[0];
				chunks += result[1];
				exposed += result[2];
			}
			GlowingMushroomFeature.enabled = before;
			RTDebug.log("worldgen test: the patch feature was asked to generate {} times", calls[0]);
			RTDebug.log("worldgen test: real terrain generation, {} areas of {}x{} decorated chunks: {} Glowing Mushrooms in {} chunks, {} not under a roof", areas, INNER, INNER, mushrooms, chunks, exposed);
			c.check(mushrooms > 0, "real terrain generation must place Glowing Mushrooms underground (found none in " + areas * INNER * INNER + " chunks)");
			c.check(chunks >= 2, "mushroom patches must show up in several chunks (got " + chunks + ")");
		}));


		// ---- Peace Candle church -------------------------------------------------------------
		tests.add(RTTest.named("worldgen.peace_candle.plains_houses_pool_is_the_church_wrapper").run(c -> {
			StructureTemplatePool pool = housesPool(c);
			c.check(pool != null, "village/plains/houses must be in the world's pool registry");
			c.eq("PeaceCandleChurchPool", pool.getClass().getSimpleName(), "the plains houses pool must be wrapped");
			c.check(vanillaHouses(pool) != pool, "the wrapper must keep the vanilla pool");
			StructureTemplate church = c.world.getStructureManager().get(ResourceLocation.parse("randomthings:peace_candle_church")).orElse(null);
			StructureTemplate vanillaChurch = c.world.getStructureManager().get(ResourceLocation.parse("minecraft:village/plains/houses/plains_temple_4")).orElse(null);
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
				List<String> plain = names(vanilla.getShuffledTemplates(RandomSource.create(seed)));
				List<String> wrapped = names(wrapper.getShuffledTemplates(RandomSource.create(seed)));
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
					for (String name : names(wrapper.getShuffledTemplates(RandomSource.create(seed))))
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
			StructureTemplateManager templates = c.world.getStructureManager();
			BlockPos base = new BlockPos(c.origin.getX() - 4, CAVE_Y, c.origin.getZ() - 4);
			BoundingBox box = piece.getBoundingBox(templates, base, Rotation.NONE);
			boolean placed = piece.place(templates, c.world, c.world.structureManager(), c.world.getChunkSource().getGenerator(), base, base, Rotation.NONE, box, RandomSource.create(1), net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings.APPLY_WATERLOGGING, false);
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

		// ---- Other natural features (registration, placement rules) ---------------------------
		String[][] natural = { { "bean_sprout", "2", "VEGETAL_DECORATION" }, { "lotus", "10", "VEGETAL_DECORATION" }, { "ancient_furnace", "400", "LOCAL_MODIFICATIONS" } };

		for (String[] spec : natural)
		{
			tests.add(RTTest.named("worldgen." + spec[0] + ".is_registered_in_every_overworld_biome_in_its_step_with_the_ported_rarity").run(c -> {
				skipWithoutBiomeModifiers(c);
				Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);
				int step = GenerationStep.Decoration.valueOf(spec[2]).ordinal();
				int overworldStyle = 0;

				for (Biome biome : biomes)
				{
					boolean found = false;
					String json = "";
					List<HolderSet<PlacedFeature>> stages = biome.getGenerationSettings().features();

					for (int i = 0; i < stages.size(); i++)
					{
						for (Holder<PlacedFeature> holder : stages.get(i))
						{
							String encoded = PlacedFeature.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, holder.value()).result().map(Object::toString).orElse("");

							if (encoded.contains("randomthings:" + spec[0]))
							{
								c.eq(step, i, spec[0] + " must be in its own decoration step in " + biomes.getKey(biome));
								found = true;
								json = encoded;
							}
						}
					}

					boolean overworld = biomes.getOrThrow(biomes.getResourceKey(biome).get()).is(net.minecraft.tags.BiomeTags.IS_OVERWORLD);

					if (overworld)
					{
						overworldStyle++;
						c.check(found, spec[0] + " must be decorated into " + biomes.getKey(biome));
						c.check(json.contains("\"chance\":" + spec[1]) && json.contains("minecraft:heightmap") && json.contains("minecraft:in_square"), biomes.getKey(biome) + ": " + spec[0] + " placement must be 1 in " + spec[1] + " chunks, random column, on the heightmap");
					}
					else
					{
						c.check(!found, spec[0] + " must not be decorated into " + biomes.getKey(biome));
					}
				}

				c.check(overworldStyle > 40, "expected the full vanilla biome list, saw " + overworldStyle);
			}));
		}

		tests.add(RTTest.named("worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block").run(c -> {
			BlockPos pos = c.at(2, 1, 2);
			Block[] soils = { Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.FARMLAND };

			for (Block soil : soils)
			{
				c.world.setBlock(pos.below(), soil.defaultBlockState(), 18);
				c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
				boolean placed = ModFeatures.BEAN_SPROUT.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new LegacyRandomSource(1), pos);
				Block block = c.world.getBlockState(pos).getBlock();
				c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
				c.check(placed && block == ModBlocks.BEAN_SPROUT, "a sprout must be placed on " + soil);
			}

			c.world.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 18);
			c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
			boolean stone = ModFeatures.BEAN_SPROUT.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new LegacyRandomSource(1), pos);
			c.world.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
			c.world.setBlock(pos, Blocks.STONE.defaultBlockState(), 18);
			boolean solid = ModFeatures.BEAN_SPROUT.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new LegacyRandomSource(1), pos);
			c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
			c.check(!stone, "a sprout must not be placed on stone");
			c.check(!solid, "a sprout must not replace a solid block");
		}));

		tests.add(RTTest.named("worldgen.lotus.snowy_biome_check_and_refuses_in_a_non_snowy_biome").run(c -> {
			Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);
			c.check(lumien.randomthings.util.BiomeUtil.hasType(biomes.getValue(Biomes.SNOWY_PLAINS), "SNOWY"), "snowy_plains must count as SNOWY");
			c.check(!lumien.randomthings.util.BiomeUtil.hasType(biomes.getValue(Biomes.PLAINS), "SNOWY"), "plains must not count as SNOWY");
			c.check(!lumien.randomthings.util.BiomeUtil.hasType(biomes.getValue(Biomes.DESERT), "SNOWY"), "desert must not count as SNOWY");
			BlockPos pos = c.at(2, 1, 2);

			if (!lumien.randomthings.util.BiomeUtil.hasType(c.world.getBiome(pos).value(), "SNOWY"))
			{
				c.world.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
				c.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
				boolean placed = ModFeatures.LOTUS.place(NoneFeatureConfiguration.INSTANCE, c.world, c.world.getChunkSource().getGenerator(), new LegacyRandomSource(1), pos);
				c.check(!placed, "a lotus must not generate in a non-snowy biome");
			}
		}));

		// the test world is warm, so the area is repainted with a cold biome (and restored afterwards); the feature reads the biome through
		// the level's biome manager, which blends the neighbouring cells, hence the whole surrounding chunks are painted
		tests.add(RTTest.named("worldgen.ancient_furnace.generates_only_in_cold_biomes_on_solid_ground").run(c -> {
			Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);
			BlockPos pos = c.at(2, 1, 2);
			Holder<Biome> original = c.world.getBiome(pos);
			Holder<Biome> cold = biomes.getOrThrow(Biomes.SNOWY_PLAINS);
			c.check(original.value().warmEnoughToRain(pos.below(), c.world.getSeaLevel()), "the flat test world is expected to be warm (got " + original.unwrapKey().map(Object::toString).orElse("?") + ")");
			ServerLevel w = c.world;
			RandomSource random = new LegacyRandomSource(1);

			try
			{
				w.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(NoneFeatureConfiguration.INSTANCE, w, w.getChunkSource().getGenerator(), random, pos), "no furnace in a warm biome");
				c.eq(Blocks.STONE, w.getBlockState(pos.below()).getBlock(), "the ground must be untouched in a warm biome");

				paintBiome(c, cold);
				c.check(!w.getBiome(pos).value().warmEnoughToRain(pos.below(), c.world.getSeaLevel()), "the painted biome must read back as cold");

				w.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(NoneFeatureConfiguration.INSTANCE, w, w.getChunkSource().getGenerator(), random, pos), "no furnace over air");
				w.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 18);
				c.check(!ModFeatures.ANCIENT_FURNACE.place(NoneFeatureConfiguration.INSTANCE, w, w.getChunkSource().getGenerator(), random, pos), "no furnace into water");

				w.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 18);
				c.check(ModFeatures.ANCIENT_FURNACE.place(NoneFeatureConfiguration.INSTANCE, w, w.getChunkSource().getGenerator(), random, pos), "a furnace must generate on stone in a cold biome");
				c.eq(ModBlocks.ANCIENT_FURNACE, w.getBlockState(pos.below()).getBlock(), "the furnace replaces the ground block");
				c.check(!ModFeatures.ANCIENT_FURNACE.place(NoneFeatureConfiguration.INSTANCE, w, w.getChunkSource().getGenerator(), random, pos), "an existing furnace is not replaced again");
			}
			finally
			{
				paintBiome(c, original);
				w.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 18);
			}
		}));

		// the heat-up's finish (warm the surroundings, then explode) is split: only the warming is run, the explosion would wreck the test area
		tests.add(RTTest.named("worldgen.ancient_furnace.heating_converts_cold_biomes_melts_snow_and_ice_and_leaves_warm_biomes_alone").run(c -> {
			Registry<Biome> biomes = c.world.registryAccess().lookupOrThrow(Registries.BIOME);
			Holder<Biome> original = c.world.getBiome(c.at(2, 1, 2));
			BlockPos snow = c.at(2, 1, 2);
			BlockPos ice = c.at(4, 1, 2);
			ServerLevel w = c.world;
			java.lang.reflect.Method warm;

			try
			{
				warm = lumien.randomthings.tileentity.AncientFurnaceTileEntity.class.getDeclaredMethod("warmSurroundings", net.minecraft.world.level.Level.class, BlockPos.class);
				warm.setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach warmSurroundings: " + e);
			}

			try
			{
				// a warm (unconvertible) area first: neither the biome nor the snow may change
				w.setBlock(snow, Blocks.SNOW_BLOCK.defaultBlockState(), 18);
				warm.invoke(null, w, c.at(0, 1, 0));
				c.eq(original.unwrapKey().get(), w.getBiome(snow).unwrapKey().get(), "a warm biome is not converted");
				c.eq(Blocks.SNOW_BLOCK, w.getBlockState(snow).getBlock(), "snow in a biome without a warm counterpart stays");

				// then a snowy area: biome becomes plains, snow disappears, ice melts
				paintBiome(c, biomes.getOrThrow(Biomes.SNOWY_PLAINS));
				w.setBlock(ice, Blocks.PACKED_ICE.defaultBlockState(), 18);
				warm.invoke(null, w, c.at(0, 1, 0));
				c.eq(Biomes.PLAINS, w.getBiome(snow).unwrapKey().get(), "snowy plains must become plains");
				c.eq(Biomes.PLAINS, w.getBiome(ice).unwrapKey().get(), "the whole radius is converted");
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
				w.setBlock(snow, Blocks.AIR.defaultBlockState(), 18);
				w.setBlock(ice, Blocks.AIR.defaultBlockState(), 18);
			}
		}));

		// client side of the heat-up: the server rewrites the biome, the client only learns about it from the chunk resend (checklist N6)
		final java.lang.reflect.Method[] warmClient = new java.lang.reflect.Method[1];
		final Holder<?>[] originalClient = new Holder<?>[1];
		tests.add(RTTest.named("worldgen.ancient_furnace.client_sees_the_biome_change_after_heating").run(c -> {
			if (net.neoforged.fml.loading.FMLEnvironment.dist != net.neoforged.api.distmarker.Dist.CLIENT)
			{
				throw new TestCtx.Skip("needs a client run (the dedicated server has no client to look at)");
			}

			try
			{
				warmClient[0] = lumien.randomthings.tileentity.AncientFurnaceTileEntity.class.getDeclaredMethod("warmSurroundings", net.minecraft.world.level.Level.class, BlockPos.class);
				warmClient[0].setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach warmSurroundings: " + e);
			}

			originalClient[0] = c.world.getBiome(c.at(2, 1, 2));
			paintBiome(c, c.world.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.SNOWY_PLAINS));
			resendAround(c);
		}).waitTicks(20).run(c -> {
			if (warmClient[0] == null)
			{
				return;
			}

			c.eq(Biomes.SNOWY_PLAINS, ClientProbe.biomeAt(c.at(2, 1, 2)), "precondition: the client must show the repainted biome after the resend");

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

			ResourceKey<Biome> client = ClientProbe.biomeAt(c.at(2, 1, 2));
			@SuppressWarnings("unchecked")
			Holder<Biome> original = (Holder<Biome>) originalClient[0];
			paintBiome(c, original);
			resendAround(c);
			c.eq(Biomes.PLAINS, client, "the client must see the converted biome without relogging");
		}));

		// ---- Spectre dimension generator -------------------------------------------------------
		tests.add(RTTest.named("worldgen.spectre.dimension_uses_the_void_generator_and_a_256_tall_zero_based_height").run(c -> {
			lumien.randomthings.handler.spectre.SpectreHandler handler = lumien.randomthings.handler.spectre.SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			ServerLevel spectre = handler.getWorld();
			c.check(spectre.getChunkSource().getGenerator() instanceof lumien.randomthings.handler.spectre.SpectreChunkGenerator, "the Spectre dimension must use SpectreChunkGenerator, got " + spectre.getChunkSource().getGenerator().getClass());
			c.eq(0, spectre.getMinY(), "Spectre min build height");
			c.eq(256, spectre.getHeight(), "Spectre height");
			c.check(!spectre.dimensionType().hasSkyLight() && !spectre.dimensionType().hasCeiling(), "no skylight, no ceiling");
		}));

		tests.add(RTTest.named("worldgen.spectre.a_generated_chunk_far_from_the_cubes_is_empty_air").run(c -> {
			lumien.randomthings.handler.spectre.SpectreHandler handler = lumien.randomthings.handler.spectre.SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			ServerLevel spectre = handler.getWorld();
			ChunkAccess chunk = spectre.getChunk(5000, 5000);

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

	/**
	 * A structure manager that knows no structures. The generator asks it about structure starts in a wide chunk range around
	 * every chunk it generates; this test only has a small patch of chunks in hand, so the answer is "none" instead of a
	 * region lookup out of bounds (the real one would also block on the server thread's own chunk source).
	 */
	private static class NoStructures extends StructureManager
	{
		private final ServerLevel world;

		private NoStructures(ServerLevel world, net.minecraft.world.level.LevelAccessor level)
		{
			super(level, world.getServer().getWorldData().worldGenOptions(), null);
			this.world = world;
		}

		static NoStructures of(ServerLevel world)
		{
			return new NoStructures(world, world);
		}

		@Override
		public StructureManager forWorldGenRegion(WorldGenRegion region)
		{
			return new NoStructures(world, region);
		}

		@Override
		public List<StructureStart> startsForStructure(ChunkPos pos, java.util.function.Predicate<Structure> predicate)
		{
			return Collections.emptyList();
		}

		@Override
		public List<StructureStart> startsForStructure(net.minecraft.core.SectionPos pos, Structure structure)
		{
			return Collections.emptyList();
		}

		@Override
		public boolean hasAnyStructureAt(BlockPos pos)
		{
			return false;
		}

		@Override
		public Map<Structure, it.unimi.dsi.fastutil.longs.LongSet> getAllStructuresAt(BlockPos pos)
		{
			return Collections.emptyMap();
		}

		@Override
		public StructureStart getStructureAt(BlockPos pos, Structure structure)
		{
			return StructureStart.INVALID_START;
		}

		@Override
		public StructureStart getStructureWithPieceAt(BlockPos pos, Structure structure)
		{
			return StructureStart.INVALID_START;
		}

		@Override
		public net.minecraft.world.level.levelgen.structure.StructureCheckResult checkStructurePresence(ChunkPos pos, Structure structure, net.minecraft.world.level.levelgen.structure.placement.StructurePlacement placement, boolean skipKnown)
		{
			return net.minecraft.world.level.levelgen.structure.StructureCheckResult.START_NOT_PRESENT;
		}
	}
}
