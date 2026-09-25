package lumien.randomthings.tileentity;

import java.util.function.Supplier;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("randomthings")
public class ModTileEntityTypes
{
	@ObjectHolder("advanced_redstone_torch")
	public static TileEntityType<AdvancedRedstoneTorchTileEntity> ADVANCED_REDSTONE_TORCH;

	@ObjectHolder("blood_rose")
	public static TileEntityType<BloodRoseTileEntity> BLOOD_ROSE;

	@ObjectHolder("analog_emitter")
	public static TileEntityType<AnalogEmitterTileEntity> ANALOG_EMITTER;

	@ObjectHolder("igniter")
	public static TileEntityType<IgniterTileEntity> IGNITER;

	@ObjectHolder("online_detector")
	public static TileEntityType<OnlineDetectorTileEntity> ONLINE_DETECTOR;

	@ObjectHolder("item_collector")
	public static TileEntityType<ItemCollectorTileEntity> ITEM_COLLECTOR;

	@ObjectHolder("plate_extraction")
	public static TileEntityType<ExtractionPlateTileEntity> EXTRACTION_PLATE;

	@ObjectHolder("plate_processing")
	public static TileEntityType<ProcessingPlateTileEntity> PROCESSING_PLATE;

	@ObjectHolder("advanced_redstone_repeater")
	public static TileEntityType<AdvancedRedstoneRepeaterTileEntity> ADVANCED_REDSTONE_REPEATER;

	@ObjectHolder("iron_dropper")
	public static TileEntityType<IronDropperTileEntity> IRON_DROPPER;

	@ObjectHolder("player_interface")
	public static TileEntityType<PlayerInterfaceTileEntity> PLAYER_INTERFACE;

	@ObjectHolder("inventory_tester")
	public static TileEntityType<InventoryTesterTileEntity> INVENTORY_TESTER;

	@ObjectHolder("inventory_rerouter")
	public static TileEntityType<InventoryRerouterTileEntity> INVENTORY_REROUTER;

	@ObjectHolder("chat_detector")
	public static TileEntityType<ChatDetectorTileEntity> CHAT_DETECTOR;

	@ObjectHolder("redstone_observer")
	public static TileEntityType<RedstoneObserverTileEntity> REDSTONE_OBSERVER;

	@ObjectHolder("potion_vaporizer")
	public static TileEntityType<PotionVaporizerTileEntity> POTION_VAPORIZER;

	@ObjectHolder("special_chest")
	public static TileEntityType<SpecialChestTileEntity> SPECIAL_CHEST;

	@ObjectHolder("entity_detector")
	public static TileEntityType<EntityDetectorTileEntity> ENTITY_DETECTOR;

	@ObjectHolder("plate_filtered_redirector")
	public static TileEntityType<FilteredRedirectorPlateTileEntity> FILTERED_REDIRECTOR_PLATE;

	@ObjectHolder("slime_cube")
	public static TileEntityType<SlimeCubeTileEntity> SLIME_CUBE;

	@ObjectHolder("advanced_item_collector")
	public static TileEntityType<AdvancedItemCollectorTileEntity> ADVANCED_ITEM_COLLECTOR;

	@ObjectHolder("filtered_super_lubricent_platform")
	public static TileEntityType<FilteredSuperLubricentPlatformTileEntity> FILTERED_SUPER_LUBRICENT_PLATFORM;

	@ObjectHolder("notification_interface")
	public static TileEntityType<NotificationInterfaceTileEntity> NOTIFICATION_INTERFACE;

	@ObjectHolder("global_chat_detector")
	public static TileEntityType<GlobalChatDetectorTileEntity> GLOBAL_CHAT_DETECTOR;

	@ObjectHolder("biome_radar")
	public static TileEntityType<BiomeRadarTileEntity> BIOME_RADAR;


