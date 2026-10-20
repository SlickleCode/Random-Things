package lumien.randomthings.tileentity;

import net.minecraft.core.registries.Registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Direct port of 1.12.2's {@code TileEntityAncientFurnace} heating/explosion
 * state machine. {@code HEATING_DURATION}/{@code AREA_BLOCKS} match the
 * wiki's own description ("after heating up", "10000 blocks (configurable)")
 * as closely as a hardcoded default can - 1.12.2 exposed both through its
 * config file, which this port hasn't built yet (see {@code
 * WIKI_FEATURE_STATUS.md}), so both are plain constants here instead.
 * <p>
 * Real biome reassignment, added 2026-09-27 - a previous session disclosed
 * this as impossible ("no supported public API"), which turned out to be
 * wrong: {@link Chunk#getBiomes()} returns the chunk's own live {@code
 * Biome[]} array (confirmed via the decompiled source, not a defensive
 * copy), and {@code IChunk.getBiome(BlockPos)} - what every gameplay biome
 * read in the game goes through - indexes straight into that same array
 * fresh on every call ({@code getBiomes()[z<<4|x]}, one biome per column).
 * Mutating that array in place is a legitimate public-API biome
 * reassignment, no reflection needed - ground-truthed from 1.12.2's own
 * {@code AncientFurnaceConversion}/{@code WorldUtil#setBiome} (the original
 * did the exact same kind of direct biome-array write, just with 1.12.2's
 * {@code byte[]} version of it) via {@code git show origin/1.12.2:...}.
 * {@link #COLD_TO_WARM} is that same conversion table, re-mapped onto
 * 1.14.4's renamed biome fields (e.g. {@code ICE_PLAINS} became {@code
 * SNOWY_TUNDRA}, the "Mutated" biomes became "_Mountains"-suffixed ones),
 * ground-truthed via {@code javap} against {@code Biomes}' real field list
 * rather than guessed. Snow/ice melting is now gated on the same per-column
 * conversion happening, matching 1.12.2's own conditional structure, rather
 * than melting unconditionally everywhere in radius (that was this session's
 * own earlier simplification, made when a "real" biome check seemed
 * unreachable - no longer needed now that one exists).
 * <p>
 * Disclosed simplification kept: 1.12.2's real mechanism was a per-tick
 * flood-fill outward from the furnace (one column converted per tick, up to
 * a configurable cap, spreading only through connected matching-biome
 * neighbors) rather than an instant fixed-radius circular scan. This port
 * keeps the simpler instant-radius approach from before (same visible
 * end result for a roughly-10000-block area, just not identically paced) -
 * revisit if the user wants the exact flood-fill pacing reproduced.
 * <p>
 * Real bug, found 2026-09-28 (reported by user: waited out a full heat-up,
 * saw the snow melt and the explosion, but no visible biome/color change).
 * The gap above ("not attempted this session") is exactly what that was -
 * the mutation was already server-authoritative-correct, but nothing told an
 * already-connected client's own separate copy of the chunk's biome data to
 * update. Fixed via {@link #resyncBiomesToClients}: resends each touched
 * chunk's real {@code SChunkDataPacket} (ground-truthed via source that it
 * always serializes {@code Chunk#getBiomes()} regardless of section filter,
 * so a full resend is the correct, working mechanism) to every player
 * currently tracking it - the same packet vanilla's own {@code ChunkManager
 * #sendChunkData} sends when a chunk first loads for a player.
 */
public class AncientFurnaceTileEntity extends BlockEntity {
    /** ~56 blocks, the radius of a circle with area 10000 (the wiki's own default). */
    private static final int RADIUS = 56;
    private static final int HEATING_DURATION = 20 * 60 * 5;

    /**
     * Ground-truthed from 1.12.2's real {@code AncientFurnaceConversion} map
     * (via {@code git show origin/1.12.2:...}), re-mapped onto 1.14.4's
     * renamed biome fields (confirmed to exist via {@code javap} against
     * {@code Biomes}): {@code ICE_PLAINS}->{@code SNOWY_TUNDRA}, {@code
     * ICE_MOUNTAINS}->{@code SNOWY_MOUNTAINS}, {@code FOREST_HILLS}->{@code
     * WOODED_HILLS}, {@code COLD_BEACH}->{@code SNOWY_BEACH}, {@code
     * COLD_TAIGA}->{@code SNOWY_TAIGA}, {@code COLD_TAIGA_HILLS}->{@code
     * SNOWY_TAIGA_HILLS}, {@code REDWOOD_TAIGA_HILLS}->{@code
     * GIANT_TREE_TAIGA_HILLS}, the "Mutated" variants ->
     * "_Mountains"/"_Hills"-suffixed 1.14.4 names ({@code
     * MUTATED_TAIGA}->{@code TAIGA_MOUNTAINS}, {@code MUTATED_ICE_FLATS} was
     * always really "Ice Spikes" ->{@code ICE_SPIKES}, {@code
     * MUTATED_TAIGA_COLD}->{@code SNOWY_TAIGA_MOUNTAINS}), with their target
     * "warmed" biomes similarly renamed where needed ({@code MUTATED_FOREST}
     * was always really "Flower Forest" ->{@code FLOWER_FOREST}, {@code
     * MUTATED_BIRCH_FOREST}->{@code TALL_BIRCH_FOREST}, {@code
     * MUTATED_BIRCH_FOREST_HILLS}->{@code TALL_BIRCH_HILLS}).
     */
    private static final Map<ResourceKey<Biome>, ResourceKey<Biome>> COLD_TO_WARM = buildConversionMap();

    private static Map<ResourceKey<Biome>, ResourceKey<Biome>> buildConversionMap() {
        Map<ResourceKey<Biome>, ResourceKey<Biome>> map = new HashMap<>();
        // 1.18 removed the *_HILLS / *_MOUNTAINS biome variants and renamed the cold ones; the closest warm counterparts
        map.put(Biomes.TAIGA, Biomes.FOREST);
        map.put(Biomes.FROZEN_OCEAN, Biomes.OCEAN);
        map.put(Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_OCEAN);
        map.put(Biomes.FROZEN_RIVER, Biomes.RIVER);
        map.put(Biomes.SNOWY_PLAINS, Biomes.PLAINS);
        map.put(Biomes.SNOWY_SLOPES, Biomes.MEADOW);
        map.put(Biomes.GROVE, Biomes.FOREST);
        map.put(Biomes.SNOWY_BEACH, Biomes.BEACH);
        map.put(Biomes.SNOWY_TAIGA, Biomes.BIRCH_FOREST);
        map.put(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_BIRCH_FOREST);
        map.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.OLD_GROWTH_BIRCH_FOREST);
        map.put(Biomes.ICE_SPIKES, Biomes.FLOWER_FOREST);
        return map;
    }

    private boolean heating;
    private int ticks;

    public AncientFurnaceTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ANCIENT_FURNACE, pos, state);
    }

    public void startHeating() {
        this.heating = true;
        this.ticks = 0;
        this.setChanged();
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide || !this.heating) {
            return;
        }

        this.ticks++;

        if (this.ticks >= HEATING_DURATION) {
            this.heating = false;
            finish(this.level, this.worldPosition);
        }
    }

    private static void finish(Level world, BlockPos center) {
        warmSurroundings(world, center);
        world.removeBlock(center, false);
        world.explode(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 4.0F, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
    }

    private static void warmSurroundings(Level world, BlockPos center) {
        int cx = center.getX();
        int cz = center.getZ();

        Set<LevelChunk> touchedChunks = new HashSet<>();
        Registry<Biome> biomeRegistry = world.registryAccess().registryOrThrow(Registries.BIOME);

        // Every column is classified before any biome is written: a biome cell is 4x4 blocks wide, so writing one column
        // converts its 15 neighbours too, and they would otherwise look already warm and keep their snow and ice.
        List<BlockPos> columns = new ArrayList<>();
        List<Holder<Biome>> warmBiomes = new ArrayList<>();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS) {
                    continue;
                }

                BlockPos columnPos = new BlockPos(cx + dx, world.getMinBuildHeight(), cz + dz);

                if (!world.hasChunkAt(columnPos)) {
                    continue;
                }

                ResourceKey<Biome> coldKey = world.getBiome(columnPos).unwrapKey().orElse(null);
                ResourceKey<Biome> warmKey = coldKey == null ? null : COLD_TO_WARM.get(coldKey);
                Holder<Biome> warmBiome = warmKey == null ? null : biomeRegistry.getHolder(warmKey).orElse(null);

                if (warmBiome != null) {
                    columns.add(columnPos);
                    warmBiomes.add(warmBiome);
                }
            }
        }

        for (int i = 0; i < columns.size(); i++) {
            BlockPos columnPos = columns.get(i);
            int x = columnPos.getX();
            int z = columnPos.getZ();
            LevelChunk chunk = world.getChunkAt(columnPos);
            setBiomeColumn(chunk, x, z, warmBiomes.get(i));
            chunk.setUnsaved(true);
            touchedChunks.add(chunk);

            int top = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

            for (int y = Math.min(top + 2, world.getMaxBuildHeight() - 1); y >= Math.max(top - 4, world.getMinBuildHeight()); y--) {
                BlockPos p = new BlockPos(x, y, z);
                Block block = world.getBlockState(p).getBlock();

                if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
                    world.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                } else if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE) {
                    world.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        resyncBiomesToClients(world, touchedChunks);
    }

    /**
     * Closes the gap this class's own javadoc previously disclosed and left
     * unattempted: the biome-array mutation above is a real, correct write
     * on the server, but a client's own separate copy of each chunk's biome
     * data never learns about it on its own - nothing re-sends it. Real bug,
     * found 2026-09-28 (reported by user, waited out a full heat-up and saw
     * the snow melt but no biome/color change). Fixed by resending each
     * touched chunk's full {@code SChunkDataPacket} (ground-truthed via
     * source that it always serializes {@code Chunk#getBiomes()} regardless
     * of the passed section filter, so a full resend is the real, working
     * mechanism - not something narrower) to every player currently tracking
     * it, the same packet/audience vanilla's own {@code ChunkManager
     * #sendChunkData} uses when a chunk first loads for a player, just
     * invoked directly since that method itself is private.
     */
    /**
     * 1.18 stores biomes per 4x4x4 cell in each chunk section's paletted container (no more flat per-chunk array), so
     * the whole column of the cell is rewritten in every section: the conversion is quantized to 4x4 blocks horizontally.
     */
    private static void setBiomeColumn(LevelChunk chunk, int x, int z, Holder<Biome> biome) {
        for (LevelChunkSection section : chunk.getSections()) {
            for (int cellY = 0; cellY < 4; cellY++) {
                ((net.minecraft.world.level.chunk.PalettedContainer<Holder<Biome>>) section.getBiomes()).set((x >> 2) & 3, cellY, (z >> 2) & 3, biome);
            }
        }
    }

    private static void resyncBiomesToClients(Level world, Set<LevelChunk> touchedChunks) {
        if (!(world instanceof ServerLevel) || touchedChunks.isEmpty()) {
            return;
        }

        ServerLevel serverWorld = (ServerLevel) world;

        for (LevelChunk chunk : touchedChunks) {
            ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, serverWorld.getChunkSource().getLightEngine(), null, null);

            serverWorld.getChunkSource().chunkMap.getPlayers(chunk.getPos(), false)
                    .forEach(player -> player.connection.send(packet));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("heating", heating);
        compound.putInt("ticks", ticks);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.heating = compound.getBoolean("heating");
        this.ticks = compound.getInt("ticks");
    }
}
