package lumien.randomthings.tileentity;

import lumien.randomthings.util.WeakSets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Set;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Direct port of 1.12.2's {@code TileEntityRainShield} - a plain marker with
 * a static registry ({@link #shields}) that {@link
 * lumien.randomthings.asm.AsmHandler#overrideIsRainingAt} scans, same
 * "cubes"/"candles"-style pattern already used by {@code SlimeCubeTileEntity}
 * /{@code TileEntityPeaceCandle}. Disclosed simplification: 1.12.2 cached
 * each queried {@code BlockPos}'s result in a {@code ConcurrentHashMap}
 * (never invalidated on a shield's own power-toggle, only implicitly stale
 * until server restart) - not ported, since the coremod redirect this now
 * goes through isn't called anywhere near as often as 1.12.2's two separate
 * ASM entry points were, and a shield toggling active/inactive now takes
 * effect immediately instead of waiting out a stale cache entry, which is
 * strictly more correct than what it replaces.
 */
public class RainShieldTileEntity extends BlockEntity {
    /** Read from the render thread (rain drawing) and the server thread, so every access goes through the lock. */
    public static final Set<RainShieldTileEntity> shields = WeakSets.create();

    /** A copy that is safe to iterate while shields are added or removed on another thread. */
    public static java.util.List<RainShieldTileEntity> snapshot() {
        return WeakSets.snapshot(shields);
    }

    private boolean active = true;

    public RainShieldTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.RAIN_SHIELD, pos, state);

        shields.add(this);
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Horizontal-only distance check (matches 1.12.2 exactly - the shield's
     * own Y is ignored entirely, so it shields straight up/down without
     * limit but only {@code range} blocks outward).
     */
    public boolean isInRange(Level world, BlockPos pos, double range) {
        if (!active || this.level != world) {
            return false;
        }

        double dx = this.worldPosition.getX() - pos.getX();
        double dz = this.worldPosition.getZ() - pos.getZ();

        return dx * dx + dz * dz < range * range;
    }

    public void updatePowerState(boolean powered) {
        boolean desired = !powered;

        if (desired != this.active) {
            this.active = desired;
            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);
        compound.putBoolean("active", active);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);
        this.active = !compound.contains("active") || compound.getBoolean("active");
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        shields.remove(this);
    }
}
