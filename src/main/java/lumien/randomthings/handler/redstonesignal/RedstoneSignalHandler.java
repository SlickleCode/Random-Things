package lumien.randomthings.handler.redstonesignal;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds timed "phantom" redstone signals - {@link lumien.randomthings.item.RedstoneActivatorItem}
 * and {@link lumien.randomthings.item.RedstoneRemoteItem} both broadcast one
 * of these at an arbitrary world position for a fixed duration, making that
 * position appear powered to vanilla redstone (wire, comparators, ...)
 * without any real redstone source physically there. Queried from the new
 * {@code WorldRedstonePowerTransformer}/{@code WorldReaderStrongPowerTransformer}
 * coremods via {@code AsmHandler#overrideRedstonePower}/{@code overrideStrongPower} -
 * see their javadoc for why a coremod is the only way to inject power at an
 * arbitrary vanilla position. Direct port of 1.12.2's {@code RedstoneSignalHandler},
 * restructured onto 1.14.4's {@code WorldSavedData}/{@code DimensionSavedDataManager}
 * (matching {@code FlooNetworkHandler}'s precedent) - one handler per world
 * instead of one global handler keyed by dimension id, since every call site
 * already has the right {@code World} instance in hand (see {@link RedstoneSignal}'s
 * javadoc).
 */
public class RedstoneSignalHandler extends SavedData {
    private static final String ID = "randomthings_redstone_signals";
    /** 1.21.5+ saved data types are codec based: the old CompoundTag save/load code is wrapped by the compound codec. */
    public static final net.minecraft.world.level.saveddata.SavedDataType<RedstoneSignalHandler> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(ID, RedstoneSignalHandler::new,
            CompoundTag.CODEC.xmap(tag -> RedstoneSignalHandler.loadFrom(tag, lumien.randomthings.util.StackTag.provider()), data -> data.rtSave(new CompoundTag(), lumien.randomthings.util.StackTag.provider())));

    private final List<RedstoneSignal> signals = new ArrayList<>();

    public RedstoneSignalHandler() {
    }

    public static RedstoneSignalHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        RedstoneSignalHandler data = new RedstoneSignalHandler();
        data.readData(nbt);
        return data;
    }

    public static RedstoneSignalHandler get(Level world) {
        return ((ServerLevel) world).getDataStorage().computeIfAbsent(TYPE);
    }

    public synchronized boolean addSignal(Level world, BlockPos pos, int duration, int strength) {
        if (!world.hasChunkAt(pos)) {
            return false;
        }

        signals.add(new RedstoneSignal(pos, duration, strength));
        this.setDirty();

        updatePosition(world, pos);
        return true;
    }

    public synchronized void tick(Level world) {
        boolean changed = false;

        Iterator<RedstoneSignal> iterator = signals.iterator();

        while (iterator.hasNext()) {
            RedstoneSignal signal = iterator.next();

            if (world.hasChunkAt(signal.getPosition()) && signal.tick()) {
                iterator.remove();
                changed = true;

                updatePosition(world, signal.getPosition());
            }
        }

        if (changed) {
            this.setDirty();
        }
    }

    public synchronized int getStrongPower(Level world, BlockPos pos, Direction facing) {
        BlockPos source = pos.relative(facing.getOpposite());

        int strongest = 0;

        for (RedstoneSignal signal : signals) {
            if (signal.getPosition().equals(source)) {
                strongest = Math.max(strongest, signal.getRedstoneStrength());
            }
        }

        return strongest;
    }

    private static void updatePosition(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        state.handleNeighborChanged(world, pos, Blocks.REDSTONE_BLOCK, null, false);
        world.updateNeighborsAt(pos, Blocks.REDSTONE_BLOCK);
    }

    public void readData(CompoundTag nbt) {
        ListTag signalList = nbt.getListOrEmpty("signals");

        for (int i = 0; i < signalList.size(); i++) {
            RedstoneSignal signal = new RedstoneSignal();
            signal.read(signalList.getCompoundOrEmpty(i));
            this.signals.add(signal);
        }
    }

    public CompoundTag rtSave(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag signalList = new ListTag();

        for (RedstoneSignal signal : signals) {
            CompoundTag signalCompound = new CompoundTag();
            signal.write(signalCompound);
            signalList.add(signalCompound);
        }

        compound.put("signals", signalList);

        return compound;
    }
}
