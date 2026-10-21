package lumien.randomthings.network.messages;

import io.netty.buffer.Unpooled;
import lumien.randomthings.container.ISignalContainer;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * ContainerSignalMessage
 */
public class ContainerSignalMessage implements IRTMessage
{
	int id;
	FriendlyByteBuf data;

	public ContainerSignalMessage(int id, FriendlyByteBuf data)
	{
		this.id = id;
		this.data = data;
	}

	public ContainerSignalMessage()
	{

	}

	@Override
	public void read(FriendlyByteBuf pb)
	{
		this.id = pb.readInt();

		byte[] byteArray = pb.readByteArray();
		this.data = new FriendlyByteBuf(Unpooled.copiedBuffer(byteArray));
	}

	@Override
	public void write(FriendlyByteBuf pb)
	{
		pb.writeInt(id);
		pb.writeByteArray(data.array());
	}

	@Override
	public void handle(IPayloadContext context)
	{
		ServerPlayer player = (ServerPlayer) context.player();

		AbstractContainerMenu container = player.containerMenu;

		if (container instanceof ISignalContainer)
		{
			((ISignalContainer) container).handle(id, data);
		}
	}


}