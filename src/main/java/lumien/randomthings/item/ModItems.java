package lumien.randomthings.item;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import net.minecraft.world.item.Rarity;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.BuiltInRegistries;
import lumien.randomthings.lib.RTRegistry;
import lumien.randomthings.lib.ObjectHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ModItems {
    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:fertilized_dirt")
    public static Item FERTILIZED_DIRT;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:diaphanous_block")
    public static Item DIAPHANOUS_BLOCK;


    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:block_of_sticks")
    public static Item BLOCK_OF_STICKS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:block_of_sticks_returning")
    public static Item BLOCK_OF_STICKS_RETURNING;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:rainbow_lamp")
    public static Item RAINBOW_LAMP;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:beans")
    public static Item BEANS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:lesser_magic_bean")
    public static Item LESSER_MAGIC_BEAN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:lotus_blossom")
    public static Item LOTUS_BLOSSOM;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:lotus_seeds")
    public static Item LOTUS_SEEDS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:ectoplasm")
    public static Item ECTOPLASM;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:bean_stew")
    public static Item BEAN_STEW;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:blaze_and_steel")
    public static Item BLAZE_AND_STEEL;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:bottle_of_air")
    public static Item BOTTLE_OF_AIR;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:stable_enderpearl")
    public static Item STABLE_ENDERPEARL;


    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:super_lubricent_boots")
    public static Item SUPER_LUBRICENT_BOOTS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:id_card")
    public static Item ID_CARD;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:position_filter")
    public static Item POSITION_FILTER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:golden_compass")
    public static Item GOLDEN_COMPASS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:emerald_compass")
    public static Item EMERALD_COMPASS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:biome_crystal")
    public static Item BIOME_CRYSTAL;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:sound_pattern")
    public static Item SOUND_PATTERN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:sound_recorder")
    public static Item SOUND_RECORDER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:portable_sound_dampener")
    public static Item PORTABLE_SOUND_DAMPENER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:rune_pattern")
    public static Item RUNE_PATTERN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:imbue_fire")
    public static Item IMBUE_FIRE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:imbue_poison")
    public static Item IMBUE_POISON;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:imbue_experience")
    public static Item IMBUE_EXPERIENCE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:imbue_wither")
    public static Item IMBUE_WITHER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:floo_powder")
    public static Item FLOO_POWDER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_ingot")
    public static Item SPECTRE_INGOT;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_key")
    public static Item SPECTRE_KEY;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:floo_pouch")
    public static Item FLOO_POUCH;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:floo_sign")
    public static Item FLOO_SIGN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:floo_token")
    public static Item FLOO_TOKEN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:eclipsed_clock")
    public static Item ECLIPSED_CLOCK;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:weather_egg_sun")
    public static Item WEATHER_EGG_SUN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:weather_egg_rain")
    public static Item WEATHER_EGG_RAIN;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:weather_egg_storm")
    public static Item WEATHER_EGG_STORM;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:time_in_a_bottle")
    public static Item TIME_IN_A_BOTTLE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:golden_egg")
    public static Item GOLDEN_EGG;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:summoning_pendulum")
    public static Item SUMMONING_PENDULUM;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:magic_hood")
    public static Item MAGIC_HOOD;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:ender_letter")
    public static Item ENDER_LETTER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:ender_bucket")
    public static Item ENDER_BUCKET;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:reinforced_ender_bucket")
    public static Item REINFORCED_ENDER_BUCKET;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_illuminator")
    public static Item SPECTRE_ILLUMINATOR;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:portkey")
    public static Item PORTKEY;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_anchor")
    public static Item SPECTRE_ANCHOR;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:luminous_powder")
    public static Item LUMINOUS_POWDER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:redstone_activator")
    public static Item REDSTONE_ACTIVATOR;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:redstone_remote")
    public static Item REDSTONE_REMOTE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_sword")
    public static Item SPECTRE_SWORD;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_pickaxe")
    public static Item SPECTRE_PICKAXE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_axe")
    public static Item SPECTRE_AXE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_shovel")
    public static Item SPECTRE_SHOVEL;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_charger_normal")
    public static Item SPECTRE_CHARGER_NORMAL;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_charger_redstone")
    public static Item SPECTRE_CHARGER_REDSTONE;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_charger_ender")
    public static Item SPECTRE_CHARGER_ENDER;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_charger_genesis")
    public static Item SPECTRE_CHARGER_GENESIS;

    @ObjectHolder(registryName = "minecraft:item", value = "randomthings:spectre_string")
    public static Item SPECTRE_STRING;

    public static CreativeModeTab RT_ITEM_GROUP;

    public static void registerItems(RTRegistry<Item> registry) {


        registry.register("beans", new BeanItem(new Item.Properties(), ModBlocks.BEAN_SPROUT, false));
        registry.register("lesser_magic_bean", new BeanItem(new Item.Properties(), ModBlocks.LESSER_BEAN_STALK, true));
        registry.register("magic_bean", new BeanItem(new Item.Properties().rarity(Rarity.RARE), ModBlocks.BEAN_STALK, true));

        registry.register("lotus_blossom", new LotusBlossomItem(new Item.Properties()));
        registry.register("lotus_seeds", new LotusSeedsItem(new Item.Properties()));
        registry.register("ectoplasm", new Item(new Item.Properties()));
        registry.register("spectre_ingot", new Item(new Item.Properties()));
        registry.register("spectre_string", new Item(new Item.Properties()));
        registry.register("spectre_key", new SpectreKeyItem(new Item.Properties().stacksTo(1)));
        registry.register("biome_sensor", new Item(new Item.Properties()));
        registry.register("luminous_powder", new Item(new Item.Properties()));
        registry.register("blackout_powder", new BlackoutPowderItem(new Item.Properties()));

        registry.register("bean_stew", new BeanStewItem(new Item.Properties().stacksTo(1).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build())));
        registry.register("blaze_and_steel", new BlazeAndSteelItem(new Item.Properties().stacksTo(1).durability(64)));
        registry.register("bottle_of_air", new BottleOfAirItem(new Item.Properties().stacksTo(1)));
        registry.register("stable_enderpearl", new StableEnderpearlItem(new Item.Properties()));

        registry.register("super_lubricent_boots", new SuperLubricentBootsItem(new Item.Properties()));
        // Plain crafting ingredient (1.12.2's ItemIngredient.SUPERLUBRICENT_TINCTURE has no behavior of its own).
        registry.register("super_lubricent_tincture", new Item(new Item.Properties()));

        // Divining Rods
        registerDiviningRod(registry, "coal", new Color(20, 20, 20, 50), Tags.Blocks.ORES_COAL.location().toString());
        registerDiviningRod(registry, "iron", new Color(211, 180, 159, 50), Tags.Blocks.ORES_IRON.location().toString());
        registerDiviningRod(registry, "gold", new Color(246, 233, 80, 50), Tags.Blocks.ORES_GOLD.location().toString());
        registerDiviningRod(registry, "lapis", new Color(5, 45, 150, 50), Tags.Blocks.ORES_LAPIS.location().toString());
        registerDiviningRod(registry, "redstone", new Color(211, 1, 1, 50), Tags.Blocks.ORES_REDSTONE.location().toString());
        registerDiviningRod(registry, "emerald", new Color(0, 220, 0, 50), Tags.Blocks.ORES_EMERALD.location().toString());
        registerDiviningRod(registry, "diamond", new Color(87, 221, 229, 50), Tags.Blocks.ORES_DIAMOND.location().toString());
        registerDiviningRod(registry, "vanilla", DiviningRodScratch.COLOR_HOLDER.toArray(new Color[0]), DiviningRodScratch.TAG_HOLDER.toArray(new String[0]));

        DiviningRodScratch.TAG_HOLDER.clear();
        DiviningRodScratch.COLOR_HOLDER.clear();

        // Item Blocks
        registerItemForBlock(registry, ModBlocks.FERTILIZED_DIRT);
        registerItemForBlock(registry, ModBlocks.RAINBOW_LAMP);
        registerItemForBlock(registry, ModBlocks.SUPER_LUBRICENT_STONE);
        registry.register(RegUtil.id(ModBlocks.ADVANCED_REDSTONE_TORCH), new StandingAndWallBlockItem(ModBlocks.ADVANCED_REDSTONE_TORCH, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH, new Item.Properties(), net.minecraft.core.Direction.DOWN));
        registerItemForBlock(registry, ModBlocks.BLOCK_OF_STICKS, ModBlocks.BLOCK_OF_STICKS_RETURNING);
        registerItemForBlock(registry, ModBlocks.PLATFORM_OAK, ModBlocks.PLATFORM_SPRUCE, ModBlocks.PLATFORM_BIRCH, ModBlocks.PLATFORM_JUNGLE, ModBlocks.PLATFORM_ACACIA, ModBlocks.PLATFORM_DARKOAK);

        registerItemForBlock(registry, ModBlocks.LAPIS_GLASS);
        registerItemForBlock(registry, ModBlocks.LAPIS_LAMP);
        registerItemForBlock(registry, ModBlocks.QUARTZ_GLASS);
        registerItemForBlock(registry, ModBlocks.QUARTZ_LAMP);
        registerItemForBlock(registry, ModBlocks.SUPER_LUBRICENT_ICE);
        registerItemForBlock(registry, ModBlocks.SUPER_LUBRICENT_PLATFORM);
        registerItemForBlock(registry, ModBlocks.TRIGGER_GLASS);
        // No creative-tab group: only obtainable in-world by right-clicking a Slime
        // Block with a shovel (see RandomThings.java), not meant to be handed out
        // directly.
        {
            Item compressedSlimeBlockItem = new BlockItem(ModBlocks.COMPRESSED_SLIME_BLOCK, new Item.Properties());
            registry.register(RegUtil.id(ModBlocks.COMPRESSED_SLIME_BLOCK), compressedSlimeBlockItem);
        }
        registerItemForBlock(registry, ModBlocks.CONTACT_BUTTON);
        registerItemForBlock(registry, ModBlocks.CONTACT_LEVER);
        registerItemForBlock(registry, ModBlocks.SPECTRE_BLOCK);

        registerItemForBlock(registry, ModBlocks.SPECTRE_LOG);
        registerItemForBlock(registry, ModBlocks.SPECTRE_PLANK);
        registerItemForBlock(registry, ModBlocks.SPECTRE_LEAF);
        registerItemForBlock(registry, ModBlocks.SPECTRE_SAPLING);

        registerItemForBlock(registry, ModBlocks.GLOWING_MUSHROOM);
        registerItemForBlock(registry, ModBlocks.SIDED_REDSTONE);

        // No creative-tab group: the real acquisition path is planting the Grass Seeds
        // items below (block placed via the COLOR state), or Silk Touch (the item then
        // carries a BlockStateTag for its color, see colored_grass.json's loot table).
        {
            Item coloredGrassItem = new BlockItem(ModBlocks.COLORED_GRASS, new Item.Properties()) {
                @Override
                public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatable("block.randomthings.colored_grass." + lumien.randomthings.block.ColoredGrassBlock.getColor(stack).getName());
                }
            };
            registry.register(RegUtil.id(ModBlocks.COLORED_GRASS), coloredGrassItem);
        }

        registry.register("grass_seeds", new GrassSeedsItem(new Item.Properties(), null));
        for (DyeColor color : DyeColor.values()) {
            registry.register("grass_seeds_" + color.getName(), new GrassSeedsItem(new Item.Properties(), color));
        }

        for (DyeColor color : DyeColor.values()) {
            registerItemForBlock(registry, lookupBlock(ModBlocks.stainedBrickRegistryName(color, false)));
            registerItemForBlock(registry, lookupBlock(ModBlocks.stainedBrickRegistryName(color, true)));

            registerItemForBlock(registry, lookupBlock(ModBlocks.luminousBlockRegistryName(color, false)));
            registerItemForBlock(registry, lookupBlock(ModBlocks.luminousBlockRegistryName(color, true)));
        }

        registerItemForBlock(registry, ModBlocks.ANALOG_EMITTER);
        registerItemForBlock(registry, ModBlocks.IGNITER);
        registerItemForBlock(registry, ModBlocks.ONLINE_DETECTOR);
        registerItemForBlock(registry, ModBlocks.ITEM_COLLECTOR);
        registerItemForBlock(registry, ModBlocks.ADVANCED_REDSTONE_REPEATER);
        registerItemForBlock(registry, ModBlocks.IRON_DROPPER);
        registerItemForBlock(registry, ModBlocks.PLAYER_INTERFACE);
        registerItemForBlock(registry, ModBlocks.INVENTORY_TESTER);
        registerItemForBlock(registry, ModBlocks.CHAT_DETECTOR);
        registerItemForBlock(registry, ModBlocks.REDSTONE_OBSERVER);
        registerItemForBlock(registry, ModBlocks.BASIC_REDSTONE_INTERFACE, ModBlocks.ADVANCED_REDSTONE_INTERFACE);
        registry.register("redstone_tool", new RedstoneToolItem(new Item.Properties().stacksTo(1)));
        registerItemForBlock(registry, ModBlocks.POTION_VAPORIZER);

        registerItemForBlock(registry, ModBlocks.ENTITY_DETECTOR);

        registry.register("entity_filter", new EntityFilterItem(new Item.Properties().stacksTo(1)));

        registerItemForBlock(registry, ModBlocks.SLIME_CUBE);
        registerItemForBlock(registry, ModBlocks.ADVANCED_ITEM_COLLECTOR);
        registerItemForBlock(registry, ModBlocks.FILTERED_SUPER_LUBRICENT_PLATFORM);
        registerItemForBlock(registry, ModBlocks.NOTIFICATION_INTERFACE);
        registerItemForBlock(registry, ModBlocks.GLOBAL_CHAT_DETECTOR);
        registerItemForBlock(registry, ModBlocks.BIOME_RADAR);
        registerItemForBlock(registry, ModBlocks.SOUND_BOX);
        registerItemForBlock(registry, ModBlocks.SOUND_DAMPENER);

        registry.register("biome_crystal", new BiomeCrystalItem(new Item.Properties().stacksTo(1)));

        registry.register("sound_pattern", new SoundPatternItem(new Item.Properties()));
        registry.register("sound_recorder", new SoundRecorderItem(new Item.Properties().stacksTo(1)));
        registry.register("portable_sound_dampener", new PortableSoundDampenerItem(new Item.Properties().stacksTo(1)));
        registry.register("id_card", new IdCardItem(new Item.Properties()));

        registry.register("position_filter", new PositionFilterItem(new Item.Properties()));
        registry.register("golden_compass", new GoldenCompassItem(new Item.Properties().stacksTo(1)));
        registry.register("emerald_compass", new EmeraldCompassItem(new Item.Properties().stacksTo(1)));
        registry.register("escape_rope", new EscapeRopeItem(new Item.Properties().stacksTo(1).durability(20)));
        registry.register("chunk_analyzer", new ChunkAnalyzerItem(new Item.Properties().stacksTo(1)));

        for (DyeColor color : DyeColor.values()) {
            RuneDustItem dust = new RuneDustItem(new Item.Properties(), color);
            registry.register("rune_dust_" + color.getName(), dust);
            RuneDustItems.BY_COLOR.put(color, dust);
        }

        registry.register("rune_pattern", new RunePatternItem(new Item.Properties().stacksTo(1)));

        registerItemForBlock(registry, ModBlocks.IMBUING_STATION);
        registry.register("imbue_fire", new ImbueItem(new Item.Properties(), () -> lumien.randomthings.potion.ModEffects.IMBUE_FIRE));
        registry.register("imbue_poison", new ImbueItem(new Item.Properties(), () -> lumien.randomthings.potion.ModEffects.IMBUE_POISON));
        registry.register("imbue_experience", new ImbueItem(new Item.Properties(), () -> lumien.randomthings.potion.ModEffects.IMBUE_EXPERIENCE));
        registry.register("imbue_wither", new ImbueItem(new Item.Properties(), () -> lumien.randomthings.potion.ModEffects.IMBUE_WITHER));

        registry.register("floo_powder", new Item(new Item.Properties()));
        registry.register("floo_pouch", new FlooPouchItem(new Item.Properties().stacksTo(1)));
        registry.register("floo_sign", new FlooSignItem(new Item.Properties().stacksTo(1)));
        registry.register("floo_token", new FlooTokenItem(new Item.Properties()));

        registry.register("eclipsed_clock", new EclipsedClockItem(new Item.Properties().stacksTo(1)));
        registry.register("weather_egg_sun", new WeatherEggItem(new Item.Properties()));
        registry.register("weather_egg_rain", new WeatherEggItem(new Item.Properties()));
        registry.register("weather_egg_storm", new WeatherEggItem(new Item.Properties()));
        registry.register("time_in_a_bottle", new TimeInABottleItem(new Item.Properties().stacksTo(1)));

        registry.register("ender_letter", new EnderLetterItem(new Item.Properties().stacksTo(1)));
        registerItemForBlock(registry, ModBlocks.ENDER_MAILBOX);

        registry.register("ender_bucket", new EnderBucketItem(new Item.Properties().stacksTo(16)));
        registry.register("reinforced_ender_bucket", new ReinforcedEnderBucketItem(new Item.Properties().stacksTo(1)));

        registry.register("summoning_pendulum", new SummoningPendulumItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
        registry.register("golden_egg", new GoldenEggItem(new Item.Properties()));
        registry.register("spirit_spawn_egg", new net.neoforged.neoforge.common.DeferredSpawnEggItem(() -> lumien.randomthings.entity.ModEntityTypes.SPIRIT, 0x000000, 0x0009F6, new Item.Properties()));
        registerItemForBlock(registry, ModBlocks.FLUID_DISPLAY);

        registry.register("evil_tear", new EvilTearItem(new Item.Properties()));
        registerItemForBlock(registry, ModBlocks.CREATIVE_PLAYER_INTERFACE);
        registry.register("magic_hood", new MagicHoodItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
        registerItemForBlock(registry, ModBlocks.RAIN_SHIELD);
        registerItemForBlock(registry, ModBlocks.ENDER_BRIDGE, ModBlocks.PRISMARINE_ENDER_BRIDGE, ModBlocks.ENDER_ANCHOR);
        registerItemForBlock(registry, ModBlocks.PEACE_CANDLE, ModBlocks.ANCIENT_FURNACE);
        registerItemForBlock(registry, ModBlocks.BLOCK_BREAKER);
        registerItemForBlock(registry, ModBlocks.BLOCK_DESTABILIZER);

        registry.register("spectre_illuminator", new SpectreIlluminatorItem(new Item.Properties()));
        registry.register("portkey", new PortkeyItem(new Item.Properties().stacksTo(1)));
        registry.register("spectre_anchor", new Item(new Item.Properties()));
        registry.register("redstone_activator", new lumien.randomthings.item.RedstoneActivatorItem(new Item.Properties().stacksTo(1)));
        registry.register("redstone_remote", new lumien.randomthings.item.RedstoneRemoteItem(new Item.Properties().stacksTo(1)));

        registry.register("spectre_sword", new lumien.randomthings.item.spectretools.SpectreSwordItem(new Item.Properties()));
        registry.register("spectre_pickaxe", new lumien.randomthings.item.spectretools.SpectrePickaxeItem(new Item.Properties()));
        registry.register("spectre_axe", new lumien.randomthings.item.spectretools.SpectreAxeItem(new Item.Properties()));
        registry.register("spectre_shovel", new lumien.randomthings.item.spectretools.SpectreShovelItem(new Item.Properties()));

        registerItemForBlock(registry, ModBlocks.SPECTRE_ENERGY_INJECTOR);
        registerItemForBlock(registry, ModBlocks.SPECTRE_COIL_NORMAL, ModBlocks.SPECTRE_COIL_REDSTONE, ModBlocks.SPECTRE_COIL_ENDER, ModBlocks.SPECTRE_COIL_GENESIS);
        registry.register("spectre_coil_number", new lumien.randomthings.item.block.NumberedSpectreCoilBlockItem(ModBlocks.SPECTRE_COIL_NUMBER, new Item.Properties()));

        registry.register("spectre_charger_normal", new SpectreChargerItem(new Item.Properties().stacksTo(1), SpectreChargerItem.Tier.NORMAL));
        registry.register("spectre_charger_redstone", new SpectreChargerItem(new Item.Properties().stacksTo(1), SpectreChargerItem.Tier.REDSTONE));
        registry.register("spectre_charger_ender", new SpectreChargerItem(new Item.Properties().stacksTo(1), SpectreChargerItem.Tier.ENDER));
        registry.register("spectre_charger_genesis", new SpectreChargerItem(new Item.Properties().stacksTo(1), SpectreChargerItem.Tier.GENESIS));

        registerItemForBlock(registry, ModBlocks.SPECTRE_LENS);

        registerItemForBlock(registry, ModBlocks.LIGHT_REDIRECTOR);

        registry.register("diaphanous_block", new lumien.randomthings.item.block.DiaphanousBlockItem(ModBlocks.DIAPHANOUS_BLOCK, new Item.Properties()));

        registerItemForBlock(registry, ModBlocks.DYEING_MACHINE);
    }

    private static Block lookupBlock(String name) {
        return BuiltInRegistries.BLOCK.get(new ResourceLocation(ModConstants.MOD_ID, name));
    }

    private static void registerDiviningRod(RTRegistry<Item> registry, String name, Color[] colors, String[] tags) {
        DiviningRodScratch.TAG_HOLDER.addAll(Arrays.asList(tags));
        DiviningRodScratch.COLOR_HOLDER.addAll(Arrays.asList(colors));

        registry.register("divining_rod_" + name, new DiviningRodItem(new Item.Properties(), colors, tags));
    }

    private static void registerDiviningRod(RTRegistry<Item> registry, String name, Color color, String tag) {
        registerDiviningRod(registry, name, new Color[]{color}, new String[]{tag});
    }

    private static void registerItemForBlock(RTRegistry<Item> registry, Block... blocks) {
        for (Block block : blocks) {
            Item itemInstance = new BlockItem(block, new Item.Properties());
            registry.register(RegUtil.id(block), itemInstance);
        }
    }

    /** The mod's creative tab (1.20: registered on {@code Registries.CREATIVE_MODE_TAB}; the contents are every Random Things item in registry order). */
    public static CreativeModeTab createItemGroup() {
        RT_ITEM_GROUP = CreativeModeTab.builder()
                .title(net.minecraft.network.chat.Component.translatable("itemGroup.randomthings"))
                .icon(() -> new ItemStack(ModBlocks.FERTILIZED_DIRT))
                .displayItems((parameters, output) -> {
                    for (Item item : BuiltInRegistries.ITEM) {
                        if (!ModConstants.MOD_ID.equals(RegUtil.id(item).getNamespace())) {
                            continue;
                        }

                        if (item instanceof lumien.randomthings.item.block.DiaphanousBlockItem diaphanous) {
                            diaphanous.addCreativeStacks(output);
                        } else {
                            output.accept(item);
                        }
                    }
                })
                .build();
        return RT_ITEM_GROUP;
    }

}
