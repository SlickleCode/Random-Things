package lumien.randomthings.network;

import net.neoforged.fml.common.EventBusSubscriber;
import java.util.HashMap;
import java.util.Map;

import lumien.randomthings.network.messages.BiomeRadarAntennaMessage;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import lumien.randomthings.network.messages.EclipsedClockAnimationMessage;
import lumien.randomthings.network.messages.NotificationMessage;
import lumien.randomthings.network.messages.PlayedSoundMessage;
import lumien.randomthings.network.messages.RedstoneRemoteActivateMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * RTPacketHandler
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RTPacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	private static final Map<Class<?>, CustomPacketPayload.Type<? extends CustomPacketPayload>> TYPES = new HashMap<>();

	public static CustomPacketPayload.Type<? extends CustomPacketPayload> typeOf(Class<?> messageClass)
	{
		return TYPES.get(messageClass);
	}

	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

		register(registrar, "container_signal", ContainerSignalMessage.class);
		register(registrar, "notification", NotificationMessage.class);
		register(registrar, "biome_radar_antenna", BiomeRadarAntennaMessage.class);
		register(registrar, "played_sound", PlayedSoundMessage.class);
		register(registrar, "redstone_remote_activate", RedstoneRemoteActivateMessage.class);
		register(registrar, "eclipsed_clock_animation", EclipsedClockAnimationMessage.class);
	}

	public static void sendTo(IRTMessage message, ServerPlayer player)
	{
		PacketDistributor.sendToPlayer(player, message);
	}

	public static void sendToServer(IRTMessage message)
	{
		PacketDistributor.sendToServer(message);
	}

	public static void sendToAllNear(IRTMessage message, Level world, BlockPos pos, float radius)
	{
		if (world instanceof ServerLevel serverLevel)
		{
			PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), radius, message);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void register(PayloadRegistrar registrar, String name, Class messageClass)
	{
		CustomPacketPayload.Type type = new CustomPacketPayload.Type<>(new ResourceLocation("randomthings", name));
		TYPES.put(messageClass, type);

		StreamCodec<RegistryFriendlyByteBuf, IRTMessage> codec = StreamCodec.of((buf, msg) -> msg.write(buf), buf -> {
			try
			{
				IRTMessage instance = (IRTMessage) messageClass.getDeclaredConstructor().newInstance();
				instance.read(buf);
				return instance;
			}
			catch (ReflectiveOperationException e)
			{
				throw new IllegalStateException("Failed to instantiate network message " + messageClass.getName(), e);
			}
		});

		registrar.playBidirectional(type, (StreamCodec) codec, (msg, ctx) -> ((IRTMessage) msg).handle(ctx));
	}
}
