# Mod bugs recorded during the wiki audit (for you to investigate; NOT fixed, NOT described on the wiki)

The wiki describes the intended behaviour in each case. Sources: `audit/edits/*.md` (SKIPPED-POSSIBLE-BUG), `audit/verify/R*.md`, `audit/facts/*.md`.
"Decided" = you confirmed it is a bug or told me the intended behaviour. "Reported" = found by code reading, not yet confirmed by you.

## Decided by you (intended behaviour is what the wiki says)
1. Lotus: seeds should only grow on farmland; code accepts grass, dirt and farmland (`LotusBlock.isValidGround`). Wiki: farmland only.
2. Peace Candle ignores slimes (early return in `AsmHandler.overrideSpawnResult`, `AsmHandler.java:169-181`); it should block all hostile (MONSTER) spawns.
3. Peace Candle church generation is plains villages only (`PeaceCandleChurchPool`); it should cover every village. Wiki: "village church".
4. Spectre Lens is keyed per owner UUID in overworld storage and works in any dimension (`SpectreLensHandler.java:25-41`); intended: per player per dimension. Wiki keeps "one lens per dimension".
5. Advanced Item Collector: hard-coded `MAX_RANGE = 16` (`AdvancedItemCollectorTileEntity.java:45`), no config. Decided: documented as 16, no config. (The original had a config capped at 10.)
6. (Withdrawn, not a bug) Super Lubricent Tincture: the 1.14.4 recipe already requires a Water Bottle (`forge:nbt`); my first comparison missed the NBT. Wiki recipe shows "Water Bottle".
7. Online Detector name matching: code reads case-insensitive (vanilla player lookup); your playtest says it is case sensitive. Wiki: case sensitive. Check which one is real and make code and wiki agree.
8. Ender Letters / Mailboxes: saved data looks per dimension in code (`EnderLetterHandler` via `ServerWorld.getSavedData()`); your playtest says they work across dimensions. Wiki: works across dimensions.
9. Rain Shield: 1.14.4 only patches `renderRainSnow` (`GameRendererTransformer.js:26`); 1.16.5+ also patch rain particle/sound hooks. Your playtest says sounds stop on the tested build. Wiki: particles and sounds suppressed.
10. Redstone Tool: the Observer/Interface blocks always open their GUI on right click, so linking only works while sneaking. Wiki now says shift right click (decided). Consider letting a plain right click with the tool link.

## Reported by code reading (wiki keeps the original wording or says nothing)
- Block Breaker: collecting drops in the inventory behind it depends on config `MagneticEnchantment` being true (original had no such dependency). Documented as is.
- Redstone Interface: red connection lines are only drawn for Redstone Observers (`RedstoneObserverLineRenderer.java:62`); the wiki still says Interfaces show them too.
- Slime Cube: code forces "allow" at any height; the original probably kept the height limit (wiki keeps "any height").
- Missing config keys compared with the original: `RainShieldRange`, Ancient Furnace area (fixed values documented).
- Advanced Item Collector wiki/original max range 10 vs 16 (item 5).
- Potion Vaporizer did not drop its contents when broken (FIXED). Correction: the original Imbuing Station and Inventory Tester never dropped items either, so they were left unchanged (an earlier fact sheet was wrong).
- Notification Interface drops its copied icon stack on break (you verified there is no duplication; nothing recorded further).
- Reinforced Ender Bucket removes the source block before checking the fluid fits (fluid lost when mixing).
- Eclipsed Clock: dropped item loses its target time; sneaking from 0 gives a negative target time.
- Floo: a fireplace may be removed from the registry when its target chunk is unloaded; a sign name that already exists makes creation fail silently and consumes the sign.
- Stable Ender Pearl: bound player in another dimension is not transferred (the original did); a tag without a player UUID throws; a pearl bound to an offline player falls into the random-entity branch.
- Sound Box and Slime Cube `powered` flags are not saved (can replay or reset after reload). Advanced Redstone Torch strength setters skip `markDirty`. Observer output is not recomputed on load.
- Analog Emitter reads its input with `facing` where the original used the opposite side.
- Advanced Redstone Interface / Remote ignore the dimension stored in a Position Filter.
- Advanced Redstone Repeater delay range 0..120 (original 2..10000); documented as is (you asked for it).
- Entity Detector: Custom filter with an empty slot plus Invert is permanently on.
- Missing features compared with the old wiki text (the six left-as-is pages): Spectre Sapling conversion with ectoplasm, Runic Pattern from paper, Luminous Powder
  glint recipe, Numbered Spectre Coils, Blackout Powder, Nature Core, Magic Bean obtain path (creative only).
