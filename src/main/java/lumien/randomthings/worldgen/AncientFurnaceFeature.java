package lumien.randomthings.worldgen;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import net.minecraft.world.ISeedReader;
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
public class AncientFurnaceFeature extends Feature<NoFeatureConfig> {
    public AncientFurnaceFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (!lumien.randomthings.config.RTConfig.ANCIENT_FURNACE.get()) {
            return false;
        }

        BlockPos ground = pos.down();

        if (ground.getY() <= 0 || ground.getY() >= 255) {
            return false;
        }

        Biome biome = worldIn.getBiome(ground);

        if (biome.getTemperature(ground) >= 0.15F) {
            return false;
        }

        if (worldIn.isAirBlock(ground) || worldIn.getBlockState(ground).getMaterial().isLiquid()) {
            return false;
        }

        if (worldIn.getBlockState(ground).getBlock() == ModBlocks.ANCIENT_FURNACE) {
            return false;
        }

        worldIn.setBlockState(ground, ModBlocks.ANCIENT_FURNACE.getDefaultState(), 2);
        return true;
    }
}
