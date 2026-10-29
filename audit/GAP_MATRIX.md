# GAP MATRIX: Random Things wiki vs mod (baseline 1.14.4)

Status: Phase 0, 1, 2 done. NO wiki page has been edited. Nothing committed or pushed. `audit/` is untracked.
Produced 2026-10-29. ASCII only.

## 0. How this was made, and what is NOT covered

- Mod source = `git archive origin/1.14.4` (commit 6609cea), extracted read-only outside the repo. 1.12.2 = `origin/1.12.2`
  (b9bca79) used only for spot checks. No game run, no build, no network use beyond `git fetch` of those two branches.
- Layout difference vs the plan: this checkout IS the `wiki` branch (no `wiki/` or `1.14.4/` subfolders; the plan sits at
  the repo root). The wiki is already committed (5773e5c), not "untracked". The parent-folder `CLAUDE.md` does not exist
  in any branch, so it could not be read; `1.14.4/CLAUDE.md`, `scope-decisions.md`, `wiki-feature-status.md` were read fully.
- 8 subagents wrote the fact sheets in `audit/facts/` (A redstone, B machines, C spectre, D decor, E world, F tools,
  G entities, H sound + registry). Each fact there has `file:line` (paths relative to the extracted mod root; `J/` = 
  `src/main/java/lumien/randomthings/`, `R/` = `src/main/resources/`) and a grade.
- Evidence grades: `code` (read in source), `test` (automated behavior test or checklist PASS row), `playtest`
  (checklist/fix-log says verified in game), `unverified`.
- I re-checked these high-severity claims myself against wiki JSON + source: Igniter modes, Escape Rope uses,
  Magic Hood "indestructible", Quartz Lamp light, Entity Detector invert text, Position Filter cube, Rain Shield
  "configurable", Runic Dust paper claim, Time in a Bottle costs, "blackout" absent from the mod. All matched the agents.
  The rest of the findings below are agent-reported with citations I did not individually re-read.
- NOT done: (1) wiki recipe PICTURES were not compared one by one with recipe JSON (83 pages have only a picture; the
  fact sheets confirm the real recipes from JSON, so pictures can be replaced by structured recipes in Phase 3 - see J12).
  (2) Media (images/videos) staleness not judged visually. (3) Parity check of branches 1.16.5..1.21.11 NOT run (see 8).
  (4) 1.12.2 comparison only where noted.
- Checklist/status docs: `DONE-UNTESTED` labels are ignored as evidence, as instructed. Most open rows of
  `TESTING_CHECKLIST.md` have blank results; only archive PASS rows and automated tests count.

## 1. Summary

100 existing pages. Verdict counts (a page can have several findings; this is its worst verdict):

| Worst verdict | Pages |
|---|---|
| LEGACY (nothing registered; removed or will-not-migrate) | 9, plus biome_blocks (split, J6) = 10 |
| WRONG (a claim contradicted by code) | 22 |
| EXTRA (claims something not in the mod) | 7 |
| OUTDATED | 1 (biome_radar) |
| MISSING / VAGUE only (no wrong claim, but facts omitted) | 39 |
| OK (matches; only trivial wording) | 21 |

(Counted by script from the tables in section 3; total 100.)

Non-text checks (script `W_wikicheck.txt`): 100/100 category valid, 0 non-ASCII, 0 remote media, 0 missing image files,
0 orphan html, 0 pages without generated html. 83 pages have only recipe pictures, 16 have no recipe at all, 1 has a
structured recipe. 0 unresolved recipe keys (because almost nothing is structured yet).

Registry: 491 rows swept (`H_registry.md`). 10 wiki ids with nothing registered, 7 registered things with no
page, 8 recipes with no wiki page output (see 6).

## 2. Judgement calls - please decide before I edit anything

Format: what the conflict is, my recommended default (used in the matrix unless you say otherwise).

- **J1 Wiki follows the mod as it is today, not 1.12.2 or intent.** Where the wiki describes a 1.12.2 feature the port lacks
  (Spectre Sapling by right-click with ectoplasm, Runic Pattern from paper, Luminous Powder enchant-glow recipe, Numbered
  Spectre Coils, Blackout Powder, Nature Core plant chest), I class it WRONG/EXTRA and remove it from the wiki. None of these is a
  listed deliberate divergence in `scope-decisions.md`, so some may be mod gaps you want to fix instead; they are also
  listed in section 9 (mod issues). Default: remove from wiki now, re-add if you port them.
