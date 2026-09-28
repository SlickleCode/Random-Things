package lumien.randomthings.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;

import java.util.HashMap;
import java.util.Map;

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
 * Disclosed gap: the biome mutation is server-authoritative and correct
 * immediately for anything that reads it server-side (spawn tables, weather,
 * this feature's own re-runs) - but a client already standing in the area
 * won't see the visual grass/foliage/fog color shift until the chunk
 * reloads (leaving and rejoining, or the chunk unloading), since that's
 * driven by the client's own separate copy of the chunk's biome data and
 * nothing here re-sends it. Not attempted this session - a full per-chunk
 * resync would need the server to resend `SChunkDataPacket` to every
 * tracking player, a meaningfully bigger change than the mutation itself.
 */
public class AncientFurnaceTileEntity extends TileEntity implements ITickableTileEntity {
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
    private static final Map<Biome, Biome> COLD_TO_WARM = buildConversionMap();

    private static Map<Biome, Biome> buildConversionMap() {
        Map<Biome, Biome> map = new HashMap<>();
        map.put(Biomes.TAIGA, Biomes.FOREST);
        map.put(Biomes.FROZEN_OCEAN, Biomes.OCEAN);
        map.put(Biomes.FROZEN_RIVER, Biomes.RIVER);
        map.put(Biomes.SNOWY_TUNDRA, Biomes.PLAINS);
        map.put(Biomes.SNOWY_MOUNTAINS, Biomes.WOODED_HILLS);
        map.put(Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS);
        map.put(Biomes.SNOWY_BEACH, Biomes.BEACH);
        map.put(Biomes.SNOWY_TAIGA, Biomes.BIRCH_FOREST);
        map.put(Biomes.SNOWY_TAIGA_HILLS, Biomes.BIRCH_FOREST_HILLS);
        map.put(Biomes.GIANT_TREE_TAIGA_HILLS, Biomes.BIRCH_FOREST_HILLS);
        map.put(Biomes.TAIGA_MOUNTAINS, Biomes.TALL_BIRCH_FOREST);
        map.put(Biomes.ICE_SPIKES, Biomes.FLOWER_FOREST);
        map.put(Biomes.SNOWY_TAIGA_MOUNTAINS, Biomes.TALL_BIRCH_HILLS);
        return map;
    }

    private boolean heating;
    private int ticks;

    public AncientFurnaceTileEntity() {
        super(ModTileEntityTypes.ANCIENT_FURNACE);
    }

    public void startHeating() {
        this.heating = true;
        this.ticks = 0;
        this.markDirty();
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isRemote || !this.heating) {
            return;
        }

        this.ticks++;

        if (this.ticks >= HEATING_DURATION) {
            this.heating = false;
            finish(this.world, this.pos);
        }
    }

    private static void finish(World world, BlockPos center) {
        warmSurroundings(world, center);
        world.removeBlock(center, false);
        world.createExplosion(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 4.0F, Explosion.Mode.BREAK);
    }

    private static void warmSurroundings(World world, BlockPos center) {
        int cx = center.getX();
        int cz = center.getZ();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS) {
                    continue;
                }

                int x = cx + dx;
                int z = cz + dz;
                BlockPos columnPos = new BlockPos(x, 0, z);

                if (!world.isBlockLoaded(columnPos)) {
                    continue;
                }

                Biome warmBiome = COLD_TO_WARM.get(world.getBiome(columnPos));

                if (warmBiome == null) {
                    continue;
                }

                Chunk chunk = world.getChunkAt(columnPos);
                chunk.getBiomes()[(z & 15) << 4 | (x & 15)] = warmBiome;
                chunk.markDirty();

                int top = world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z);

                for (int y = Math.min(top + 2, 255); y >= Math.max(top - 4, 0); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    Block block = world.getBlockState(p).getBlock();

                    if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    } else if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE) {
                        world.setBlockState(p, Blocks.WATER.getDefaultState(), 2);
                    }
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putBoolean("heating", heating);
        compound.putInt("ticks", ticks);
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.heating = compound.getBoolean("heating");
        this.ticks = compound.getInt("ticks");
    }
}
