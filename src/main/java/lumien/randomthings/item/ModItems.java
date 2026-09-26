package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.renderer.SpecialChestItemRenderer;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@ObjectHolder("randomthings")
public class ModItems {
    @ObjectHolder("fertilized_dirt")
    public static Item FERTILIZED_DIRT;


    @ObjectHolder("block_of_sticks")
    public static Item BLOCK_OF_STICKS;

    @ObjectHolder("block_of_sticks_returning")
    public static Item BLOCK_OF_STICKS_RETURNING;

    @ObjectHolder("rainbow_lamp")
    public static Item RAINBOW_LAMP;

    @ObjectHolder("beans")
    public static Item BEANS;

    @ObjectHolder("lesser_magic_bean")
    public static Item LESSER_MAGIC_BEAN;

    @ObjectHolder("lotus_blossom")
    public static Item LOTUS_BLOSSOM;

    @ObjectHolder("lotus_seeds")
    public static Item LOTUS_SEEDS;

    @ObjectHolder("sakanade_spores")
    public static Item SAKANADE_SPORES;

    @ObjectHolder("ectoplasm")
    public static Item ECTOPLASM;

    @ObjectHolder("bean_stew")
    public static Item BEAN_STEW;

    @ObjectHolder("blaze_and_steel")
    public static Item BLAZE_AND_STEEL;

    @ObjectHolder("bottle_of_air")
    public static Item BOTTLE_OF_AIR;

    @ObjectHolder("stable_enderpearl")
    public static Item STABLE_ENDERPEARL;

    @ObjectHolder("obsidian_skull")
    public static Item OBSIDIAN_SKULL;

    @ObjectHolder("lava_charm")
    public static Item LAVA_CHARM;

    @ObjectHolder("lava_wader")
    public static Item LAVA_WADER;

    @ObjectHolder("water_walking_boots")
    public static Item WATER_WALKING_BOOTS;

    @ObjectHolder("obsidian_water_walking_boots")
    public static Item OBSIDIAN_WATER_WALKING_BOOTS;

    @ObjectHolder("super_lubricent_boots")
    public static Item SUPER_LUBRICENT_BOOTS;

    @ObjectHolder("id_card")
    public static Item ID_CARD;

    @ObjectHolder("position_filter")
    public static Item POSITION_FILTER;

    @ObjectHolder("golden_compass")
    public static Item GOLDEN_COMPASS;

    @ObjectHolder("emerald_compass")
    public static Item EMERALD_COMPASS;

    @ObjectHolder("biome_crystal")
    public static Item BIOME_CRYSTAL;

    @ObjectHolder("sound_pattern")
    public static Item SOUND_PATTERN;

    @ObjectHolder("sound_recorder")
    public static Item SOUND_RECORDER;

    @ObjectHolder("portable_sound_dampener")
    public static Item PORTABLE_SOUND_DAMPENER;

    @ObjectHolder("rune_pattern")
    public static Item RUNE_PATTERN;

    @ObjectHolder("imbue_fire")
    public static Item IMBUE_FIRE;

    @ObjectHolder("imbue_poison")
    public static Item IMBUE_POISON;

    @ObjectHolder("imbue_experience")
    public static Item IMBUE_EXPERIENCE;

    @ObjectHolder("imbue_wither")
    public static Item IMBUE_WITHER;

    @ObjectHolder("floo_powder")
    public static Item FLOO_POWDER;

    @ObjectHolder("floo_pouch")
    public static Item FLOO_POUCH;

    @ObjectHolder("floo_sign")
    public static Item FLOO_SIGN;

    @ObjectHolder("floo_token")
    public static Item FLOO_TOKEN;

    @ObjectHolder("eclipsed_clock")
    public static Item ECLIPSED_CLOCK;

    @ObjectHolder("weather_egg_sun")
    public static Item WEATHER_EGG_SUN;

    @ObjectHolder("weather_egg_rain")
    public static Item WEATHER_EGG_RAIN;

    @ObjectHolder("weather_egg_storm")
    public static Item WEATHER_EGG_STORM;

    @ObjectHolder("time_in_a_bottle")
    public static Item TIME_IN_A_BOTTLE;

    @ObjectHolder("ender_letter")
    public static Item ENDER_LETTER;

    @ObjectHolder("ender_bucket")
    public static Item ENDER_BUCKET;

    @ObjectHolder("reinforced_ender_bucket")
    public static Item REINFORCED_ENDER_BUCKET;

