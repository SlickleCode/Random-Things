package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.PotionVaporizerTileEntity;
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
 * PotionVaporizerContainer
 */
public class PotionVaporizerContainer extends Container
{
	IWorldPosCallable pos;

	public IntReferenceHolder duration = IntReferenceHolder.single();
	public IntReferenceHolder durationLeft = IntReferenceHolder.single();
	public IntReferenceHolder color = IntReferenceHolder.single();
	public IntReferenceHolder fuelBurnTime = IntReferenceHolder.single();
	public IntReferenceHolder fuelBurn = IntReferenceHolder.single();

	public PotionVaporizerContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(3), IWorldPosCallable.DUMMY);
	}

	public PotionVaporizerContainer(int windowId, PlayerInventory playerInventory, IItemHandler itemHandler, IWorldPosCallable pos)
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

		this.trackInt(duration);
		this.trackInt(durationLeft);
		this.trackInt(color);
		this.trackInt(fuelBurnTime);
		this.trackInt(fuelBurn);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.POTION_VAPORIZER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

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

			if (index < 3)
			{
				if (!this.mergeItemStack(stackInSlot, 3, 39, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, 2, false))
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
}
