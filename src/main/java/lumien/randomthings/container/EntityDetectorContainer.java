package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.EntityDetectorTileEntity;
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
 * EntityDetectorContainer
 */
public class EntityDetectorContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder rangeX = IntReferenceHolder.single();
	public IntReferenceHolder rangeY = IntReferenceHolder.single();
	public IntReferenceHolder rangeZ = IntReferenceHolder.single();
	public IntReferenceHolder filter = IntReferenceHolder.single();
	public IntReferenceHolder invert = IntReferenceHolder.single();
	public IntReferenceHolder strongOutput = IntReferenceHolder.single();

	public EntityDetectorContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(1), IWorldPosCallable.DUMMY);
	}

	public EntityDetectorContainer(int windowId, PlayerInventory playerInventory, IItemHandler filterInventory, IWorldPosCallable pos)
	{
		super(ModContainerTypes.ENTITY_DETECTOR, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(filterInventory, 0, 140, 95));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 122 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 180));
		}

		this.trackInt(rangeX);
		this.trackInt(rangeY);
		this.trackInt(rangeZ);
		this.trackInt(filter);
		this.trackInt(invert);
		this.trackInt(strongOutput);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ENTITY_DETECTOR);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof EntityDetectorTileEntity)
			{
				EntityDetectorTileEntity edte = (EntityDetectorTileEntity) te;

				this.rangeX.set(edte.getRangeX());
				this.rangeY.set(edte.getRangeY());
				this.rangeZ.set(edte.getRangeZ());
				this.filter.set(edte.getFilter().ordinal());
				this.invert.set(edte.invert() ? 1 : 0);
				this.strongOutput.set(edte.strongOutput() ? 1 : 0);
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

			if (!(te instanceof EntityDetectorTileEntity))
			{
				return;
			}

			EntityDetectorTileEntity edte = (EntityDetectorTileEntity) te;

			switch (id)
			{
				case 0:
					edte.adjustRangeX(-1);
					break;
				case 1:
					edte.adjustRangeX(1);
					break;
				case 2:
					edte.adjustRangeY(-1);
					break;
				case 3:
					edte.adjustRangeY(1);
					break;
				case 4:
					edte.adjustRangeZ(-1);
					break;
				case 5:
					edte.adjustRangeZ(1);
					break;
				case 6:
					edte.cycleFilter();
					break;
				case 7:
					edte.toggleInvert();
					break;
				case 8:
					edte.toggleStrongOutput();
					break;
			}
		});
	}
}
