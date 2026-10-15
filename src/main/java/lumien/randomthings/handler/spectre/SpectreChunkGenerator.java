package lumien.randomthings.handler.spectre;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

/**
 * An empty void: generates nothing, the pocket rooms are placed by {@link SpectreCube#generate}. Port of 1.14.4's
 * {@code SpectreChunkGenerator}; chunk generators are codec-registered (see {@code ModDimensions}) and take
 * their biome source from the dimension JSON. The dimension is 256 blocks tall starting at y=0 (see
 * {@code data/randomthings/dimension_type/spectre.json}), the same as before 1.18's -64..320 overworld.
 */
public class SpectreChunkGenerator extends ChunkGenerator {
    public static final Codec<SpectreChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> commonCodec(instance).and(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)).apply(instance, instance.stable(SpectreChunkGenerator::new)));

    public SpectreChunkGenerator(Registry<StructureSet> structureSets, BiomeSource biomeSource) {
        super(structureSets, Optional.empty(), biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return Climate.empty();
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, BiomeManager biomeManager, StructureFeatureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureFeatureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType, LevelHeightAccessor level) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level) {
        return new NoiseColumn(level.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, BlockPos pos) {
    }
}
