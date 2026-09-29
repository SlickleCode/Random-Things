package lumien.randomthings.handler.spectre;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;

/**
 * Direct port of 1.12.2's {@code SpectreWorldProvider}: an always-dark,
 * non-surface void dimension that exists purely to host per-player pocket
 * rooms (see {@link SpectreHandler}/{@link SpectreCube}). {@code hasSkyLight}
 * isn't overridden here - {@link Dimension#hasSkyLight()} already delegates
 * to the {@code DimensionType}'s own flag, which {@code ModDimensions}
 * registers as {@code false}, matching the original's {@code this.hasSkyLight
 * = false} constructor line.
 * <p>
 * Disclosed simplification: reuses vanilla's own {@code Biomes.THE_VOID}
 * instead of porting a new custom Biome class - this port hasn't needed a
 * custom Biome anywhere else (Ancient Furnace's worldgen work reassigns
 * existing biomes rather than introducing one), and THE_VOID already matches
 * the empty-void theming this dimension wants (no mob spawns, no vegetation).
 */
public class SpectreDimension extends Dimension {
    public SpectreDimension(World world, DimensionType type) {
        super(world, type);
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator() {
        return new SpectreChunkGenerator(this.world, new SingleBiomeProvider(new SingleBiomeProviderSettings().setBiome(Biomes.THE_VOID)), new GenerationSettings());
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int i = 0; i <= 15; ++i) {
            this.lightBrightnessTable[i] = 1;
        }
    }

    @Override
    public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
        return new BlockPos(chunkPosIn.getXStart() + 8, 8, chunkPosIn.getZStart() + 8);
    }

    @Override
    public BlockPos findSpawn(int x, int z, boolean checkValid) {
        return new BlockPos(x, 8, z);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.5F;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.03, 0.2, 0.2);
    }

    @Override
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return new float[] { 0, 0, 0, 0 };
    }

    @Override
    public float getCloudHeight() {
        return -5;
    }
}
