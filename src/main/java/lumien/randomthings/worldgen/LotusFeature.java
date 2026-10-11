package lumien.randomthings.worldgen;

import lumien.randomthings.block.LotusBlock;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Random;
import java.util.function.Function;
import com.mojang.serialization.Codec;
import net.minecraft.world.ISeedReader;
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
public class LotusFeature extends Feature<NoFeatureConfig> {
    public LotusFeature(Codec<NoFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(ISeedReader worldIn, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (!lumien.randomthings.config.RTConfig.LOTUS.get()) {
            return false;
        }

        if (pos.getY() < 0) {
            return false;
        }

        Biome biome = worldIn.getBiome(pos);

        if (!BiomeUtil.hasType(biome, BiomeDictionary.Type.SNOWY)) {
            return false;
        }

        BlockState existing = worldIn.getBlockState(pos);

        if (!existing.isAir() && !existing.getMaterial().isReplaceable()) {
            return false;
        }

        BlockState placeState = ModBlocks.LOTUS.getDefaultState().with(LotusBlock.AGE, rand.nextInt(4));

        if (!placeState.isValidPosition(worldIn, pos)) {
            return false;
        }

        worldIn.setBlockState(pos, placeState, 2);
        return true;
    }
}
