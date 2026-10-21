package lumien.randomthings.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * IRTMessage - one mod network message; wrapped as a {@link CustomPacketPayload} (1.20.5+ networking), the codec is built from {@link #read}/{@link #write}
 * by {@link RTPacketHandler}.
 */
public interface IRTMessage extends CustomPacketPayload
{
	public void read(FriendlyByteBuf pb);

	public void write(FriendlyByteBuf pb);

	public void handle(IPayloadContext ctx);

	@Override
	public default Type<? extends CustomPacketPayload> type()
	{
		return RTPacketHandler.typeOf(getClass());
	}
}
