package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.NotificationInterfaceTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * NotificationInterfaceContainer
 */
public class NotificationInterfaceContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;
	String title;
	String description;

	public NotificationInterfaceContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(1), resolvePos(playerInventory, extraData.readBlockPos()), extraData.readString(), extraData.readString());
	}

	public NotificationInterfaceContainer(int windowId, PlayerInventory playerInventory, IItemHandler iconInventory, IWorldPosCallable pos, String title, String description)
	{
		super(ModContainerTypes.NOTIFICATION_INTERFACE, windowId);

		this.pos = pos;
		this.title = title;
		this.description = description;

		this.addSlot(new SlotItemHandler(iconInventory, 0, 80, 76));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 100 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 158));
		}
	}

	private static IWorldPosCallable resolvePos(IInventory playerInventory, BlockPos blockPos)
	{
		PlayerInventory inv = (PlayerInventory) playerInventory;
		return IWorldPosCallable.of(inv.player.world, blockPos);
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.NOTIFICATION_INTERFACE);
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
		if (id == 0)
		{
			String newTitle = data.readString();
			String newDescription = data.readString();

			this.title = newTitle;
			this.description = newDescription;

			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof NotificationInterfaceTileEntity)
				{
					((NotificationInterfaceTileEntity) te).setData(newTitle, newDescription);
				}
			});
		}
	}
}
