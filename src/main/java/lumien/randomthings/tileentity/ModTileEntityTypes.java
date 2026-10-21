package lumien.randomthings.tileentity;


import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

public class ModTileEntityTypes
{
	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:advanced_redstone_torch")
	public static BlockEntityType<AdvancedRedstoneTorchTileEntity> ADVANCED_REDSTONE_TORCH;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:analog_emitter")
	public static BlockEntityType<AnalogEmitterTileEntity> ANALOG_EMITTER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:igniter")
	public static BlockEntityType<IgniterTileEntity> IGNITER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:online_detector")
	public static BlockEntityType<OnlineDetectorTileEntity> ONLINE_DETECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:item_collector")
	public static BlockEntityType<ItemCollectorTileEntity> ITEM_COLLECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:advanced_redstone_repeater")
	public static BlockEntityType<AdvancedRedstoneRepeaterTileEntity> ADVANCED_REDSTONE_REPEATER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:iron_dropper")
	public static BlockEntityType<IronDropperTileEntity> IRON_DROPPER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:player_interface")
	public static BlockEntityType<PlayerInterfaceTileEntity> PLAYER_INTERFACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:inventory_tester")
	public static BlockEntityType<InventoryTesterTileEntity> INVENTORY_TESTER;


	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:chat_detector")
	public static BlockEntityType<ChatDetectorTileEntity> CHAT_DETECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:redstone_observer")
	public static BlockEntityType<RedstoneObserverTileEntity> REDSTONE_OBSERVER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:basic_redstone_interface")
	public static BlockEntityType<lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity> BASIC_REDSTONE_INTERFACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:advanced_redstone_interface")
	public static BlockEntityType<lumien.randomthings.tileentity.redstoneinterface.AdvancedRedstoneInterfaceTileEntity> ADVANCED_REDSTONE_INTERFACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:potion_vaporizer")
	public static BlockEntityType<PotionVaporizerTileEntity> POTION_VAPORIZER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:entity_detector")
	public static BlockEntityType<EntityDetectorTileEntity> ENTITY_DETECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:slime_cube")
	public static BlockEntityType<SlimeCubeTileEntity> SLIME_CUBE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:advanced_item_collector")
	public static BlockEntityType<AdvancedItemCollectorTileEntity> ADVANCED_ITEM_COLLECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:filtered_super_lubricent_platform")
	public static BlockEntityType<FilteredSuperLubricentPlatformTileEntity> FILTERED_SUPER_LUBRICENT_PLATFORM;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:notification_interface")
	public static BlockEntityType<NotificationInterfaceTileEntity> NOTIFICATION_INTERFACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:global_chat_detector")
	public static BlockEntityType<GlobalChatDetectorTileEntity> GLOBAL_CHAT_DETECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:biome_radar")
	public static BlockEntityType<BiomeRadarTileEntity> BIOME_RADAR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:sound_box")
	public static BlockEntityType<SoundBoxTileEntity> SOUND_BOX;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:sound_dampener")
	public static BlockEntityType<SoundDampenerTileEntity> SOUND_DAMPENER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:rune_base")
	public static BlockEntityType<RuneBaseTileEntity> RUNE_BASE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:imbuing_station")
	public static BlockEntityType<ImbuingStationTileEntity> IMBUING_STATION;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:floo_brick")
	public static BlockEntityType<FlooBrickTileEntity> FLOO_BRICK;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:ender_mailbox")
	public static BlockEntityType<EnderMailboxTileEntity> ENDER_MAILBOX;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:fluid_display")
	public static BlockEntityType<FluidDisplayTileEntity> FLUID_DISPLAY;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:creative_player_interface")
	public static BlockEntityType<CreativePlayerInterfaceTileEntity> CREATIVE_PLAYER_INTERFACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:ender_bridge")
	public static BlockEntityType<EnderBridgeTileEntity> ENDER_BRIDGE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:prismarine_ender_bridge")
	public static BlockEntityType<PrismarineEnderBridgeTileEntity> PRISMARINE_ENDER_BRIDGE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:ender_anchor")
	public static BlockEntityType<EnderAnchorTileEntity> ENDER_ANCHOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:rain_shield")
	public static BlockEntityType<RainShieldTileEntity> RAIN_SHIELD;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:peace_candle")
	public static BlockEntityType<PeaceCandleTileEntity> PEACE_CANDLE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:ancient_furnace")
	public static BlockEntityType<AncientFurnaceTileEntity> ANCIENT_FURNACE;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:block_breaker")
	public static BlockEntityType<BlockBreakerTileEntity> BLOCK_BREAKER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:block_destabilizer")
	public static BlockEntityType<BlockDestabilizerTileEntity> BLOCK_DESTABILIZER;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:spectre_energy_injector")
	public static BlockEntityType<SpectreEnergyInjectorTileEntity> SPECTRE_ENERGY_INJECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:spectre_coil")
	public static BlockEntityType<SpectreCoilTileEntity> SPECTRE_COIL;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:spectre_lens")
	public static BlockEntityType<SpectreLensTileEntity> SPECTRE_LENS;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:light_redirector")
	public static BlockEntityType<LightRedirectorTileEntity> LIGHT_REDIRECTOR;

	@ObjectHolder(registryName = "minecraft:block_entity_type", value = "randomthings:diaphanous_block")
	public static BlockEntityType<DiaphanousBlockTileEntity> DIAPHANOUS_BLOCK;


	public static void registerTypes(RTRegistry<BlockEntityType<?>> registry)
	{

		registerSimple(registry, "advanced_redstone_torch", AdvancedRedstoneTorchTileEntity::new, ModBlocks.ADVANCED_REDSTONE_TORCH, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH);
		registerSimple(registry, "analog_emitter", AnalogEmitterTileEntity::new, ModBlocks.ANALOG_EMITTER);
		registerSimple(registry, "igniter", IgniterTileEntity::new, ModBlocks.IGNITER);
		registerSimple(registry, "online_detector", OnlineDetectorTileEntity::new, ModBlocks.ONLINE_DETECTOR);
		registerSimple(registry, "item_collector", ItemCollectorTileEntity::new, ModBlocks.ITEM_COLLECTOR);
		registerSimple(registry, "advanced_redstone_repeater", AdvancedRedstoneRepeaterTileEntity::new, ModBlocks.ADVANCED_REDSTONE_REPEATER);
		registerSimple(registry, "iron_dropper", IronDropperTileEntity::new, ModBlocks.IRON_DROPPER);
		registerSimple(registry, "player_interface", PlayerInterfaceTileEntity::new, ModBlocks.PLAYER_INTERFACE);
		registerSimple(registry, "inventory_tester", InventoryTesterTileEntity::new, ModBlocks.INVENTORY_TESTER);
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

	private static void registerSimple(RTRegistry<BlockEntityType<?>> registry, String name, BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factoryIn, Block... validBlocks)
	{
		registry.register(name, BlockEntityType.Builder.of(factoryIn, validBlocks).build(null));
	}
}
