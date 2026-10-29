# F_tools fact sheet (1.14.4 baseline). Paths relative to MOD (scratchpad/mod1144). 1.12.2 branch: origin/1.12.2 exists locally but was NOT consulted (not needed).
Abbrev: J=src/main/java/lumien/randomthings, R=src/main/resources, RC=R/data/randomthings/recipes, CL=TESTING_CHECKLIST archive docs/testing/checklist-archive, CBT=docs/testing/covered-by-tests.md

## divining_rods (Divining Rods)  [registry: items randomthings:divining_rod_{coal,iron,gold,lapis,redstone,emerald,diamond,vanilla}]
OBTAIN: 7 ore rods shaped "RSR/SES/SS" (R=ore item, S=stick, E=spider_eye): coal(minecraft:coal), iron(iron_ingot), gold(gold_ingot), lapis(lapis_lazuli), redstone(redstone), emerald(emerald), diamond(diamond); each x1 [code RC/divining_rod_*.json]. NOTE coal recipe key has "data":0 (ignored in 1.14) [code RC/divining_rod_coal.json].
OBTAIN: vanilla rod shaped "CSD/IBE/GLR": C=rod_coal S=stick D=rod_diamond I=rod_iron B=slime_ball E=rod_emerald G=rod_gold L=rod_lapis R=rod_redstone x1 [code RC/divining_rod_vanilla.json]. No chest/loot entry (grep LootHandler: none) [code J/handler/LootHandler.java:44-67].
BEHAVIOR: each rod stores tag-id strings + colors; item stack size 1, creative tab RT [code J/item/DiviningRodItem.java:19-24, ModItems.java:420-429].
BEHAVIOR: detects blocks by BLOCK TAG membership, not specific blocks: coal=forge:ores/coal, iron=forge:ores/iron, gold=forge:ores/gold, lapis=forge:ores/lapis, redstone=forge:ores/redstone, emerald=forge:ores/emerald, diamond=forge:ores/diamond (Tags.Blocks.ORES_*) [code J/item/ModItems.java:228-234]. Any mod block in those tags also matches [code DiviningRodItem.java:34-43]. Whether lit redstone ore / nether ores are in the Forge tag set: [unverified].
BEHAVIOR: vanilla rod = all 7 tags above in order coal,iron,gold,lapis,redstone,emerald,diamond (built from the scratch accumulators of the 7 registrations; Nether quartz and nether gold NOT included) [code ModItems.java:228-238, DiviningRodScratch.java:62-66]. First matching tag wins per block [code DiviningRodItem.java:35-40]. Each ore keeps its own color: coal(20,20,20,a50) iron(211,180,159) gold(246,233,80) lapis(5,45,150) redstone(211,1,1) emerald(0,220,0) diamond(87,221,229), all alpha 50 [code ModItems.java:228-234].
BEHAVIOR: purely CLIENT-side visual; nothing on the server. Active when a rod is in MAIN hand (checked first) else OFF hand [code J/client/renderer/DiviningRodRenderer.java:178-195]. Scan area = offsets -5..+5 on each axis = 11x11x11 cube centered on player.getPosition() (feet block) [code DiviningRodRenderer.java:198-219,196]. Scans 60 positions per client tick (full cube 1331 blocks = ~22.2 ticks per sweep) via a persistent cursor [code :198-217]. Only positions where world.isAreaLoaded(target,0) are tested [code :222].
BEHAVIOR: each hit creates a cube indicator lasting 160 ticks (8 s) drawn at block center, size = (1 - duration/160)*0.2+0.1 (grows from 0.1 to 0.3 blocks), depth test DISABLED (visible through walls), texture disabled, blended [code :46-85,72,227]. Same block is re-added on every sweep (no dedupe), so overlapping cubes accumulate [code :224-230, no contains check].
BEHAVIOR: hooked at ClientTickEvent END (tick) and RenderWorldLastEvent (render) [code J/client/ClientSetup.java:127-131,185-187]. shouldGlow() exists but has NO caller (grep) [code DiviningRodRenderer.java:46-49]. renderWat() is dead code (never called) [code :87].
CONFIG: none.
DIVERGENCE: modded-ore rods (aluminum, copper, tin, etc.) WILL NOT MIGRATE (user decision 2026-09-29) [docs/testing... see docs/project/wiki-feature-status.md grep "modded-ore Divining Rods" in CBT/state doc]. Rods are discrete items per ore (no metadata) [scope-decisions.md standing simplifications].
PLAYTEST-NEEDED: cube rendering/colors/alpha, through-wall visibility, sweep speed. No automated test or checklist row for rods found (grep tests, CL) [unverified].
WIKI-CLAIM-CHECK: "range 11x11x11 cube centered on you" -> confirmed (DiviningRodRenderer.java:198-219). "main or off hand" -> confirmed (:178-191). "basic rods show one ore, vanilla all vanilla ores" -> confirmed for the 7 ores (ModItems.java:228-238), but vanilla excludes nether quartz/nether gold. "Depending on mods more rods" -> not-in-code (no mod-specific rods; decision says will not migrate). Wiki lists 8 recipe images -> 8 recipes exist.