	public static void registerTypes(RegistryEvent.Register<TileEntityType<?>> typeRegistryEvent)
	{
		IForgeRegistry<TileEntityType<?>> registry = typeRegistryEvent.getRegistry();

		registerSimple(registry, "advanced_redstone_torch", AdvancedRedstoneTorchTileEntity::new, ModBlocks.ADVANCED_REDSTONE_TORCH, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH);
		registerSimple(registry, "blood_rose", BloodRoseTileEntity::new, ModBlocks.BLOOD_ROSE);
		registerSimple(registry, "analog_emitter", AnalogEmitterTileEntity::new, ModBlocks.ANALOG_EMITTER);
		registerSimple(registry, "igniter", IgniterTileEntity::new, ModBlocks.IGNITER);
		registerSimple(registry, "online_detector", OnlineDetectorTileEntity::new, ModBlocks.ONLINE_DETECTOR);
		registerSimple(registry, "item_collector", ItemCollectorTileEntity::new, ModBlocks.ITEM_COLLECTOR);
		registerSimple(registry, "plate_extraction", ExtractionPlateTileEntity::new, ModBlocks.PLATE_EXTRACTION);
		registerSimple(registry, "plate_processing", ProcessingPlateTileEntity::new, ModBlocks.PLATE_PROCESSING);
		registerSimple(registry, "advanced_redstone_repeater", AdvancedRedstoneRepeaterTileEntity::new, ModBlocks.ADVANCED_REDSTONE_REPEATER);
		registerSimple(registry, "iron_dropper", IronDropperTileEntity::new, ModBlocks.IRON_DROPPER);
		registerSimple(registry, "player_interface", PlayerInterfaceTileEntity::new, ModBlocks.PLAYER_INTERFACE);
		registerSimple(registry, "inventory_tester", InventoryTesterTileEntity::new, ModBlocks.INVENTORY_TESTER);
		registerSimple(registry, "inventory_rerouter", InventoryRerouterTileEntity::new, ModBlocks.INVENTORY_REROUTER);
		registerSimple(registry, "chat_detector", ChatDetectorTileEntity::new, ModBlocks.CHAT_DETECTOR);
		registerSimple(registry, "redstone_observer", RedstoneObserverTileEntity::new, ModBlocks.REDSTONE_OBSERVER);
		registerSimple(registry, "potion_vaporizer", PotionVaporizerTileEntity::new, ModBlocks.POTION_VAPORIZER);
		registerSimple(registry, "special_chest", SpecialChestTileEntity::new, ModBlocks.SPECIAL_CHEST_NATURE, ModBlocks.SPECIAL_CHEST_WATER);
		registerSimple(registry, "entity_detector", EntityDetectorTileEntity::new, ModBlocks.ENTITY_DETECTOR);
		registerSimple(registry, "plate_filtered_redirector", FilteredRedirectorPlateTileEntity::new, ModBlocks.PLATE_FILTERED_REDIRECTOR);
		registerSimple(registry, "slime_cube", SlimeCubeTileEntity::new, ModBlocks.SLIME_CUBE);
		registerSimple(registry, "advanced_item_collector", AdvancedItemCollectorTileEntity::new, ModBlocks.ADVANCED_ITEM_COLLECTOR);
		registerSimple(registry, "filtered_super_lubricent_platform", FilteredSuperLubricentPlatformTileEntity::new, ModBlocks.FILTERED_SUPER_LUBRICENT_PLATFORM);
		registerSimple(registry, "notification_interface", NotificationInterfaceTileEntity::new, ModBlocks.NOTIFICATION_INTERFACE);
		registerSimple(registry, "global_chat_detector", GlobalChatDetectorTileEntity::new, ModBlocks.GLOBAL_CHAT_DETECTOR);
		registerSimple(registry, "biome_radar", BiomeRadarTileEntity::new, ModBlocks.BIOME_RADAR);
	}

	private static void registerSimple(IForgeRegistry<TileEntityType<?>> registry, String name, Supplier<? extends TileEntity> factoryIn, Block... validBlocks)
	{
		registry.register(TileEntityType.Builder.create(factoryIn, validBlocks).build(null).setRegistryName(name));
	}
}
