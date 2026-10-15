package lumien.randomthings.network;

import org.apache.logging.log4j.LogManager;
import lumien.randomthings.network.messages.BiomeRadarAntennaMessage;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import lumien.randomthings.network.messages.NotificationMessage;
import lumien.randomthings.network.messages.PlayedSoundMessage;
import lumien.randomthings.network.messages.RedstoneRemoteActivateMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.PacketDistributor.TargetPoint;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * RTPacketHandler
 */
public class RTPacketHandler
{
	private static final String PROTOCOL_VERSION = "1";
	private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder.named(new ResourceLocation("randomthings", "main_channel")).clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

	public static void register()
	{
		int disc = 0;

		register(disc++, ContainerSignalMessage.class);
		register(disc++, NotificationMessage.class);
		register(disc++, BiomeRadarAntennaMessage.class);
		register(disc++, PlayedSoundMessage.class);
		register(disc++, RedstoneRemoteActivateMessage.class);
	}

	public static void sendTo(IRTMessage message, ServerPlayer player)
	{
		HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	public static void sendToServer(IRTMessage message)
	{
		HANDLER.sendToServer(message);
	}

	public static void sendToAllNear(IRTMessage message, Level world, BlockPos pos, float radius)
	{
		PacketTarget target = PacketDistributor.NEAR.with(() -> new TargetPoint(pos.getX(), pos.getY(), pos.getZ(), radius, world.dimension()));
		HANDLER.send(target, message);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void register(int id, Class messageClass)
	{
		HANDLER.registerMessage(id, messageClass, (msg, pb) -> msg.write(pb), (pb) -> {
			IRTMessage instance = null;
			try
			{
				instance = (IRTMessage) messageClass.newInstance();
				instance.read(pb);
			}
			catch (InstantiationException e)
			{
				LogManager.getLogger().error("Failed to instantiate network message", e);
			}
			catch (IllegalAccessException e)
			{
				LogManager.getLogger().error("Failed to instantiate network message", e);
			}

			return instance;
		}, (msg, ctx) -> msg.enqueue(ctx));
	}
}