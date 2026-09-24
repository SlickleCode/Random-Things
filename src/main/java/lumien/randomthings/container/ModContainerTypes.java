package lumien.randomthings.container;

import lumien.randomthings.tileentity.AdvancedRedstoneTorchTileEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("randomthings")
public class ModContainerTypes
{
	@ObjectHolder("advanced_redstone_torch")
	public static ContainerType<AdvancedRedstoneTorchContainer> ADVANCED_REDSTONE_TORCH;

	@ObjectHolder("analog_emitter")
	public static ContainerType<AnalogEmitterContainer> ANALOG_EMITTER;

	@ObjectHolder("igniter")
	public static ContainerType<IgniterContainer> IGNITER;

	@ObjectHolder("online_detector")
	public static ContainerType<OnlineDetectorContainer> ONLINE_DETECTOR;

	@ObjectHolder("plate_extraction")
	public static ContainerType<ExtractionPlateContainer> EXTRACTION_PLATE;

	@ObjectHolder("plate_processing")
	public static ContainerType<ProcessingPlateContainer> PROCESSING_PLATE;

	@ObjectHolder("advanced_redstone_repeater")
	public static ContainerType<AdvancedRedstoneRepeaterContainer> ADVANCED_REDSTONE_REPEATER;

	@ObjectHolder("iron_dropper")
	public static ContainerType<IronDropperContainer> IRON_DROPPER;

	@ObjectHolder("inventory_tester")
	public static ContainerType<InventoryTesterContainer> INVENTORY_TESTER;

	@ObjectHolder("chat_detector")
	public static ContainerType<ChatDetectorContainer> CHAT_DETECTOR;

	@ObjectHolder("redstone_observer")
	public static ContainerType<RedstoneObserverContainer> REDSTONE_OBSERVER;

	@ObjectHolder("potion_vaporizer")
	public static ContainerType<PotionVaporizerContainer> POTION_VAPORIZER;

	@ObjectHolder("entity_detector")
	public static ContainerType<EntityDetectorContainer> ENTITY_DETECTOR;

	@ObjectHolder("plate_filtered_redirector")
	public static ContainerType<FilteredRedirectorPlateContainer> FILTERED_REDIRECTOR_PLATE;

	@ObjectHolder("advanced_item_collector")
	public static ContainerType<AdvancedItemCollectorContainer> ADVANCED_ITEM_COLLECTOR;

	@ObjectHolder("filtered_super_lubricent_platform")
	public static ContainerType<FilteredSuperLubricentPlatformContainer> FILTERED_SUPER_LUBRICENT_PLATFORM;

	@ObjectHolder("notification_interface")
	public static ContainerType<NotificationInterfaceContainer> NOTIFICATION_INTERFACE;

	@ObjectHolder("global_chat_detector")
	public static ContainerType<GlobalChatDetectorContainer> GLOBAL_CHAT_DETECTOR;

	public static void registerContainerTypes(Register<ContainerType<?>> containerTypeRegistryEvent)
	{
		IForgeRegistry<ContainerType<?>> registry = containerTypeRegistryEvent.getRegistry();

		registry.register(IForgeContainerType.create(AdvancedRedstoneTorchContainer::new).setRegistryName("advanced_redstone_torch"));
		registry.register(IForgeContainerType.create(AnalogEmitterContainer::new).setRegistryName("analog_emitter"));
		registry.register(IForgeContainerType.create(IgniterContainer::new).setRegistryName("igniter"));
		registry.register(IForgeContainerType.create(OnlineDetectorContainer::new).setRegistryName("online_detector"));
		registry.register(IForgeContainerType.create(ExtractionPlateContainer::new).setRegistryName("plate_extraction"));
		registry.register(IForgeContainerType.create(ProcessingPlateContainer::new).setRegistryName("plate_processing"));
		registry.register(IForgeContainerType.create(AdvancedRedstoneRepeaterContainer::new).setRegistryName("advanced_redstone_repeater"));
		registry.register(IForgeContainerType.create(IronDropperContainer::new).setRegistryName("iron_dropper"));
		registry.register(IForgeContainerType.create(InventoryTesterContainer::new).setRegistryName("inventory_tester"));
		registry.register(IForgeContainerType.create(ChatDetectorContainer::new).setRegistryName("chat_detector"));
		registry.register(IForgeContainerType.create(RedstoneObserverContainer::new).setRegistryName("redstone_observer"));
		registry.register(IForgeContainerType.create(PotionVaporizerContainer::new).setRegistryName("potion_vaporizer"));
		registry.register(IForgeContainerType.create(EntityDetectorContainer::new).setRegistryName("entity_detector"));
		registry.register(IForgeContainerType.create(FilteredRedirectorPlateContainer::new).setRegistryName("plate_filtered_redirector"));
		registry.register(IForgeContainerType.create(AdvancedItemCollectorContainer::new).setRegistryName("advanced_item_collector"));
		registry.register(IForgeContainerType.create(FilteredSuperLubricentPlatformContainer::new).setRegistryName("filtered_super_lubricent_platform"));
		registry.register(IForgeContainerType.create(NotificationInterfaceContainer::new).setRegistryName("notification_interface"));
		registry.register(IForgeContainerType.create(GlobalChatDetectorContainer::new).setRegistryName("global_chat_detector"));
	}
}