## emerald_compass (Emerald Compass)  [registry: items randomthings:emerald_compass, randomthings:id_card; recipe serializer randomthings:emerald_compass_set_target]
OBTAIN: shaped "E/ECE/E": E=emerald, C=minecraft:compass, x1 [code RC/emerald_compass.json]. Target-set recipe: special recipe type randomthings:emerald_compass_set_target [code RC/emerald_compass_set_target.json].
OBTAIN id_card: shaped "PI": paper + ink_sac, x1 (stack 64 default) [code RC/id_card.json, ModItems.java:334].
BEHAVIOR set-target: shapeless-style special recipe: exactly one emerald compass + exactly one ID card, nothing else in grid; ID card must have a "uuid" tag; result = compass copy with NBT string "uuid"; the ID card is returned unconsumed (count 1 copy) [code J/recipes/EmeraldCompassSetTargetRecipe.java:152-188,211-246].
BEHAVIOR compass: stack size 1 [code ModItems.java:338]. Every 20 game ticks on the SERVER, while the stack is anywhere in an inventory that ticks it (inventoryTick, any slot), if NBT "uuid" present: looks up that player in the server player list; online -> writes ints targetX/targetZ = player's block pos; offline -> removes targetX/targetZ [code J/item/EmeraldCompassItem.java:36-62]. Same-dimension is NOT checked (X/Z only; target in another dimension still points to its raw X/Z) [code :51-54].
BEHAVIOR needle: item property "angle" (same key as vanilla): if NBT has targetX -> needle points at (targetX+0.5, targetZ+0.5) relative to holder; no target -> random angle smoothed by wobble (only when held by an entity; item frames get a raw random angle) [code J/item/CompassItemBase.java:41-78]. Inventory-wide ticking of NBT every second means pointing updates at 1 Hz [code EmeraldCompassItem.java:40].
BEHAVIOR id_card: right-click (air) writes holder's uuid+name to NBT (server side); re-right-click overwrites; tooltip shows name; implements IEntityFilterItem: apply() true only for a player entity whose UUID == card uuid, or ANY player if card unbound [code J/item/IdCardItem.java:220-250].
BEHAVIOR reequip: no re-equip animation when only NBT changes [code EmeraldCompassItem.java:29-33].
CONFIG: none.
DIVERGENCE: set-target recipe drops a 1.12.2 copy-paste leftover check on "spectreAnchor" NBT (always true there) [code EmeraldCompassSetTargetRecipe.java javadoc :132-141].
TESTS: checklist #115 (ID card bind) PASS, #139-141 PASS [CL/5-batch-3-...-1.md rows 115,139-141 (via grep)]; no automated test for the compass itself (grep tests) [playtest/checklist].
PLAYTEST-NEEDED: needle visuals (32-frame model overrides) and wobble feel.
WIKI-CLAIM-CHECK: "points to a specific player; change target by combining with an ID Card in a crafting table" -> confirmed (recipe above). Omitted by wiki: compass stops tracking (random wobble) when target offline [code :56-60]; ID card is not consumed; cross-dimension ignored.

