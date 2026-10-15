package lumien.randomthings.client.screen;

import lumien.randomthings.container.ModContainerTypes;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * ModScreens
 */
public class ModScreens
{

	public static void register()
	{
		MenuScreens.register(ModContainerTypes.ADVANCED_REDSTONE_TORCH, AdvancedRedstoneTorchScreen::new);
		MenuScreens.register(ModContainerTypes.ANALOG_EMITTER, AnalogEmitterScreen::new);
		MenuScreens.register(ModContainerTypes.IGNITER, IgniterScreen::new);
		MenuScreens.register(ModContainerTypes.ONLINE_DETECTOR, OnlineDetectorScreen::new);
		MenuScreens.register(ModContainerTypes.ADVANCED_REDSTONE_REPEATER, AdvancedRedstoneRepeaterScreen::new);
		MenuScreens.register(ModContainerTypes.IRON_DROPPER, IronDropperScreen::new);
		MenuScreens.register(ModContainerTypes.INVENTORY_TESTER, InventoryTesterScreen::new);
		MenuScreens.register(ModContainerTypes.CHAT_DETECTOR, ChatDetectorScreen::new);
		MenuScreens.register(ModContainerTypes.REDSTONE_OBSERVER, RedstoneObserverScreen::new);
		MenuScreens.register(ModContainerTypes.BASIC_REDSTONE_INTERFACE, BasicRedstoneInterfaceScreen::new);
		MenuScreens.register(ModContainerTypes.ADVANCED_REDSTONE_INTERFACE, AdvancedRedstoneInterfaceScreen::new);
		MenuScreens.register(ModContainerTypes.REDSTONE_REMOTE_EDIT, RedstoneRemoteEditScreen::new);
		MenuScreens.register(ModContainerTypes.REDSTONE_REMOTE_USE, RedstoneRemoteUseScreen::new);
		MenuScreens.register(ModContainerTypes.POTION_VAPORIZER, PotionVaporizerScreen::new);
		MenuScreens.register(ModContainerTypes.ENTITY_DETECTOR, EntityDetectorScreen::new);
		MenuScreens.register(ModContainerTypes.ADVANCED_ITEM_COLLECTOR, AdvancedItemCollectorScreen::new);
		MenuScreens.register(ModContainerTypes.FILTERED_SUPER_LUBRICENT_PLATFORM, FilteredSuperLubricentPlatformScreen::new);
		MenuScreens.register(ModContainerTypes.NOTIFICATION_INTERFACE, NotificationInterfaceScreen::new);
		MenuScreens.register(ModContainerTypes.GLOBAL_CHAT_DETECTOR, GlobalChatDetectorScreen::new);
		MenuScreens.register(ModContainerTypes.CHUNK_ANALYZER, ChunkAnalyzerScreen::new);
		MenuScreens.register(ModContainerTypes.SOUND_RECORDER, SoundRecorderScreen::new);
		MenuScreens.register(ModContainerTypes.SOUND_DAMPENER, SoundDampenerScreen::new);
		MenuScreens.register(ModContainerTypes.PORTABLE_SOUND_DAMPENER, PortableSoundDampenerScreen::new);
		MenuScreens.register(ModContainerTypes.IMBUING_STATION, ImbuingStationScreen::new);
		MenuScreens.register(ModContainerTypes.ENDER_LETTER, EnderLetterScreen::new);
		MenuScreens.register(ModContainerTypes.ENDER_MAILBOX, EnderMailboxScreen::new);
		MenuScreens.register(ModContainerTypes.BLOCK_DESTABILIZER, BlockDestabilizerScreen::new);
		MenuScreens.register(ModContainerTypes.DYEING_MACHINE, DyeingMachineScreen::new);
	}
}
