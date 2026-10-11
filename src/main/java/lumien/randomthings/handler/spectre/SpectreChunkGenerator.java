package lumien.randomthings.handler.spectre;

import java.util.Collections;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.Blockreader;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;

/**
 * An empty void: generates nothing, the pocket rooms are placed by {@link SpectreCube#generate}. Port of 1.14.4's
 * {@code SpectreChunkGenerator}; 1.16 chunk generators are codec-registered (see {@code ModDimensions}) and take
 * their biome source from the dimension JSON.
 */
public class SpectreChunkGenerator extends ChunkGenerator {
    public static final Codec<SpectreChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeProvider.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeProvider)).apply(instance, instance.stable(SpectreChunkGenerator::new)));

    public SpectreChunkGenerator(BiomeProvider biomeProvider) {
        super(biomeProvider, new DimensionStructuresSettings(Optional.empty(), Collections.emptyMap()));
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @Override
    public ChunkGenerator func_230349_a_(long seed) {
        return this;
    }

    @Override
    public void generateSurface(WorldGenRegion region, IChunk chunk) {
    }

    @Override
    public void func_230352_b_(IWorld world, StructureManager structureManager, IChunk chunk) {
    }

    @Override
    public int getGroundHeight() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
        return 0;
    }

    @Override
    public IBlockReader func_230348_a_(int x, int z) {
        return new Blockreader(new BlockState[0]);
    }
}
