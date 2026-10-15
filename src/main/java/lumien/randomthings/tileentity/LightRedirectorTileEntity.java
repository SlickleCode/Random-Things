package lumien.randomthings.tileentity;

import lumien.randomthings.util.WeakSets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

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
public class LightRedirectorTileEntity extends BlockEntity {
    public static final Set<LightRedirectorTileEntity> REDIRECTORS = WeakSets.create();

    private final Map<Direction, Boolean> enabledMap = new EnumMap<>(Direction.class);
    public final Map<BlockPos, BlockPos> targets = new HashMap<>();
    public boolean established;

    public LightRedirectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.LIGHT_REDIRECTOR, pos, state);

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
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.level.isClientSide) {
            established = true;

            for (Direction facing : Direction.values()) {
                this.level.sendBlockUpdated(this.worldPosition.relative(facing), this.level.getBlockState(this.worldPosition.relative(facing)), this.level.getBlockState(this.worldPosition.relative(facing)), 3);
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        REDIRECTORS.remove(this);

        if (this.level != null && this.level.isClientSide) {
            for (Direction facing : Direction.values()) {
                this.level.sendBlockUpdated(this.worldPosition.relative(facing), this.level.getBlockState(this.worldPosition.relative(facing)), this.level.getBlockState(this.worldPosition.relative(facing)), 3);
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        // 1.18 delivers an empty update tag as null
        this.load(pkt.getTag() != null ? pkt.getTag() : new CompoundTag());

        targets.clear();

        for (Direction facing : Direction.values()) {
            this.level.sendBlockUpdated(this.worldPosition.relative(facing), this.level.getBlockState(this.worldPosition.relative(facing)), this.level.getBlockState(this.worldPosition.relative(facing)), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        for (Direction facing : Direction.values()) {
            compound.putBoolean("enabled_" + facing.getSerializedName(), enabledMap.get(facing));
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        for (Direction facing : Direction.values()) {
            String key = "enabled_" + facing.getSerializedName();

            if (compound.contains(key)) {
                enabledMap.put(facing, compound.getBoolean(key));
            }
        }
    }
}