- **J2 Quartz Lamp.** Wiki: "emits light 15, not visible". Mod: emits no light; only denies monster spawns within 4 blocks
  (`QuartzLampBlock.java:8-23`, comment says the drop of the 1.12.2 server-only light was deliberate, but it is not in
  `scope-decisions.md`). Default: WRONG, rewrite as "no light, prevents monster spawns in 4 blocks" with the short divergence note.
- **J3 Advanced Redstone Repeater delay range.** Port 0..120 step 1, default 20; 1.12.2 was 2..10000 with steps 1/10/100/1000
  (`A_redstone.md`). Wiki gives no range. Undocumented divergence. Default: document the real range with a divergence note.
- **J4 Experience Imbue.** Wiki says +50%; code doubles (+100%), same as 1.12.2. So the wiki was wrong even for the original.
  Default: fix to +100%.
- **J5 Config the wiki promises but the mod lacks.** Rain Shield "configurable" (80 blocks hard-coded; 1.12.2 had
  `RainShieldRange`), Ancient Furnace "10000 blocks (configurable)" (fixed radius 56). Default: wiki states fixed values; list
  the missing keys in section 9 for you.
- **J6 Biome Blocks page.** Biome Stone/Glass are removed, but Biome Crystal is still registered, looted (20% in every
  `chests/*` table, config `BiomeCrystal`) and used by the Biome Radar. Default: move `biome_blocks` to Legacy (blocks), and
  create a new "Biome Crystal" page (or fold into Biome Radar - your choice, default new page).
- **J7 Super Lubricent.** The wiki page "Super Lubricent" is a one-line description of the Tincture (a plain ingredient).
  Super Lubricent Ice, Platform and Filtered Platform have no page; the 0.35 b/t and 15 deg/t caps are undocumented.
  Default: keep `super_lubricent` as the tincture/overview, add new page `super_lubricent_ice` (ice, platform, filtered platform).
- **J8 Pages that are folded.** Global Chat Detector, Advanced Item Collector, Advanced Redstone Torch (wall variant), Ender
  Mailbox/Anchor/Prismarine Bridge, Floo Bricks, Imbue items are covered inside another page. Default: keep folded, but each
  folded block/item gets its own recipe and rules in that page. Say if you want separate pages for Global Chat Detector /
  Advanced Item Collector / Ender Mailbox.
- **J9 New pages proposed (registered or kept, no page):** Super Lubricent Ice family, Bottle of Air, Biome Crystal, ID Card,
  Redstone Tool, Spectre Ingot and String (+ Spectre Core/Blocks), and one "Configuration" page for config-only features.
  Details in section 6. Default: create all seven.
- **J10 Legacy list.** Full Legacy (confirmed nothing registered + status doc): item_filter, water_walking_boots,
  obsidian_water_walking_boots, obsidian_skull, lava_charm, lava_waders, pitcher_plant, custom_crafting_tables,
  inventory_rerouter. `wiki-feature-status.md` rows exist for all except lava_waders (listed as "Lava Waders ... REMOVED",
  line 73, so also confirmed). The plan feared some boots/rods might be partly kept: they are not (grep of registry); Divining
  Rods stays active (8 rods registered).
- **J11 OpenComputers / third-party text.** Online Detector, Notification Interface, Creative Player Interface,
  Redstone Interface claim OC components; Divining Rods claims mod-ore rods. All dropped by scope decision. Default: delete
  those sentences (not Legacy, not a divergence note).
- **J12 Recipes.** Default: replace recipe pictures with structured `recipe` blocks built from the recipe JSON, so the wiki
  matches the data and the 16 recipe-less pages are fixed too. Pictures kept only where no structured form fits (special
  recipes, Imbuing Station hard-coded recipes).
- **J13 Possible mod bugs found while reading** (section 9) are NOT wiki errors. I did not treat them as DIVERGENCE-BY-DESIGN.
  Where a wiki claim depends on one (Redstone Tool linking, Advanced Interface reload, Spectre Coil cross-dimension), the
  page is marked HOLD until a playtest decides.
- **J14 Dimension scope of Floo / Ender Letter / Spectre Coil saved data** is unresolved from code alone (is
  `getSavedData()` per-dimension in 1.14.4?). Default: write only what is certain, mark HOLD.

