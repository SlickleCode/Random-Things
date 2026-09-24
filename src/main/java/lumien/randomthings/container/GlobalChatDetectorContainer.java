package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * GlobalChatDetectorContainer
 */
public class GlobalChatDetectorContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;
	String chatMessage;

	public IntReferenceHolder consume = IntReferenceHolder.single();

	public GlobalChatDetectorContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(9), resolvePos(playerInventory, extraData.readBlockPos()), extraData.readString());
	}

	public GlobalChatDetectorContainer(int windowId, PlayerInventory playerInventory, IItemHandler idCardInventory, IWorldPosCallable pos, String chatMessage)
	{
		super(ModContainerTypes.GLOBAL_CHAT_DETECTOR, windowId);

		this.pos = pos;
		this.chatMessage = chatMessage;

		for (int i = 0; i < 9; i++)
		{
			this.addSlot(new SlotItemHandler(idCardInventory, i, 8 + i * 18, 62));
		}

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 156));
		}

		this.trackInt(consume);
	}

	private static IWorldPosCallable resolvePos(IInventory playerInventory, BlockPos blockPos)
	{
		PlayerInventory inv = (PlayerInventory) playerInventory;
		return IWorldPosCallable.of(inv.player.world, blockPos);
	}

	public String getChatMessage()
	{
		return chatMessage;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.GLOBAL_CHAT_DETECTOR);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				this.consume.set(((GlobalChatDetectorTileEntity) te).consume() ? 1 : 0);
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

			if (index < 9)
			{
				if (!this.mergeItemStack(stackInSlot, 9, 45, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, 9, false))
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
			String newMessage = data.readString();
			this.chatMessage = newMessage;

			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof GlobalChatDetectorTileEntity)
				{
					((GlobalChatDetectorTileEntity) te).setChatMessage(newMessage);
				}
			});
		}
		else if (id == 1)
		{
			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof GlobalChatDetectorTileEntity)
				{
					GlobalChatDetectorTileEntity gcte = (GlobalChatDetectorTileEntity) te;
					gcte.setConsume(!gcte.consume());
				}
			});
		}
	}
}
