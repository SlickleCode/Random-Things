package lumien.randomthings.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

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
public class RainShieldTileEntity extends TileEntity {
    public static final Set<RainShieldTileEntity> shields = Collections.newSetFromMap(new WeakHashMap<>());

    private boolean active = true;

    public RainShieldTileEntity() {
        super(ModTileEntityTypes.RAIN_SHIELD);

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
    public boolean isInRange(World world, BlockPos pos, double range) {
        if (!active || this.world != world) {
            return false;
        }

        double dx = this.pos.getX() - pos.getX();
        double dz = this.pos.getZ() - pos.getZ();

        return dx * dx + dz * dz < range * range;
    }

    public void updatePowerState(boolean powered) {
        boolean desired = !powered;

        if (desired != this.active) {
            this.active = desired;
            this.markDirty();

            if (this.world != null) {
                this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putBoolean("active", active);
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.active = !compound.contains("active") || compound.getBoolean("active");
    }

    @Override
    public void remove() {
        super.remove();
        shields.remove(this);
    }
}