## 3. Page matrix (existing 100 pages)

Verdict key: OK, VAGUE, MISSING, WRONG, EXTRA, OUTDATED, DIV (divergence by design, short note), LEGACY, HOLD (needs playtest).
Sev: H = misleads gameplay, M = medium, L = wording. Ev = evidence grade of the finding. "Sheet" = file in `audit/facts/`.

### 3.1 Redstone (sheet A)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| advanced_redstone_interface | MISSING, HOLD | 9 filter slots OK; Position Filter dimension ignored; targets may be lost after world reload (`refreshTargets` early return, unverified) | M | code / unverified |
| advanced_redstone_repeater | MISSING, DIV | range 0..120 step 1 default 20 (J3); recipe grid OK | M | code |
| advanced_redstone_torch | OK | wall torch shares item; strengths 0..15 each confirmed; recipe matches | L | code |
| analog_emitter | OK, DIV | input read uses `facing` where 1.12.2 used opposite: direction-dependent sources may differ | L | code vs git |
| contact_button_lever | MISSING | button pulse 20 ticks, main hand only; wiki omits | L | code+test |
| redstone_activator | OK | durations 2/20/100 confirmed; coremod-dependent | L | code |
| redstone_interface | WRONG, EXTRA, HOLD | "red lines" are drawn only for Observers (`RedstoneObserverLineRenderer.java:62`); OC component not in code; Tool linking reachability doubtful (GUI opens before tool check) | H | code / unverified |
| redstone_observer | OK, HOLD | same Tool reachability question; preserves per-side strength | M | code |
| redstone_remote | OK | camo row implemented (scope-decisions); 20 tick pulse; 9 filters | L | code |
| sided_redstone | OK | | L | code |
| trigger_glass | MISSING, DIV | chain limit config default 20 (port-specific), BFS; 60-tick no-collision confirmed | M | code+test |

