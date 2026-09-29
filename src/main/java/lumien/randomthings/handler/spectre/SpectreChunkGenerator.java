package lumien.randomthings.handler.spectre;

import net.minecraft.world.IWorld;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;

/**
 * Direct port of 1.12.2's {@code SpectreChunkProvider}: a totally empty
 * generator - no terrain, no structures, no mob spawns, no decoration -
 * matching this dimension's void theming ({@link SpectreCube} carves out the
 * only real geometry, on demand, per player). Modeled directly on vanilla's
 * own {@code DebugChunkGenerator} (the simplest real {@code ChunkGenerator}
 * implementation in this Forge version - confirmed via its source that every
 * terrain-shaping hook can be a clean no-op).
 * <p>
 * Real bug found and fixed (reported by user: "a layer of stone" around the
 * teleport-in point): {@code carve}/{@code decorate} are concrete (not
 * abstract) methods on the base {@code ChunkGenerator}, so leaving them
 * un-overridden - as a first pass here mistakenly did, reasoning "this
 * dimension has no use for decoration" without actually suppressing it -
 * left vanilla's *default* decoration step active. That default step runs
 * every feature registered to the chunk's biome ({@code Biomes.THE_VOID}),
 * and - ground-truthed from vanilla's own {@code TheVoidBiome} source before
 * concluding this, since this mod's own {@code RandomThings
 * #registerWorldgenFeatures}-added features (Ancient Furnace included) are
 * all correctly gated out of THE_VOID by their own per-attempt biome checks
 * (Ancient Furnace's "cold biomes only" needs {@code temperature < 0.15F};
 * THE_VOID is defined at {@code 0.5F}) - the actual culprit is vanilla
 * itself: {@code TheVoidBiome}'s constructor hard-codes a {@code
 * Feature.VOID_START_PLATFORM} decoration, which unconditionally places a
 * ~33x33 flat disc of solid Stone (Cobblestone at its exact center) at Y 3
 * around block (8, 3, 8) - vanilla's ordinary "spawn platform for a void
 * world" mechanic, completely independent of this mod. That block happens to
 * sit right in the middle of the very first {@link SpectreCube} (whose own
 * spawn is (8, 1, 8)), so it looked like part of the room. Both {@code
 * carve}/{@code decorate} are now real no-ops, so nothing but {@link
 * SpectreCube}'s own explicit block placement ever touches this dimension -
 * this vanilla platform included.
 */
public class SpectreChunkGenerator extends ChunkGenerator<GenerationSettings> {
    public SpectreChunkGenerator(IWorld world, BiomeProvider biomeProvider, GenerationSettings settings) {
        super(world, biomeProvider, settings);
    }

    @Override
    public void generateSurface(IChunk chunkIn) {
    }

    @Override
    public void makeBase(IWorld worldIn, IChunk chunkIn) {
    }

    @Override
    public void carve(IChunk chunkIn, GenerationStage.Carving carvingStage) {
    }

    @Override
    public void decorate(WorldGenRegion region) {
    }

    @Override
    public int getGroundHeight() {
        return 0;
    }

    @Override
    public int func_222529_a(int x, int z, Heightmap.Type heightmapType) {
        return 0;
    }
}
