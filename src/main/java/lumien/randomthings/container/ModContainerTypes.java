package lumien.randomthings.container;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

public class ModContainerTypes {
    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:advanced_redstone_torch")
    public static MenuType<AdvancedRedstoneTorchContainer> ADVANCED_REDSTONE_TORCH;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:analog_emitter")
    public static MenuType<AnalogEmitterContainer> ANALOG_EMITTER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:igniter")
    public static MenuType<IgniterContainer> IGNITER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:online_detector")
    public static MenuType<OnlineDetectorContainer> ONLINE_DETECTOR;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:advanced_redstone_repeater")
    public static MenuType<AdvancedRedstoneRepeaterContainer> ADVANCED_REDSTONE_REPEATER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:iron_dropper")
    public static MenuType<IronDropperContainer> IRON_DROPPER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:inventory_tester")
    public static MenuType<InventoryTesterContainer> INVENTORY_TESTER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:chat_detector")
    public static MenuType<ChatDetectorContainer> CHAT_DETECTOR;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:redstone_observer")
    public static MenuType<RedstoneObserverContainer> REDSTONE_OBSERVER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:basic_redstone_interface")
    public static MenuType<BasicRedstoneInterfaceContainer> BASIC_REDSTONE_INTERFACE;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:advanced_redstone_interface")
    public static MenuType<AdvancedRedstoneInterfaceContainer> ADVANCED_REDSTONE_INTERFACE;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:redstone_remote_edit")
    public static MenuType<RedstoneRemoteEditContainer> REDSTONE_REMOTE_EDIT;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:redstone_remote_use")
    public static MenuType<RedstoneRemoteUseContainer> REDSTONE_REMOTE_USE;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:potion_vaporizer")
    public static MenuType<PotionVaporizerContainer> POTION_VAPORIZER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:entity_detector")
    public static MenuType<EntityDetectorContainer> ENTITY_DETECTOR;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:advanced_item_collector")
    public static MenuType<AdvancedItemCollectorContainer> ADVANCED_ITEM_COLLECTOR;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:filtered_super_lubricent_platform")
    public static MenuType<FilteredSuperLubricentPlatformContainer> FILTERED_SUPER_LUBRICENT_PLATFORM;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:notification_interface")
    public static MenuType<NotificationInterfaceContainer> NOTIFICATION_INTERFACE;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:global_chat_detector")
    public static MenuType<GlobalChatDetectorContainer> GLOBAL_CHAT_DETECTOR;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:chunk_analyzer")
    public static MenuType<ChunkAnalyzerContainer> CHUNK_ANALYZER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:sound_recorder")
    public static MenuType<SoundRecorderContainer> SOUND_RECORDER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:sound_dampener")
    public static MenuType<SoundDampenerContainer> SOUND_DAMPENER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:portable_sound_dampener")
    public static MenuType<PortableSoundDampenerContainer> PORTABLE_SOUND_DAMPENER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:imbuing_station")
    public static MenuType<ImbuingStationContainer> IMBUING_STATION;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:ender_letter")
    public static MenuType<EnderLetterContainer> ENDER_LETTER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:ender_mailbox")
    public static MenuType<EnderMailboxContainer> ENDER_MAILBOX;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:block_destabilizer")
    public static MenuType<BlockDestabilizerContainer> BLOCK_DESTABILIZER;

    @ObjectHolder(registryName = "minecraft:menu", value = "randomthings:dyeing_machine")
    public static MenuType<DyeingMachineContainer> DYEING_MACHINE;

    public static void registerContainerTypes(RTRegistry<MenuType<?>> registry) {

        registry.register("advanced_redstone_torch", IMenuTypeExtension.create(AdvancedRedstoneTorchContainer::new));
        registry.register("analog_emitter", IMenuTypeExtension.create(AnalogEmitterContainer::new));
        registry.register("igniter", IMenuTypeExtension.create(IgniterContainer::new));
        registry.register("online_detector", IMenuTypeExtension.create(OnlineDetectorContainer::new));
        registry.register("advanced_redstone_repeater", IMenuTypeExtension.create(AdvancedRedstoneRepeaterContainer::new));
        registry.register("iron_dropper", IMenuTypeExtension.create(IronDropperContainer::new));
        registry.register("inventory_tester", IMenuTypeExtension.create(InventoryTesterContainer::new));
        registry.register("chat_detector", IMenuTypeExtension.create(ChatDetectorContainer::new));
        registry.register("redstone_observer", IMenuTypeExtension.create(RedstoneObserverContainer::new));
        registry.register("basic_redstone_interface", IMenuTypeExtension.create(BasicRedstoneInterfaceContainer::new));
        registry.register("advanced_redstone_interface", IMenuTypeExtension.create(AdvancedRedstoneInterfaceContainer::new));
        registry.register("redstone_remote_edit", IMenuTypeExtension.create(RedstoneRemoteEditContainer::new));
        registry.register("redstone_remote_use", IMenuTypeExtension.create(RedstoneRemoteUseContainer::new));
        registry.register("potion_vaporizer", IMenuTypeExtension.create(PotionVaporizerContainer::new));
        registry.register("entity_detector", IMenuTypeExtension.create(EntityDetectorContainer::new));
        registry.register("advanced_item_collector", IMenuTypeExtension.create(AdvancedItemCollectorContainer::new));
        registry.register("filtered_super_lubricent_platform", IMenuTypeExtension.create(FilteredSuperLubricentPlatformContainer::new));
        registry.register("notification_interface", IMenuTypeExtension.create(NotificationInterfaceContainer::new));
        registry.register("global_chat_detector", IMenuTypeExtension.create(GlobalChatDetectorContainer::new));
        registry.register("chunk_analyzer", IMenuTypeExtension.create(ChunkAnalyzerContainer::new));
        registry.register("sound_recorder", IMenuTypeExtension.create(SoundRecorderContainer::new));
        registry.register("sound_dampener", IMenuTypeExtension.create(SoundDampenerContainer::new));
        registry.register("portable_sound_dampener", IMenuTypeExtension.create(PortableSoundDampenerContainer::new));
        registry.register("imbuing_station", IMenuTypeExtension.create(ImbuingStationContainer::new));
        registry.register("ender_letter", IMenuTypeExtension.create(EnderLetterContainer::new));
        registry.register("ender_mailbox", IMenuTypeExtension.create(EnderMailboxContainer::new));
        registry.register("block_destabilizer", IMenuTypeExtension.create(BlockDestabilizerContainer::new));
        registry.register("dyeing_machine", IMenuTypeExtension.create(DyeingMachineContainer::new));
    }
}
