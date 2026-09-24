package lumien.randomthings.block;

import lumien.randomthings.block.plates.AcceleratorPlateBlock;
import lumien.randomthings.block.plates.BouncyPlateBlock;
import lumien.randomthings.block.plates.CollectionPlateBlock;
import lumien.randomthings.block.plates.CorrectorPlateBlock;
import lumien.randomthings.block.plates.DirectionalAcceleratorPlateBlock;
import lumien.randomthings.block.plates.ExtractionPlateBlock;
import lumien.randomthings.block.plates.FilteredRedirectorPlateBlock;
import lumien.randomthings.block.plates.ItemRejuvenatorPlateBlock;
import lumien.randomthings.block.plates.ItemSealerPlateBlock;
import lumien.randomthings.block.plates.ProcessingPlateBlock;
import lumien.randomthings.block.plates.RedirectorPlateBlock;
import lumien.randomthings.block.plates.RedstonePlateBlock;
import lumien.randomthings.block.spectretree.SpectreLeafBlock;
import lumien.randomthings.block.spectretree.SpectreLogBlock;
import lumien.randomthings.block.spectretree.SpectrePlankBlock;
import lumien.randomthings.block.spectretree.SpectreSaplingBlock;
import net.minecraft.block.Block;
import net.minecraft.item.DyeColor;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder("randomthings")
public class ModBlocks
{
	@ObjectHolder("fertilized_dirt")
	public static Block FERTILIZED_DIRT;

	@ObjectHolder("rainbow_lamp")
	public static Block RAINBOW_LAMP;


	@ObjectHolder("advanced_redstone_torch")
	public static Block ADVANCED_REDSTONE_TORCH;

	@ObjectHolder("advanced_redstone_wall_torch")
	public static Block ADVANCED_WALL_REDSTONE_TORCH;

	@ObjectHolder("super_lubricent_stone")
	public static Block SUPER_LUBRICENT_STONE;


	@ObjectHolder("block_of_sticks")
	public static Block BLOCK_OF_STICKS;

	@ObjectHolder("block_of_sticks_returning")
	public static Block BLOCK_OF_STICKS_RETURNING;


	@ObjectHolder("platform_oak")
	public static Block PLATFORM_OAK;

	@ObjectHolder("platform_spruce")
	public static Block PLATFORM_SPRUCE;

	@ObjectHolder("platform_birch")
	public static Block PLATFORM_BIRCH;

	@ObjectHolder("platform_jungle")
	public static Block PLATFORM_JUNGLE;

	@ObjectHolder("platform_acacia")
	public static Block PLATFORM_ACACIA;

	@ObjectHolder("platform_darkoak")
	public static Block PLATFORM_DARKOAK;
	
	@ObjectHolder("blood_rose")
	public static Block BLOOD_ROSE;


	@ObjectHolder("lapis_glass")
	public static Block LAPIS_GLASS;

	@ObjectHolder("lapis_lamp")
	public static Block LAPIS_LAMP;

	@ObjectHolder("quartz_glass")
	public static Block QUARTZ_GLASS;

	@ObjectHolder("quartz_lamp")
	public static Block QUARTZ_LAMP;

	@ObjectHolder("super_lubricent_ice")
	public static Block SUPER_LUBRICENT_ICE;

	@ObjectHolder("super_lubricent_platform")
	public static Block SUPER_LUBRICENT_PLATFORM;

	@ObjectHolder("trigger_glass")
	public static Block TRIGGER_GLASS;

	@ObjectHolder("compressed_slime_block")
	public static Block COMPRESSED_SLIME_BLOCK;

	@ObjectHolder("contact_button")
	public static Block CONTACT_BUTTON;

	@ObjectHolder("contact_lever")
	public static Block CONTACT_LEVER;

	@ObjectHolder("spectre_block")
	public static Block SPECTRE_BLOCK;

	@ObjectHolder("spectre_log")
	public static Block SPECTRE_LOG;

	@ObjectHolder("spectre_plank")
	public static Block SPECTRE_PLANK;

	@ObjectHolder("spectre_leaf")
	public static Block SPECTRE_LEAF;

	@ObjectHolder("spectre_sapling")
	public static Block SPECTRE_SAPLING;


	@ObjectHolder("plate_accelerator")
	public static Block PLATE_ACCELERATOR;

