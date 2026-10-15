package lumien.randomthings.container;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import net.minecraft.network.FriendlyByteBuf;

/**
 * ISignalContainer
 */
public interface ISignalContainer
{
	public void handle(int id, FriendlyByteBuf data);

	public default void send(int id, Consumer<FriendlyByteBuf> consumer)
	{
		FriendlyByteBuf pb = new FriendlyByteBuf(Unpooled.buffer());
		consumer.accept(pb);
		RTPacketHandler.sendToServer(new ContainerSignalMessage(id, pb));
	}
}