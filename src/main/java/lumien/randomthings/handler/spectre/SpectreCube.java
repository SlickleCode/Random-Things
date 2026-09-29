package lumien.randomthings.handler.spectre;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code SpectreCube}: one player's private 16x16
 * room in the shared Spectre dimension, laid out in a row along X (each cube
 * a fresh 16-block-wide slot, {@link SpectreHandler} handing out the next
 * one). Disclosed simplification: the original's spawn point could be
 * customized by dropping an {@code ItemPositionFilter} into the cube's own
 * small inventory ({@code onInventoryChanged}); that item was never ported
 * to 1.14.4 (a separate, still-NOT-STARTED feature), so the spawn point is
 * always the cube's center - the same fallback the original used whenever no
 * filter was set, so this is a true subset of the original behavior, not a
 * changed default. The original's {@code guests} list is dropped entirely -
 * ground-truthed against the real 1.12.2 source that it was never actually
 * read anywhere outside its own NBT round-trip (no access-control check ever
 * consulted it), so it was already dead weight there.
 */
public class SpectreCube {
    private UUID owner;
    private int height;
    private int position;
    private BlockPos spawnBlock;

    private SpectreCube() {
        this.height = 2;
    }

    public SpectreCube(UUID owner, int position) {
        this();

        this.owner = owner;
        this.position = position;

        this.spawnBlock = new BlockPos(position * 16 + 8, 0, 8);
    }

    public static SpectreCube readFrom(CompoundNBT compound) {
        SpectreCube cube = new SpectreCube();
        cube.owner = compound.getUniqueId("owner");
        cube.position = compound.getInt("position");
        cube.height = compound.getInt("height");
        cube.spawnBlock = new BlockPos(compound.getInt("spawnBlockX"), compound.getInt("spawnBlockY"), compound.getInt("spawnBlockZ"));
        return cube;
    }

    public void writeTo(CompoundNBT compound) {
        compound.putUniqueId("owner", owner);
        compound.putInt("position", position);
        compound.putInt("height", height);
        compound.putInt("spawnBlockX", spawnBlock.getX());
        compound.putInt("spawnBlockY", spawnBlock.getY());
        compound.putInt("spawnBlockZ", spawnBlock.getZ());
    }

    public UUID getOwner() {
        return owner;
    }

    public int getPosition() {
        return position;
    }

    public int getHeight() {
        return height;
    }

    public BlockPos getSpawnBlock() {
        return spawnBlock;
    }

    public void generate(World world) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);

        BlockPos pos1 = corner;
        BlockPos pos2 = corner.add(15, height + 1, 15);

        generateCube(world, pos1, pos2, ModBlocks.SPECTRE_BLOCK.getDefaultState(), 3);
        generateCore(world, pos1.add(7, 0, 7));
    }

    /**
     * Places the 2x2 {@code SPECTRE_CORE} pedestal, one block per corner,
     * each with its own {@code ORIENTATION} value set directly - 1.12.2's
     * {@code BlockSpectreCore} instead detected this dynamically every render
     * via {@code getActualState} scanning its neighbors for another Core
     * block; since generation always places exactly these 4 blocks in this
     * exact arrangement, setting the correct orientation once up front is
     * equivalent and avoids porting that neighbor-scanning hack.
     */
    private static void generateCore(World world, BlockPos nw) {
        BlockState core = ModBlocks.SPECTRE_CORE.getDefaultState();

        world.setBlockState(nw, core.with(lumien.randomthings.block.SpectreCoreBlock.ORIENTATION, lumien.randomthings.block.SpectreCoreBlock.CoreOrientation.NW), 3);
        world.setBlockState(nw.add(1, 0, 0), core.with(lumien.randomthings.block.SpectreCoreBlock.ORIENTATION, lumien.randomthings.block.SpectreCoreBlock.CoreOrientation.NE), 3);
        world.setBlockState(nw.add(0, 0, 1), core.with(lumien.randomthings.block.SpectreCoreBlock.ORIENTATION, lumien.randomthings.block.SpectreCoreBlock.CoreOrientation.SW), 3);
        world.setBlockState(nw.add(1, 0, 1), core.with(lumien.randomthings.block.SpectreCoreBlock.ORIENTATION, lumien.randomthings.block.SpectreCoreBlock.CoreOrientation.ES), 3);
    }

    public int increaseHeight(int amount, World world) {
        int heightLeft = 255 - (height + 1);

        int newHeight;

        if (heightLeft - amount > 0) {
            newHeight = height + amount;
        } else {
            newHeight = height + Math.max(heightLeft, 0);
        }

        int change = newHeight - height;

        if (newHeight != height) {
            changeHeight(newHeight, world);
        }

        return change;
    }

    private void changeHeight(int newHeight, World world) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);

        BlockPos pos1 = corner;
        BlockPos pos2 = corner.add(15, height + 1, 15);

        generateCube(world, pos1, pos2, Blocks.AIR.getDefaultState(), 2);
        this.height = newHeight;
        generate(world);
    }

    private static void generateCube(World world, BlockPos pos1, BlockPos pos2, BlockState state, int flag) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());

        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ) {
                        world.setBlockState(new BlockPos(x, y, z), state, flag);
                    }
                }
            }
        }
    }
}
