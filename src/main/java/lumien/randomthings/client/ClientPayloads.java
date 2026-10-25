package lumien.randomthings.client;

import lumien.randomthings.network.IRTMessage;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.BiomeRadarAntennaMessage;
import lumien.randomthings.network.messages.EclipsedClockAnimationMessage;
import lumien.randomthings.network.messages.NotificationMessage;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Client side handlers of the clientbound messages (NeoForge 21.9+ registers them apart from the payload type). */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientPayloads
{
	@SubscribeEvent
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void register(RegisterClientPayloadHandlersEvent event)
	{
		for (Class<? extends IRTMessage> message : new Class[] { NotificationMessage.class, BiomeRadarAntennaMessage.class, EclipsedClockAnimationMessage.class })
		{
			CustomPacketPayload.Type type = RTPacketHandler.typeOf(message);
			event.register(type, (payload, context) -> ((IRTMessage) payload).handle(context));
		}
	}
}
