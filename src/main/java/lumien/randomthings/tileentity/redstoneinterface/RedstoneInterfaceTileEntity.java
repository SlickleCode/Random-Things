package lumien.randomthings.tileentity.redstoneinterface;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
public abstract class RedstoneInterfaceTileEntity extends TileEntity {
    private static final Set<RedstoneInterfaceTileEntity> INTERFACES = Collections.newSetFromMap(new WeakHashMap<>());

    private final Map<Direction, Integer> weakPower = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer> strongPower = new EnumMap<>(Direction.class);

    protected RedstoneInterfaceTileEntity(TileEntityType<?> type) {
        super(type);

        INTERFACES.add(this);

        for (Direction facing : Direction.values()) {
            weakPower.put(facing, 0);
            strongPower.put(facing, 0);
        }
    }

    protected abstract boolean isTargeting(BlockPos pos);

    protected abstract void notifyTargets(net.minecraft.block.Block neighborBlock);

    public static int getWeakPower(World world, BlockPos pos, Direction facing) {
        BlockPos checking = pos.offset(facing.getOpposite());
        int strongest = 0;

        for (RedstoneInterfaceTileEntity ri : INTERFACES) {
            if (!ri.isRemoved() && ri.world == world && ri.isTargeting(checking)) {
                strongest = Math.max(strongest, ri.weakPower.getOrDefault(facing, 0));
            }
        }

        return strongest;
    }

    public static int getStrongPower(World world, BlockPos pos, Direction facing) {
        BlockPos checking = pos.offset(facing.getOpposite());
        int strongest = 0;

        for (RedstoneInterfaceTileEntity ri : INTERFACES) {
            if (!ri.isRemoved() && ri.world == world && ri.isTargeting(checking)) {
                strongest = Math.max(strongest, ri.strongPower.getOrDefault(facing, 0));
            }
        }

        return strongest;
    }

    public static void notifyNeighbor(NeighborNotifyEvent event) {
        for (RedstoneInterfaceTileEntity ri : INTERFACES) {
            if (ri.world == event.getWorld() && !ri.isRemoved() && ri.pos.equals(event.getPos())) {
                ri.updateRedstoneState();
            }
        }
    }

    private boolean updating = false;

    private void updateRedstoneState() {
        if (updating || this.world == null) {
            return;
        }

        updating = true;
        boolean changed = false;

        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = this.pos.offset(facing);

            int newWeak = this.world.getRedstonePower(neighborPos, facing);
            if (!weakPower.get(facing).equals(newWeak)) {
                weakPower.put(facing, newWeak);
                changed = true;
            }

            int newStrong = this.world.getStrongPower(neighborPos, facing);
            if (!strongPower.get(facing).equals(newStrong)) {
                strongPower.put(facing, newStrong);
                changed = true;
            }
        }

        if (changed) {
            notifyTargets(Blocks.REDSTONE_BLOCK);

            this.markDirty();

            if (this.world != null) {
                this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
            }
        }

        updating = false;
    }

    @Override
    public void remove() {
        super.remove();

        notifyTargets(Blocks.REDSTONE_BLOCK);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        CompoundNBT weakCompound = new CompoundNBT();
        CompoundNBT strongCompound = new CompoundNBT();

        for (Direction facing : Direction.values()) {
            weakCompound.putInt(facing.ordinal() + "", weakPower.get(facing));
            strongCompound.putInt(facing.ordinal() + "", strongPower.get(facing));
        }

        compound.put("weakPower", weakCompound);
        compound.put("strongPower", strongCompound);

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        CompoundNBT weakCompound = compound.getCompound("weakPower");
        CompoundNBT strongCompound = compound.getCompound("strongPower");

        for (Direction facing : Direction.values()) {
            weakPower.put(facing, weakCompound.getInt(facing.ordinal() + ""));
            strongPower.put(facing, strongCompound.getInt(facing.ordinal() + ""));
        }
    }
}
