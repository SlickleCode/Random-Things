package lumien.randomthings.worldgen;

import net.minecraft.util.RandomSource;
import lumien.randomthings.block.ModBlocks;
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
 * Naturally embeds a single {@code ANCIENT_FURNACE} at the surface, replacing
 * whatever block is there - direct port of 1.12.2's own {@code
 * WorldGenAncientFurnace}. "Rare" is handled at the registration side
 * ({@code RandomThings#registerWorldgenFeatures}'s {@code ChanceConfig});
 * "cold biomes" is this class's own {@code biome.getTemperature(pos) < 0.15F}
 * check - the same vanilla "cold enough to snow" threshold, and the same
 * per-attempt-gating division of labor this file's now-removed Pitcher Plant
 * counterpart used for its own (opposite) warm-biome restriction.
 */
public class AncientFurnaceFeature extends Feature<NoneFeatureConfiguration> {
    public AncientFurnaceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        BlockPos pos = context.origin();
        RandomSource rand = context.random();

        if (!lumien.randomthings.config.RTConfig.ANCIENT_FURNACE.get()) {
            return false;
        }

        BlockPos ground = pos.below();

        if (ground.getY() <= worldIn.getMinBuildHeight() || ground.getY() >= worldIn.getMaxBuildHeight() - 1) {
            return false;
        }

        Biome biome = worldIn.getBiome(ground).value();

        if (biome.warmEnoughToRain(ground)) {
            return false;
        }

        if (worldIn.isEmptyBlock(ground) || worldIn.getBlockState(ground).getMaterial().isLiquid()) {
            return false;
        }

        if (worldIn.getBlockState(ground).getBlock() == ModBlocks.ANCIENT_FURNACE) {
            return false;
        }

        worldIn.setBlock(ground, ModBlocks.ANCIENT_FURNACE.defaultBlockState(), 2);
        return true;
    }
}
