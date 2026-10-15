package lumien.randomthings.debug.test;

import java.util.HashMap;
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

				boolean overworld = biomes.getHolderOrThrow(biomes.getResourceKey(biome).get()).is(net.minecraft.tags.BiomeTags.IS_OVERWORLD);
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

				for (long seed : seedsWithCentre(Math.max(0, c.world.getMinBuildHeight()), maxY, CAVE_Y + 3, 6))
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
				ModBlocks.GLOWING_MUSHROOM.tick(c.world.getBlockState(source), c.world, source, random);
			}

			int count = countMushrooms(c, null);
			clearCave(c);
			c.check(count > 1, "a lone Glowing Mushroom must spread to neighbouring dark spots after many random ticks (got " + count + ")");
		}));

		return tests;
	}
}
