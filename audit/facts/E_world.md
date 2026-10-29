# E_world: PLANTS / WORLD / WORLDGEN / LOOT / CONFIG fact sheet (1.14.4 baseline)
All paths relative to MOD (= scratchpad/mod1144). J = src/main/java/lumien/randomthings, R = src/main/resources.
Evidence: [code] read in source; [test] automated test (name + file:line); [playtest] archive checklist row with PASS (docs/testing/checklist-archive/*.md, user results 2026-10-04/11); [unverified].
Note: open TESTING_CHECKLIST.md rows are blank (untested by a person); the archive holds the PASS rows.

## bean (Beans)  [registry: items randomthings:beans, lesser_magic_bean, magic_bean, bean_stew; blocks bean_sprout, bean_stalk (magic), lesser_bean_stalk, bean_pod; worldgen feature bean_sprout]
OBTAIN:
- Wild Bean Sprout: BeanSproutFeature in EVERY biome (loop over ForgeRegistries.BIOMES), VEGETAL_DECORATION, CHANCE_HEIGHTMAP ChanceConfig(2) = ~1 attempt in 2 chunks; no biome restriction, no config gate; needs air at pos, y 1..254 and sprout valid position [code J/RandomThings.java:699, J/worldgen/BeanSproutFeature.java:24-34 (disp+10 offset: place() body lines 24-34)] [test worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block debug/test/WorldgenTests.java:711]
- Beans item: drop of Bean Sprout. bean_sprout.json: pool1 always 1 beans; pool2 (only block state age=7) +2-3 beans => young break = 1, mature break = 3-4 [code R/data/randomthings/loot_tables/blocks/bean_sprout.json] ; right-click a mature sprout (age 7, CropsBlock max age) = reset to age 0 and give 1-2 beans (nextInt(2)+1) to inventory or ground, sprout stays [code J/block/BeanSproutBlock.java:54-70] [playtest archive #62 PASS]
- Lesser Magic Bean: shaped "NNN/NBN/NNN", N = tag forge:nuggets/gold, B = randomthings:beans -> 1 [code R/data/randomthings/recipes/lesser_magic_bean.json]
- Magic Bean (randomthings:magic_bean, Rarity.RARE): NO recipe, NO loot table entry, NO worldgen source in code (grep of whole src/main finds only registration/lang/model). Creative tab only [code J/item/ModItems.java:207; grep]. Nature Core / "plant chest" is NOT ported (docs/project/wiki-feature-status.md:593-594 lists Nature Core as out of scope).
- Bean Stew: two shaped recipes: bean_stew "W/BBB/O" and bean_stew_2 "BBB/W/O" (W wheat, B beans, O bowl) -> 1 stew [code R/.../recipes/bean_stew.json, bean_stew_2.json]
- Bean Pod: only produced by a magic bean stalk reaching the cap (see below); no recipe. bean_pod.json = 5 separate pools, each survives_explosion: iron_ingot 8-20, gold_ingot 4-15, diamond 1-5, emerald 0-2, beans 4-8 (all always rolled, no chance) [code R/.../loot_tables/blocks/bean_pod.json]. PodBlock.getDrops adds 1 Golden Egg if RTConfig.GOLDEN_EGG (default true); with an explosion the egg survives with chance 1/radius, else always [code J/block/PodBlock.java:34-49] [test checklist.config.golden_egg_off_removes_the_egg_from_bean_pods debug/test/ChecklistTests.java:281]
- Bean Stalk blocks (bean_stalk, lesser_bean_stalk): NO loot table -> drop nothing (checklist #333) [code: absent from R/.../loot_tables/blocks listing].
BEHAVIOR:
- Planting: BeanItem.onItemUse only on the UP face, target space must be air, block default state must be valid position; places block, plays place sound, shrinks stack by 1 (also in creative: no creative check). Beans -> bean_sprout (no scheduled tick); lesser/magic -> stalk + scheduleTick 20 [code J/item/BeanItem.java:32-62] [test use_item.beans.plant_only_on_top_of_soil_and_use_up_one_bean debug/test/UseItemTests.java:438] [playtest archive #59-61 PASS]
- Sprout (CropsBlock subclass): valid ground = grass block, dirt, coarse dirt, podzol, farmland (vanilla crop growth/bonemeal logic inherited) [code J/block/BeanSproutBlock.java:48-51]. Cutout render, no collision.
- Stalk growth: on each scheduled tick, if y+1 >= 255 stop; magic only: if y+1 >= 253 place Bean Pod at y+1 and stop; else grows only if block above is air OR replaceable material (grass, snow layer, water etc.); next tick 5 ticks (lesser) / 1 tick (magic); plays place sound each segment. If the block above is solid the stalk just STOPS (no breaking, no pod) [code J/block/BeanStalkBlock.java:71-103 (disp+18: lines 81,86-88,94,97)]. Lesser stalk never makes a pod [code same].
- Stalk valid ground: below is another stalk of same block, grass, dirt, coarse dirt, podzol or farmland (farmland is a deliberate extra); loses support => becomes air [code BeanStalkBlock.java:116-122].
- Climbing: isLadder true; plus onEntityCollision pushes entity up 0.5 (magic) / 0.2 (lesser) per tick when not on ground and motion.y >= 0.1 [code BeanStalkBlock.java:56-69,132-135].
- Bean Pod: hardness 1.5, no collision [code PodBlock.java:30].
- Bean Stew: food 8 hunger, 0.6 saturation, stack size 1; returns a bowl (to inventory else dropped) [code J/item/ModItems.java:218, J/item/BeanStewItem.java:20-41] [unverified in play: archive row #121 blank]
- Other uses of beans: Floo Powder, Super Lubricent Tincture recipes; Lesser Magic Bean in Imbuing Station -> Imbue Experience [code RandomThings.java:719, recipes floo_powder.json, super_lubricent_tincture.json].
CONFIG: [features] GoldenEgg=true (Bean Pod also drops Golden Egg) [code J/config/RTConfig.java:64]. No config for sprout generation.
DIVERGENCE: stalk simplified - "self-destroys-into-a-pod-when-blocked" edge case dropped [code BeanStalkBlock.java:5-17]; farmland as valid ground is an addition; growth sound cue is new (not 1.12.2) [code BeanStalkBlock.java:99-101]; Nature Core absent.
PLAYTEST-NEEDED: stalk climb feel; sound spam.
PARITY: none stated.
WIKI-CLAIM-CHECK:
- "magic bean ... continue to grow till the height limit breaking any blocks in its way" -> CONTRADICTED: stalks never break blocks, they stop at the first non-air/non-replaceable block (BeanStalkBlock.java:94).
- "Lesser ... continue to grow till it hits a block" -> confirmed (lesser stops at solid block or y 255).
- "If you find a Nature Core ... plant chest ... always contain a magic bean" -> not-in-code (Nature Core not ported; magic bean has no obtain path other than creative).
- "On top of it will also be a bean pod that drops random amounts of beans, iron, gold, diamonds and emeralds" -> confirmed for pod existing at y=253 cap (magic stalk only, and only if not blocked earlier) and its drops; also drops a Golden Egg (wiki omits).
- "Bean Stew slightly better than mushroom stew" -> 8 hunger/0.6 sat vs vanilla mushroom stew 6 (vanilla value from memory, not in repo).
- "quickly climb" -> confirmed (isLadder + push).
- "breaking them will give you some beans" -> confirmed: 1 (young) / 3-4 (mature).
- Wiki images show two crafting images; code has Lesser Magic Bean + stew (2 recipes) only; no recipe for Magic Bean.

## lotus (Lotus)  [registry: block lotus; items lotus_seeds, lotus_blossom; feature lotus]
OBTAIN:
- Wild: LotusFeature in every biome's VEGETAL_DECORATION, CHANCE_HEIGHTMAP(10) (~1 in 10 chunks per attempt); place() requires config Lotus, y>=0, biome has BiomeDictionary.Type.SNOWY, target is air/replaceable, then random age 0-3, requires valid position (ground grass/dirt/farmland) [code J/RandomThings.java:700, J/worldgen/LotusFeature.java:35-61 (disp+14)] [test worldgen.lotus.snowy_biome_check_and_refuses_in_a_non_snowy_biome WorldgenTests.java:736]
- Breaking the lotus (any age) drops 1 lotus_seeds (loot table redirects block to seeds, not self) [code R/.../loot_tables/blocks/lotus.json]. Pick-block gives seeds [code J/block/LotusBlock.java:103].
- Lotus Blossom: right-click lotus at age 3 -> resets to age 0, gives 1 blossom (inventory else ground) [code LotusBlock.java:80-98] [test checklist.lotus.grows_through_four_stages_and_is_harvested_at_the_last_without_breaking ChecklistTests.java:436] [playtest archive #67, #67.1 PASS]
- No crafting recipes for either (recipes dir has none).
BEHAVIOR:
- Growth: random tick, 1/10 chance to age +1 up to 3; no light/water/time requirement [code LotusBlock.java:67-75]. Ground: grass block, dirt, farmland only [code :63-66]. Shapes height 5/9/11/14 px by age.
- Lotus Seeds: right-click UP face, space above air, ground must canSustainPlant (PlantType.Plains) -> places age-0 lotus, shrink 1 [code J/item/LotusSeedsItem.java:22-47] [test use_item.lotus_seeds.plant_on_grass_but_not_on_stone UseItemTests.java:457] [playtest #66 PASS]. (Plains-sustaining ground that LotusBlock itself rejects, e.g. podzol/coarse dirt: [unverified] what happens.)
- Lotus Blossom: right-click = eat animation 10 ticks (UseAction.EAT, NOT a food item: no hunger), on finish spawns XP orbs totalling 3 + nextInt(5) + nextInt(5) = 3..11, consumes 1 (also in creative) [code J/item/LotusBlossomItem.java:25-48] [test use_item.lotus_blossom.eating_it_drops_three_to_eleven_experience UseItemTests.java:472]
CONFIG: [worldgen] Lotus=true [code RTConfig.java:48].
DIVERGENCE: dead IGrowable bonemeal code from 1.12.2 dropped [code LotusBlock.java:5-9].
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK:
- "find Lotus plants in biomes with the Snowy tag" -> confirmed (BiomeDictionary SNOWY, LotusFeature.java:43-47).
- "fully grown ... right click ... Lotus Blossom ... eat to get some experience" -> confirmed (3-11 XP).
- "When broken, Lotus plants will drop 1 lotus seed" -> confirmed at all ages (lotus.json has no age condition).

## glowing_mushrooms (Glowing Mushrooms)  [registry: block+item glowing_mushroom; feature glowing_mushroom; item luminous_powder uses it]
OBTAIN:
- Worldgen: GlowingMushroomFeature in every biome, UNDERGROUND_DECORATION, CHANCE_HEIGHTMAP(4) (~1 patch per 4 chunk-attempts); centre Y = random in [0, surfaceY-4) (needs surfaceY-4 > 0); 64 tries within +-7 x/z, +-3 y; places only in air with soil (grass, dirt, stone, fertilized dirt, mycelium) below and NO sky access; light rule skipped during worldgen [code J/RandomThings.java:702, J/worldgen/GlowingMushroomFeature.java:29-61 (disp+13), J/block/GlowingMushroomBlock.java:115-125 (disp+311 numbering)] [test worldgen.glowing_mushroom.* debug/test/WorldgenTests.java:344,377,415,440,479,494]
- Block drops itself (loot table glowing_mushroom.json, default self-drop). Recipe: Luminous Powder shaped "DG/M" (glowstone dust, glass, glowing mushroom) -> 4 luminous_powder [code R/.../recipes/luminous_powder.json].
- No other recipe uses it. NO brewing recipe / glowstone replacement exists anywhere in code (grep of src/main for glowing_mushroom/brewing finds none) [code grep].
BEHAVIOR:
- Light level 12 (must stay <13 or it self-destroys) [code GlowingMushroomBlock.java:29-31]. Survives if soil is mycelium (any light) or (light <13 AND soil in {grass, dirt, stone, fertilized dirt}) [code :126-146 (numbers: canBlockStay 437-452 in disp = 126-141 real)]. Drops itself if support/light fails (neighbor update + tick).
- Spreads: random tick, 1/20 chance; counts mushrooms in a 9x3x9 box (-4..4, -1..1, -4..4); if 5 or more it does nothing; else random-walks up to 4 steps (+-1 xz, +-1 y) over air-with-valid-spot and sets a new mushroom [code :63-99 (disp 374-410)] [test worldgen.glowing_mushroom.spreads_in_a_dark_cave WorldgenTests.java:494]
- Hardness 0, no collision, cutout; shape 6.4px wide, 6.4 tall.
CONFIG: [worldgen] GlowingMushrooms=true [code RTConfig.java:51]; feature reads it per place() [GlowingMushroomFeature.java:35-37] [test worldgen.glowing_mushroom.config_switch_stops_the_patches WorldgenTests.java:415]
COREMOD: none involved (the wiki-brief listed "coremod" for this one: no transformer references glowing mushrooms; 1.12.2 used ASM for brewing, not ported).
DIVERGENCE: none stated; brewing use not ported.
PLAYTEST-NEEDED: visual glow in cave; archive row #49 (block) blank, #341 (worldgen) blank.
WIKI-CLAIM-CHECK:
- "can be found in caves" -> confirmed (underground, no sky access, any biome incl. Nether/End biomes iterated too but needs air+soil from list).
- "replacement for glowstone in all brewing recipes" -> NOT-IN-CODE (no brewing integration).
- "used to craft Luminous Powder" -> confirmed (needs glowstone dust + glass too).

## colored_grass (Colored Grass)  [registry: block colored_grass (state color = 16 DyeColor); items colored_grass (no tab), grass_seeds, grass_seeds_<16 colours>]
OBTAIN:
- Grass Seeds: shapeless 1 minecraft:grass_block -> 1 grass_seeds (NO silk-touch/shovel requirement in the recipe itself; a grass block must be obtained first) [code R/.../recipes/grass_seeds.json]
- Coloured seeds: 16 shapeless recipes grass_seeds + <colour>_dye -> grass_seeds_<colour> (white, orange, magenta, light_blue, yellow, lime, pink, gray, light_gray, cyan, purple, blue, brown, green, red, black; all vanilla 1.14 dye items) [code R/.../recipes/grass_seeds_*.json]. So 17 seed items total [code J/item/ModItems.java:289-292].
- Colored Grass block item has NO creative-tab group (acquired via seeds or Silk Touch) [code ModItems.java:275-286].
- Loot: Silk Touch (match_tool enchantment silk_touch >=1) -> colored_grass item with BlockStateTag {color}; otherwise dirt [code R/.../loot_tables/blocks/colored_grass.json] [test retest.loot.colored_grass_drops_dirt_and_only_itself_with_silk_touch debug/test/RetestBehaviorTests.java:74]
BEHAVIOR:
- Grass Seeds item: right-click a plain minecraft:dirt block only (not coarse dirt/podzol/fertilized dirt/grass): plain seed -> vanilla grass_block; coloured -> colored_grass with that colour; consumes 1 seed except in creative [code J/item/GrassSeedsItem.java:35-56 (disp-119)] [test use_item.grass_seeds.turn_dirt_into_grass_and_the_dyed_ones_into_coloured_grass UseItemTests.java:497] [playtest #56 PASS user 2026-10-04]
- Spread: random tick; if light above <4 and above block opacity >2 -> reverts to DIRT; else if light above >=9: 4 tries at random offset (+-1 x/z, -3..+1 y); target must be exactly Blocks.DIRT with light>=4 and opacity<=2 above -> becomes the same coloured grass (copies state). Never converts vanilla grass or other grass colours [code J/block/ColoredGrassBlock.java:78-111]
- Hardness 0.6, harvest tool SHOVEL (preference only), PLANT sound, tinted via colorMultiplier = DyeColor float components [code ColoredGrassBlock.java:28,39-43].
- Naming: item/block name "<Color> Grass Block" via lang block.randomthings.colored_grass.<colour>; block getNameTextComponent hardcoded "White Grass Block" fallback [code ModItems.java:279-284, ColoredGrassBlock.java:64-68, R/assets/randomthings/lang/en_us.json:33-48,341-344]. Seeds named "<Color> Grass Seeds" (lang :132-148).
CONFIG: none.
DIVERGENCE: seeds not consumed in creative (comment: unlike 1.12.2) [code GrassSeedsItem.java:138 comment]; naming/shovel preference per docs/project/scope-decisions.md:10.
PLAYTEST-NEEDED: tint colours vs dye.
PARITY: none.
WIKI-CLAIM-CHECK:
- "Dye Grass in any of the vanilla colors" -> confirmed, all 16 dye colours.
- "pick up some grass with your silktouch shovel, put it in a crafting table to get grass seeds and then color the seeds" -> partly: recipe takes a grass block (needs silk touch to get one, but any tool path to the block works, e.g. creative); dye step is shapeless seeds+dye.
- "Right clicking with a grass seed on a piece of dirt turns it into grass with the colour" -> confirmed (plain dirt only).
- "spreads like normal grass but will not grow over normal grass" -> confirmed (spreads to Blocks.DIRT only; also reverts to dirt when covered).

## fertilized_dirt (Fertilized Dirt)  [registry: block+item fertilized_dirt (state tilled), creative tab icon]
OBTAIN: shaped "RBR/BDB/RBR" R rotten_flesh, B bone_meal, D dirt -> 2 fertilized_dirt [code R/.../recipes/fertilized_dirt.json]. Drops itself [loot fertilized_dirt.json default]. Is the creative tab icon [code ModItems.java:443].
BEHAVIOR:
- Hardness 0.5, ground sound, ticks randomly, shovel effective [code J/block/FertilizedDirtBlock.java:36,63-66 (disp-181)].
- Growth boost: on its own random tick, up to 3 times it random-ticks the plant block directly above (only if that block is IPlantable and itself ticksRandomly; stops at first non-plant) -> extra plant ticks ~3 per dirt random tick (so ~4x the plain rate for a plant that also ticks normally; exact factor derived, not measured) [code FertilizedDirtBlock.java:43-61] [unverified: no test].
- Hoe: UseHoeEvent sets state tilled=true (plays hoe sound); tilled block has 15px collision/shape; no water requirement; isFertile true; there is NO trample/moisture logic at all (no onFallenUpon override) so crops are never destroyed [code J/RandomThings.java:112-125, FertilizedDirtBlock.java:75-79,126-129]
- Planting rules (canSustainPlant): Crop plants need tilled; Plains/Beach/Desert/Cave need untilled (Plains also allowed tilled only for beetroot); Nether/Water never; attached stems always [code FertilizedDirtBlock.java:92-123].
- Placing a sapling grown tree no longer turns it to dirt (BlockEvent.EntityPlaceEvent cancelled when dirt replaces fertilized dirt) [code RandomThings.java:654-661].
- Glowing mushroom may stand on it [code GlowingMushroomBlock.java:146 (real)].
CONFIG: none.
WIKI-CLAIM-CHECK:
- "same as vanilla farmland, no nearby water" -> confirmed. - "increases growth by factor of 3" -> approximately (3 extra ticks per dirt tick; exact multiplier is derived: 3 extra random ticks per dirt random tick, not "x3 total"; see code). - "crops not destroyed when walking/jumping" -> confirmed (no trample code).

## ancient_furnace (Ancient Furnace)  [registry: block+item ancient_furnace (state heating), TE ancient_furnace, feature ancient_furnace]
OBTAIN: not craftable; worldgen only (+creative tab). AncientFurnaceFeature in every biome, LOCAL_MODIFICATIONS, CHANCE_HEIGHTMAP(400) (~1 per 400 chunk-attempts); place(): config AncientFurnace, ground = surface-1 must be solid non-liquid, 1<y<255, biome.getTemperature(ground) < 0.15 (cold), replaces that ground block, not onto itself [code J/RandomThings.java:701, J/worldgen/AncientFurnaceFeature.java:33-60 (disp+11)] [test worldgen.ancient_furnace.generates_only_in_cold_biomes_on_solid_ground WorldgenTests.java:753] [playtest #262 PASS]. Loot: drops itself (hardness 50, resistance 10, Material.ROCK, no tool requirement) [code J/block/AncientFurnaceBlock.java:42; loot ancient_furnace.json] [playtest #266 PASS].
BEHAVIOR:
- Right-click with a Nether Star (any hand): consumes 1 unless creative, sets heating=true, TE.startHeating, fire-charge sound. Anything else or already heating: nothing [code AncientFurnaceBlock.java:63-93] [playtest #263,#264 PASS]
- Heat time HEATING_DURATION = 20*60*5 = 6000 ticks (5 min), state saved in NBT keys heating, ticks [code J/tileentity/AncientFurnaceTileEntity.java:77,233-234 (disp-108)] [playtest #267 PASS]. Flame+smoke particles while heating (client) [AncientFurnaceBlock.java:95-107].
- On finish: warmSurroundings then furnace removed and explosion power 4.0 Mode.BREAK at the block [code TE:145-149].
- warmSurroundings: ALL loaded columns inside circle radius 56 (fixed constant) whose current biome is a key of COLD_TO_WARM are rewritten in the chunk biome array; then in y [top-4, top+2] of that column: Blocks.SNOW and SNOW_BLOCK -> air, ICE/PACKED_ICE/BLUE_ICE/FROSTED_ICE -> water. Columns whose biome is not in the map are left untouched (no melting). Chunks resent to tracking players [code TE:76,157-192,215-228]
- Conversion table (13 entries): taiga->forest, frozen_ocean->ocean, frozen_river->river, snowy_tundra->plains, snowy_mountains->wooded_hills, taiga_hills->wooded_hills, snowy_beach->beach, snowy_taiga->birch_forest, snowy_taiga_hills->birch_forest_hills, giant_tree_taiga_hills->birch_forest_hills, taiga_mountains->tall_birch_forest, ice_spikes->flower_forest, snowy_taiga_mountains->tall_birch_hills [code TE:100-116]. Not converted: mountains, giant_tree_taiga, etc.
- Test [test worldgen.ancient_furnace.heating_converts_cold_biomes_melts_snow_and_ice_and_leaves_warm_biomes_alone WorldgenTests.java:788; client resync test :837]. Playtest row #265 archive status FIXED (client sync bug), open-checklist row 265 blank.
CONFIG: [worldgen] AncientFurnace=true (generation only). Area is NOT configurable (hardcoded radius 56 ~ 9850 columns; TE javadoc says config "not built yet") [code RTConfig.java:50; TE:71-76 comments].
DIVERGENCE: 1.12.2 flood-fill paced conversion replaced with instant radius scan [code TE class javadoc]; hardness/duration/explosion values are guesses, wiki gave none [AncientFurnaceBlock.java:33-36].
PLAYTEST-NEEDED: visual biome colour change after heat-up; explosion feel.
WIKI-CLAIM-CHECK:
- "rarely find in cold biomes" -> confirmed (temperature < 0.15, 1/400). - "nether star on the opening on top" -> confirmed (any click face, not only the top; no top-face check). - "area of 10000 blocks (configurable)" -> area ~= pi*56^2 ~ 9.85k columns confirmed, "configurable" CONTRADICTED (constant, no config key). - "warmer biome, removing surface snow" -> confirmed only for biomes in the 13-entry table. - "explode once done" -> confirmed.

## peace_candle (Peace Candle)  [registry: block+item peace_candle, TE peace_candle; jigsaw piece randomthings:peace_candle_church; coremod SpawnPlacementTransformer]
OBTAIN: Creative tab (registerItemForBlock) [code ModItems.java:384]; NO recipe. Village: PeaceCandleChurchPool wraps jigsaw pool village/plains/houses (installed at FMLServerAboutToStartEvent); when pool is asked for a random/shuffled piece list, with probability 1/3 (and config on) every vanilla church piece Single[minecraft:village/plains/houses/plains_temple_4] is replaced by randomthings:peace_candle_church (NBT R/data/randomthings/structures/peace_candle_church.nbt, same footprint); other plains houses, other biome villages and zombie villages untouched [code J/worldgen/PeaceCandleChurchPool.java:79-101,104-110, J/RandomThings.java:104] [test worldgen.peace_candle.* WorldgenTests.java:520,533,580,605,617,635] [playtest #270 listed in archive; result column not confirmed]
BEHAVIOR:
- Block: hardness 0, no collision, 4x10x4 px, needs solid block below else drops itself (neighbor update); loot default self-drop [code J/block/PeaceCandleBlock.java:40,60-70] [playtest #268 PASS].
- Effect: TE registers itself in a static weak set; AsmHandler.overrideSpawnResult (called at the end of EntitySpawnPlacementRegistry.func_223515_a via coremod transformer/SpawnPlacementTransformer.js) returns false for any EntityType whose classification is MONSTER when a candle TE in the same world is within Chebyshev distance 3 CHUNKS (square of chunks, 7x7 around the candle chunk) of the spawn position; only natural/placement-predicate spawns - not other sources [code J/asm/AsmHandler.java:164,181-187; J/tileentity/PeaceCandleTileEntity.java:31-40 (disp-81), R/transformer/SpawnPlacementTransformer.js] [test coremod.spawn.peace_candle_denies_monster_spawns_in_range debug/test/CoremodTests.java:101] [playtest #269 PASS]
- ORDER QUIRK: EntityType.SLIME is handled first and returns early (Slime Cube/vanilla result), so a Peace Candle does NOT block slimes [code AsmHandler.java:169-179, before :181].
- Only MONSTER classification: passive/ambient/water creatures unaffected. Lapis Lamp / Quartz Lamp scan is checked after the candle.
CONFIG: [worldgen] PeaceCandle=true (church swap; read each world load, no restart) [code RTConfig.java:49, PeaceCandleChurchPool.java:79].
DIVERGENCE: 1.12.2 patched village church via ASM; here jigsaw pool wrapper, plains only (docs: wiki-feature-status.md:377 states 1.12.2 covered every village biome). PeaceCandleBlock javadoc is stale (still says creative-only) [code PeaceCandleBlock.java:25-34].
PLAYTEST-NEEDED: church look; actual spawn suppression over time.
PARITY: wiki-feature-status.md:377 says 1.12.2 covered every village biome.
WIKI-CLAIM-CHECK: "prevent Natural Mob Spawning in a 3 Chunk Radius" -> confirmed for MONSTER classification only, not slimes, not animals. "chance of 33% for one to be in a village church" -> approximately confirmed for plains villages only.

## slime_cube (Slime Cube)  [registry: block+item slime_cube (state powered), TE slime_cube; coremod SpawnPlacementTransformer]
OBTAIN: shaped "S/SNS/S" S slime_ball, N nether_star -> 1 [code R/.../recipes/slime_cube.json]. Chest loot: simple_dungeon weight 10/100, jungle_temple 80/100 (see WORLD-LEVEL), config SlimeCube. Drops itself.
BEHAVIOR:
- Block: Material.CLAY, hardness 0.5, no collision, 4px cube, translucent; neighborChanged sets POWERED = isBlockPowered (any redstone) and pushes into TE [code J/block/SlimeCubeBlock.java:31-82].
- Effect: same dispatch hook: for EntityType.SLIME only, if any registered cube TE is in the same world AND the same chunk (x>>4, z>>4) as the spawn pos, the result is overridden to `!cube.isPowered()`: unpowered = always ALLOW (bypasses vanilla light/height/slime-chunk/difficulty predicate result), powered = always DENY; no cube -> vanilla [code J/asm/AsmHandler.java:169-179; J/tileentity/SlimeCubeTileEntity.java:60-67] [test coremod.spawn.slime_cube_allows_slimes_unpowered_and_denies_them_when_powered CoremodTests.java:136; ...only_affects_slimes_in_its_own_chunk_and_stops_when_removed :147] [playtest #110,#111 PASS]
- No spawn-rate multiplier exists in code (only a gate). TE `powered` has no NBT write/read, so after reload it is false until a neighbor update, even if blockstate says powered [code SlimeCubeTileEntity.java (no write/read overrides)] [unverified how often this matters].
- Powered DENY applies to ALL slime spawn attempts in that chunk (also swamp), not just slime chunks [code same].
CONFIG: [loot] SlimeCube=true.
DIVERGENCE: tile-entity javadoc says it was replaced by a CheckSpawn listener; actual mechanism is the coremod dispatch hook (javadoc stale) [code SlimeCubeTileEntity.java:15-22 vs AsmHandler].
WIKI-CLAIM-CHECK: "allow Slimes to spawn in the Chunk, any height" -> confirmed as ALLOW-gate, "significantly increased rate" -> NOT-IN-CODE (no rate change; just forced allow). "Powered ... prevents slimes from spawning through vanilla spawning mechanics (Slime Chunk / Swampland)" -> confirmed (denies slime placement).

## biome_blocks (Biome Blocks)  [registry: item biome_crystal; block biome_radar; item biome_sensor; NO biome stone/glass]
- Biome Blocks (BiomeStone/BiomeGlass) are NOT registered: removed (docs/project/wiki-feature-status.md:32 "REMOVED (user request 2026-10-03)"; WIKI_FEATURE_STATUS.md:5). ModBlocks/ModItems/lang have no biome_stone/biome_glass; only biome_radar, biome_crystal, biome_sensor [code grep ModBlocks/ModItems/en_us.json:201,260,261]. Stale: TESTING archive #349 still mentions them.
- Surviving: Biome Crystal (maxStack 1, tab item): NBT string "biomeName"; display name "Biome Crystal (<Biome>)", tooltip biome name, enchant-glint if biome has BiomeDictionary MAGICAL, tint = biome colour [code J/item/BiomeCrystalItem.java:34-89]. Tab version has no biome. Obtained ONLY via chest loot (below) / creative / give. Used in: biome_sensor recipe "III/RCI/IRI" (I forge:ingots/iron, R redstone, C biome_crystal) and Biome Radar insert [code R/.../recipes/biome_sensor.json; tests behavior.biome_radar.right_click_inserts_the_crystal... UtilityBehaviorTests.java:117].
- Loot injection: see WORLD-LEVEL. [test behavior.loot.rolled_chest_biome_crystals_carry_a_biome UtilityBehaviorTests.java:92]
WIKI-CLAIM-CHECK: "All of these Biome Blocks change colour depending on biome" -> NOT-IN-CODE (blocks removed). "Biome Crystals can be found in all dungeon chests" -> mostly confirmed but wider: 20% extra roll in EVERY minecraft:chests/* table (not just dungeons) and tuned to a uniformly random registered biome.

## pitcher_plant / custom_crafting_tables / inventory_rerouter
- Nothing registered. grep of ModBlocks/ModItems/ModTileEntityTypes/lang/recipes for pitcher, rerouter, custom crafting table finds nothing. Docs: wiki-feature-status.md:44 (Custom Crafting Tables REMOVED), :65 (Inventory Rerouter REMOVED 2026-10-03), :85 (Pitcher Plant REMOVED); WIKI_FEATURE_STATUS.md:5. AncientFurnaceFeature comment mentions pitcher only as history. [code]

# WORLD-LEVEL

## A. RTConfig (J/config/RTConfig.java) - file randomthings-common.toml (Type.COMMON, registered RandomThings.java:93); no client config; all read at use time
| section.key | default | effect (code) |
|---|---|---|
| worldgen.Lotus (:48) | true | LotusFeature.place returns false if off |
| worldgen.PeaceCandle (:49) | true | enables 1/3 church swap in plains houses pool |
| worldgen.AncientFurnace (:50) | true | AncientFurnaceFeature.place off switch |
| worldgen.GlowingMushrooms (:51) | true | GlowingMushroomFeature.place off switch |
| loot.MagicHood (:55) | true | Magic Hood into simple_dungeon (5) + village_weaponsmith (15) |
| loot.SummoningPendulum (:56) | true | Pendulum into simple_dungeon (10) + stronghold_corridor (50) |
| loot.BiomeCrystal (:57) | true | 20% crystal pool into every minecraft chests/* table |
| loot.SlimeCube (:58) | true | Slime Cube into simple_dungeon (10) + jungle_temple (80) |
| features.ArtificialEndPortal (:62) | true | Evil Tear on End Rod can create Artificial End Portal (EvilTearItem.java:35) |
| features.MagneticEnchantment (:63) | true | Magnetic obtainable at table/books and the loot modifier works (MagneticEnchantment.java:35-41, MagneticLootModifier.java:41); off keeps it registered |
| features.GoldenEgg (:64) | true | Bean Pod adds a Golden Egg (PodBlock.java:38) |
| features.EnderAnchorChunkloading (:65) | true | anchors register chunk (EnderAnchorTileEntity.java:35; tooltip EnderAnchorBlock.java:48) |
| visual.FlatRunes (:69) | false | flat rune texture (RuneBaseTileEntityRenderer.java:51) |
| visual.HideCoordinates (:70) | false | hides Portkey/Position Filter coordinates (PortkeyItem.java:71, PositionFilterItem.java:35) |
| numbers.BlockDestabilizerLimit (:74) | 50 (0..MAX; 0 = unlimited) | cap on destabilized blocks (BlockDestabilizerTileEntity.java:170) |
| numbers.TriggerGlassChainLimit (:75) | 20 (0 = unlimited) | cap on touching Trigger Glass per pulse; port-specific, 1.12.2 unlimited (TriggerGlassBlock.java:60) |
| numbers.SpiritLifeTime (:76) | 400 ticks (1..MAX) | Spirit despawns after this age (SpiritEntity.java:70) |
| numbers.SpiritChanceNormal (:77) | 0.01 (0..1) | base chance a player-killed entity spawns a Spirit (RandomThings.java:488); only kills by a real (non-fake) player, not of Spirits |
| numbers.SpiritChanceMoonMult (:78) | 2.0 (0..1000) | at night with sky access: + moonPhaseFactor/100 * mult (RandomThings.java:495-503) |
| numbers.SpiritChanceEndIncrease (:79) | 0.07 | added once the Ender Dragon has been killed (RandomThings.java:493) |
Tests: golden egg [test ChecklistTests.java:281], chest pools vs toggles [test ChecklistTests.java:390]. NOT in config (wiki may claim): Ancient Furnace area/duration, Lava Charm etc.

## B. Chest loot injection (J/handler/LootHandler.java, listener RandomThings.java:100, LootTableLoadEvent)
Pool form: one roll, entry item weight N vs empty entry weight 100-N => chance N% per chest roll of that table [code LootHandler.java:75-84].
| table | item | chance | config gate |
|---|---|---|---|
| chests/simple_dungeon | summoning_pendulum | 10% | loot.SummoningPendulum (:46-47) |
| chests/simple_dungeon | magic_hood | 5% | loot.MagicHood (:48-49) |
| chests/simple_dungeon | slime_cube (block item) | 10% | loot.SlimeCube (:50-51) |
| chests/village/village_weaponsmith | magic_hood | 15% | loot.MagicHood (:55-56) |
| chests/stronghold_corridor | summoning_pendulum | 50% | loot.SummoningPendulum (:60-61) |
| chests/jungle_temple | slime_cube | 80% | loot.SlimeCube (:65-66) |
| EVERY table with namespace minecraft and path starting "chests/" (incl. the 5 above) | biome_crystal tuned to a uniformly random biome (ForgeRegistries.BIOMES key list, new random draw per crystal) | RandomChance 0.2 per table roll, 1 roll, count 1 | loot.BiomeCrystal (:69-74, RandomBiomeFunction :87-100) |
Evidence [test behavior.loot.* UtilityBehaviorTests.java:92,111; ChecklistTests.java:390; archive #377 PASS "targets exist"]; chance maths not directly tested beyond ~20% crystal count.
Notes: Lava Charm and numbered Spectre Coils not ported (LootHandler.java javadoc). Config read at table load (restart/reload needed; archive #338).
Global loot modifier: R/data/forge/loot_modifiers/global_loot_modifiers.json -> randomthings:magnetic (config MAGNETIC_ENCHANTMENT).

## C. Worldgen (J/RandomThings.java:697-704 registerWorldgenFeatures; Forge 28.2.26, mutates every Biome in ForgeRegistries.BIOMES in all dimensions, no BiomeLoadingEvent)
| feature | stage | placement | conditions |
|---|---|---|---|
| bean_sprout | VEGETAL_DECORATION | CHANCE_HEIGHTMAP 1/2 | air, valid soil (grass/dirt/coarse/podzol/farmland), y 1..254; no config |
| lotus | VEGETAL_DECORATION | CHANCE_HEIGHTMAP 1/10 | config Lotus; SNOWY biome type; air/replaceable; grass/dirt/farmland below; random age 0-3 |
| ancient_furnace | LOCAL_MODIFICATIONS | CHANCE_HEIGHTMAP 1/400 | config AncientFurnace; temperature < 0.15; solid non-liquid ground replaced |
| glowing_mushroom | UNDERGROUND_DECORATION | CHANCE_HEIGHTMAP 1/4 | config GlowingMushrooms; patch below surface-4, no sky, soil list |
Structures: only jigsaw piece randomthings:peace_candle_church (R/data/randomthings/structures/peace_candle_church.nbt), swapped into village/plains/houses at 1/3 (config PeaceCandle). No Nature Core, no other structures. Also a Spectre dimension with void generator exists (ModDimensions; test worldgen.spectre.* WorldgenTests.java:885,898) - belongs to another group.
No worldgen Features other than these 4 (ModFeatures.java registerFeatures).

## D. Block loot tables that are NOT the default self-drop (R/data/randomthings/loot_tables/blocks, 122 files, all others = single item pool survives_explosion)
- advanced_redstone_wall_torch.json -> drops advanced_redstone_torch (item shared).
- bean_pod.json -> iron 8-20, gold 4-15, diamond 1-5, emerald 0-2, beans 4-8 (+Golden Egg in code, config).
- bean_sprout.json -> beans 1 always, +2-3 when age=7.
- colored_grass.json -> silk touch: coloured grass item with BlockStateTag per colour; else dirt.
- compressed_slime_block.json -> minecraft:slime_block (not itself).
- lotus.json -> lotus_seeds.
- spectre_leaf.json -> ectoplasm with random_chance 0.01818182 (~1/55) [no sapling].
- Registered blocks with NO loot table (drop nothing unless code overrides): bean_stalk, lesser_bean_stalk, blazing_fire, diaphanous_block (returns camo in code), floo_brick, rune_base, spectre_block, spectre_core. (Computed by diffing ModBlocks setRegistryName list (83) vs files.)
- 16 stained_brick_*, 16 luminous_stained_brick_*, 16 luminous_block_*, 16 luminous_block_translucent_* loot files exist for blocks registered in loops (default self-drop).

## E. Commands and key bindings
- Only command: /rtdebug (sub-commands allguis [delay], scene <id>, fixture build|verify, manual, test [filter], gui <id> [sneak]); requires permission level 2; registered ONLY when system property -Drandomthings.debug=true (DevMode.ENABLED); gradle runClient sets it. Dev-only, absent in normal play [code J/debug/DevMode.java:16, DevTools.java:41, RTDebugCommand.java:61-79,137-141]. No /randomthings (RTCommand) in this port (wiki-feature-status.md:597 says out of scope).
- Key bindings: none registered (grep KeyBinding/registerKeyBinding across src/main finds nothing) [code grep].

## F. Creative tab
- One ItemGroup "randomthings" (key itemGroup.randomthings), icon = fertilized_dirt block item [code J/item/ModItems.java:437-445]. Every item registered with `.group(RT_ITEM_GROUP)`: all registry items except the two registered without group: colored_grass block item and compressed_slime_block item [code ModItems.java:259,279]. No fillItemGroup tuning except Diaphanous Block item (adds one stack camo=stone) [code J/item/block/DiaphanousBlockItem.java:30-43]. Biome Crystal appears untuned. Magic Bean, Golden Egg, all 17 grass seeds, Peace Candle, Ancient Furnace, Bean Pod (no item? Bean Pod/stalks/sprout have no BlockItem - registerItemForBlock list does not include them [unverified: not exhaustively checked]) are in tab.
- Lang tooltips for items of this group: none (no tooltip keys for beans/lotus/etc. in en_us.json).

UNMAPPED:
- Item randomthings:magic_bean (no obtain path except creative) - belongs to bean page claims.
- Golden Egg item / thrown_golden_egg entity / golden_chicken (J/item/GoldenEggItem.java) - tied to bean_pod drop; wiki page elsewhere (not in my group).
- Bean Pod / Bean Stalk blocks have wiki text only inside "bean".
- Block spectre_leaf loot (ectoplasm 1/55) - other group.
- PeaceCandleChurchPool/peace_candle_church.nbt - structure with no separate wiki page.
- Wiki pages with nothing registered: biome_blocks (partly: crystal survives), pitcher_plant, custom_crafting_tables, inventory_rerouter.

OPEN QUESTIONS:
1. Does the SpawnPlacement hook run for spawner/spawn-egg paths? Affects whether Slime Cube/Peace Candle also act on those [unverified].
2. Fertilized dirt exact growth factor is derived, no test.
3. Lotus seeds on podzol/coarse dirt (canSustainPlant Plains vs LotusBlock.isValidGround) behavior [unverified].
4. Whether Bean Pod/Sprout/Stalk have no BlockItem (assumed from ModItems; not exhaustively diffed).
5. Archive #270 (peace candle church) result column not read.