	@ObjectHolder("plate_bouncy")
	public static Block PLATE_BOUNCY;

	@ObjectHolder("plate_collection")
	public static Block PLATE_COLLECTION;

	@ObjectHolder("plate_corrector")
	public static Block PLATE_CORRECTOR;

	@ObjectHolder("plate_accelerator_directional")
	public static Block PLATE_ACCELERATOR_DIRECTIONAL;

	@ObjectHolder("plate_itemrejuvenator")
	public static Block PLATE_ITEMREJUVENATOR;

	@ObjectHolder("plate_itemsealer")
	public static Block PLATE_ITEMSEALER;

	@ObjectHolder("plate_redirector")
	public static Block PLATE_REDIRECTOR;

	@ObjectHolder("plate_redstone")
	public static Block REDSTONE_PLATE;

	@ObjectHolder("plate_redstone_powered")
	public static Block REDSTONE_PLATE_POWERED;

	@ObjectHolder("glowing_mushroom")
	public static Block GLOWING_MUSHROOM;

	@ObjectHolder("sided_redstone")
	public static Block SIDED_REDSTONE;

	@ObjectHolder("pitcher_plant")
	public static Block PITCHER_PLANT;

	@ObjectHolder("biome_glass")
	public static Block BIOME_GLASS;

	@ObjectHolder("biome_stone_cobble")
	public static Block BIOME_STONE_COBBLE;

	@ObjectHolder("biome_stone_smooth")
	public static Block BIOME_STONE_SMOOTH;

	@ObjectHolder("biome_stone_brick")
	public static Block BIOME_STONE_BRICK;

	@ObjectHolder("biome_stone_cracked")
	public static Block BIOME_STONE_CRACKED;

	@ObjectHolder("biome_stone_chiseled")
	public static Block BIOME_STONE_CHISELED;

	@ObjectHolder("colored_grass")
	public static Block COLORED_GRASS;

	@ObjectHolder("bean_sprout")
	public static Block BEAN_SPROUT;

	@ObjectHolder("bean_stalk")
	public static Block BEAN_STALK;

	@ObjectHolder("lesser_bean_stalk")
	public static Block LESSER_BEAN_STALK;

	@ObjectHolder("bean_pod")
	public static Block BEAN_POD;

	@ObjectHolder("lotus")
	public static Block LOTUS;

	@ObjectHolder("sakanade")
	public static Block SAKANADE;

	@ObjectHolder("blazing_fire")
	public static Block BLAZING_FIRE;

	@ObjectHolder("analog_emitter")
	public static Block ANALOG_EMITTER;

	@ObjectHolder("igniter")
	public static Block IGNITER;

	@ObjectHolder("online_detector")
	public static Block ONLINE_DETECTOR;

	@ObjectHolder("item_collector")
	public static Block ITEM_COLLECTOR;

	@ObjectHolder("plate_extraction")
	public static Block PLATE_EXTRACTION;

	@ObjectHolder("plate_processing")
	public static Block PLATE_PROCESSING;

	@ObjectHolder("advanced_redstone_repeater")
	public static Block ADVANCED_REDSTONE_REPEATER;

	@ObjectHolder("iron_dropper")
	public static Block IRON_DROPPER;

	@ObjectHolder("player_interface")
	public static Block PLAYER_INTERFACE;

	@ObjectHolder("inventory_tester")
	public static Block INVENTORY_TESTER;

	@ObjectHolder("inventory_rerouter")
	public static Block INVENTORY_REROUTER;

	@ObjectHolder("chat_detector")
	public static Block CHAT_DETECTOR;

	@ObjectHolder("redstone_observer")
	public static Block REDSTONE_OBSERVER;

	@ObjectHolder("potion_vaporizer")
	public static Block POTION_VAPORIZER;

	@ObjectHolder("special_chest_nature")
	public static Block SPECIAL_CHEST_NATURE;

	@ObjectHolder("special_chest_water")
	public static Block SPECIAL_CHEST_WATER;

	@ObjectHolder("entity_detector")
	public static Block ENTITY_DETECTOR;

	@ObjectHolder("plate_filtered_redirector")
	public static Block PLATE_FILTERED_REDIRECTOR;

	@ObjectHolder("slime_cube")
	public static Block SLIME_CUBE;

	@ObjectHolder("advanced_item_collector")
	public static Block ADVANCED_ITEM_COLLECTOR;

