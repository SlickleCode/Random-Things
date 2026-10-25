package lumien.randomthings.tileentity;

import lumien.randomthings.util.CapUtil;
import net.minecraft.core.BlockPos;

import lumien.randomthings.block.InventoryTesterBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.InventoryTesterContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;


import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ItemHandlerHelper;
import lumien.randomthings.itemhandler.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A tiny redstone-comparator-like nub: whatever item sits in its own single
 * slot is simulation-inserted (not actually consumed) into the inventory it
 * faces every few ticks, and it emits weak power if that insertion would
 * succeed (optionally inverted).
 */
public class InventoryTesterTileEntity extends BlockEntity implements MenuProvider
{
	private final ItemStackHandler itemHandler = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	};

	private boolean invertSignal = false;
	private boolean emitRedstone = false;
	private int counter = 0;

	public InventoryTesterTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.INVENTORY_TESTER, pos, state);
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
		this.setChanged();
	}

	public void tick()
	{
		if (this.level.isClientSide() || ++counter < 2)
		{
			return;
		}

		counter = 0;

		ItemStack testStack = itemHandler.getStackInSlot(0);

		if (testStack.isEmpty())
		{
			return;
		}

		Direction facing = this.getBlockState().getValue(InventoryTesterBlock.FACING);
		BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(facing.getOpposite()));

		if (target == null)
		{
			return;
		}

		IItemHandler targetHandler = CapUtil.itemHandler(target, facing.getOpposite());

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
			this.setChanged();
			this.level.updateNeighborsAt(worldPosition, ModBlocks.INVENTORY_TESTER);
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

	private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		compound.putBoolean("emitRedstone", emitRedstone);
		compound.putBoolean("invertSignal", invertSignal);
		compound.put("inventory", itemHandler.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		this.emitRedstone = compound.getBooleanOr("emitRedstone", false);
		this.invertSignal = compound.getBooleanOr("invertSignal", false);
		itemHandler.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new InventoryTesterContainer(windowId, playerInventory, itemHandler, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.inventory_tester");
	}
}