    /**
     * Filled directly during registration (16 distinct registry names, one
     * per {@link DyeColor}) rather than via {@code @ObjectHolder}, which only
     * covers single fixed names.
     */
    public static final java.util.Map<DyeColor, Item> RUNE_DUST = new java.util.EnumMap<>(DyeColor.class);

    public static ItemGroup RT_ITEM_GROUP;

    public static void registerItems(Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();


        registry.register(new BeanItem(new Item.Properties().group(RT_ITEM_GROUP), ModBlocks.BEAN_SPROUT, false).setRegistryName("beans"));
        registry.register(new BeanItem(new Item.Properties().group(RT_ITEM_GROUP), ModBlocks.LESSER_BEAN_STALK, true).setRegistryName("lesser_magic_bean"));
        registry.register(new BeanItem(new Item.Properties().group(RT_ITEM_GROUP).rarity(Rarity.RARE), ModBlocks.BEAN_STALK, true).setRegistryName("magic_bean"));

        registry.register(new LotusBlossomItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("lotus_blossom"));
        registry.register(new LotusSeedsItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("lotus_seeds"));
        registry.register(new Item(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("sakanade_spores"));
        registry.register(new Item(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("ectoplasm"));
        registry.register(new Item(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("biome_sensor"));
        registry.register(new Item(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("luminous_powder"));

        registry.register(new BeanStewItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1).food(new net.minecraft.item.Food.Builder().hunger(8).saturation(0.6F).build())).setRegistryName("bean_stew"));
        registry.register(new BlazeAndSteelItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1).maxDamage(64)).setRegistryName("blaze_and_steel"));
        registry.register(new BottleOfAirItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("bottle_of_air"));
        registry.register(new StableEnderpearlItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("stable_enderpearl"));

        registry.register(new ObsidianSkullItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("obsidian_skull"));
        registry.register(new LavaCharmItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("lava_charm"));
        registry.register(new LavaWaderItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("lava_wader"));
        registry.register(new WaterWalkingBootsItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("water_walking_boots"));
        registry.register(new ObsidianWaterWalkingBootsItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("obsidian_water_walking_boots"));
        registry.register(new SuperLubricentBootsItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("super_lubricent_boots"));

        // Divining Rods
        registerDiviningRod(registry, "coal", new Color(20, 20, 20, 50), Tags.Blocks.ORES_COAL.getId().toString());
        registerDiviningRod(registry, "iron", new Color(211, 180, 159, 50), Tags.Blocks.ORES_IRON.getId().toString());
        registerDiviningRod(registry, "gold", new Color(246, 233, 80, 50), Tags.Blocks.ORES_GOLD.getId().toString());
        registerDiviningRod(registry, "lapis", new Color(5, 45, 150, 50), Tags.Blocks.ORES_LAPIS.getId().toString());
        registerDiviningRod(registry, "redstone", new Color(211, 1, 1, 50), Tags.Blocks.ORES_REDSTONE.getId().toString());
        registerDiviningRod(registry, "emerald", new Color(0, 220, 0, 50), Tags.Blocks.ORES_EMERALD.getId().toString());
        registerDiviningRod(registry, "diamond", new Color(87, 221, 229, 50), Tags.Blocks.ORES_DIAMOND.getId().toString());
        registerDiviningRod(registry, "vanilla", colorHolder.toArray(new Color[0]), tagHolder.toArray(new String[0]));

        tagHolder.clear();
        colorHolder.clear();

        // Item Blocks
        registerItemForBlock(registry, ModBlocks.FERTILIZED_DIRT);
        registerItemForBlock(registry, ModBlocks.RAINBOW_LAMP);
        registerItemForBlock(registry, ModBlocks.SUPER_LUBRICENT_STONE);
        registry.register(new WallOrFloorItem(ModBlocks.ADVANCED_REDSTONE_TORCH, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH, new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName(ModBlocks.ADVANCED_REDSTONE_TORCH.getRegistryName()));
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
            compressedSlimeBlockItem.setRegistryName(ModBlocks.COMPRESSED_SLIME_BLOCK.getRegistryName());
            registry.register(compressedSlimeBlockItem);
        }
        registerItemForBlock(registry, ModBlocks.CONTACT_BUTTON);
        registerItemForBlock(registry, ModBlocks.CONTACT_LEVER);
        registerItemForBlock(registry, ModBlocks.SPECTRE_BLOCK);

        registerItemForBlock(registry, ModBlocks.SPECTRE_LOG);
        registerItemForBlock(registry, ModBlocks.SPECTRE_PLANK);
        registerItemForBlock(registry, ModBlocks.SPECTRE_LEAF);
        registerItemForBlock(registry, ModBlocks.SPECTRE_SAPLING);

        registerItemForBlock(registry, ModBlocks.PLATE_ACCELERATOR);
        registerItemForBlock(registry, ModBlocks.PLATE_BOUNCY);
        registerItemForBlock(registry, ModBlocks.PLATE_COLLECTION);
        registerItemForBlock(registry, ModBlocks.PLATE_CORRECTOR);
        registerItemForBlock(registry, ModBlocks.PLATE_ACCELERATOR_DIRECTIONAL);
        registerItemForBlock(registry, ModBlocks.PLATE_ITEMREJUVENATOR);
        registerItemForBlock(registry, ModBlocks.PLATE_ITEMSEALER);
        registerItemForBlock(registry, ModBlocks.PLATE_REDIRECTOR);
        registerItemForBlock(registry, ModBlocks.REDSTONE_PLATE);

        registerItemForBlock(registry, ModBlocks.GLOWING_MUSHROOM);
        registerItemForBlock(registry, ModBlocks.SIDED_REDSTONE);
        registerItemForBlock(registry, ModBlocks.PITCHER_PLANT);

        registerItemForBlock(registry, ModBlocks.BIOME_GLASS);
        registerItemForBlock(registry, ModBlocks.BIOME_STONE_COBBLE, ModBlocks.BIOME_STONE_SMOOTH, ModBlocks.BIOME_STONE_BRICK, ModBlocks.BIOME_STONE_CRACKED, ModBlocks.BIOME_STONE_CHISELED);
        // No creative-tab group: the real 1.12.2 acquisition path is planting a
        // colored Grass Seeds item (16 dye-color variants) - that item, and the
        // matching 16-color version of this block, aren't ported yet (this port
        // currently only has the single default-white variant). Not craftable
        // either in the original - hidden here rather than left reachable through
        // a path (creative search) that doesn't exist upstream.
        {
            Item coloredGrassItem = new BlockItem(ModBlocks.COLORED_GRASS, new Item.Properties());
            coloredGrassItem.setRegistryName(ModBlocks.COLORED_GRASS.getRegistryName());
            registry.register(coloredGrassItem);
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
        registerItemForBlock(registry, ModBlocks.PLATE_EXTRACTION);
        registerItemForBlock(registry, ModBlocks.PLATE_PROCESSING);
        registerItemForBlock(registry, ModBlocks.ADVANCED_REDSTONE_REPEATER);
        registerItemForBlock(registry, ModBlocks.IRON_DROPPER);
        registerItemForBlock(registry, ModBlocks.PLAYER_INTERFACE);
        registerItemForBlock(registry, ModBlocks.INVENTORY_TESTER);
        registerItemForBlock(registry, ModBlocks.INVENTORY_REROUTER);
        registerItemForBlock(registry, ModBlocks.CHAT_DETECTOR);
        registerItemForBlock(registry, ModBlocks.REDSTONE_OBSERVER);
        registry.register(new RedstoneToolItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("redstone_tool"));
        registerItemForBlock(registry, ModBlocks.POTION_VAPORIZER);

        registry.register(new BlockItem(ModBlocks.SPECIAL_CHEST_NATURE, new Item.Properties().group(RT_ITEM_GROUP).setTEISR(() -> SpecialChestItemRenderer::new)).setRegistryName(ModBlocks.SPECIAL_CHEST_NATURE.getRegistryName()));
        registry.register(new BlockItem(ModBlocks.SPECIAL_CHEST_WATER, new Item.Properties().group(RT_ITEM_GROUP).setTEISR(() -> SpecialChestItemRenderer::new)).setRegistryName(ModBlocks.SPECIAL_CHEST_WATER.getRegistryName()));

        registerItemForBlock(registry, ModBlocks.ENTITY_DETECTOR);

        registry.register(new EntityFilterItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("entity_filter"));

        registerItemForBlock(registry, ModBlocks.PLATE_FILTERED_REDIRECTOR);
        registerItemForBlock(registry, ModBlocks.SLIME_CUBE);
        registerItemForBlock(registry, ModBlocks.ADVANCED_ITEM_COLLECTOR);
        registerItemForBlock(registry, ModBlocks.FILTERED_SUPER_LUBRICENT_PLATFORM);
        registerItemForBlock(registry, ModBlocks.NOTIFICATION_INTERFACE);
        registerItemForBlock(registry, ModBlocks.GLOBAL_CHAT_DETECTOR);
        registerItemForBlock(registry, ModBlocks.BIOME_RADAR);
        registerItemForBlock(registry, ModBlocks.SOUND_BOX);
        registerItemForBlock(registry, ModBlocks.SOUND_DAMPENER);

        registry.register(new BiomeCrystalItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("biome_crystal"));

        registry.register(new SoundPatternItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("sound_pattern"));
        registry.register(new SoundRecorderItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("sound_recorder"));
        registry.register(new PortableSoundDampenerItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("portable_sound_dampener"));
        registry.register(new IdCardItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("id_card"));

        registry.register(new PositionFilterItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("position_filter"));
        registry.register(new GoldenCompassItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("golden_compass"));
        registry.register(new EmeraldCompassItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("emerald_compass"));
        registry.register(new EscapeRopeItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1).maxDamage(20)).setRegistryName("escape_rope"));
        registry.register(new ChunkAnalyzerItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("chunk_analyzer"));