### 3.2 Machines, detectors, interfaces (sheet B)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| chat_detector | MISSING, DIV | exact full-string match, 20-tick pulse, owner fixed; Global variant has no op bypass (scope-decisions) | L | code |
| creative_player_interface | EXTRA | all 5 OC functions not in code; no recipe (creative only) | M | code |
| entity_detector | WRONG | invert "Players" does NOT power for non-players; it powers when NO player in range; Weak/Strong output toggle omitted | H | code |
| igniter | WRONG | 3 modes claimed; only Toggle and Ignite exist (Keep Ignited removed, checklist #74); places fire into an air block | H | code (re-checked) |
| imbuing_station | WRONG, MISSING | Experience imbue is +100% not +50% (J4); recipes (incl. hard-coded mossy cobblestone recipe) omitted; no inventory drop on break (see 9) | M | code |
| inventory_tester | MISSING | updates every 2 ticks, holds old state when slot empty | L | code |
| iron_dropper | VAGUE | discrete values (delay None/5/20, pulse/repeat) not listed | L | code |
| item_collector | WRONG, EXTRA | Advanced Item Collector range max is 16, wiki says 0-10; Item Filter item does not exist: single example-item slot, whitelist only | H | code |
| notification_interface | EXTRA | OC component not in code; toast confirmed | M | code |
| online_detector | EXTRA | OC component not in code; name-case behavior unverified | M | code |
| player_interface | EXTRA | OC mention (via Creative variant) | L | code |
| potion_vaporizer | OK, DIV | LivingEntity only; flood fill passes non-full blocks (scope-decisions) | L | code+test |
| rain_shield | WRONG | range hard-coded 80 blocks (5 chunks), not configurable (J5); sounds suppression unverified | M | code / unverified |
| biome_radar | OUTDATED | Biome Crystals are in ALL vanilla chests at 20%, not dungeon only; config `BiomeCrystal` | M | code |
| block_breaker | OK | uses fake player + iron pickaxe; drop routing depends on Magnetic config being on (see 9) | M | code |
| block_destabilizer | OK, DIV | limit 50 configurable OK; matches full cubes only; lazy-mode semantics unverified | L | code / unverified |
| dyeing_machine | WRONG, MISSING | colors ANY item (not only non-self-coloring); block items rejected by the slot; "purple restores glow" has no code | M | code (coremod) |
| fluid_display | OK | | L | code |

### 3.3 Spectre family (sheet C)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| spectre_anchor | MISSING | recipe (6 iron + ectoplasm); anchor consumed; coremod-dependent death drop | L | code+test |
| spectre_charger | MISSING, DIV | pool cap 1,000,000; works anywhere in inventory (Baubles belt dropped); Genesis creative-only (no recipe); Injector recipe | M | code |
| spectre_coils | WRONG, HOLD | Numbered coils not ported (not in dungeon loot, no 128 FE/t); "works across dimensions" unresolved (J14); coil needs energy block behind it | H | code / unverified |
| spectre_illuminator | EXTRA, MISSING | Blackout Powder does not exist (wiki illuminator page only); light value 14, one orb per chunk, nothing within 2 blocks of borders | H | code |
| spectre_key | OK, MISSING | 5 s charge, cube 16x16x4, ectoplasm raises height confirmed; empty-hand click on Core also exits; vanilla Void biome; guests list dropped | M | code+playtest |
| spectre_lens | WRONG | one lens per PLAYER, not per dimension; effects follow the owner in any dimension (`SpectreLensHandler.java:25-41`); beacon needs valid pyramid | H | code+playtest |
| spectre_sapling | WRONG, DIV | cannot be made by right-click with ectoplasm (J1: creative-only); leaf drops ectoplasm only 1/55 (also on random ticks); bone meal works | H | code |
| spectre_tools | MISSING | recipes use spectre ingot + obsidian; sword has NO reach bonus, others +3 main hand; hurts Spirits | M | code |
| ectoplasm | OK, MISSING | Spirit appears only when a real player kills; moon bonus needs night + open sky; spirit lasts 400 ticks default; spectre ingot recipe | M | code+test |

### 3.4 Lamps, glass, decor (sheet D)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| lapis_glass | OK | | L | code |
| quartz_glass | OK | projectile case unverified | L | code |
| lapis_lamp | VAGUE | actively forces monster spawns allowed within 4 blocks (not on Peaceful) | M | code+test |
| quartz_lamp | WRONG, DIV | emits NO light (J2); denies monster spawns in 4 blocks | H | code |
| rainbow_lamp | OK | | L | code |
| luminous_blocks | OK | both variants are real level-15 lights | L | code |
| luminous_powder | EXTRA | "combine with enchanted item to glow" not implemented (J1); used by Rune Dust and Illuminator | M | code |
| stained_bricks | OK | made from vanilla bricks block + dye | L | code |
| diaphanous_blocks | OK, MISSING | fade distances (about 3.6 / 6.2 blocks); checklist line 56 says drops nothing but code drops the item | L | code |
| light_redirector | OK | client-side rendering only | L | code |
| platforms | MISSING | 6 woods; top 2/16 solid; passable when feet below the top | L | code |
| block_of_sticks | MISSING | Returning variant: only non-creative players within 50 blocks, item lost if inventory full | L | code |
| compressed_slime_block | MISSING | formed only by shovel on slime block (costs 1 durability), drops plain slime block; shovel hand-filter unverified | L | code |
| super_lubricent | VAGUE | only the tincture is described (J7) | M | code |
| super_lubricent_stone | MISSING, DIV | 0.35 b/t speed cap undocumented | M | code |
| runic_dust | WRONG | paper -> Runic Pattern right-click is not in code (J1); pattern is creative-tab only; stamping consumes dust; left click clears whole base (scope-decisions); `FlatRunes` | H | code |

### 3.5 Plants, world, loot (sheet E)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| bean | WRONG, EXTRA, MISSING | Magic Bean stalk does NOT break blocks, stops at first obstacle (`BeanStalkBlock.java:94`); Nature Core / plant chest not ported, Magic Bean creative-only; pod also drops Golden Egg (config `GoldenEgg`); stew values | H | code |
| lotus | OK | | L | code |
| glowing_mushrooms | EXTRA | "replacement for glowstone in brewing" not in code; only Luminous Powder ingredient; caves only | M | code |
| colored_grass | OK, DIV | named "<Color> Grass Block", shovel preference (scope-decisions); only spreads onto plain dirt | L | code |
| fertilized_dirt | VAGUE | "x3" growth is 3 extra random ticks (derived, no test) | L | code |
| ancient_furnace | WRONG | area is fixed radius 56 (not configurable, J5); 13 biome conversions only; any face works | M | code |
| peace_candle | VAGUE | blocks MONSTER-class natural spawns in 3 chunks, NOT slimes, not animals; church swap plains villages only, about 1/3 | M | code |
| slime_cube | WRONG | no increased spawn rate; it only forces ALLOW in the chunk; powered denies | H | code |
| biome_blocks | LEGACY (split, J6) | Biome Stone/Glass removed; crystal in all chests 20% | M | code |

### 3.6 Tools, compasses, boots, enchant (sheet F)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| divining_rods | WRONG, EXTRA, MISSING | mod-specific rods absent (J11); vanilla rod finds 7 ores (no quartz/nether gold); client-side only; scan 11^3 around feet; indicators 160 ticks | M | code |
| emerald_compass | MISSING | tracks X/Z only, ignores dimension; random wobble when target offline; ID Card not consumed | M | code+playtest |
| golden_compass | MISSING | X/Z only, ignores Y and dimension; reads only recipe-set NBT (scope-decisions) | M | code+playtest |
| position_filter | WRONG | purple cube highlight not in code (no renderer); coordinates hideable by config `HideCoordinates` | M | code |
| entity_filter | MISSING | exact entity type; empty filter matches everything | L | code |
| chunk_analyzer | OK | scan time about 1-2 s (unverified) | L | code / unverified |
| magic_hood | WRONG | NOT indestructible: leather durability, chain stats (`MagicHoodItem.java:105-143`); loot dungeon 5%, weaponsmith 15%, config `MagicHood` | H | code+test |
| magnetic_enchantment | MISSING, DIV | config `MagneticEnchantment` off = registered but unobtainable; rarity vs Silk Touch and hoes unverified | M | code / unverified |
| blaze_and_steel | VAGUE | only fire tick rate halved (15 vs 30); spread chance unchanged | M | code |
| escape_rope | WRONG | breaks on the 20th use, not 21 (`ModItems.java:339`, test `the_twentieth_successful_use_breaks_it`) | H | code (re-checked)+test |
| super_lubricent_boots | MISSING, DIV | 0.35 b/t cap; sneaking disables; coremod | M | code |

### 3.7 Entities, teleport, ender (sheet G)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| eclipsed_clock | WRONG, MISSING | hover with bottle does NOT show needed time (label is the target HH:mm only); skip is animated (~12 s) and forward only; no message when bottle lacks time | H | code |
| time_in_a_bottle | WRONG | costs: place 30 s, then 30/60/120/240/480 for 2x/4x/8x/16x/32x = 6 clicks to 32x (wiki says 5 clicks starting 30/60/..) | H | code (re-checked) |
| weather_egg | OK, MISSING | 5-15 min, 2 per craft | L | code |
| golden_egg | WRONG | chicken drops 3 ingots per gold ORE item fed after 30-60 s, it does not lay unprompted; pod egg gated by `GoldenEgg` | M | code |
| summoning_pendulum | OK, MISSING | any non-hostile non-player living entity; main-hand stack always | L | code |
| stable_ender_pearl | OK | registry name `stable_enderpearl`; cross-dimension: stays in own dimension | L | code |
| artificial_end_portal | OK | 10 s charge, 40-tick structure check, config `ArtificialEndPortal` | L | code |
| floo_teleportation | VAGUE, HOLD | token fireplace costs no powder; powder in main hand; max 21 bricks; cross-dimension storage unresolved; Floo Powder shapeless (scope-decisions) | M | code / unverified |
| portkey | VAGUE | same dimension only; landing within 2 horizontally, up to 10 below target Y; unbound key glows forever (intentional); camo recipe | M | code |
| ender_bridge | VAGUE | box pos-2..pos+2; also moves items and minecarts; anchor teleports to block above; chunkloading config | M | code |
| ender_bucket | OK, MISSING | stacks 16 empty; same fluid only; BFS cap 2000 | L | code |
| ender_letter | OK, MISSING | message body (new, scope-decisions); mailbox recipe | L | code |

### 3.8 Sound (sheet H)

| Page | Verdict | Findings | Sev | Ev |
|---|---|---|---|---|
| sound_box | MISSING | plays once per rising edge; removal works with any hand; powered flag not saved | L | code+test |
| sound_dampener | MISSING | client-side only; 20 blocks from dampener to sound origin; music never muted | M | code+test |
| portable_sound_dampener | MISSING | main-inventory 36 slots only, no radius; Baubles slot dropped | L | code |
| sound_pattern | MISSING | sneak-click wipes whole stack NBT | L | code |
| sound_recorder | MISSING | cannot open GUI while recording; new recording wipes old; any recorder in inventory records; music not recordable | M | code+test |

### 3.9 Legacy (nothing registered)

| Page | Status source | Action |
|---|---|---|
| item_filter | WILL NOT MIGRATE (status doc line 69) | move to Legacy; consumers use single example-item slot |
| water_walking_boots | REMOVED (line 123) | Legacy |
| obsidian_water_walking_boots | REMOVED (line 82) | Legacy |
| obsidian_skull | REMOVED (line 81) | Legacy |
| lava_charm | REMOVED (line 72) | Legacy |
| lava_waders | REMOVED (line 73) | Legacy |
| pitcher_plant | REMOVED (line 85) | Legacy |
| custom_crafting_tables | REMOVED (line 44) | Legacy |
| inventory_rerouter | REMOVED (line 65) | Legacy |
| biome_blocks | REMOVED blocks (line 32), crystal survives | Legacy for blocks (J6) |

Each Legacy page keeps its text with a first-line status ("Not in the current mod: removed / not migrated"). `data/categories.json`
gets a `legacy` entry. Nothing is deleted.

## 4. Registry lists (full tables in `facts/H_registry.md`)

(a) Registered, no page of their own: spectre_core (block), spectre_string, spectre_ingot (only in ectoplasm text), biome_sensor,
biome_crystal, bottle_of_air, id_card, rune_pattern (inside runic_dust), redstone_tool, spirit_spawn_egg, spectre_block/log/plank/leaf
(inside spectre_sapling), blazing_fire (not obtainable), floo/ender helper blocks (inside their pages).
(b) Page but nothing registered: the 9 Legacy ids plus biome_blocks (3.9). Also claims about unregistered things inside live
pages: Blackout Powder, Nature Core, Numbered Spectre Coils, mod-ore Divining Rods, Item Filter item.
(c) Matched: the remaining 90 pages.
Survival-obtainability gaps (registered, no recipe/loot path): Magic Bean, Rune Pattern, Spectre Sapling/Log/Leaf, Genesis Charger/Coil,
Spirit spawn egg, Creative Player Interface; worldgen/loot-only items are fine.

## 5. Config and world-level facts to put on the wiki (sheet E, section WORLD-LEVEL)

20 `RTConfig` options (worldgen 4, loot 4, features 4, visual 2, numbers 6) with defaults; chest loot injection table (Summoning Pendulum
dungeon/stronghold, Magic Hood dungeon 5% / weaponsmith 15%, Slime Cube dungeon/jungle temple, Biome Crystal 20% every chest);
4 worldgen features plus the plains-village church. Only command is dev-only `/rtdebug`; no key bindings; one creative tab.
These feed the new Configuration page (J9) and the property rows "Obtain" / "Config key".

## 6. New pages proposed (J9)

| New page | Covers | Category |
|---|---|---|
| super_lubricent_ice | Super Lubricent Ice, Platform, Filtered Platform (recipes, 0.35 b/t cap, 15 deg/t boats, example-item filter) | block |
| bottle_of_air | Bottle of Air (breath refill underwater; kept per status doc) | item |
| biome_crystal | Biome Crystal (loot, config, Radar use) | item |
| id_card | ID Card (recipe; used by Emerald Compass, Entity Detector, Global Chat Detector) | item |
| redstone_tool | Redstone Tool (recipe, link flow) - HOLD on linking reachability | item |
| spectre_ingot | Spectre Ingot, Spectre String, Spectre Core and tree blocks overview | item |
| configuration | All 20 config options, defaults, effect | misc |
Folded and staying folded unless you say otherwise (J8): Global Chat Detector, Advanced Item Collector, Ender Mailbox/Anchor,
Floo Bricks, Imbue items, Rune Base/Pattern, Spirit.

## 7. Divergence notes to add (short, from scope-decisions.md, verified present in code)

Spectre Leaf drops Ectoplasm only; Floo Powder shapeless; Colored Grass naming; Potion Vaporizer / Block Destabilizer flood fills pass
non-full blocks; Global Chat Detector no op bypass; Rune Base left-click clears the block, `FlatRunes`; Super Lubricent caps; Trigger Glass
chain limit; Magnetic config; Remote camo row; Ender Letter message body; Item Filter replaced by example slots; plus the new
undocumented ones from J2/J3.

## 8. Playtest list (cannot be settled from code; needs your window, I will not start the game)

1. TeleporterTransformer: does the Spectre -> Overworld return trip run vanilla portal code? (C)
2. Redstone Tool linking an Observer / Basic Interface (GUI opens first). (A)
3. Advanced Redstone Interface keeps its 9 targets after save/reload. (A)
4. Spectre Coil buffer across dimensions; Floo network and Ender Letter inbox across dimensions. (C, G)
5. Shovel on slime block firing for both hands. (D)
6. Rain Shield visuals and rain sounds; Dyeing Machine held/worn rendering; Biome Radar particles; Fluid Display;
   Imbuing Station animation; Notification toast look; Time in a Bottle wheel (M5); Ender Bucket fluid layer (M16). (B, G)
7. Block Breaker on blocks needing better than an iron pickaxe; Online Detector name case. (B)
8. Blaze and Steel fire speed (FireBlockTransformer); Magnetic on hoes and rarity vs Silk Touch. (F)
9. Diaphanous fade radii feel; Quartz Glass projectile collision. (D)
10. Stable Ender Pearl, Ender Anchor persistence, Mailbox GUI. (G)
11. Spawn hooks with spawners/eggs for Lapis Lamp, Peace Candle, Slime Cube. (D, E)
12. Audible muting for Sound Dampeners. (H)

## 9. Possible mod issues (NOT wiki work, not fixed, for your decision; some feed J1/J5/J13)

Redstone Tool linking unreachable (A); Advanced Interface target reload (A); Notification Interface drops copied icon stack (item
duplication, B); Imbuing Station / Potion Vaporizer / Inventory Tester keep no inventory on break (B); Reinforced Ender Bucket drains
the source before checking fluid (G); Eclipsed Clock drop loses target time, negative time when sneaking from 0 (G); Floo fireplace may be
removed when its chunk is unloaded (G); Stable Pearl NBT without UUID throws (G); Slime Cube powered flag and Sound Box powered flag not
saved (E, H); Advanced Redstone Torch setters skip markDirty, Observer output not recomputed on load (A); Analog Emitter input facing
(A); Missing config keys vs 1.12.2: RainShieldRange, Ancient Furnace area (B, E); missing features vs wiki: sapling conversion, Rune
Pattern from paper, Luminous Powder glint (C, D); stale javadocs (Escape Rope "open sky", RainShieldBlock, PeaceCandleBlock,
SlimeCubeTileEntity, repeater "2-10000"); TESTING_CHECKLIST line 56 and fix-log #287 text stale. Magic Bean has no obtain path.

## 10. Parity-bugs list (versions 1.16.5 .. 1.21.11 that differ from 1.14.4)

NOT checked. The plan's cheap check (version-notes, `/drift`, `/parity`, per-class grep) needs those branches, which I did not
fetch. From docs only: `TESTING_CHECKLIST.md:62` records a Spectre cube-saving fix (2026-10-28) that affects the 1.14.4 -> 1.16.5
migration of saved cubes. Proposal: after you approve this matrix, fetch the other branches read-only and grep only the classes behind
the WRONG/HOLD rows above. No version wording goes into the wiki either way.

## 11. Verified vs not verified (honest summary)

Verified by me directly: the 10 spot checks listed in section 0, plus the 5 mechanical wiki checks.
Verified by agents from source with citations ([code]): the large majority of the findings above; automated tests back Escape Rope,
Diaphanous (partly), Entity Detector, Trigger Glass, Sound Box/Dampener/Recorder, Lapis Lamp, Spirit lifetime, Spectre cube/anchor.
Playtest-backed only where checklist archive PASS rows exist (Illuminator, Spectre Key, Lens, compasses, tree blocks, tools #304-306).
Unverified (about 80 facts across sheets, mostly rendering, timing, cross-dimension storage, vanilla internals): see section 8 and the
OPEN QUESTIONS at the end of each sheet. Claims about vanilla values (hunger, Silk Touch rarity) come from agent memory, not the repo,
and must not be written to the wiki without a source.