- Stale javadocs/docs: Escape Rope ("open sky"), RainShieldBlock, PeaceCandleBlock, SlimeCubeTileEntity, repeater "2-10000", `TESTING_CHECKLIST.md` line 56, fix-log #287.
- Spectre Key return trip: `TeleporterTransformer` may build a portal on the return trip (your playtest says the return works; code comment and behaviour should be reconciled).

Version differences (other branches) are in `audit/PARITY_BUGS.md`.

## Status after the code fixes (2026-10-29, all nine branches, written WITHOUT a compiler: build, run the suites and retest AF1-AF12)
FIXED on every branch (commit "Fix bugs found in the wiki audit", rows AF1-AF12 in each TESTING_CHECKLIST.md): Redstone Tool plain click (items 10), Online Detector case (7),
Lotus farmland-only (1), Peace Candle vs slimes (2), Spectre Lens per player per dimension (4), Ender Letter store (8), Sound Box / Slime Cube powered saved,
Advanced Redstone Torch markDirty, Potion Vaporizer drops, Ender Bucket drain check, Eclipsed Clock target time + negative time, Stable Ender Pearl guard.
Also: the existing Lotus Seeds test was rewritten on every branch for the farmland-only rule.
NOT FIXED: Peace Candle churches in all villages (3): needs the vanilla village pool and church piece names of desert, savanna, snowy and taiga villages; the repo only references plains.
NOT A BUG / no change: Advanced Item Collector range (5, decided: 16, no config), Rain Shield sounds (9, your playtest says they stop), Tincture recipe (6, already a Water Bottle),
Imbuing Station / Inventory Tester drops (the original had none).
Still open from the "reported" list (not requested): Block Breaker Magnetic config dependency, Redstone Interface lines, Slime Cube height, missing config keys, Floo chunk-unload removal,
Floo duplicate sign names, Analog Emitter input side, Position Filter dimension ignored by the Advanced Interface/Remote, Observer output on load, Entity Detector empty Custom filter,
Stable Ender Pearl cross-dimension, and every item in PARITY_BUGS.md.
Known per-branch limits: 1.21.x fix AF1 only covers the main hand on 1.21.1 / 1.21.4 (1.14.4 also covers the off-hand); every "return PASS lets Item.useOn run" assumption is unverified in game.

## Round 2 (2026-10-29, all nine branches, also written WITHOUT a compiler; rows AG1-AG11 in each TESTING_CHECKLIST.md)
ADDED (matching 1.12.2): Spectre Sapling from a vanilla sapling + Ectoplasm; Runic Pattern from paper on a placed Rune Base (the original trigger; the wiki sentence "Runic Dust on the ground with a piece of paper"
is only loosely the same); Luminous Powder recipe + flag + tooltip; Numbered Spectre Coils (chest loot 10/8/30, config `loot.NumberedSpectreCoils`, 128 FE/tick, no recipe); Blackout Powder.
FIXED: Floo teleport into an unloaded chunk no longer deletes the fireplace; Floo duplicate sign name now shows a message; Stable Ender Pearl changes the bound player's dimension; Advanced Redstone Interface and
Redstone Remote respect a Position Filter's dimension; Redstone Observer recomputes after load; Analog Emitter reads its input from the opposite side again.
CORRECTIONS to the list above: Entity Detector with an empty Custom filter + Invert is the original behaviour (no change, a test was added); the Floo Sign was never consumed on a duplicate name (only the message was missing);
the Stable Ender Pearl bound to an offline player already matched the original.
NOT DONE: the glow-in-the-dark Luminous glint RENDERING (needs a glint coremod hook that forces full bright lighting, as 1.12.2's AsmHandler.preEnchantment/postEnchantment did), Nature Core and the Magic Bean source (still will-not-migrate),
Peace Candle churches in all village types (needs the vanilla pool/church names).
Branch differences: 1.18.2 refreshes the Observer through a block entity ticker instead of a tick scheduled in onLoad; Position Filter dimensions are strings from 1.16.5 on; 1.20.6/1.21.1/1.21.4/1.21.11 build the enchanted check
of the Luminous recipe by looping over the enchantment registry (assumed API combination, unverified); sapling/paper listeners cancel RightClickBlock (no precedent on the 1.20.6+ branches).
Wiki pages left as is (spectre_sapling, runic_dust, luminous_powder, spectre_coils, spectre_illuminator, bean) now describe features that exist, except the Luminous glow and Nature Core / Magic Bean; their recipe pictures are old.