        for (DyeColor color : DyeColor.values()) {
            RuneDustItem dust = new RuneDustItem(new Item.Properties().group(RT_ITEM_GROUP), color);
            dust.setRegistryName("rune_dust_" + color.getName());
            registry.register(dust);
            RUNE_DUST.put(color, dust);
        }

        registry.register(new RunePatternItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("rune_pattern"));

        registerItemForBlock(registry, ModBlocks.IMBUING_STATION);
        registry.register(new ImbueItem(new Item.Properties().group(RT_ITEM_GROUP), () -> lumien.randomthings.potion.ModEffects.IMBUE_FIRE).setRegistryName("imbue_fire"));
        registry.register(new ImbueItem(new Item.Properties().group(RT_ITEM_GROUP), () -> lumien.randomthings.potion.ModEffects.IMBUE_POISON).setRegistryName("imbue_poison"));
        registry.register(new ImbueItem(new Item.Properties().group(RT_ITEM_GROUP), () -> lumien.randomthings.potion.ModEffects.IMBUE_EXPERIENCE).setRegistryName("imbue_experience"));
        registry.register(new ImbueItem(new Item.Properties().group(RT_ITEM_GROUP), () -> lumien.randomthings.potion.ModEffects.IMBUE_WITHER).setRegistryName("imbue_wither"));

