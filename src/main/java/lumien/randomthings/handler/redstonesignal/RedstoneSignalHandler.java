package lumien.randomthings.handler.redstonesignal;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

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
public class RedstoneSignalHandler extends WorldSavedData {
    private static final String ID = "randomthings_redstone_signals";

    private final List<RedstoneSignal> signals = new ArrayList<>();

    public RedstoneSignalHandler() {
        super(ID);
    }

    public static RedstoneSignalHandler get(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(RedstoneSignalHandler::new, ID);
    }

    public synchronized boolean addSignal(World world, BlockPos pos, int duration, int strength) {
        if (!world.isBlockLoaded(pos)) {
            return false;
        }

        signals.add(new RedstoneSignal(pos, duration, strength));
        this.markDirty();

        updatePosition(world, pos);
        return true;
    }

    public synchronized void tick(World world) {
        boolean changed = false;

        Iterator<RedstoneSignal> iterator = signals.iterator();

        while (iterator.hasNext()) {
            RedstoneSignal signal = iterator.next();

            if (world.isBlockLoaded(signal.getPosition()) && signal.tick()) {
                iterator.remove();
                changed = true;

                updatePosition(world, signal.getPosition());
            }
        }

        if (changed) {
            this.markDirty();
        }
    }

    public synchronized int getStrongPower(World world, BlockPos pos, Direction facing) {
        BlockPos source = pos.offset(facing.getOpposite());

        int strongest = 0;

        for (RedstoneSignal signal : signals) {
            if (signal.getPosition().equals(source)) {
                strongest = Math.max(strongest, signal.getRedstoneStrength());
            }
        }

        return strongest;
    }

    private static void updatePosition(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        state.neighborChanged(world, pos, Blocks.REDSTONE_BLOCK, pos, false);
        world.notifyNeighborsOfStateChange(pos, Blocks.REDSTONE_BLOCK);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT signalList = nbt.getList("signals", 10);

        for (int i = 0; i < signalList.size(); i++) {
            RedstoneSignal signal = new RedstoneSignal();
            signal.read(signalList.getCompound(i));
            this.signals.add(signal);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT signalList = new ListNBT();

        for (RedstoneSignal signal : signals) {
            CompoundNBT signalCompound = new CompoundNBT();
            signal.write(signalCompound);
            signalList.add(signalCompound);
        }

        compound.put("signals", signalList);

        return compound;
    }
}
