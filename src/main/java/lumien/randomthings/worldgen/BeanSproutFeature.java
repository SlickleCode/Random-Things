package lumien.randomthings.worldgen;

import net.minecraft.util.RandomSource;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
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
 * Naturally spawns a single {@code BEAN_SPROUT} at the resolved surface
 * position, no biome restriction - direct port of 1.12.2's own {@code
 * WorldGenPlants}'s bean-sprout half (a 50% chance per chunk there,
 * matched here by registering with {@code Placement.CHANCE_HEIGHTMAP} +
 * {@code ChanceConfig(2)} in {@code RandomThings#setupCommon}). The random
 * XZ pick, heightmap-surface Y lookup, and chance roll 1.12.2 did by hand are
 * the {@code Placement} decorator's job in 1.14.4 - this class only needs
 * the final per-position validity check.
 */
public class BeanSproutFeature extends Feature<NoneFeatureConfiguration> {
    public BeanSproutFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        BlockPos pos = context.origin();
        RandomSource rand = context.random();

        if (pos.getY() <= worldIn.getMinBuildHeight() || pos.getY() >= worldIn.getMaxBuildHeight() - 1 || !worldIn.isEmptyBlock(pos)) {
            return false;
        }

        if (!ModBlocks.BEAN_SPROUT.defaultBlockState().canSurvive(worldIn, pos)) {
            return false;
        }

        worldIn.setBlock(pos, ModBlocks.BEAN_SPROUT.defaultBlockState(), 2);
        return true;
    }
}
