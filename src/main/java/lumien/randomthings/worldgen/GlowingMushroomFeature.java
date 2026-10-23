package lumien.randomthings.worldgen;

import net.minecraft.util.RandomSource;
import java.util.Random;
import java.util.function.Function;


import lumien.randomthings.block.GlowingMushroomBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.RTConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import lumien.randomthings.util.BiomeUtil;

/**
 * Underground patch of Glowing Mushrooms - port of 1.12.2's {@code WorldGenEventHandler} mushroom
 * branch + {@code WorldGenPatches}. Registered with {@code Placement.CHANCE_HEIGHTMAP} +
 * {@code ChanceConfig(4)} (the original's per-chunk {@code nextInt(4) == 0}); the placement supplies
 * a random XZ and the surface Y, from which the patch center's Y is rolled anywhere below
 * {@code surface - 4}, exactly like the original. Then 64 tries within +-7/+-3 of the center to place
 * one in an air block that can hold it and has no sky access.
 */
public class GlowingMushroomFeature extends Feature<NoneFeatureConfiguration> {
    /** The {@code GlowingMushrooms} config switch; a supplier so the automated tests can flip it without rewriting the config file. */
    public static java.util.function.BooleanSupplier enabled = () -> RTConfig.GLOWING_MUSHROOM.get();

    public GlowingMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        BlockPos pos = context.origin();
        RandomSource rand = context.random();

        if (!enabled.getAsBoolean()) {
            return false;
        }

        // the original rolled the patch centre between bedrock (y 0) and the surface; 1.18 digs down to -64, which would mostly
        // pick solid deepslate, so the roll keeps starting at y 0
        int minY = Math.max(0, worldIn.getMinY());
        int maxY = pos.getY() - 4;

        if (maxY <= minY) {
            return false;
        }

        BlockPos center = new BlockPos(pos.getX(), minY + rand.nextInt(maxY - minY), pos.getZ());
        BlockState mushroom = ModBlocks.GLOWING_MUSHROOM.defaultBlockState();

        for (int i = 0; i < 64; ++i) {
            BlockPos target = center.offset(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (target.getY() > minY && target.getY() < worldIn.getMaxY() + 1 - 1 && worldIn.isEmptyBlock(target) && GlowingMushroomBlock.canGrowDuringWorldgen(worldIn, target) && !worldIn.canSeeSkyFromBelowWater(target)) {
                worldIn.setBlock(target, mushroom, 2);
            }
        }

        return true;
    }
}
