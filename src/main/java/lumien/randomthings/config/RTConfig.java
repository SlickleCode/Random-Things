package lumien.randomthings.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

/**
 * Common config ({@code randomthings-common.toml}), the 1.14.4 counterpart of 1.12.2's
 * {@code Worldgen}/{@code Features} {@code @ConfigOption} booleans. Only options for features
 * that exist in this port are included, and all are read at use time (never at registry time,
 * since the config isn't guaranteed loaded that early). Names/defaults match 1.12.2.
 */
public class RTConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final BooleanValue LOTUS;
	public static final BooleanValue PEACE_CANDLE;
	public static final BooleanValue ANCIENT_FURNACE;
	public static final BooleanValue GLOWING_MUSHROOM;

	public static final BooleanValue MAGIC_HOOD;
	public static final BooleanValue SUMMONING_PENDULUM;
	public static final BooleanValue BIOME_CRYSTAL;
	public static final BooleanValue SLIME_CUBE;

	public static final BooleanValue ARTIFICIAL_END_PORTAL;
	public static final BooleanValue MAGNETIC_ENCHANTMENT;
	public static final BooleanValue GOLDEN_EGG;
	public static final BooleanValue ENDER_ANCHOR_CHUNKLOADING;

	public static final BooleanValue FLAT_RUNES;
	public static final BooleanValue HIDE_COORDINATES;

	public static final ForgeConfigSpec.IntValue BLOCK_DESTABILIZER_LIMIT;
	public static final ForgeConfigSpec.IntValue TRIGGER_GLASS_CHAIN_LIMIT;

	public static final ForgeConfigSpec.IntValue SPIRIT_LIFETIME;
	public static final ForgeConfigSpec.DoubleValue SPIRIT_CHANCE_NORMAL;
	public static final ForgeConfigSpec.DoubleValue SPIRIT_CHANCE_MOON_MULT;
	public static final ForgeConfigSpec.DoubleValue SPIRIT_CHANCE_END_INCREASE;

	static
	{
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

		b.push("worldgen");
		LOTUS = b.comment("Generate Lotus plants in the world").define("Lotus", true);
		PEACE_CANDLE = b.comment("Replace roughly a third of plains village churches with a Peace Candle church").define("PeaceCandle", true);
		ANCIENT_FURNACE = b.comment("Generate Ancient Furnaces in the world").define("AncientFurnace", true);
		GLOWING_MUSHROOM = b.comment("Generate patches of Glowing Mushrooms underground").define("GlowingMushrooms", true);
		b.pop();

		b.push("loot");
		MAGIC_HOOD = b.comment("Add the Magic Hood to dungeon and village chests").define("MagicHood", true);
		SUMMONING_PENDULUM = b.comment("Add the Summoning Pendulum to dungeon and stronghold chests").define("SummoningPendulum", true);
		BIOME_CRYSTAL = b.comment("Add Biome Crystals to vanilla chests").define("BiomeCrystal", true);
		SLIME_CUBE = b.comment("Add the Slime Cube to dungeon and jungle temple chests").define("SlimeCube", true);
		b.pop();

		b.push("features");
		ARTIFICIAL_END_PORTAL = b.comment("Whether Evil Tears can create Artificial End Portals").define("ArtificialEndPortal", true);
		MAGNETIC_ENCHANTMENT = b.comment("Whether the Magnetic enchantment is obtainable (enchanting table, books, loot) and functions").define("MagneticEnchantment", true);
		GOLDEN_EGG = b.comment("Whether every Bean Pod also drops a Golden Egg").define("GoldenEgg", true);
		ENDER_ANCHOR_CHUNKLOADING = b.comment("Should Ender Anchors keep the chunk they are in loaded (lets an Ender Bridge reach an Anchor with nobody nearby; each Anchor holds one chunk loaded permanently). Anchors already loaded release their chunks the next time they load if this is turned off").define("EnderAnchorChunkloading", true);
		b.pop();

		b.push("visual");
		FLAT_RUNES = b.comment("Replaces the noisy default rune texture with a flat version").define("FlatRunes", false);
		HIDE_COORDINATES = b.comment("When true, the coordinates a Position Filter / Portkey points to are not shown in its tooltip").define("HideCoordinates", false);
		b.pop();

		b.push("numbers");
		BLOCK_DESTABILIZER_LIMIT = b.comment("How many blocks the Block Destabilizer can destabilize at once (0 = no limit)").defineInRange("BlockDestabilizerLimit", 50, 0, Integer.MAX_VALUE);
		TRIGGER_GLASS_CHAIN_LIMIT = b.comment("How many touching Trigger Glass blocks a single redstone pulse can trigger (0 = no limit; large values can lag). Port-specific: 1.12.2 had no limit at all, this port caps it at 20 by default after lag reports").defineInRange("TriggerGlassChainLimit", 20, 0, Integer.MAX_VALUE);
		SPIRIT_LIFETIME = b.comment("How long a Spirit stays in the world after spawning (20 = 1 second)").defineInRange("SpiritLifeTime", 20 * 20, 1, Integer.MAX_VALUE);
		SPIRIT_CHANCE_NORMAL = b.comment("The base chance of a spirit spawning when an entity dies (0.01 = 1%)").defineInRange("SpiritChanceNormal", 0.01, 0.0, 1.0);
		SPIRIT_CHANCE_MOON_MULT = b.comment("How much does the moon increase the chance of a spirit spawning (only at night under open sky, scaled by the moon phase)").defineInRange("SpiritChanceMoonMult", 2.0, 0.0, 1000.0);
		SPIRIT_CHANCE_END_INCREASE = b.comment("How much does the chance of a spirit spawning increase after the Ender Dragon has been defeated").defineInRange("SpiritChanceEndIncrease", 0.07, 0.0, 1.0);
		b.pop();

		SPEC = b.build();
	}
}
