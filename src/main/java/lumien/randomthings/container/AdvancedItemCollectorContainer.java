package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AdvancedItemCollectorTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * AdvancedItemCollectorContainer
 */
public class AdvancedItemCollectorContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder rangeX = IntReferenceHolder.single();
	public IntReferenceHolder rangeY = IntReferenceHolder.single();
	public IntReferenceHolder rangeZ = IntReferenceHolder.single();

	public AdvancedItemCollectorContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(1), IWorldPosCallable.DUMMY);
	}

	public AdvancedItemCollectorContainer(int windowId, PlayerInventory playerInventory, IItemHandler filterInventory, IWorldPosCallable pos)
	{
		super(ModContainerTypes.ADVANCED_ITEM_COLLECTOR, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(filterInventory, 0, 80, 20));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}

		this.trackInt(rangeX);
		this.trackInt(rangeY);
		this.trackInt(rangeZ);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ADVANCED_ITEM_COLLECTOR);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof AdvancedItemCollectorTileEntity)
			{
				AdvancedItemCollectorTileEntity aicte = (AdvancedItemCollectorTileEntity) te;

				this.rangeX.set(aicte.getRangeX());
				this.rangeY.set(aicte.getRangeY());
				this.rangeZ.set(aicte.getRangeZ());
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			result = stackInSlot.copy();

			if (index < 1)
			{
				if (!this.mergeItemStack(stackInSlot, 1, 37, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, 1, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
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
	public void handle(int id, PacketBuffer data)
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

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
