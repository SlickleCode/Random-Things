package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import org.apache.logging.log4j.LogManager;
import java.lang.reflect.Field;
import java.util.UUID;

import lumien.randomthings.handler.spectrelens.SpectreLensHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraft.world.level.block.state.BlockState;

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
public class SpectreLensTileEntity extends BlockEntity {
    private static final Field PRIMARY_EFFECT;
    private static final Field SECONDARY_EFFECT;
    private static final Field LEVELS;

    static {
        Field primary = null;
        Field secondary = null;
        Field levels = null;
        try {
            // SRG names (primaryEffect / secondaryEffect) so they also resolve in a production jar
            primary = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "f_58652_");
            secondary = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "f_58653_");
            levels = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "f_58650_");
        } catch (net.minecraftforge.fml.util.ObfuscationReflectionHelper.UnableToFindFieldException e) {
            LogManager.getLogger().error("Spectre Lens could not access the beacon effect fields", e);
        }
        PRIMARY_EFFECT = primary;
        SECONDARY_EFFECT = secondary;
        LEVELS = levels;
    }

    private UUID owner;

    public SpectreLensTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SPECTRE_LENS, pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (owner != null && !level.isClientSide) {
            SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).removeLens(owner);
        }
    }

    public void tick() {
        if (level.isClientSide || owner == null || level.getGameTime() % 80 != 0) {
            return;
        }

        BlockEntity below = level.getBlockEntity(worldPosition.below());

        if (below instanceof BeaconBlockEntity) {
            BeaconBlockEntity beacon = (BeaconBlockEntity) below;

            try {
                int beaconLevels = LEVELS.getInt(beacon);

                if (beaconLevels > 0) {
                    MobEffect primary = (MobEffect) PRIMARY_EFFECT.get(beacon);
                    MobEffect secondary = (MobEffect) SECONDARY_EFFECT.get(beacon);

                    SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).addLens(owner, beaconLevels, primary, secondary);
                    return;
                }
            } catch (IllegalAccessException e) {
                LogManager.getLogger().error("Spectre Lens could not access the beacon effect fields", e);
            }
        }

        SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).removeLens(owner);
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if (owner != null) {
            compound.putUUID("owner", owner);
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        }
    }
}
