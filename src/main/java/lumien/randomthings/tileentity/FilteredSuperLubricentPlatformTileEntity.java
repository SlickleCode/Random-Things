package lumien.randomthings.tileentity;

import lumien.randomthings.container.FilteredSuperLubricentPlatformContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Holds the one example-item filter slot used by
 * {@code FilteredSuperLubricentPlatformBlock} to decide which dropped items
 * fall straight through it instead of resting on top.
 * <p>
 * Simplification, disclosed here: same as {@code AdvancedItemCollectorTileEntity}
 * - 1.12.2's filter was the fully configurable `ItemItemFilter` item; this
 * matches by plain item type ({@code ItemStack.areItemsEqual}) instead.
 */
public class FilteredSuperLubricentPlatformTileEntity extends TileEntity implements INamedContainerProvider
{
	private final ItemStackHandler filterInventory = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}
	};

	public FilteredSuperLubricentPlatformTileEntity()
	{
		super(ModTileEntityTypes.FILTERED_SUPER_LUBRICENT_PLATFORM);
	}

	public ItemStackHandler filterInventory()
	{
		return filterInventory;
	}

	public boolean matchesFilter(ItemStack stack)
	{
		ItemStack filterStack = filterInventory.getStackInSlot(0);
		return !filterStack.isEmpty() && ItemStack.areItemsEqual(filterStack, stack);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.put("inventory", filterInventory.serializeNBT());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		filterInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new FilteredSuperLubricentPlatformContainer(windowId, playerInventory, filterInventory, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.filtered_super_lubricent_platform");
	}
}
