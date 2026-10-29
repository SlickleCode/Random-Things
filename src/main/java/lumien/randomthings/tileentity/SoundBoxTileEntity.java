package lumien.randomthings.tileentity;

import net.minecraft.core.registries.Registries;

import net.minecraft.core.BlockPos;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.SoundBoxBlock;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds one {@link SoundPatternItem} and plays it (at the block, volume/pitch
 * 1.0) on a redstone rising edge. Direct port of 1.12.2's
 * {@code TileEntitySoundBox}.
 */
public class SoundBoxTileEntity extends BlockEntity {
    private ItemStack pattern = ItemStack.EMPTY;
    private boolean powered;

    public SoundBoxTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SOUND_BOX, pos, state);
    }

    public boolean hasPattern() {
        return !this.pattern.isEmpty();
    }

    public ItemStack getPattern() {
        return this.pattern;
    }

    public void insertPattern(ItemStack newPattern) {
        this.pattern = newPattern;

        boolean hasPattern = !this.pattern.isEmpty();

        if (this.level.getBlockState(this.worldPosition).getValue(SoundBoxBlock.HAS_PATTERN) != hasPattern) {
            this.level.setBlockAndUpdate(this.worldPosition, ModBlocks.SOUND_BOX.defaultBlockState().setValue(SoundBoxBlock.HAS_PATTERN, hasPattern));
        }

        this.setChanged();
    }

    /**
     * Called by the block's neighborChanged whenever the block's own redstone
     * power state may have changed (matching this port's established
     * IRedstoneSensitive-replacement convention - see IgniterTileEntity).
     */
    public void updatePowerState(boolean newPowered) {
        boolean oldPowered = this.powered;
        this.powered = newPowered;

        if (!oldPowered && newPowered && !this.pattern.isEmpty()) {
            ResourceLocation soundLocation = SoundPatternItem.getSoundLocation(this.pattern);

            if (soundLocation != null) {
                SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundLocation);

                if (soundEvent != null) {
                    this.level.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putBoolean("powered", powered);

        if (!pattern.isEmpty()) {
            compound.put("pattern", pattern.save(new CompoundTag()));
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.powered = compound.getBoolean("powered");

        this.pattern = compound.contains("pattern") ? ItemStack.of(compound.getCompound("pattern")) : ItemStack.EMPTY;
    }
}
