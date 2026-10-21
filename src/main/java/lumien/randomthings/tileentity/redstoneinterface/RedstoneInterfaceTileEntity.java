package lumien.randomthings.tileentity.redstoneinterface;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.util.WeakSets;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared base for {@link BasicRedstoneInterfaceTileEntity}/{@link AdvancedRedstoneInterfaceTileEntity}:
 * tracks the weak/strong redstone power currently hitting this block's own 6
 * faces (refreshed whenever one of its own neighbors changes, same
 * {@code NeighborNotifyEvent} hook {@code RedstoneObserverTileEntity} already
 * uses - just matching against this TE's own position instead of a remote
 * target), and re-broadcasts it onto whatever position(s) this interface is
 * pointed at via the new {@code WorldRedstonePowerTransformer}/
 * {@code WorldReaderStrongPowerTransformer} coremods (see
 * {@code AsmHandler#overrideRedstonePower}/{@code overrideStrongPower}).
 * Direct port of 1.12.2's {@code TileEntityRedstoneInterface}.
 */
public abstract class RedstoneInterfaceTileEntity extends BlockEntity {
    private static final Set<RedstoneInterfaceTileEntity> INTERFACES = WeakSets.create();

    private final Map<Direction, Integer> weakPower = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer> strongPower = new EnumMap<>(Direction.class);

    protected RedstoneInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        INTERFACES.add(this);

        for (Direction facing : Direction.values()) {
            weakPower.put(facing, 0);
            strongPower.put(facing, 0);
        }
    }

    protected abstract boolean isTargeting(BlockPos pos);

    protected abstract void notifyTargets(net.minecraft.world.level.block.Block neighborBlock);

    public static int getWeakPower(Level world, BlockPos pos, Direction facing) {
        BlockPos checking = pos.relative(facing.getOpposite());
        int strongest = 0;

        for (RedstoneInterfaceTileEntity ri : WeakSets.snapshot(INTERFACES)) {
            if (!ri.isRemoved() && ri.level == world && ri.isTargeting(checking)) {
                strongest = Math.max(strongest, ri.weakPower.getOrDefault(facing, 0));
            }
        }

        return strongest;
    }

    public static int getStrongPower(Level world, BlockPos pos, Direction facing) {
        BlockPos checking = pos.relative(facing.getOpposite());
        int strongest = 0;

        for (RedstoneInterfaceTileEntity ri : WeakSets.snapshot(INTERFACES)) {
            if (!ri.isRemoved() && ri.level == world && ri.isTargeting(checking)) {
                strongest = Math.max(strongest, ri.strongPower.getOrDefault(facing, 0));
            }
        }

        return strongest;
    }

    private boolean updating = false;

    /**
     * Re-reads the power hitting this interface's own faces and, if anything changed, re-broadcasts it to the
     * target(s). Called from the owning block's {@code neighborChanged}/{@code onBlockAdded}, like 1.12.2's
     * {@code TileEntityRedstoneInterface#neighborChanged}. (A {@code NeighborNotifyEvent} listener was used before
     * and never worked: that event's position is the block that <em>changed</em>, not the block being notified, so
     * the interface only refreshed when its own block changed.)
     */
    public void updateRedstoneState() {
        if (updating || this.level == null) {
            return;
        }

        updating = true;
        boolean changed = false;

        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(facing);

            int newWeak = this.level.getSignal(neighborPos, facing);
            if (!weakPower.get(facing).equals(newWeak)) {
                weakPower.put(facing, newWeak);
                changed = true;
            }

            int newStrong = this.level.getDirectSignal(neighborPos, facing);
            if (!strongPower.get(facing).equals(newStrong)) {
                strongPower.put(facing, newStrong);
                changed = true;
            }
        }

        if (changed) {
            notifyTargets(Blocks.REDSTONE_BLOCK);

            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }

        updating = false;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        notifyTargets(Blocks.REDSTONE_BLOCK);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        CompoundTag weakCompound = new CompoundTag();
        CompoundTag strongCompound = new CompoundTag();

        for (Direction facing : Direction.values()) {
            weakCompound.putInt(facing.ordinal() + "", weakPower.get(facing));
            strongCompound.putInt(facing.ordinal() + "", strongPower.get(facing));
        }

        compound.put("weakPower", weakCompound);
        compound.put("strongPower", strongCompound);

    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        CompoundTag weakCompound = compound.getCompound("weakPower");
        CompoundTag strongCompound = compound.getCompound("strongPower");

        for (Direction facing : Direction.values()) {
            weakPower.put(facing, weakCompound.getInt(facing.ordinal() + ""));
            strongPower.put(facing, strongCompound.getInt(facing.ordinal() + ""));
        }
    }
}
