package lumien.randomthings.worldgen;

import com.mojang.datafixers.Dynamic;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;
import java.util.function.Function;

/**
 * Naturally spawns a single {@code PITCHER_PLANT} at the resolved surface
 * position, warm biomes only ({@code temperature >= 0.8F}, matching 1.12.2's
 * own {@code WorldGenPlants} check exactly) - a 10% chance per chunk there,
 * matched here via {@code Placement.CHANCE_HEIGHTMAP} + {@code
 * ChanceConfig(10)} in {@code RandomThings#setupCommon}.
 */
public class PitcherPlantFeature extends Feature<NoFeatureConfig> {
    public PitcherPlantFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> deserializer) {
        super(deserializer);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (pos.getY() < 0 || !worldIn.isAirBlock(pos)) {
            return false;
        }

        Biome biome = worldIn.getBiome(pos);

        if (biome.getTemperature(pos) < 0.8F) {
            return false;
        }

        if (!ModBlocks.PITCHER_PLANT.getDefaultState().isValidPosition(worldIn, pos)) {
            return false;
        }

        worldIn.setBlockState(pos, ModBlocks.PITCHER_PLANT.getDefaultState(), 2);
        return true;
    }
}
