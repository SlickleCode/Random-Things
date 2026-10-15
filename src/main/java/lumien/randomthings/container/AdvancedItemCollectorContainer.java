package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AdvancedItemCollectorTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * AdvancedItemCollectorContainer
 */
public class AdvancedItemCollectorContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot rangeX = DataSlot.standalone();
	public DataSlot rangeY = DataSlot.standalone();
	public DataSlot rangeZ = DataSlot.standalone();

	public AdvancedItemCollectorContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(1), ContainerLevelAccess.NULL);
	}

	public AdvancedItemCollectorContainer(int windowId, Inventory playerInventory, IItemHandler filterInventory, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.ADVANCED_ITEM_COLLECTOR, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(filterInventory, 0, 77, 98));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 129 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 187));
		}

		this.addDataSlot(rangeX);
		this.addDataSlot(rangeY);
		this.addDataSlot(rangeZ);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ADVANCED_ITEM_COLLECTOR);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof AdvancedItemCollectorTileEntity)
			{
				AdvancedItemCollectorTileEntity aicte = (AdvancedItemCollectorTileEntity) te;

				this.rangeX.set(aicte.getRangeX());
				this.rangeY.set(aicte.getRangeY());
				this.rangeZ.set(aicte.getRangeZ());
			}
		});

		super.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem())
		{
			ItemStack stackInSlot = slot.getItem();
			result = stackInSlot.copy();

			if (index < 1)
			{
				if (!this.moveItemStackTo(stackInSlot, 1, 37, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.moveItemStackTo(stackInSlot, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}

			if (stackInSlot.getCount() == result.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, stackInSlot);
		}

		return result;
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (!(te instanceof AdvancedItemCollectorTileEntity))
			{
				return;
			}

			AdvancedItemCollectorTileEntity aicte = (AdvancedItemCollectorTileEntity) te;

			switch (id)
			{
				case 0:
					aicte.adjustRangeX(-1);
					break;
				case 1:
					aicte.adjustRangeX(1);
					break;
				case 2:
					aicte.adjustRangeY(-1);
					break;
				case 3:
					aicte.adjustRangeY(1);
					break;
				case 4:
					aicte.adjustRangeZ(-1);
					break;
				case 5:
					aicte.adjustRangeZ(1);
					break;
			}
		});
	}
}
