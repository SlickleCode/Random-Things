package lumien.randomthings.client.screen;

import lumien.randomthings.container.ModContainerTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * ModScreens
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModScreens
{

	@SubscribeEvent
	public static void register(RegisterMenuScreensEvent event)
	{
		event.register(ModContainerTypes.ADVANCED_REDSTONE_TORCH, AdvancedRedstoneTorchScreen::new);
		event.register(ModContainerTypes.ANALOG_EMITTER, AnalogEmitterScreen::new);
		event.register(ModContainerTypes.IGNITER, IgniterScreen::new);
		event.register(ModContainerTypes.ONLINE_DETECTOR, OnlineDetectorScreen::new);
		event.register(ModContainerTypes.ADVANCED_REDSTONE_REPEATER, AdvancedRedstoneRepeaterScreen::new);
		event.register(ModContainerTypes.IRON_DROPPER, IronDropperScreen::new);
		event.register(ModContainerTypes.INVENTORY_TESTER, InventoryTesterScreen::new);
		event.register(ModContainerTypes.CHAT_DETECTOR, ChatDetectorScreen::new);
		event.register(ModContainerTypes.REDSTONE_OBSERVER, RedstoneObserverScreen::new);
		event.register(ModContainerTypes.BASIC_REDSTONE_INTERFACE, BasicRedstoneInterfaceScreen::new);
		event.register(ModContainerTypes.ADVANCED_REDSTONE_INTERFACE, AdvancedRedstoneInterfaceScreen::new);
		event.register(ModContainerTypes.REDSTONE_REMOTE_EDIT, RedstoneRemoteEditScreen::new);
		event.register(ModContainerTypes.REDSTONE_REMOTE_USE, RedstoneRemoteUseScreen::new);
		event.register(ModContainerTypes.POTION_VAPORIZER, PotionVaporizerScreen::new);
		event.register(ModContainerTypes.ENTITY_DETECTOR, EntityDetectorScreen::new);
		event.register(ModContainerTypes.ADVANCED_ITEM_COLLECTOR, AdvancedItemCollectorScreen::new);
		event.register(ModContainerTypes.FILTERED_SUPER_LUBRICENT_PLATFORM, FilteredSuperLubricentPlatformScreen::new);
		event.register(ModContainerTypes.NOTIFICATION_INTERFACE, NotificationInterfaceScreen::new);
		event.register(ModContainerTypes.GLOBAL_CHAT_DETECTOR, GlobalChatDetectorScreen::new);
		event.register(ModContainerTypes.CHUNK_ANALYZER, ChunkAnalyzerScreen::new);
		event.register(ModContainerTypes.SOUND_RECORDER, SoundRecorderScreen::new);
		event.register(ModContainerTypes.SOUND_DAMPENER, SoundDampenerScreen::new);
		event.register(ModContainerTypes.PORTABLE_SOUND_DAMPENER, PortableSoundDampenerScreen::new);
		event.register(ModContainerTypes.IMBUING_STATION, ImbuingStationScreen::new);
		event.register(ModContainerTypes.ENDER_LETTER, EnderLetterScreen::new);
		event.register(ModContainerTypes.ENDER_MAILBOX, EnderMailboxScreen::new);
		event.register(ModContainerTypes.BLOCK_DESTABILIZER, BlockDestabilizerScreen::new);
		event.register(ModContainerTypes.DYEING_MACHINE, DyeingMachineScreen::new);
	}
}
