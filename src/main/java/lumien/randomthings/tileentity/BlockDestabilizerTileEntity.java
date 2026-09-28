package lumien.randomthings.tileentity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lumien.randomthings.block.BlockDestabilizerBlock;
import lumien.randomthings.container.BlockDestabilizerContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Ported from 1.12.2's {@code TileEntityBlockDestabilizer}: BFS flood-fills
 * every block matching the one directly in front (by exact {@code
 * BlockState}, or just {@code Block} type in "fuzzy" mode), then drops the
 * whole matched set as falling-block entities, top-to-bottom then nearest-
 * first.
 *
 * <p>1.12.2 needed its own {@code EntityFallingBlockSpecial} - a near
 * line-for-line copy of vanilla's own falling-block entity, just with {@code
 * shouldDropItem} exposed - because that flag isn't public on every version
 * of vanilla's class. In this Forge build {@code
 * net.minecraft.entity.item.FallingBlockEntity#shouldDropItem} already is a
 * public mutable field, so this port spawns vanilla's own entity directly
 * instead of maintaining a parallel copy.
 *
 * <p>Not build-verified in this sandbox (network policy blocks the Forge/
 * Mojang Maven hosts {@code ./gradlew build} needs) - flag for retest.
 */
public class BlockDestabilizerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private static final int SEARCH_LIMIT = 50;

    private enum State {
        IDLE, SEARCHING, DROPPING;
    }

    private State state = State.IDLE;

    private Set<BlockPos> alreadyChecked;
    private List<BlockPos> toCheck;
    private Set<BlockPos> targetBlocks;
    private BlockState targetState;

    private List<BlockPos> targetBlocksSorted;
    private int dropCounter;

    private boolean lazy;
    private boolean fuzzy;
    private Set<BlockPos> invalidBlocks;

    private boolean poweredLastCheck;

    public BlockDestabilizerTileEntity() {
        super(ModTileEntityTypes.BLOCK_DESTABILIZER);
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isFuzzy() {
        return fuzzy;
    }

    public void toggleLazy() {
        if (state == State.IDLE) {
            lazy = !lazy;

            if (!lazy) {
                invalidBlocks = null;
            }

            markDirty();
        }
    }

    public void toggleFuzzy() {
        fuzzy = !fuzzy;
        markDirty();
    }

    public void resetLazy() {
        if (state == State.IDLE) {
            invalidBlocks = null;
            markDirty();
        }
    }

    public void neighborChanged(boolean powered) {
        boolean wasPowered = poweredLastCheck;
        poweredLastCheck = powered;

        if (!wasPowered && powered && state == State.IDLE) {
            initStart();
        }
    }

    @Override
    public void tick() {
        if (world == null || world.isRemote) {
            return;
        }

        if (state == State.SEARCHING) {
            stepSearch();
        } else if (state == State.DROPPING) {
            dropNextBlock();
        }
    }

    private void initStart() {
        Direction facing = world.getBlockState(pos).get(BlockDestabilizerBlock.FACING);
        BlockPos targetPos = pos.offset(facing);

        if (world.isAirBlock(targetPos)) {
            return;
        }

        BlockState candidate = world.getBlockState(targetPos);
        if (candidate.getBlockHardness(world, targetPos) < 0) {
            return;
        }

        targetState = candidate;
        state = State.SEARCHING;
        toCheck = new ArrayList<>();
        toCheck.add(targetPos);
        targetBlocks = new HashSet<>();
        alreadyChecked = new HashSet<>();

        if (lazy) {
            if (invalidBlocks == null) {
                invalidBlocks = new HashSet<>();
            } else {
                alreadyChecked.addAll(invalidBlocks);
            }
        } else {
            invalidBlocks = null;
        }

        markDirty();
    }

    private void stepSearch() {
        if (toCheck.isEmpty() || targetBlocks.size() >= SEARCH_LIMIT) {
            initDrop();
            return;
        }

        BlockPos nextPos = toCheck.remove(0);

        if (alreadyChecked.contains(nextPos)) {
            return;
        }

        alreadyChecked.add(nextPos);
        BlockState blockStateAt = world.getBlockState(nextPos);

        boolean matches = fuzzy ? blockStateAt.getBlock() == targetState.getBlock() : blockStateAt.equals(targetState);

        if (matches) {
            targetBlocks.add(nextPos);

            for (Direction facing : Direction.values()) {
                BlockPos neighbor = nextPos.offset(facing);

                if (!alreadyChecked.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        } else if (lazy) {
            invalidBlocks.add(nextPos);
        }

        markDirty();
    }

    private void initDrop() {
        targetBlocksSorted = new ArrayList<>(targetBlocks);

        BlockPos origin = this.pos;
        targetBlocksSorted.sort(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingDouble(p -> p.distanceSq(origin)));

        state = State.DROPPING;
        dropCounter = 0;

        targetBlocks = null;
        toCheck = null;
        alreadyChecked = null;

        markDirty();
    }

    private void dropNextBlock() {
        if (dropCounter >= targetBlocksSorted.size()) {
            state = State.IDLE;
            targetBlocksSorted = null;
            targetState = null;
            markDirty();
            return;
        }

        BlockPos targetPos = targetBlocksSorted.get(dropCounter);
        BlockState target = world.getBlockState(targetPos);

        boolean matches = fuzzy ? target.getBlock() == targetState.getBlock() : target.equals(targetState);

        if (matches && world.getTileEntity(targetPos) == null) {
            FallingBlockEntity fallingEntity = new FallingBlockEntity(world, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, target);
            fallingEntity.shouldDropItem = false;

            world.addEntity(fallingEntity);
        }

        dropCounter++;
        markDirty();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putInt("state", state.ordinal());
        compound.putBoolean("lazy", lazy);
        compound.putBoolean("fuzzy", fuzzy);
        compound.putBoolean("poweredLastCheck", poweredLastCheck);

        if (lazy && invalidBlocks != null) {
            compound.put("invalidBlocks", writePosList(invalidBlocks));
        }

        if (state == State.SEARCHING) {
            compound.put("alreadyChecked", writePosList(alreadyChecked));
            compound.put("toCheck", writePosList(toCheck));
            compound.put("targetBlocks", writePosList(targetBlocks));
            compound.put("targetState", NBTUtil.writeBlockState(targetState));
        } else if (state == State.DROPPING) {
            compound.putInt("dropCounter", dropCounter);
            compound.put("targetBlocksSorted", writePosList(targetBlocksSorted));
            compound.put("targetState", NBTUtil.writeBlockState(targetState));
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        state = State.values()[compound.getInt("state")];
        lazy = compound.getBoolean("lazy");
        fuzzy = compound.getBoolean("fuzzy");
        poweredLastCheck = compound.getBoolean("poweredLastCheck");

        if (lazy && compound.contains("invalidBlocks")) {
            invalidBlocks = readPosSet(compound.getList("invalidBlocks", 10));
        }

        if (state == State.SEARCHING) {
            alreadyChecked = readPosSet(compound.getList("alreadyChecked", 10));
            toCheck = readPosList(compound.getList("toCheck", 10));
            targetBlocks = readPosSet(compound.getList("targetBlocks", 10));
            targetState = compound.contains("targetState") ? NBTUtil.readBlockState(compound.getCompound("targetState")) : Blocks.STONE.getDefaultState();
        } else if (state == State.DROPPING) {
            dropCounter = compound.getInt("dropCounter");
            targetBlocksSorted = readPosList(compound.getList("targetBlocksSorted", 10));
            targetState = compound.contains("targetState") ? NBTUtil.readBlockState(compound.getCompound("targetState")) : Blocks.STONE.getDefaultState();
        }
    }

    private static ListNBT writePosList(Iterable<BlockPos> positions) {
        ListNBT list = new ListNBT();

        for (BlockPos p : positions) {
            list.add(NBTUtil.writeBlockPos(p));
        }

        return list;
    }

    private static List<BlockPos> readPosList(ListNBT list) {
        List<BlockPos> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(NBTUtil.readBlockPos(list.getCompound(i)));
        }

        return result;
    }

    private static Set<BlockPos> readPosSet(ListNBT list) {
        Set<BlockPos> result = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(NBTUtil.readBlockPos(list.getCompound(i)));
        }

        return result;
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new BlockDestabilizerContainer(windowId, IWorldPosCallable.of(this.world, pos));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.block_destabilizer");
    }
}
