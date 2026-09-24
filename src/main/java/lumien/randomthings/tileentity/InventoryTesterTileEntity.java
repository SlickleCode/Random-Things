package lumien.randomthings.tileentity;

import lumien.randomthings.block.InventoryTesterBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.InventoryTesterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A tiny redstone-comparator-like nub: whatever item sits in its own single
 * slot is simulation-inserted (not actually consumed) into the inventory it
 * faces every few ticks, and it emits weak power if that insertion would
 * succeed (optionally inverted).
 */
public class InventoryTesterTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	private final ItemStackHandler itemHandler = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}
	};

	private boolean invertSignal = false;
	private boolean emitRedstone = false;
	private int counter = 0;

	public InventoryTesterTileEntity()
	{
		super(ModTileEntityTypes.INVENTORY_TESTER);
	}

	public IItemHandler itemHandler()
	{
		return itemHandler;
	}

	public boolean isPowered()
	{
		return emitRedstone;
	}

	public boolean invertSignal()
	{
		return invertSignal;
	}

	public void toggleInvert()
	{
		this.invertSignal = !this.invertSignal;
		this.markDirty();
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote || ++counter < 2)
		{
			return;
		}

		counter = 0;

		ItemStack testStack = itemHandler.getStackInSlot(0);

		if (testStack.isEmpty())
		{
			return;
		}

		Direction facing = this.getBlockState().get(InventoryTesterBlock.FACING);
		TileEntity target = this.world.getTileEntity(this.pos.offset(facing.getOpposite()));

		if (target == null)
		{
			return;
		}

		LazyOptional<IItemHandler> targetCap = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
		IItemHandler targetHandler = targetCap.orElse(null);

		if (targetHandler == null)
		{
			return;
		}

		ItemStack testResult = ItemHandlerHelper.insertItemStacked(targetHandler, testStack, true);
		boolean newRedstone = testResult.isEmpty();

		if (invertSignal)
		{
			newRedstone = !newRedstone;
		}

		if (newRedstone != emitRedstone)
		{
			this.emitRedstone = newRedstone;
			this.markDirty();
			this.world.notifyNeighborsOfStateChange(pos, ModBlocks.INVENTORY_TESTER);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putBoolean("emitRedstone", emitRedstone);
		compound.putBoolean("invertSignal", invertSignal);
		compound.put("inventory", itemHandler.serializeNBT());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.emitRedstone = compound.getBoolean("emitRedstone");
		this.invertSignal = compound.getBoolean("invertSignal");
		itemHandler.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new InventoryTesterContainer(windowId, playerInventory, itemHandler, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.inventory_tester");
	}
}
