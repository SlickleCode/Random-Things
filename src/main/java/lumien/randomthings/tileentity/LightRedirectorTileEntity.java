package lumien.randomthings.tileentity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * Direct port of 1.12.2's {@code TileEntityLightRedirector}: which of the 6
 * faces are "open" (see {@link lumien.randomthings.block.LightRedirectorBlock}
 * for the actual per-face texture swap, driven by real {@code BlockState}
 * properties instead of 1.12.2's {@code getActualState} trick, which doesn't
 * exist in this version), plus the lazily-computed opposite-face target map
 * {@link lumien.randomthings.asm.AsmHandler#getSwitchedPosition} walks to
 * find what a redirected block position should actually render as - see that
 * method's own javadoc and {@code BlockRendererDispatcherTransformer.js} for
 * the coremod this feeds.
 * <p>
 * {@link #established} mirrors 1.12.2 exactly: only ever set on the CLIENT
 * (in {@link #onLoad()}), since the redirect effect is purely a client-side
 * rendering trick - a server-side instance sits in {@link #REDIRECTORS} too
 * (harmlessly unused, since {@code AsmHandler.renderBlock} is only ever
 * reached from client-only rendering code) but never becomes "established."
 * <p>
 * 1.12.2 pushed the "a neighbor's own appearance may have just changed"
 * refresh through a dedicated {@code MessageLightRedirector} network message,
 * sent explicitly from both a toggle and a break. This port's TEs already
 * sync generically via {@link #getUpdatePacket()}/{@link #onDataPacket}
 * (see {@code FluidDisplayTileEntity}'s own javadoc for why the override
 * below is required at all in this Forge version), so the exact same neighbor
 * {@code notifyBlockUpdate} sweep 1.12.2's message handler did just runs
 * directly out of {@link #onDataPacket} instead - no separate message class
 * needed.
 */
public class LightRedirectorTileEntity extends TileEntity {
    public static final Set<LightRedirectorTileEntity> REDIRECTORS = Collections.newSetFromMap(new WeakHashMap<>());

    private final Map<Direction, Boolean> enabledMap = new EnumMap<>(Direction.class);
    public final Map<BlockPos, BlockPos> targets = new HashMap<>();
    public boolean established;

    public LightRedirectorTileEntity() {
        super(ModTileEntityTypes.LIGHT_REDIRECTOR);

        for (Direction facing : Direction.values()) {
            enabledMap.put(facing, true);
        }

        REDIRECTORS.add(this);
    }

    public boolean isEnabled(Direction facing) {
        return enabledMap.get(facing);
    }

    public void toggleSide(Direction facing) {
        enabledMap.put(facing, !enabledMap.get(facing));
        this.markDirty();

        if (this.world != null) {
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.world.isRemote) {
            established = true;

            for (Direction facing : Direction.values()) {
                this.world.notifyBlockUpdate(this.pos.offset(facing), this.world.getBlockState(this.pos.offset(facing)), this.world.getBlockState(this.pos.offset(facing)), 3);
            }
        }
    }

    @Override
    public void remove() {
        super.remove();

        REDIRECTORS.remove(this);

        if (this.world != null && this.world.isRemote) {
            for (Direction facing : Direction.values()) {
                this.world.notifyBlockUpdate(this.pos.offset(facing), this.world.getBlockState(this.pos.offset(facing)), this.world.getBlockState(this.pos.offset(facing)), 3);
            }
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());

        targets.clear();

        for (Direction facing : Direction.values()) {
            this.world.notifyBlockUpdate(this.pos.offset(facing), this.world.getBlockState(this.pos.offset(facing)), this.world.getBlockState(this.pos.offset(facing)), 3);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        for (Direction facing : Direction.values()) {
            compound.putBoolean("enabled_" + facing.getName(), enabledMap.get(facing));
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        for (Direction facing : Direction.values()) {
            String key = "enabled_" + facing.getName();

            if (compound.contains(key)) {
                enabledMap.put(facing, compound.getBoolean(key));
            }
        }
    }
}
