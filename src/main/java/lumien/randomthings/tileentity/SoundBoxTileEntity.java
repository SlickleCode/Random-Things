package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import net.minecraft.core.BlockPos;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.SoundBoxBlock;
import lumien.randomthings.item.SoundPatternItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;
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
            Identifier soundLocation = SoundPatternItem.getSoundLocation(this.pattern);

            if (soundLocation != null) {
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(soundLocation);

                if (soundEvent != null) {
                    this.level.playSound(null, this.worldPosition, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
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
        compound.putBoolean("powered", powered);

        if (!pattern.isEmpty()) {
            compound.put("pattern", StackTag.saveStack(pattern));
        }

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        this.powered = compound.getBooleanOr("powered", false);

        this.pattern = compound.contains("pattern") ? lumien.randomthings.util.StackTag.loadStack(compound.getCompoundOrEmpty("pattern")) : ItemStack.EMPTY;
    }

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.SoundBoxBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