## golden_compass (Golden Compass)  [registry: item randomthings:golden_compass; serializer randomthings:golden_compass_set_position]
OBTAIN: shaped "G/GCG/G": G=gold_ingot, C=compass x1 [code RC/golden_compass.json]; set-position special recipe [code RC/golden_compass_set_position.json].
BEHAVIOR set-position: exactly one golden compass + exactly one position filter with stored position (hasPosition), nothing else; result = compass copy with ints targetX/targetZ = filter's X/Z; filter returned unconsumed; Y and dimension are dropped [code J/recipes/GoldenCompassSetPositionRecipe.java:294-330,353-367,372-390].
BEHAVIOR: item class has no logic; fresh compass wobbles randomly (matches 1.12.2 per javadoc) [code J/item/GoldenCompassItem.java:69-77]; needle logic shared CompassItemBase (see emerald) [code CompassItemBase.java:41-78]. Stack size 1 [code ModItems.java:337]. Target is static: no tick updates.
CONFIG: none.
TESTS: checklist #137-138 PASS [CL ... row 137,138].
PLAYTEST-NEEDED: needle rendering.
WIKI-CLAIM-CHECK: "set target using a position filter, combine in crafting grid" -> confirmed. "points toward the location of the position filter" -> confirmed for X/Z only (horizontal angle; Y/dimension ignored; compass in another dimension still points to raw X/Z).

## position_filter (Position Filter)  [registry: item randomthings:position_filter]
OBTAIN: shaped "D/DPD/D": D=minecraft:purple_dye, P=paper x1 [code RC/position_filter.json].
BEHAVIOR: use on block (server side) stores NBT hasPosition=true, dimension(int dim id), filterX/Y/Z = the CLICKED block's pos (not the offset face) and returns SUCCESS (so also consumes the use on interactive blocks? it overrides onItemUse only) [code J/item/PositionFilterItem.java:140-165]. Stack size 64 default (no maxStackSize set) [code ModItems.java:336].
BEHAVIOR tooltip: without Shift: "Hold Shift for more information"; with Shift and a position: Dimension: %d / Position-X / Y / Z lines [code PositionFilterItem.java:113-137; lang en_us.json:270,273-276]. Hidden entirely when config HideCoordinates=true [code :118-122].
BEHAVIOR other consumers: golden compass recipe, Biome Radar, Advanced Redstone Interface, Redstone Remote, Portkey, Spectre Cube, Filtered Super Lubricent Platform, Advanced Item Collector (files grep PositionFilter) [code grep: BiomeRadarTileEntity, AdvancedRedstoneInterfaceTileEntity, RedstoneRemoteActivateMessage, SpectreCube, PortkeyItem etc.].
BEHAVIOR highlight: NO renderer draws a purple (or any) cube around the stored block when the filter is held; the only drawCube caller is DiviningRodRenderer [code grep drawCube in J/: client/renderer/DiviningRodRenderer.java:74 only].
CONFIG: HideCoordinates (RTConfig key "HideCoordinates", default false) - hides coordinate tooltip and shift hint [code J/config/RTConfig.java:70]. Checklist #353 (human row, blank result in open list) [TESTING_CHECKLIST.md:60 unmarked].
TESTS: checklist #136 PASS (the archive row; wiki-feature-status says "needs retest (#136)") [CL row 136; docs/project/wiki-feature-status.md:90]. NOTE row 136 number also appears for Super Lubricent Stone in another archive - ambiguity [unverified].
PLAYTEST-NEEDED: n/a.
WIKI-CLAIM-CHECK: "Right clicking on a block sets it" -> confirmed. "while holding it in your hand the selected block will show a purple cube around it" -> NOT-IN-CODE (no highlight renderer anywhere; grep drawCube/PositionFilter in client/ and handler/ finds none). Property "Highlight Color: Purple" -> not-in-code.

