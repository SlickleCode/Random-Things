package lumien.randomthings.tileentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.SoundBoxBlock;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Holds one {@link SoundPatternItem} and plays it (at the block, volume/pitch
 * 1.0) on a redstone rising edge. Direct port of 1.12.2's
 * {@code TileEntitySoundBox}.
 */
public class SoundBoxTileEntity extends TileEntity {
    private ItemStack pattern = ItemStack.EMPTY;
    private boolean powered;

    public SoundBoxTileEntity() {
        super(ModTileEntityTypes.SOUND_BOX);
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

        if (this.world.getBlockState(this.pos).get(SoundBoxBlock.HAS_PATTERN) != hasPattern) {
            this.world.setBlockState(this.pos, ModBlocks.SOUND_BOX.getDefaultState().with(SoundBoxBlock.HAS_PATTERN, hasPattern));
        }

        this.markDirty();
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
                    this.world.playSound(null, this.pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (!pattern.isEmpty()) {
            compound.put("pattern", pattern.write(new CompoundNBT()));
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        this.pattern = compound.contains("pattern") ? ItemStack.read(compound.getCompound("pattern")) : ItemStack.EMPTY;
    }
}
