package lumien.randomthings.worldgen;

import net.minecraft.util.RandomSource;
import lumien.randomthings.block.LotusBlock;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Random;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import lumien.randomthings.util.BiomeUtil;

/**
 * Naturally spawns a single {@code LOTUS} (random age 0-3) at the resolved
 * surface position, snowy biomes only ({@code BiomeDictionary.Type.SNOWY} -
 * matching 1.12.2's own {@code WorldGenPlants} check exactly, including
 * accepting a replaceable block there, e.g. snow layers, not just air) - a
 * 10% chance per chunk there, matched here via {@code
 * Placement.CHANCE_HEIGHTMAP} + {@code ChanceConfig(10)} in {@code
 * RandomThings#setupCommon}.
 */
public class LotusFeature extends Feature<NoneFeatureConfiguration> {
    public LotusFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        BlockPos pos = context.origin();
        RandomSource rand = context.random();

        if (!lumien.randomthings.config.RTConfig.LOTUS.get()) {
            return false;
        }

        if (pos.getY() < worldIn.getMinBuildHeight()) {
            return false;
        }

        Biome biome = worldIn.getBiome(pos).value();

        if (!BiomeUtil.hasType(biome, "SNOWY")) {
            return false;
        }

        BlockState existing = worldIn.getBlockState(pos);

        if (!existing.isAir() && !existing.canBeReplaced()) {
            return false;
        }

        BlockState placeState = ModBlocks.LOTUS.defaultBlockState().setValue(LotusBlock.AGE, rand.nextInt(4));

        if (!placeState.canSurvive(worldIn, pos)) {
            return false;
        }

        worldIn.setBlock(pos, placeState, 2);
        return true;
    }
}
