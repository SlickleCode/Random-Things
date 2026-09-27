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
import net.minecraft.world.gen.Heightmap;

/**
 * Direct port of 1.12.2's {@code TileEntityAncientFurnace} heating/explosion
 * state machine. {@code HEATING_DURATION}/{@code AREA_BLOCKS} match the
 * wiki's own description ("after heating up", "10000 blocks (configurable)")
 * as closely as a hardcoded default can - 1.12.2 exposed both through its
 * config file, which this port hasn't built yet (see {@code
 * WIKI_FEATURE_STATUS.md}), so both are plain constants here instead.
 * <p>
 * Disclosed simplification: "turn an area... into a warmer biome" is ported
 * as its concretely observable effect - melting surface snow/ice back to the
 * blocks underneath/water, exactly matching the wiki's own "removing any
 * surface snow in the process" - rather than as a literal biome-registry
 * reassignment. 1.14.4 biomes are baked into each chunk's {@code
 * BiomeContainer} at generation time with no supported public API to
 * reassign them at runtime (unlike a block, a biome can't just be
 * {@code setBlockState}'d), so doing this "for real" would mean reaching
 * into a private, Mojang-mapped field via reflection with no way to verify
 * it here - not attempted. Anything that actually depends on the biome
 * registry (spawn tables, grass/foliage color, whether it snows there again
 * later) is therefore unaffected; the visible "melted" ground is not.
 */
public class AncientFurnaceTileEntity extends TileEntity implements ITickableTileEntity {
    /** ~56 blocks, the radius of a circle with area 10000 (the wiki's own default). */
    private static final int RADIUS = 56;
    private static final int HEATING_DURATION = 20 * 60 * 5;

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

                if (!world.isBlockLoaded(new BlockPos(x, 0, z))) {
                    continue;
                }

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
