package lumien.randomthings.container;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("randomthings")
public class ModContainerTypes {
    @ObjectHolder("advanced_redstone_torch")
    public static MenuType<AdvancedRedstoneTorchContainer> ADVANCED_REDSTONE_TORCH;

    @ObjectHolder("analog_emitter")
    public static MenuType<AnalogEmitterContainer> ANALOG_EMITTER;

    @ObjectHolder("igniter")
    public static MenuType<IgniterContainer> IGNITER;

    @ObjectHolder("online_detector")
    public static MenuType<OnlineDetectorContainer> ONLINE_DETECTOR;

    @ObjectHolder("advanced_redstone_repeater")
    public static MenuType<AdvancedRedstoneRepeaterContainer> ADVANCED_REDSTONE_REPEATER;

    @ObjectHolder("iron_dropper")
    public static MenuType<IronDropperContainer> IRON_DROPPER;

    @ObjectHolder("inventory_tester")
    public static MenuType<InventoryTesterContainer> INVENTORY_TESTER;

    @ObjectHolder("chat_detector")
    public static MenuType<ChatDetectorContainer> CHAT_DETECTOR;

    @ObjectHolder("redstone_observer")
    public static MenuType<RedstoneObserverContainer> REDSTONE_OBSERVER;

    @ObjectHolder("basic_redstone_interface")
    public static MenuType<BasicRedstoneInterfaceContainer> BASIC_REDSTONE_INTERFACE;

    @ObjectHolder("advanced_redstone_interface")
    public static MenuType<AdvancedRedstoneInterfaceContainer> ADVANCED_REDSTONE_INTERFACE;

    @ObjectHolder("redstone_remote_edit")
    public static MenuType<RedstoneRemoteEditContainer> REDSTONE_REMOTE_EDIT;

    @ObjectHolder("redstone_remote_use")
    public static MenuType<RedstoneRemoteUseContainer> REDSTONE_REMOTE_USE;

    @ObjectHolder("potion_vaporizer")
    public static MenuType<PotionVaporizerContainer> POTION_VAPORIZER;

    @ObjectHolder("entity_detector")
    public static MenuType<EntityDetectorContainer> ENTITY_DETECTOR;

    @ObjectHolder("advanced_item_collector")
    public static MenuType<AdvancedItemCollectorContainer> ADVANCED_ITEM_COLLECTOR;

    @ObjectHolder("filtered_super_lubricent_platform")
    public static MenuType<FilteredSuperLubricentPlatformContainer> FILTERED_SUPER_LUBRICENT_PLATFORM;

    @ObjectHolder("notification_interface")
    public static MenuType<NotificationInterfaceContainer> NOTIFICATION_INTERFACE;

    @ObjectHolder("global_chat_detector")
    public static MenuType<GlobalChatDetectorContainer> GLOBAL_CHAT_DETECTOR;

    @ObjectHolder("chunk_analyzer")
    public static MenuType<ChunkAnalyzerContainer> CHUNK_ANALYZER;

    @ObjectHolder("sound_recorder")
    public static MenuType<SoundRecorderContainer> SOUND_RECORDER;

    @ObjectHolder("sound_dampener")
    public static MenuType<SoundDampenerContainer> SOUND_DAMPENER;

    @ObjectHolder("portable_sound_dampener")
    public static MenuType<PortableSoundDampenerContainer> PORTABLE_SOUND_DAMPENER;

    @ObjectHolder("imbuing_station")
    public static MenuType<ImbuingStationContainer> IMBUING_STATION;

    @ObjectHolder("ender_letter")
    public static MenuType<EnderLetterContainer> ENDER_LETTER;

    @ObjectHolder("ender_mailbox")
    public static MenuType<EnderMailboxContainer> ENDER_MAILBOX;

    @ObjectHolder("block_destabilizer")
    public static MenuType<BlockDestabilizerContainer> BLOCK_DESTABILIZER;

    @ObjectHolder("dyeing_machine")
    public static MenuType<DyeingMachineContainer> DYEING_MACHINE;

    public static void registerContainerTypes(Register<MenuType<?>> containerTypeRegistryEvent) {
        IForgeRegistry<MenuType<?>> registry = containerTypeRegistryEvent.getRegistry();

        registry.register(IForgeMenuType.create(AdvancedRedstoneTorchContainer::new).setRegistryName("advanced_redstone_torch"));
        registry.register(IForgeMenuType.create(AnalogEmitterContainer::new).setRegistryName("analog_emitter"));
        registry.register(IForgeMenuType.create(IgniterContainer::new).setRegistryName("igniter"));
        registry.register(IForgeMenuType.create(OnlineDetectorContainer::new).setRegistryName("online_detector"));
        registry.register(IForgeMenuType.create(AdvancedRedstoneRepeaterContainer::new).setRegistryName("advanced_redstone_repeater"));
        registry.register(IForgeMenuType.create(IronDropperContainer::new).setRegistryName("iron_dropper"));
        registry.register(IForgeMenuType.create(InventoryTesterContainer::new).setRegistryName("inventory_tester"));
        registry.register(IForgeMenuType.create(ChatDetectorContainer::new).setRegistryName("chat_detector"));
        registry.register(IForgeMenuType.create(RedstoneObserverContainer::new).setRegistryName("redstone_observer"));
        registry.register(IForgeMenuType.create(BasicRedstoneInterfaceContainer::new).setRegistryName("basic_redstone_interface"));
        registry.register(IForgeMenuType.create(AdvancedRedstoneInterfaceContainer::new).setRegistryName("advanced_redstone_interface"));
        registry.register(IForgeMenuType.create(RedstoneRemoteEditContainer::new).setRegistryName("redstone_remote_edit"));
        registry.register(IForgeMenuType.create(RedstoneRemoteUseContainer::new).setRegistryName("redstone_remote_use"));
        registry.register(IForgeMenuType.create(PotionVaporizerContainer::new).setRegistryName("potion_vaporizer"));
        registry.register(IForgeMenuType.create(EntityDetectorContainer::new).setRegistryName("entity_detector"));
        registry.register(IForgeMenuType.create(AdvancedItemCollectorContainer::new).setRegistryName("advanced_item_collector"));
        registry.register(IForgeMenuType.create(FilteredSuperLubricentPlatformContainer::new).setRegistryName("filtered_super_lubricent_platform"));
        registry.register(IForgeMenuType.create(NotificationInterfaceContainer::new).setRegistryName("notification_interface"));
        registry.register(IForgeMenuType.create(GlobalChatDetectorContainer::new).setRegistryName("global_chat_detector"));
        registry.register(IForgeMenuType.create(ChunkAnalyzerContainer::new).setRegistryName("chunk_analyzer"));
        registry.register(IForgeMenuType.create(SoundRecorderContainer::new).setRegistryName("sound_recorder"));
        registry.register(IForgeMenuType.create(SoundDampenerContainer::new).setRegistryName("sound_dampener"));
        registry.register(IForgeMenuType.create(PortableSoundDampenerContainer::new).setRegistryName("portable_sound_dampener"));
        registry.register(IForgeMenuType.create(ImbuingStationContainer::new).setRegistryName("imbuing_station"));
        registry.register(IForgeMenuType.create(EnderLetterContainer::new).setRegistryName("ender_letter"));
        registry.register(IForgeMenuType.create(EnderMailboxContainer::new).setRegistryName("ender_mailbox"));
        registry.register(IForgeMenuType.create(BlockDestabilizerContainer::new).setRegistryName("block_destabilizer"));
        registry.register(IForgeMenuType.create(DyeingMachineContainer::new).setRegistryName("dyeing_machine"));
    }
}
