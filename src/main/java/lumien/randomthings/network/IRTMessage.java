package lumien.randomthings.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

/**
 * IRTMessage
 */
public interface IRTMessage
{
	public void read(FriendlyByteBuf pb);

	public void write(FriendlyByteBuf pb);

	public default void enqueue(Supplier<Context> sup)
	{
		Context ctx = sup.get();
		ctx.enqueueWork(() -> handle(ctx));
	}

	public void handle(Context ctx);
}