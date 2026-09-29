package lumien.randomthings.tileentity;

import java.lang.reflect.Field;
import java.util.UUID;

import lumien.randomthings.handler.spectrelens.SpectreLensHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Ported from 1.12.2's {@code TileEntitySpectreLens} - every 80 ticks, checks the {@link BeaconTileEntity}
 * directly below it and, if its pyramid is complete, registers its current primary/secondary effects
 * with {@link SpectreLensHandler} under this lens's owner.
 *
 * <p>1.12.2 needed reflection to read the beacon's private "is the pyramid complete" flag and its
 * primary/secondary effect fields (there was no public accessor for any of the three). Ground-truthed
 * this build's real {@code BeaconTileEntity} via {@code javap} before assuming the same gap still
 * exists: the "is complete" flag itself is gone entirely in this version - {@link BeaconTileEntity#getLevels()}
 * (already public) is {@code 0} exactly when the pyramid isn't valid, so that half needs no reflection
 * anymore. {@code primaryEffect}/{@code secondaryEffect} are still private with no getter, so those two
 * still need it - using this build's own real (already MCP-mapped, not obfuscated) field names directly.
 */
public class SpectreLensTileEntity extends TileEntity implements ITickableTileEntity {
    private static final Field PRIMARY_EFFECT;
    private static final Field SECONDARY_EFFECT;

    static {
        Field primary = null;
        Field secondary = null;
        try {
            primary = BeaconTileEntity.class.getDeclaredField("primaryEffect");
            primary.setAccessible(true);
            secondary = BeaconTileEntity.class.getDeclaredField("secondaryEffect");
            secondary.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        PRIMARY_EFFECT = primary;
        SECONDARY_EFFECT = secondary;
    }

    private UUID owner;

    public SpectreLensTileEntity() {
        super(ModTileEntityTypes.SPECTRE_LENS);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    @Override
    public void remove() {
        super.remove();

        if (owner != null && !world.isRemote) {
            SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).removeLens(owner);
        }
    }

    @Override
    public void tick() {
        if (world.isRemote || owner == null || world.getGameTime() % 80 != 0) {
            return;
        }

        TileEntity below = world.getTileEntity(pos.down());

        if (below instanceof BeaconTileEntity) {
            BeaconTileEntity beacon = (BeaconTileEntity) below;

            if (beacon.getLevels() > 0) {
                try {
                    Effect primary = (Effect) PRIMARY_EFFECT.get(beacon);
                    Effect secondary = (Effect) SECONDARY_EFFECT.get(beacon);

                    SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).addLens(owner, beacon.getLevels(), primary, secondary);
                    return;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).removeLens(owner);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (owner != null) {
            compound.putUniqueId("owner", owner);
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("owner")) {
            owner = compound.getUniqueId("owner");
        }
    }
}
