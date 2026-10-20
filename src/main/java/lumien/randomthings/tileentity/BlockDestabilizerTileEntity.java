package lumien.randomthings.tileentity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lumien.randomthings.block.BlockDestabilizerBlock;
import lumien.randomthings.container.BlockDestabilizerContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

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
 * net.minecraft.world.entity.item.FallingBlockEntity#shouldDropItem} already is a
 * public mutable field, so this port spawns vanilla's own entity directly
 * instead of maintaining a parallel copy.
 *
 * <p>Not build-verified in this sandbox (network policy blocks the Forge/
 * Mojang Maven hosts {@code ./gradlew build} needs) - flag for retest.
 */
public class BlockDestabilizerTileEntity extends BlockEntity implements MenuProvider {
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

    public BlockDestabilizerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.BLOCK_DESTABILIZER, pos, state);
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

            setChanged();
        }
    }

    public void toggleFuzzy() {
        fuzzy = !fuzzy;
        setChanged();
    }

    public void resetLazy() {
        if (state == State.IDLE) {
            invalidBlocks = null;
            setChanged();
        }
    }

    public void neighborChanged(boolean powered) {
        boolean wasPowered = poweredLastCheck;
        poweredLastCheck = powered;

        if (!wasPowered && powered && state == State.IDLE) {
            initStart();
        }
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (state == State.SEARCHING) {
            stepSearch();
        } else if (state == State.DROPPING) {
            dropNextBlock();
        }
    }

    private void initStart() {
        Direction facing = level.getBlockState(worldPosition).getValue(BlockDestabilizerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);

        if (level.isEmptyBlock(targetPos)) {
            return;
        }

        BlockState candidate = level.getBlockState(targetPos);
        if (candidate.getDestroySpeed(level, targetPos) < 0 || !candidate.isRedstoneConductor(level, targetPos)) {
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

        setChanged();
    }

    /**
     * Requested behavior change (2026-09-28), not a 1.12.2 bug fix - the
     * real original had no such restriction either, confirmed via that
     * branch's own source. The {@code isNormalCube} check below (added per
     * explicit user request) keeps the flood-fill from matching/consuming
     * decorative attachments - redstone dust, torches, levers, and the
     * like - even when their {@code Block}/{@code BlockState} happens to
     * equal the target; a torch sitting on a matched full block is now left
     * behind untouched instead of getting swept up with it.
     */
    private void stepSearch() {
        int limit = lumien.randomthings.config.RTConfig.BLOCK_DESTABILIZER_LIMIT.get();

        if (toCheck.isEmpty() || (limit != 0 && targetBlocks.size() >= limit)) {
            initDrop();
            return;
        }

        BlockPos nextPos = toCheck.remove(0);

        if (alreadyChecked.contains(nextPos)) {
            return;
        }

        alreadyChecked.add(nextPos);
        BlockState blockStateAt = level.getBlockState(nextPos);

        boolean matches = (fuzzy ? blockStateAt.getBlock() == targetState.getBlock() : blockStateAt.equals(targetState)) && blockStateAt.isRedstoneConductor(level, nextPos);

        if (matches) {
            targetBlocks.add(nextPos);

            for (Direction facing : Direction.values()) {
                BlockPos neighbor = nextPos.relative(facing);

                if (!alreadyChecked.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        } else if (lazy) {
            invalidBlocks.add(nextPos);
        }

        setChanged();
    }

    private void initDrop() {
        targetBlocksSorted = new ArrayList<>(targetBlocks);

        BlockPos origin = this.worldPosition;
        targetBlocksSorted.sort(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingDouble(p -> p.distSqr(origin)));

        state = State.DROPPING;
        dropCounter = 0;

        targetBlocks = null;
        toCheck = null;
        alreadyChecked = null;

        setChanged();
    }

    private void dropNextBlock() {
        if (dropCounter >= targetBlocksSorted.size()) {
            state = State.IDLE;
            targetBlocksSorted = null;
            targetState = null;
            setChanged();
            return;
        }

        BlockPos targetPos = targetBlocksSorted.get(dropCounter);
        BlockState target = level.getBlockState(targetPos);

        boolean matches = fuzzy ? target.getBlock() == targetState.getBlock() : target.equals(targetState);

        if (matches && level.getBlockEntity(targetPos) == null) {
            FallingBlockEntity fallingEntity = FallingBlockEntity.fall(level, targetPos, target);
            fallingEntity.dropItem = false;
        }

        dropCounter++;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

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
            compound.put("targetState", NbtUtils.writeBlockState(targetState));
        } else if (state == State.DROPPING) {
            compound.putInt("dropCounter", dropCounter);
            compound.put("targetBlocksSorted", writePosList(targetBlocksSorted));
            compound.put("targetState", NbtUtils.writeBlockState(targetState));
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

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
            targetState = compound.contains("targetState") ? NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("targetState")) : Blocks.STONE.defaultBlockState();
        } else if (state == State.DROPPING) {
            dropCounter = compound.getInt("dropCounter");
            targetBlocksSorted = readPosList(compound.getList("targetBlocksSorted", 10));
            targetState = compound.contains("targetState") ? NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("targetState")) : Blocks.STONE.defaultBlockState();
        }
    }

    private static ListTag writePosList(Iterable<BlockPos> positions) {
        ListTag list = new ListTag();

        for (BlockPos p : positions) {
            list.add(NbtUtils.writeBlockPos(p));
        }

        return list;
    }

    private static List<BlockPos> readPosList(ListTag list) {
        List<BlockPos> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }

        return result;
    }

    private static Set<BlockPos> readPosSet(ListTag list) {
        Set<BlockPos> result = new HashSet<>();

        for (int i = 0; i < list.size(); i++) {
            result.add(NbtUtils.readBlockPos(list.getCompound(i)));
        }

        return result;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new BlockDestabilizerContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.block_destabilizer");
    }
}
