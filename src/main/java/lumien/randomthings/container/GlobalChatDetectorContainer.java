package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
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
import net.minecraft.core.BlockPos;
import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ItemStackHandler;
import lumien.randomthings.itemhandler.SlotItemHandler;

/**
 * GlobalChatDetectorContainer
 */
public class GlobalChatDetectorContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;
	String chatMessage;

	public DataSlot consume = DataSlot.standalone();

	public GlobalChatDetectorContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(9), resolvePos(playerInventory, extraData.readBlockPos()), extraData.readUtf(32767));
	}

	public GlobalChatDetectorContainer(int windowId, Inventory playerInventory, IItemHandler idCardInventory, ContainerLevelAccess pos, String chatMessage)
	{
		super(ModContainerTypes.GLOBAL_CHAT_DETECTOR, windowId);

		this.pos = pos;
		this.chatMessage = chatMessage;

		for (int i = 0; i < 9; i++)
		{
			this.addSlot(new SlotItemHandler(idCardInventory, i, 8 + i * 18, 40));
		}

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 75 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 133));
		}

		this.addDataSlot(consume);
	}

	private static ContainerLevelAccess resolvePos(Container playerInventory, BlockPos blockPos)
	{
		Inventory inv = (Inventory) playerInventory;
		return ContainerLevelAccess.create(inv.player.level(), blockPos);
	}

	public String getChatMessage()
	{
		return chatMessage;
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.GLOBAL_CHAT_DETECTOR);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				this.consume.set(((GlobalChatDetectorTileEntity) te).consume() ? 1 : 0);
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

			if (index < 9)
			{
				if (!this.moveItemStackTo(stackInSlot, 9, 45, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.moveItemStackTo(stackInSlot, 0, 9, false))
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
		if (id == 0)
		{
			String newMessage = data.readUtf(32767);
			this.chatMessage = newMessage;

			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof GlobalChatDetectorTileEntity)
				{
					((GlobalChatDetectorTileEntity) te).setChatMessage(newMessage);
				}
			});
		}
		else if (id == 1)
		{
			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof GlobalChatDetectorTileEntity)
				{
					GlobalChatDetectorTileEntity gcte = (GlobalChatDetectorTileEntity) te;
					gcte.setConsume(!gcte.consume());
				}
			});
		}
	}
}
