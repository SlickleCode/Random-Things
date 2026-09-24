package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ChatDetectorTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;

/**
 * ChatDetectorContainer
 */
public class ChatDetectorContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;
	String chatMessage;

	public IntReferenceHolder consume = IntReferenceHolder.single();

	public ChatDetectorContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, resolvePos(playerInventory, extraData.readBlockPos()), extraData.readString());
	}

	public ChatDetectorContainer(int windowId, IWorldPosCallable pos, String chatMessage)
	{
		super(ModContainerTypes.CHAT_DETECTOR, windowId);

		this.pos = pos;
		this.chatMessage = chatMessage;

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
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.CHAT_DETECTOR);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof ChatDetectorTileEntity)
			{
				this.consume.set(((ChatDetectorTileEntity) te).consume() ? 1 : 0);
			}
		});

		super.detectAndSendChanges();
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

				if (te instanceof ChatDetectorTileEntity)
				{
					((ChatDetectorTileEntity) te).setChatMessage(newMessage);
				}
			});
		}
		else if (id == 1)
		{
			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof ChatDetectorTileEntity)
				{
					ChatDetectorTileEntity cte = (ChatDetectorTileEntity) te;
					cte.setConsume(!cte.consume());
				}
			});
		}
	}
}