## entity_filter (Entity Filter)  [registry: item randomthings:entity_filter]
OBTAIN: shaped "D/DPD/D": D=tag forge:dyes/blue, P=paper x1 [code RC/entity_filter.json].
BEHAVIOR: stack size 1 [code ModItems.java:318]. Using it on a LivingEntity (itemInteractionForEntity, server) stores NBT string entityType = registry key of target's EntityType; returns true [code J/item/EntityFilterItem.java:451-462]. Only LivingEntity targets (hook signature) [code :452]. Tooltip: entity name, or "No entity selected" [code :464-475; lang en_us.json:264].
BEHAVIOR match (IEntityFilterItem.apply): empty filter matches ALL entities; otherwise EXACT EntityType equality [code :477-481].
BEHAVIOR users: Entity Detector Custom mode, Filtered Super Lubricent Platform etc. (not enumerated here) [checklist #105 PASS, CL row 105].
DIVERGENCE: 1.12.2 matched class hierarchy (isAssignableFrom); port matches exact EntityType [code EntityFilterItem.java:423-435].
TESTS: checklist #108 PASS (note says no bug found) [CL row 108].
WIKI-CLAIM-CHECK: "filter by entity type; right clicking on an entity sets it" -> confirmed (exact type; empty filter matches everything - not stated by wiki).

## item_filter (Item Filter)  [registry: NONE]
OBTAIN: no item randomthings:item_filter registered; no recipe; no lang key (grep "item_filter|ItemFilter" in src: only FilteredSuperLubricentPlatformTileEntity.java and AdvancedItemCollectorTileEntity.java, which are about their own filter slots) [code grep; ModItems.java no entry].
DIVERGENCE: "ItemFilter replaced by single example-item slots" (standing simplification) [docs/project/scope-decisions.md grep "ItemFilter"].
WIKI-CLAIM-CHECK: whole page (open item filter GUI, metadata/oredict/nbt toggles, whitelist/blacklist) -> not-in-code (item does not exist).

## chunk_analyzer (Chunk Analyzer)  [registry: item randomthings:chunk_analyzer; container type via ModContainerTypes.CHUNK_ANALYZER]
OBTAIN: shaped "BB/IGI/SID": B=iron_bars, I=iron_ingot, G=glass, S=stone, D=dirt x1 [code RC/chunk_analyzer.json].
BEHAVIOR: stack size 1; right-click MAIN hand only opens GUI (offhand returns FAIL) [code J/item/ChunkAnalyzerItem.java:36-56, ModItems.java:340]. GUI has a "Scan" button (x+136,y+5, 50x14); disabled and text "Scanning Chunk..." with dots while scanning [code J/client/screen/ChunkAnalyzerScreen.java:48,107-113]. Scan button sends handle(id 0) -> startScanning [code J/container/ChunkAnalyzerContainer.java:72-83,246-250].
BEHAVIOR scan: scans the chunk containing the player at scan start; 10 columns per server tick while GUI open (=> 256 columns ~ 26 ticks, ~1.3 s, wiki says "few seconds"); per column counts every non-air blockstate from y=0 up to WORLD_SURFACE heightmap top inclusive [code ChunkAnalyzerContainer.java:95-129]. Results merged by display name, sorted by count descending, written into the held (main hand) analyzer's NBT tag "result" so they persist after closing [code :134-186]. If GUI closed mid-scan: scanning stops since it is driven by detectAndSendChanges [code :86,95] (inference; [unverified] in game).
BEHAVIOR: only blocks up to the heightmap top are counted (nothing above surface; caves under the surface ARE counted since it iterates y from 0) [code :107-116]. Counts blocks, not blockstates by name: distinct states with the same item name are merged [code :139-170].
CONFIG: none. Container canInteractWith always true [code :217].
TESTS: checklist #145 PASS [CL row 145].
PLAYTEST-NEEDED: GUI layout/scroll list.
WIKI-CLAIM-CHECK: "Open its gui and press Scan; after a few seconds results for the chunk you are in" -> confirmed (timing ~1-2 s by tick throttle, [unverified] real-time).

## magic_hood (Magic Hood)  [registry: item randomthings:magic_hood; coremod MagicHoodTransformer]
OBTAIN: NO crafting recipe (no RC file). Chest loot injected by LootHandler: minecraft:chests/simple_dungeon weight 5/100 (else empty); minecraft:chests/village/village_weaponsmith weight 15/100 [code J/handler/LootHandler.java:44-57,78-85]. Gated by config MagicHood (default true: "Add the Magic Hood to dungeon and village chests") [code J/config/RTConfig.java:55]. Appear in creative tab [code ModItems.java:381].
BEHAVIOR armor: ArmorItem HEAD slot, rarity RARE, uses a custom material: durability = LEATHER's head durability (55), defense/toughness/enchantability/sound/repair/name = CHAIN's [code J/item/MagicHoodItem.java:105-150, ModItems.java:381]. NOT indestructible (user requested leather durability; checklist #255 PASS). Armor texture textures/models/armor/magic_hood.png [code MagicHoodItem.java:147-149].
BEHAVIOR nametag: coremod MagicHoodTransformer.js patches net.minecraft.client.renderer.entity.LivingRenderer.canRenderName(LivingEntity)Z (every ireturn) to call AsmHandler.overrideCanRenderName; returns false for PLAYER entities wearing the hood in head slot (regardless of sneaking), else original [code R/transformer/MagicHoodTransformer.js, J/asm/AsmHandler.java:216-229]. Client-side rendering.
BEHAVIOR particles: PotionColorCalculationEvent listener: for players with MAGIC_HOOD in head slot sets shouldHideParticles(true) and color 0 (color 0 is what actually stops particles) [code J/RandomThings.java:614-625]. A further listener forces a recalculation when the hood is put on while an effect is active (comment) [code RandomThings.java:627-634 ff, not read in detail].
CONFIG: MagicHood (loot gate only) default true [code RTConfig.java:55]. Hood effects work regardless of config.
TESTS: automated test behavior.magic_hood.hides_the_nametag_and_the_potion_particles_only_while_worn [test J/debug/test/RemainingBehaviorTests.java:261-296] (calls AsmHandler method directly, not the real renderer); checklist #254 FIXED, #255 PASS, #253 blank/untested in archive (CBT lines 46-47 cover via test+scenario) ; chests #334/335 listed open in TESTING_CHECKLIST.md:57; test asserts weaponsmith pool exists [test J/debug/test/UtilityBehaviorTests.java:115, ChecklistTests.java:431].
PLAYTEST-NEEDED: that the nametag really disappears on a second client; faint-particle residue.
PARITY: nametag half needs coremod in 1.14.4; particle half uses Forge event [docs/project/wiki-feature-status.md:78].
WIKI-CLAIM-CHECK: "no nametag whether crouching or not" -> confirmed. "emit no potion particles" -> confirmed. "same properties as chain armor but it is indestructible" -> CONTRADICTED (leather durability, MagicHoodItem.java:105-143; durability 55). "only found in dungeon chests & blacksmith villager chests" -> confirmed for this port's injection (dungeon 5%, weaponsmith 15%; the 1.14.4 table is village_weaponsmith, LootHandler.java:29-30), subject to MagicHood config.

## magnetic_enchantment (Magnetic Enchantment)  [registry: enchantment randomthings:magnetic; global loot modifier serializer randomthings:magnetic]
OBTAIN: enchanting table / anvil books via enchantment: type EnchantmentType.DIGGER, rarity RARE, slot MAINHAND, max level 1, min enchantability 15, max = min+50 [code J/enchantment/ModEnchantments.java:17, MagneticEnchantment.java:137-166]. Whether DIGGER accepts hoe/shears: vanilla DIGGER = ToolItem instances (pickaxe/axe/shovel) [unverified - vanilla knowledge, not in repo]. Gated by config MagneticEnchantment: when false, canApplyAtEnchantingTable and isAllowedOnBooks return false (enchant stays registered) [code MagneticEnchantment.java:152-160]. Loot-table presence: "appears in loot" per config comment but no loot injection of the enchantment found in LootHandler [code LootHandler.java:39-75] [unverified].
BEHAVIOR: global loot modifier (data/forge/loot_modifiers/global_loot_modifiers.json entries ["randomthings:magnetic"]; R/data/randomthings/loot_modifiers/magnetic.json) applies to block loot where both LootParameters.TOOL and THIS_ENTITY exist, THIS_ENTITY is a PlayerEntity (incl. fake players), tool has MAGNETIC level>0, and config MagneticEnchantment true: every drop is passed to ItemHandlerHelper.giveItemToPlayer (leftovers dropped at player by that helper) and the loot list is emptied [code J/handler/MagneticLootModifier.java:203-226; serializer registered RandomThings.java:825]. Works for any block loot-table roll with a tool (incl. Block Breaker's fake pickaxe) [code MagneticLootModifier.java:194; BlockBreakerTileEntity.java:46].
BEHAVIOR: XP orbs / entity drops (mob loot lacks TOOL) not magnetized [code :206-207].
CONFIG: MagneticEnchantment default true: "Whether the Magnetic enchantment is obtainable (enchanting table, books, loot) and functions" [code RTConfig.java:63].
DIVERGENCE: no coremod; global loot modifier instead of 1.12.2 ASM; config-off keeps enchant registered but unobtainable [docs/project/scope-decisions.md:15; MagneticLootModifier javadoc :188-195].
TESTS: ChecklistTests config test (#337) [test J/debug/test/ChecklistTests.java:307-324]; CBT row 288 "scripted scenario magnetic"; archive row 288 blank.
PLAYTEST-NEEDED: enchanting-table offer frequency vs Silk Touch.
WIKI-CLAIM-CHECK: "available for pickaxes/axes/shovels" -> confirmed-ish (DIGGER type [unverified for hoes]). "roughly same chance as Silk Touch" -> rarity RARE here (Silk Touch is VERY_RARE in vanilla - from vanilla knowledge) with the same 15 / +50 enchantability; [unverified]. "only one level" -> confirmed (getMaxLevel=1, :162-165). "drops go directly into inventory" -> confirmed. Omitted by wiki: config toggle.

## blaze_and_steel (Blaze and Steel)  [registry: item randomthings:blaze_and_steel; block randomthings:blazing_fire; coremod FireBlockTransformer present but not read]
OBTAIN: shaped "IB": I=iron_ingot, B=blaze_powder x1 [code RC/blaze_and_steel.json].
BEHAVIOR: stack 1, durability 64 [code ModItems.java:219]. Use on block: target = clicked pos offset by face; if canPlayerEdit false -> FAIL; if target is air: plays flint-and-steel sound and sets randomthings:blazing_fire (flags 11); always damages tool by 1 (even when target not air) and returns SUCCESS [code J/item/BlazeAndSteelItem.java:171-196]. Cannot ignite TNT/netherportal (no special logic).
BEHAVIOR blazing fire: BlazingFireBlock extends FireBlock with tickRate 15 (vanilla 30) and CLOTH sound; copy of FIRE properties [code J/block/BlazingFireBlock.java:25-36]. Catch-chance and age-growth tweaks of 1.12.2 NOT reproduced [code BlazingFireBlock.java:16-23].
CONFIG: none.
DIVERGENCE: only tick rate is faster, not the 4x catch chance of 1.12.2 [BlazingFireBlock.java:10-23].
TESTS: test use_item.blaze_and_steel.lights_blazing_fire_on_the_clicked_face_and_wears_down [test J/debug/test/UseItemTests.java:425-436]; checklist #122 PASS.
PLAYTEST-NEEDED: spread speed vs vanilla fire.
WIKI-CLAIM-CHECK: "burns and spreads significantly faster than vanilla fire" -> partially confirmed: tickRate halved (15 vs 30) only, not "significantly" beyond that; spread-chance unchanged (BlazingFireBlock.java:16-23). Does it burn faster (age/burnout)? [unverified - depends on FireBlockTransformer / vanilla internals].

## escape_rope (Escape Rope)  [registry: item randomthings:escape_rope]
OBTAIN: shaped "SGE/GSG/EGS": S=string, G=gold_ingot, E=ender_pearl x1 [code RC/escape_rope.json].
BEHAVIOR: stack 1, durability 20 [code ModItems.java:339]. Right-click starts only if dimension hasSkyLight AND !canBlockSeeSky(player pos) AND the player's own block is air; else FAIL (so it will NOT start under open sky or standing in water/non-air) [code J/item/EscapeRopeItem.java:51-53]. Item class javadoc says the opposite ("under open sky") but code and test prove it needs a roofed location [code :17-19 vs :51; test UseItemTests.java:282-295].
BEHAVIOR: use duration 1200 ticks (60 s), use action BOW; player must keep holding right-click [code :38-45]. Server EscapeRopeHandler task: flood fill from player's position (stack/DFS order, pushes DOWN, 4 horizontal, UP), 4 block checks per server tick, passable = chunk loaded and (air or empty collision shape); success when a visited position canBlockSeeSky; gives up when visited set > 10000 or no candidates left; give-up drops the rope as an item and empties the hand [code J/util/EscapeRopeHandler.java:33,54-153]. Tasks for players no longer using the rope are skipped (not removed) [code :68-70]. The task is ticked on ServerTickEvent [code RandomThings.java:510-518].
BEHAVIOR on success: teleports to the first solid block found scanning DOWN from that position (y to 0), standing on top at x+0.5/z+0.5; else at position+1; plays enderman teleport sound before and after; resets active hand; damages rope by 1 [code EscapeRopeHandler.java:93-123]. Same-dimension only (no cross-dimension).
BEHAVIOR client: while using, spawns PORTAL particles in spiral around player; hasEffect (glint) true while this stack is the player's active item (uses Minecraft client class from common item class) [code EscapeRopeItem.java:64-86].
CONFIG: none (maxChecked is a static, test-only).
DIVERGENCE: particle: yellow colored smoke -> PORTAL particles [EscapeRopeItem.java:25-30]. Durability: breaks on the 20th use (1.12.2 broke on the 21st) [CBT line 99, test UseItemTests.java:407-423].
TESTS: UseItemTests.java:282-423 (refuses under sky, teleports and costs 1 durability, cancel on release, sealed room drops rope, search limit, long tunnel, 20th use breaks); checklist #142-144 PASS.
PLAYTEST-NEEDED: particle visuals, actual up-to-a-minute search feel.
WIKI-CLAIM-CHECK: "while in a cave" -> confirmed (no sky visible required). "search up to a minute; keep holding right click" -> confirmed (use duration 60 s; give-up is by 10000 visited blocks, not by time). "rope takes some damage" -> confirmed (1). "use it 21 times before it breaks" -> CONTRADICTED (20 uses; test the_twentieth_successful_use_breaks_it, UseItemTests.java:407-423; ModItems.java:339).

## super_lubricent_boots (Super Lubricent Boots)  [registry: item randomthings:super_lubricent_boots, randomthings:super_lubricent_tincture; coremod SuperLubricentBootsTransformer]
OBTAIN: shaped "B/S": B=iron_boots, S=randomthings:super_lubricent_tincture x1 [code RC/super_lubricent_boots.json]. Tincture is a plain item (its recipe lives in the Super Lubricent family, not read here) [code ModItems.java:224-225].
BEHAVIOR: ArmorItem FEET with ArmorMaterial.IRON (iron-boots stats and durability; NOT indestructible), rarity UNCOMMON [code J/item/SuperLubricentBootsItem.java:107-112]. Texture textures/models/armor/superlubricentboots.png [code :114-118].
BEHAVIOR: coremod SuperLubricentBootsTransformer.js retargets the BlockState.getSlipperiness call inside LivingEntity.travel to AsmHandler.bootsMaxSlip: if the entity is a LivingEntity that is NOT sneaking and has the boots in FEET, slipperiness = 1/0.91 (so friction multiplier = 1.0, i.e. no ground friction) for EVERY block; otherwise original [code R/transformer/SuperLubricentBootsTransformer.js; J/asm/AsmHandler.java:103-113]. Applies to any living entity wearing them (not just players).
BEHAVIOR speed cap: a LivingUpdateEvent listener caps horizontal speed on the ground while wearing the boots and not sneaking, same as the Super Lubricent blocks: MAX_HORIZONTAL_SPEED 0.35 blocks/tick [code J/RandomThings.java:245-261; J/block/SuperLubricentPhysics.java:134,177].
CONFIG: none.
DIVERGENCE: speed cap 0.35 b/t that 1.12.2 never had [docs/project/scope-decisions.md:13-14].
TESTS: coremod test remove_friction_on_ordinary_ground_unless_sneaking [test J/debug/test/CoremodTests.java:254-266]; checklist #134-135 PASS; prod jar #385 PASS.
PLAYTEST-NEEDED: speed-cap feel.
WIKI-CLAIM-CHECK: "same effect as walking on Super Lubricent Ice, no slowdown by friction once moving" -> confirmed, plus an undocumented 0.35 b/t cap; sneaking disables the effect (wiki omits).

## bottle_of_air (Bottle of Air)  [registry: item randomthings:bottle_of_air] (no wiki page)
OBTAIN: no recipe file, no loot entry (grep RC / LootHandler: none) -> creative tab only [code ModItems.java:220; RC listing]. Rarity RARE, stack 1 [code J/item/BottleOfAirItem.java:223-226; ModItems.java:220].
BEHAVIOR: right-click only while isInWater() (else FAIL); drink animation 32 ticks; on finish sets air to max air (server) and shrinks stack by 1 [code BottleOfAirItem.java:229-265]. Note stack max 1 so count>1 only via commands.
DIVERGENCE: 1.12.2 was a reusable hold-to-refill tool; port is single-use [BottleOfAirItem.java:211-219].
TESTS: use_item.bottle_of_air.needs_water_to_start_and_refills_breath_when_finished [test UseItemTests.java:517-534]; checklist #123 PASS.

## ModEffects imbue_* (potion effects; items imbue_*; no wiki id in this group)
REGISTRY: effects randomthings:imbue_fire (orange), imbue_poison (dark green), imbue_experience (yellow), imbue_wither (black.brighter) - all one class ImbueEffect, EffectType.NEUTRAL, no tick behavior [code J/potion/ModEffects.java:23-28, ImbueEffect.java:257-261]. Items randomthings:imbue_fire/poison/experience/wither (ModItems.java:352-355).
BEHAVIOR drink: use 32 ticks DRINK; on finish removes any other imbue effect, applies chosen effect for 24000 ticks (20 min), amplifier 0, no particles, icon shown; consumes one (non-creative) and returns an empty glass bottle [code J/item/ImbueItem.java:37-97].
BEHAVIOR combat: LivingHurtEvent (direct EntityDamageSource only; excludes IndirectEntityDamageSource, i.e. projectiles): attacker with IMBUE_FIRE -> target setFire(10); else IMBUE_WITHER -> Wither II (amp 1) 5 s; else IMBUE_POISON -> Poison II (amp 1) 10 s [code J/RandomThings.java:205-224]. IMBUE_EXPERIENCE: player kill adds originalExperience (doubles XP) [code RandomThings.java:226-231]. Priority fire > wither > poison if somehow several active (cannot be: drinking replaces).
OBTAIN: Imbuing Station recipes: water bottle + coal+flint+blaze_powder -> Fire; spider_eye+rotten_flesh+red_mushroom -> Poison; lesser_magic_bean+lapis+glowstone_dust -> Experience; wither_skeleton_skull+nether_brick+ghast_tear -> Wither (the water bottle in the center/other slot as recipes list it) [code RandomThings.java:713-720].
TESTS: UtilityBehaviorTests.java:364-375; CBT rows 170-179; checklist 388.

## UNMAPPED:
- randomthings:super_lubricent_tincture (ModItems.java:225), randomthings:bottle_of_air, randomthings:id_card (referenced from emerald_compass/chat_detector wiki pages), imbue_* items and effects (wiki page for Imbuing Station not in my group), blazing_fire block (belongs to blaze_and_steel), item BlazeAndSteel only.
- DiviningRodScratch is a helper class, not a registry entry.
- Wiki pages with NOTHING registered (grep ModItems/ModBlocks/ModEnchantments/LootHandler/ForgeRegistries, src tree): item_filter, water_walking_boots, obsidian_water_walking_boots, obsidian_skull, lava_charm, lava_waders. Registry grep for "water_walking|obsidian_skull|lava_charm|lava_waders" in src finds only a stale comment (RandomThings.java:239 "water-walking listener above" - no such listener exists in file) and LootHandler.java:26-28 javadoc "Lava Charm (removed from this port)". Checklist rows 126-129 "removed per request" [CL row 126-129]; docs/project/wiki-feature-status.md:90ff lists Lava Waders / Obsidian Skull / Obsidian Water Walking Boots as REMOVED.
- Water Walking Boots: not found at all in wiki-feature-status grep (name "Water Walking Boots" appears in docs/project/state-history-2026-10.md as removed family "fire/water-protection family") [docs/project/state-history-2026-10.md grep].

WIKI-CLAIM-CHECK (unregistered pages): water_walking_boots, obsidian_water_walking_boots, obsidian_skull, lava_charm, lava_waders -> not-in-code (nothing registered; no recipe; no loot). Baubles rings: no Baubles integration (scope: OpenComputers/Thaumcraft/etc. not ported).

## OPEN QUESTIONS:
1. Are lit redstone ore / other vanilla ores in forge:ores/* tags (Forge 28.2 tag JSON not in repo)? Affects what each rod highlights.
2. Magnetic: does DIGGER accept hoes/shears? And enchantment weight vs Silk Touch (vanilla values from memory only).
3. Blazing fire "burns faster": FireBlockTransformer.js was not read; check whether it adds spread/burn changes for BlazingFireBlock.
4. Checklist row numbers #136 duplicated across archive files (Position Filter vs Super Lubricent Stone).
5. Magic hood particle follow-up listener (RandomThings.java:627+) not read in full.
6. 1.12.2 comparison deliberately not done (no need; docs/code state divergences).