        registry.register(new Item(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("floo_powder"));
        registry.register(new FlooPouchItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("floo_pouch"));
        registry.register(new FlooSignItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("floo_sign"));
        registry.register(new FlooTokenItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("floo_token"));

        registry.register(new EclipsedClockItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("eclipsed_clock"));
        registry.register(new WeatherEggItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("weather_egg_sun"));
        registry.register(new WeatherEggItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("weather_egg_rain"));
        registry.register(new WeatherEggItem(new Item.Properties().group(RT_ITEM_GROUP)).setRegistryName("weather_egg_storm"));
        registry.register(new TimeInABottleItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("time_in_a_bottle"));

        registry.register(new EnderLetterItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("ender_letter"));
        registerItemForBlock(registry, ModBlocks.ENDER_MAILBOX);

        registry.register(new EnderBucketItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(16)).setRegistryName("ender_bucket"));
        registry.register(new ReinforcedEnderBucketItem(new Item.Properties().group(RT_ITEM_GROUP).maxStackSize(1)).setRegistryName("reinforced_ender_bucket"));
    }

    private static Block lookupBlock(String name) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ModConstants.MOD_ID, name));
    }

    static ArrayList<Color> colorHolder = new ArrayList<Color>();
    static ArrayList<String> tagHolder = new ArrayList<String>();

    private static void registerDiviningRod(IForgeRegistry<Item> registry, String name, Color[] colors, String[] tags) {
        tagHolder.addAll(Arrays.asList(tags));
        colorHolder.addAll(Arrays.asList(colors));

        registry.register(new DiviningRodItem(new Item.Properties().group(RT_ITEM_GROUP), colors, tags).setRegistryName("divining_rod_" + name));
    }

    private static void registerDiviningRod(IForgeRegistry<Item> registry, String name, Color color, String tag) {
        registerDiviningRod(registry, name, new Color[]{color}, new String[]{tag});
    }

    private static void registerItemForBlock(IForgeRegistry<Item> registry, Block... blocks) {
        for (Block block : blocks) {
            Item itemInstance = new BlockItem(block, new Item.Properties().group(RT_ITEM_GROUP));
            itemInstance.setRegistryName(block.getRegistryName());
            registry.register(itemInstance);
        }
    }

    public static void initItemGroup() {
        RT_ITEM_GROUP = new ItemGroup("randomthings") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(ModBlocks.FERTILIZED_DIRT);
            }
        };
    }

}
