package lumien.randomthings.worldgen;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.RTConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

/**
 * Underground patch of Glowing Mushrooms - port of 1.12.2's {@code WorldGenEventHandler} mushroom
 * branch + {@code WorldGenPatches}. Registered with {@code Placement.CHANCE_HEIGHTMAP} +
 * {@code ChanceConfig(4)} (the original's per-chunk {@code nextInt(4) == 0}); the placement supplies
 * a random XZ and the surface Y, from which the patch center's Y is rolled anywhere below
 * {@code surface - 4}, exactly like the original. Then 64 tries within +-7/+-3 of the center to place
 * one in an air block that can hold it and has no sky access.
 */
public class GlowingMushroomFeature extends Feature<NoFeatureConfig> {
    public GlowingMushroomFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> deserializer) {
        super(deserializer);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (!RTConfig.GLOWING_MUSHROOM.get()) {
            return false;
        }

        int maxY = pos.getY() - 4;

        if (maxY <= 0) {
            return false;
        }

        BlockPos center = new BlockPos(pos.getX(), rand.nextInt(maxY), pos.getZ());
        BlockState mushroom = ModBlocks.GLOWING_MUSHROOM.getDefaultState();

        for (int i = 0; i < 64; ++i) {
            BlockPos target = center.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (target.getY() > 0 && target.getY() < 255 && worldIn.isAirBlock(target) && mushroom.isValidPosition(worldIn, target) && !worldIn.canBlockSeeSky(target)) {
                worldIn.setBlockState(target, mushroom, 2);
            }
        }

        return true;
    }
}
