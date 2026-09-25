package lumien.randomthings.client.screen;

import lumien.randomthings.container.ModContainerTypes;
import net.minecraft.client.gui.ScreenManager;

/**
 * ModScreens
 */
public class ModScreens
{

	public static void register()
	{
		ScreenManager.registerFactory(ModContainerTypes.ADVANCED_REDSTONE_TORCH, AdvancedRedstoneTorchScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.ANALOG_EMITTER, AnalogEmitterScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.IGNITER, IgniterScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.ONLINE_DETECTOR, OnlineDetectorScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.EXTRACTION_PLATE, ExtractionPlateScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.PROCESSING_PLATE, ProcessingPlateScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.ADVANCED_REDSTONE_REPEATER, AdvancedRedstoneRepeaterScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.IRON_DROPPER, IronDropperScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.INVENTORY_TESTER, InventoryTesterScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.CHAT_DETECTOR, ChatDetectorScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.REDSTONE_OBSERVER, RedstoneObserverScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.POTION_VAPORIZER, PotionVaporizerScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.ENTITY_DETECTOR, EntityDetectorScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.FILTERED_REDIRECTOR_PLATE, FilteredRedirectorPlateScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.ADVANCED_ITEM_COLLECTOR, AdvancedItemCollectorScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.FILTERED_SUPER_LUBRICENT_PLATFORM, FilteredSuperLubricentPlatformScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.NOTIFICATION_INTERFACE, NotificationInterfaceScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.GLOBAL_CHAT_DETECTOR, GlobalChatDetectorScreen::new);
		ScreenManager.registerFactory(ModContainerTypes.CHUNK_ANALYZER, ChunkAnalyzerScreen::new);
	}
}
