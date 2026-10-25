package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.PotionVaporizerItemHandler;
import lumien.randomthings.tileentity.PotionVaporizerTileEntity;
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
import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.SlotItemHandler;

/**
 * PotionVaporizerContainer
 */
public class PotionVaporizerContainer extends AbstractContainerMenu
{
	ContainerLevelAccess pos;

	public DataSlot duration = DataSlot.standalone();
	public DataSlot durationLeft = DataSlot.standalone();
	public DataSlot color = DataSlot.standalone();
	public DataSlot fuelBurnTime = DataSlot.standalone();
	public DataSlot fuelBurn = DataSlot.standalone();

	public PotionVaporizerContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new PotionVaporizerItemHandler(), ContainerLevelAccess.NULL);
	}

	public PotionVaporizerContainer(int windowId, Inventory playerInventory, IItemHandler itemHandler, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.POTION_VAPORIZER, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(itemHandler, 0, 80, 53));
		this.addSlot(new SlotItemHandler(itemHandler, 1, 29, 17));
		this.addSlot(new SlotItemHandler(itemHandler, 2, 131, 17));

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

		this.addDataSlot(duration);
		this.addDataSlot(durationLeft);
		this.addDataSlot(color);
		this.addDataSlot(fuelBurnTime);
		this.addDataSlot(fuelBurn);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.POTION_VAPORIZER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof PotionVaporizerTileEntity)
			{
				PotionVaporizerTileEntity pvte = (PotionVaporizerTileEntity) te;

				this.duration.set(pvte.getDuration());
				this.durationLeft.set(pvte.getDurationLeft());
				this.color.set(pvte.getColor());
				this.fuelBurnTime.set(pvte.getFuelBurnTime());
				this.fuelBurn.set(pvte.getFuelBurn());
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

			if (index < 3)
			{
				if (!this.moveItemStackTo(stackInSlot, 3, 39, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.moveItemStackTo(stackInSlot, 0, 2, false))
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
}
