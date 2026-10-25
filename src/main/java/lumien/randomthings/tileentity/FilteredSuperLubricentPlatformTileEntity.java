package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.container.FilteredSuperLubricentPlatformContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import lumien.randomthings.itemhandler.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds the one example-item filter slot used by
 * {@code FilteredSuperLubricentPlatformBlock} to decide which dropped items
 * fall straight through it instead of resting on top.
 * <p>
 * Simplification, disclosed here: same as {@code AdvancedItemCollectorTileEntity}
 * - 1.12.2's filter was the fully configurable `ItemItemFilter` item; this
 * matches by plain item type ({@code ItemStack.areItemsEqual}) instead.
 */
public class FilteredSuperLubricentPlatformTileEntity extends BlockEntity implements MenuProvider
{
	private final ItemStackHandler filterInventory = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	};

	public FilteredSuperLubricentPlatformTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.FILTERED_SUPER_LUBRICENT_PLATFORM, pos, state);
    }

	public ItemStackHandler filterInventory()
	{
		return filterInventory;
	}

	public boolean matchesFilter(ItemStack stack)
	{
		ItemStack filterStack = filterInventory.getStackInSlot(0);
		return !filterStack.isEmpty() && ItemStack.isSameItem(filterStack, stack);
	}

	@Override
	protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
	{
		super.saveAdditional(rtOut);
		CompoundTag rtTag = new CompoundTag();
		rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
		lumien.randomthings.util.TagIO.write(rtOut, rtTag);
	}

	private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		compound.put("inventory", filterInventory.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		filterInventory.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new FilteredSuperLubricentPlatformContainer(windowId, playerInventory, filterInventory, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.filtered_super_lubricent_platform");
	}

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.FilteredSuperLubricentPlatformBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
