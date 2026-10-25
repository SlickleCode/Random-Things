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
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
            primary = net.neoforged.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "primaryPower");
            secondary = net.neoforged.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "secondaryPower");
            levels = net.neoforged.fml.util.ObfuscationReflectionHelper.findField(BeaconBlockEntity.class, "levels");
        } catch (net.neoforged.fml.util.ObfuscationReflectionHelper.UnableToFindFieldException e) {
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

        if (owner != null && !level.isClientSide()) {
            SpectreLensHandler.get(ServerLifecycleHooks.getCurrentServer()).removeLens(owner);
        }
    }

    public void tick() {
        if (level.isClientSide() || owner == null || level.getGameTime() % 80 != 0) {
            return;
        }

        BlockEntity below = level.getBlockEntity(worldPosition.below());

        if (below instanceof BeaconBlockEntity) {
            BeaconBlockEntity beacon = (BeaconBlockEntity) below;

            try {
                int beaconLevels = LEVELS.getInt(beacon);

                if (beaconLevels > 0) {
                    net.minecraft.core.Holder<MobEffect> primary = (net.minecraft.core.Holder<MobEffect>) PRIMARY_EFFECT.get(beacon);
                    net.minecraft.core.Holder<MobEffect> secondary = (net.minecraft.core.Holder<MobEffect>) SECONDARY_EFFECT.get(beacon);

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
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.saveAdditional(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (owner != null) {
            compound.put("owner", lumien.randomthings.util.TagIO.writeUuid(owner));
        }

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("owner")) {
            owner = lumien.randomthings.util.TagIO.readUuid(compound.get("owner"));
        }
    }
}
