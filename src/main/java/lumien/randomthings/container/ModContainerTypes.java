package lumien.randomthings.container;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

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

    public static void registerContainerTypes(IForgeRegistry<MenuType<?>> registry) {

        registry.register("advanced_redstone_torch", IForgeMenuType.create(AdvancedRedstoneTorchContainer::new));
        registry.register("analog_emitter", IForgeMenuType.create(AnalogEmitterContainer::new));
        registry.register("igniter", IForgeMenuType.create(IgniterContainer::new));
        registry.register("online_detector", IForgeMenuType.create(OnlineDetectorContainer::new));
        registry.register("advanced_redstone_repeater", IForgeMenuType.create(AdvancedRedstoneRepeaterContainer::new));
        registry.register("iron_dropper", IForgeMenuType.create(IronDropperContainer::new));
        registry.register("inventory_tester", IForgeMenuType.create(InventoryTesterContainer::new));
        registry.register("chat_detector", IForgeMenuType.create(ChatDetectorContainer::new));
        registry.register("redstone_observer", IForgeMenuType.create(RedstoneObserverContainer::new));
        registry.register("basic_redstone_interface", IForgeMenuType.create(BasicRedstoneInterfaceContainer::new));
        registry.register("advanced_redstone_interface", IForgeMenuType.create(AdvancedRedstoneInterfaceContainer::new));
        registry.register("redstone_remote_edit", IForgeMenuType.create(RedstoneRemoteEditContainer::new));
        registry.register("redstone_remote_use", IForgeMenuType.create(RedstoneRemoteUseContainer::new));
        registry.register("potion_vaporizer", IForgeMenuType.create(PotionVaporizerContainer::new));
        registry.register("entity_detector", IForgeMenuType.create(EntityDetectorContainer::new));
        registry.register("advanced_item_collector", IForgeMenuType.create(AdvancedItemCollectorContainer::new));
        registry.register("filtered_super_lubricent_platform", IForgeMenuType.create(FilteredSuperLubricentPlatformContainer::new));
        registry.register("notification_interface", IForgeMenuType.create(NotificationInterfaceContainer::new));
        registry.register("global_chat_detector", IForgeMenuType.create(GlobalChatDetectorContainer::new));
        registry.register("chunk_analyzer", IForgeMenuType.create(ChunkAnalyzerContainer::new));
        registry.register("sound_recorder", IForgeMenuType.create(SoundRecorderContainer::new));
        registry.register("sound_dampener", IForgeMenuType.create(SoundDampenerContainer::new));
        registry.register("portable_sound_dampener", IForgeMenuType.create(PortableSoundDampenerContainer::new));
        registry.register("imbuing_station", IForgeMenuType.create(ImbuingStationContainer::new));
        registry.register("ender_letter", IForgeMenuType.create(EnderLetterContainer::new));
        registry.register("ender_mailbox", IForgeMenuType.create(EnderMailboxContainer::new));
        registry.register("block_destabilizer", IForgeMenuType.create(BlockDestabilizerContainer::new));
        registry.register("dyeing_machine", IForgeMenuType.create(DyeingMachineContainer::new));
    }
}
