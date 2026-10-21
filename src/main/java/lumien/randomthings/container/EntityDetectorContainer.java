package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.EntityDetectorTileEntity;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * EntityDetectorContainer
 */
public class EntityDetectorContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot rangeX = DataSlot.standalone();
	public DataSlot rangeY = DataSlot.standalone();
	public DataSlot rangeZ = DataSlot.standalone();
	public DataSlot filter = DataSlot.standalone();
	public DataSlot invert = DataSlot.standalone();
	public DataSlot strongOutput = DataSlot.standalone();

	public EntityDetectorContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(1), ContainerLevelAccess.NULL);
	}

	public EntityDetectorContainer(int windowId, Inventory playerInventory, IItemHandler filterInventory, ContainerLevelAccess pos)
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

		this.addDataSlot(rangeX);
		this.addDataSlot(rangeY);
		this.addDataSlot(rangeZ);
		this.addDataSlot(filter);
		this.addDataSlot(invert);
		this.addDataSlot(strongOutput);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ENTITY_DETECTOR);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

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
