package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ChatDetectorTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.core.BlockPos;

/**
 * ChatDetectorContainer
 */
public class ChatDetectorContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;
	String chatMessage;

	public DataSlot consume = DataSlot.standalone();

	public ChatDetectorContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, resolvePos(playerInventory, extraData.readBlockPos()), extraData.readUtf(32767));
	}

	public ChatDetectorContainer(int windowId, ContainerLevelAccess pos, String chatMessage)
	{
		super(ModContainerTypes.CHAT_DETECTOR, windowId);

		this.pos = pos;
		this.chatMessage = chatMessage;

		this.addDataSlot(consume);
	}

	private static ContainerLevelAccess resolvePos(Container playerInventory, BlockPos blockPos)
	{
		Inventory inv = (Inventory) playerInventory;
		return ContainerLevelAccess.create(inv.player.level, blockPos);
	}

	public String getChatMessage()
	{
		return chatMessage;
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.CHAT_DETECTOR);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof ChatDetectorTileEntity)
			{
				this.consume.set(((ChatDetectorTileEntity) te).consume() ? 1 : 0);
			}
		});

		super.broadcastChanges();
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

				if (te instanceof ChatDetectorTileEntity)
				{
					((ChatDetectorTileEntity) te).setChatMessage(newMessage);
				}
			});
		}
		else if (id == 1)
		{
			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof ChatDetectorTileEntity)
				{
					ChatDetectorTileEntity cte = (ChatDetectorTileEntity) te;
					cte.setConsume(!cte.consume());
				}
			});
		}
	}
}
