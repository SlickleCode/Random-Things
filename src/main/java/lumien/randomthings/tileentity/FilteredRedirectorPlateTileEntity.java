package lumien.randomthings.tileentity;

import lumien.randomthings.container.FilteredRedirectorPlateContainer;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.entity.Entity;
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
 * Holds the two {@link IEntityFilterItem} slots used by
 * {@code FilteredRedirectorPlateBlock} to decide which of its two possible
 * outputs (left/right turn) an entity gets redirected to.
 */
public class FilteredRedirectorPlateTileEntity extends TileEntity implements INamedContainerProvider
{
	private final ItemStackHandler filterInventory = new ItemStackHandler(2)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack)
		{
			return stack.getItem() instanceof IEntityFilterItem;
		}
	};

	public FilteredRedirectorPlateTileEntity()
	{
		super(ModTileEntityTypes.FILTERED_REDIRECTOR_PLATE);
	}

	public ItemStackHandler filterInventory()
	{
		return filterInventory;
	}

	/**
	 * @return whether filter slot {@code index} (0 or 1) currently matches
	 * the given entity; false if that slot is empty or holds a non-filter
	 * item.
	 */
	public boolean matches(int index, Entity entity)
	{
		ItemStack stack = filterInventory.getStackInSlot(index);

		if (stack.isEmpty() || !(stack.getItem() instanceof IEntityFilterItem))
		{
			return false;
		}

		return ((IEntityFilterItem) stack.getItem()).apply(stack, entity);
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
		return new FilteredRedirectorPlateContainer(windowId, playerInventory, filterInventory, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.plate_filtered_redirector");
	}
}
