package lumien.randomthings.worldgen;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import net.minecraft.world.ISeedReader;
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
public class BeanSproutFeature extends Feature<NoFeatureConfig> {
    public BeanSproutFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (pos.getY() <= 0 || pos.getY() >= 255 || !worldIn.isAirBlock(pos)) {
            return false;
        }

        if (!ModBlocks.BEAN_SPROUT.getDefaultState().isValidPosition(worldIn, pos)) {
            return false;
        }

        worldIn.setBlockState(pos, ModBlocks.BEAN_SPROUT.getDefaultState(), 2);
        return true;
    }
}