	@ObjectHolder("filtered_super_lubricent_platform")
	public static Block FILTERED_SUPER_LUBRICENT_PLATFORM;

	@ObjectHolder("notification_interface")
	public static Block NOTIFICATION_INTERFACE;

	@ObjectHolder("global_chat_detector")
	public static Block GLOBAL_CHAT_DETECTOR;

	/**
	 * A {@code DyeColor} name (e.g. "light_gray") mapped to its texture-folder
	 * name in the resource pack; only differs from the enum's own lowercase
	 * name for LIGHT_GRAY/LIGHT_BLUE, matching the naming already established
	 * by {@link RainbowLampBlock}'s per-color resources.
	 */
	public static String stainedBrickRegistryName(DyeColor color, boolean luminous)
	{
		return (luminous ? "luminous_stained_brick_" : "stained_brick_") + color.getName();
	}

	public static String luminousBlockRegistryName(DyeColor color, boolean translucent)
	{
		return (translucent ? "luminous_block_translucent_" : "luminous_block_") + color.getName();
	}

	public static void registerBlocks(RegistryEvent.Register<Block> blockRegistryEvent)
	{
		IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

		registry.register(new FertilizedDirtBlock().setRegistryName("fertilized_dirt"));
		registry.register(new RainbowLampBlock().setRegistryName("rainbow_lamp"));

		registry.register(new AdvancedRedstoneTorchBlock().setRegistryName("advanced_redstone_torch"));
		registry.register(new AdvancedRedstoneWallTorchBlock().setRegistryName("advanced_redstone_wall_torch"));

		registry.register(new SticksBlock(false).setRegistryName("block_of_sticks"));
		registry.register(new SticksBlock(true).setRegistryName("block_of_sticks_returning"));

		registry.register(new SuperLubricentStoneBlock().setRegistryName("super_lubricent_stone"));

		registry.register(new PlatformBlock().setRegistryName("platform_oak"));
		registry.register(new PlatformBlock().setRegistryName("platform_spruce"));
		registry.register(new PlatformBlock().setRegistryName("platform_birch"));
		registry.register(new PlatformBlock().setRegistryName("platform_jungle"));
		registry.register(new PlatformBlock().setRegistryName("platform_acacia"));
		registry.register(new PlatformBlock().setRegistryName("platform_darkoak"));
		
		registry.register(new BloodRoseBlock().setRegistryName("blood_rose"));

		registry.register(new LapisGlassBlock().setRegistryName("lapis_glass"));
		registry.register(new LapisLampBlock().setRegistryName("lapis_lamp"));
		registry.register(new QuartzGlassBlock().setRegistryName("quartz_glass"));
		registry.register(new QuartzLampBlock().setRegistryName("quartz_lamp"));
		registry.register(new SuperLubricentIceBlock().setRegistryName("super_lubricent_ice"));
		registry.register(new SuperLubricentPlatformBlock().setRegistryName("super_lubricent_platform"));
		registry.register(new TriggerGlassBlock().setRegistryName("trigger_glass"));
		registry.register(new CompressedSlimeBlock().setRegistryName("compressed_slime_block"));
		registry.register(new ContactButtonBlock().setRegistryName("contact_button"));
		registry.register(new ContactLeverBlock().setRegistryName("contact_lever"));
		registry.register(new SpectreBlock().setRegistryName("spectre_block"));

		registry.register(new SpectreLogBlock().setRegistryName("spectre_log"));
		registry.register(new SpectrePlankBlock().setRegistryName("spectre_plank"));
		registry.register(new SpectreLeafBlock().setRegistryName("spectre_leaf"));
		registry.register(new SpectreSaplingBlock().setRegistryName("spectre_sapling"));

		registry.register(new AcceleratorPlateBlock().setRegistryName("plate_accelerator"));
		registry.register(new BouncyPlateBlock().setRegistryName("plate_bouncy"));
		registry.register(new CollectionPlateBlock().setRegistryName("plate_collection"));
		registry.register(new CorrectorPlateBlock().setRegistryName("plate_corrector"));
		registry.register(new DirectionalAcceleratorPlateBlock().setRegistryName("plate_accelerator_directional"));
		registry.register(new ItemRejuvenatorPlateBlock().setRegistryName("plate_itemrejuvenator"));
		registry.register(new ItemSealerPlateBlock().setRegistryName("plate_itemsealer"));
		registry.register(new RedirectorPlateBlock().setRegistryName("plate_redirector"));
		registry.register(new RedstonePlateBlock(false).setRegistryName("plate_redstone"));
		registry.register(new RedstonePlateBlock(true).setRegistryName("plate_redstone_powered"));

		registry.register(new GlowingMushroomBlock().setRegistryName("glowing_mushroom"));
		registry.register(new SidedRedstoneBlock().setRegistryName("sided_redstone"));
		registry.register(new PitcherPlantBlock().setRegistryName("pitcher_plant"));

		registry.register(new BiomeGlassBlock().setRegistryName("biome_glass"));
		registry.register(new BiomeStoneBlock().setRegistryName("biome_stone_cobble"));
		registry.register(new BiomeStoneBlock().setRegistryName("biome_stone_smooth"));
		registry.register(new BiomeStoneBlock().setRegistryName("biome_stone_brick"));
		registry.register(new BiomeStoneBlock().setRegistryName("biome_stone_cracked"));
		registry.register(new BiomeStoneBlock().setRegistryName("biome_stone_chiseled"));
		registry.register(new ColoredGrassBlock().setRegistryName("colored_grass"));

		for (DyeColor color : DyeColor.values())
		{
			registry.register(new StainedBrickBlock(false).setRegistryName(stainedBrickRegistryName(color, false)));
			registry.register(new StainedBrickBlock(true).setRegistryName(stainedBrickRegistryName(color, true)));

			registry.register(new LuminousBlock().setRegistryName(luminousBlockRegistryName(color, false)));
			registry.register(new LuminousTranslucentBlock().setRegistryName(luminousBlockRegistryName(color, true)));
		}

		registry.register(new BeanSproutBlock().setRegistryName("bean_sprout"));
		registry.register(new BeanStalkBlock(true).setRegistryName("bean_stalk"));
		registry.register(new BeanStalkBlock(false).setRegistryName("lesser_bean_stalk"));
		registry.register(new PodBlock().setRegistryName("bean_pod"));

		registry.register(new LotusBlock().setRegistryName("lotus"));
		registry.register(new SakanadeBlock().setRegistryName("sakanade"));

		registry.register(new BlazingFireBlock().setRegistryName("blazing_fire"));

		registry.register(new AnalogEmitterBlock().setRegistryName("analog_emitter"));
		registry.register(new IgniterBlock().setRegistryName("igniter"));
		registry.register(new OnlineDetectorBlock().setRegistryName("online_detector"));
		registry.register(new ItemCollectorBlock().setRegistryName("item_collector"));
		registry.register(new ExtractionPlateBlock().setRegistryName("plate_extraction"));
		registry.register(new ProcessingPlateBlock().setRegistryName("plate_processing"));

		registry.register(new AdvancedRedstoneRepeaterBlock().setRegistryName("advanced_redstone_repeater"));
		registry.register(new IronDropperBlock().setRegistryName("iron_dropper"));

		registry.register(new PlayerInterfaceBlock().setRegistryName("player_interface"));
		registry.register(new InventoryTesterBlock().setRegistryName("inventory_tester"));
		registry.register(new InventoryRerouterBlock().setRegistryName("inventory_rerouter"));
		registry.register(new ChatDetectorBlock().setRegistryName("chat_detector"));
		registry.register(new RedstoneObserverBlock().setRegistryName("redstone_observer"));
		registry.register(new PotionVaporizerBlock().setRegistryName("potion_vaporizer"));

		registry.register(new SpecialChestBlock(0).setRegistryName("special_chest_nature"));
		registry.register(new SpecialChestBlock(1).setRegistryName("special_chest_water"));

		registry.register(new EntityDetectorBlock().setRegistryName("entity_detector"));
		registry.register(new FilteredRedirectorPlateBlock().setRegistryName("plate_filtered_redirector"));
		registry.register(new SlimeCubeBlock().setRegistryName("slime_cube"));
		registry.register(new AdvancedItemCollectorBlock().setRegistryName("advanced_item_collector"));
		registry.register(new FilteredSuperLubricentPlatformBlock().setRegistryName("filtered_super_lubricent_platform"));
		registry.register(new NotificationInterfaceBlock().setRegistryName("notification_interface"));
		registry.register(new GlobalChatDetectorBlock().setRegistryName("global_chat_detector"));
	}
}
