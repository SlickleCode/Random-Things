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
 * Naturally embeds a single {@code ANCIENT_FURNACE} at the surface, replacing
 * whatever block is there - direct port of 1.12.2's own {@code
 * WorldGenAncientFurnace}. "Rare" is handled at the registration side
 * ({@code RandomThings#registerWorldgenFeatures}'s {@code ChanceConfig});
 * "cold biomes" is this class's own {@code biome.getTemperature(pos) < 0.15F}
 * check - the same vanilla "cold enough to snow" threshold, and the same
 * per-attempt-gating division of labor {@link PitcherPlantFeature} already
 * uses for its own (opposite) warm-biome restriction.
 */
public class AncientFurnaceFeature extends Feature<NoFeatureConfig> {
    public AncientFurnaceFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> deserializer) {
        super(deserializer);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
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
