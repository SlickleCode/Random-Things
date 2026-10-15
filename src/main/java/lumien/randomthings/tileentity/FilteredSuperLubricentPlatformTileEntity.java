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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.ItemStackHandler;
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
		return !filterStack.isEmpty() && ItemStack.isSame(filterStack, stack);
	}

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.put("inventory", filterInventory.serializeNBT());
	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		filterInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new FilteredSuperLubricentPlatformContainer(windowId, playerInventory, filterInventory, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return new TranslatableComponent("block.randomthings.filtered_super_lubricent_platform");
	}
}
