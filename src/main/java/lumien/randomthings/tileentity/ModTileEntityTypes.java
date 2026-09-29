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

	@ObjectHolder("analog_emitter")
	public static TileEntityType<AnalogEmitterTileEntity> ANALOG_EMITTER;

	@ObjectHolder("igniter")
	public static TileEntityType<IgniterTileEntity> IGNITER;

	@ObjectHolder("online_detector")
	public static TileEntityType<OnlineDetectorTileEntity> ONLINE_DETECTOR;

	@ObjectHolder("item_collector")
	public static TileEntityType<ItemCollectorTileEntity> ITEM_COLLECTOR;

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

	@ObjectHolder("basic_redstone_interface")
	public static TileEntityType<lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity> BASIC_REDSTONE_INTERFACE;

	@ObjectHolder("advanced_redstone_interface")
	public static TileEntityType<lumien.randomthings.tileentity.redstoneinterface.AdvancedRedstoneInterfaceTileEntity> ADVANCED_REDSTONE_INTERFACE;

	@ObjectHolder("potion_vaporizer")
	public static TileEntityType<PotionVaporizerTileEntity> POTION_VAPORIZER;

	@ObjectHolder("entity_detector")
	public static TileEntityType<EntityDetectorTileEntity> ENTITY_DETECTOR;

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

	@ObjectHolder("sound_box")
	public static TileEntityType<SoundBoxTileEntity> SOUND_BOX;

	@ObjectHolder("sound_dampener")
	public static TileEntityType<SoundDampenerTileEntity> SOUND_DAMPENER;

	@ObjectHolder("rune_base")
	public static TileEntityType<RuneBaseTileEntity> RUNE_BASE;

	@ObjectHolder("imbuing_station")
	public static TileEntityType<ImbuingStationTileEntity> IMBUING_STATION;

	@ObjectHolder("floo_brick")
	public static TileEntityType<FlooBrickTileEntity> FLOO_BRICK;

	@ObjectHolder("ender_mailbox")
	public static TileEntityType<EnderMailboxTileEntity> ENDER_MAILBOX;

	@ObjectHolder("fluid_display")
	public static TileEntityType<FluidDisplayTileEntity> FLUID_DISPLAY;

	@ObjectHolder("creative_player_interface")
	public static TileEntityType<CreativePlayerInterfaceTileEntity> CREATIVE_PLAYER_INTERFACE;

	@ObjectHolder("ender_bridge")
	public static TileEntityType<EnderBridgeTileEntity> ENDER_BRIDGE;

	@ObjectHolder("prismarine_ender_bridge")
	public static TileEntityType<PrismarineEnderBridgeTileEntity> PRISMARINE_ENDER_BRIDGE;

	@ObjectHolder("ender_anchor")
	public static TileEntityType<EnderAnchorTileEntity> ENDER_ANCHOR;

	@ObjectHolder("rain_shield")
	public static TileEntityType<RainShieldTileEntity> RAIN_SHIELD;

	@ObjectHolder("peace_candle")
	public static TileEntityType<PeaceCandleTileEntity> PEACE_CANDLE;

	@ObjectHolder("ancient_furnace")
	public static TileEntityType<AncientFurnaceTileEntity> ANCIENT_FURNACE;

	@ObjectHolder("block_breaker")
	public static TileEntityType<BlockBreakerTileEntity> BLOCK_BREAKER;

	@ObjectHolder("block_destabilizer")
	public static TileEntityType<BlockDestabilizerTileEntity> BLOCK_DESTABILIZER;

	@ObjectHolder("spectre_energy_injector")
	public static TileEntityType<SpectreEnergyInjectorTileEntity> SPECTRE_ENERGY_INJECTOR;

	@ObjectHolder("spectre_coil")
	public static TileEntityType<SpectreCoilTileEntity> SPECTRE_COIL;

	@ObjectHolder("spectre_lens")
	public static TileEntityType<SpectreLensTileEntity> SPECTRE_LENS;

	@ObjectHolder("light_redirector")
	public static TileEntityType<LightRedirectorTileEntity> LIGHT_REDIRECTOR;

	@ObjectHolder("diaphanous_block")
	public static TileEntityType<DiaphanousBlockTileEntity> DIAPHANOUS_BLOCK;


	public static void registerTypes(RegistryEvent.Register<TileEntityType<?>> typeRegistryEvent)
	{
		IForgeRegistry<TileEntityType<?>> registry = typeRegistryEvent.getRegistry();

		registerSimple(registry, "advanced_redstone_torch", AdvancedRedstoneTorchTileEntity::new, ModBlocks.ADVANCED_REDSTONE_TORCH, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH);
		registerSimple(registry, "analog_emitter", AnalogEmitterTileEntity::new, ModBlocks.ANALOG_EMITTER);
		registerSimple(registry, "igniter", IgniterTileEntity::new, ModBlocks.IGNITER);
		registerSimple(registry, "online_detector", OnlineDetectorTileEntity::new, ModBlocks.ONLINE_DETECTOR);
		registerSimple(registry, "item_collector", ItemCollectorTileEntity::new, ModBlocks.ITEM_COLLECTOR);
		registerSimple(registry, "advanced_redstone_repeater", AdvancedRedstoneRepeaterTileEntity::new, ModBlocks.ADVANCED_REDSTONE_REPEATER);
		registerSimple(registry, "iron_dropper", IronDropperTileEntity::new, ModBlocks.IRON_DROPPER);
		registerSimple(registry, "player_interface", PlayerInterfaceTileEntity::new, ModBlocks.PLAYER_INTERFACE);
		registerSimple(registry, "inventory_tester", InventoryTesterTileEntity::new, ModBlocks.INVENTORY_TESTER);
		registerSimple(registry, "inventory_rerouter", InventoryRerouterTileEntity::new, ModBlocks.INVENTORY_REROUTER);
		registerSimple(registry, "chat_detector", ChatDetectorTileEntity::new, ModBlocks.CHAT_DETECTOR);
		registerSimple(registry, "redstone_observer", RedstoneObserverTileEntity::new, ModBlocks.REDSTONE_OBSERVER);
		registerSimple(registry, "basic_redstone_interface", lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity::new, ModBlocks.BASIC_REDSTONE_INTERFACE);
		registerSimple(registry, "advanced_redstone_interface", lumien.randomthings.tileentity.redstoneinterface.AdvancedRedstoneInterfaceTileEntity::new, ModBlocks.ADVANCED_REDSTONE_INTERFACE);
		registerSimple(registry, "potion_vaporizer", PotionVaporizerTileEntity::new, ModBlocks.POTION_VAPORIZER);
		registerSimple(registry, "entity_detector", EntityDetectorTileEntity::new, ModBlocks.ENTITY_DETECTOR);
		registerSimple(registry, "slime_cube", SlimeCubeTileEntity::new, ModBlocks.SLIME_CUBE);
		registerSimple(registry, "advanced_item_collector", AdvancedItemCollectorTileEntity::new, ModBlocks.ADVANCED_ITEM_COLLECTOR);
		registerSimple(registry, "filtered_super_lubricent_platform", FilteredSuperLubricentPlatformTileEntity::new, ModBlocks.FILTERED_SUPER_LUBRICENT_PLATFORM);
		registerSimple(registry, "notification_interface", NotificationInterfaceTileEntity::new, ModBlocks.NOTIFICATION_INTERFACE);
		registerSimple(registry, "global_chat_detector", GlobalChatDetectorTileEntity::new, ModBlocks.GLOBAL_CHAT_DETECTOR);
		registerSimple(registry, "biome_radar", BiomeRadarTileEntity::new, ModBlocks.BIOME_RADAR);
		registerSimple(registry, "sound_box", SoundBoxTileEntity::new, ModBlocks.SOUND_BOX);
		registerSimple(registry, "sound_dampener", SoundDampenerTileEntity::new, ModBlocks.SOUND_DAMPENER);
		registerSimple(registry, "rune_base", RuneBaseTileEntity::new, ModBlocks.RUNE_BASE);
		registerSimple(registry, "imbuing_station", ImbuingStationTileEntity::new, ModBlocks.IMBUING_STATION);
		registerSimple(registry, "floo_brick", FlooBrickTileEntity::new, ModBlocks.FLOO_BRICK);
		registerSimple(registry, "ender_mailbox", EnderMailboxTileEntity::new, ModBlocks.ENDER_MAILBOX);
		registerSimple(registry, "fluid_display", FluidDisplayTileEntity::new, ModBlocks.FLUID_DISPLAY);
		registerSimple(registry, "creative_player_interface", CreativePlayerInterfaceTileEntity::new, ModBlocks.CREATIVE_PLAYER_INTERFACE);
		registerSimple(registry, "ender_bridge", EnderBridgeTileEntity::new, ModBlocks.ENDER_BRIDGE);
		registerSimple(registry, "prismarine_ender_bridge", PrismarineEnderBridgeTileEntity::new, ModBlocks.PRISMARINE_ENDER_BRIDGE);
		registerSimple(registry, "ender_anchor", EnderAnchorTileEntity::new, ModBlocks.ENDER_ANCHOR);
		registerSimple(registry, "rain_shield", RainShieldTileEntity::new, ModBlocks.RAIN_SHIELD);
		registerSimple(registry, "peace_candle", PeaceCandleTileEntity::new, ModBlocks.PEACE_CANDLE);
		registerSimple(registry, "ancient_furnace", AncientFurnaceTileEntity::new, ModBlocks.ANCIENT_FURNACE);
		registerSimple(registry, "block_breaker", BlockBreakerTileEntity::new, ModBlocks.BLOCK_BREAKER);
		registerSimple(registry, "block_destabilizer", BlockDestabilizerTileEntity::new, ModBlocks.BLOCK_DESTABILIZER);

		registerSimple(registry, "spectre_energy_injector", SpectreEnergyInjectorTileEntity::new, ModBlocks.SPECTRE_ENERGY_INJECTOR);
		registerSimple(registry, "spectre_coil", SpectreCoilTileEntity::new, ModBlocks.SPECTRE_COIL_NORMAL, ModBlocks.SPECTRE_COIL_REDSTONE, ModBlocks.SPECTRE_COIL_ENDER, ModBlocks.SPECTRE_COIL_GENESIS);
		registerSimple(registry, "spectre_lens", SpectreLensTileEntity::new, ModBlocks.SPECTRE_LENS);

		registerSimple(registry, "light_redirector", LightRedirectorTileEntity::new, ModBlocks.LIGHT_REDIRECTOR);
		registerSimple(registry, "diaphanous_block", DiaphanousBlockTileEntity::new, ModBlocks.DIAPHANOUS_BLOCK);
	}

	private static void registerSimple(IForgeRegistry<TileEntityType<?>> registry, String name, Supplier<? extends TileEntity> factoryIn, Block... validBlocks)
	{
		registry.register(TileEntityType.Builder.create(factoryIn, validBlocks).build(null).setRegistryName(name));
	}
}
