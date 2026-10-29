# Mod bugs recorded during the wiki audit (for you to investigate; NOT fixed, NOT described on the wiki)

The wiki describes the intended behaviour in each case. Sources: `audit/edits/*.md` (SKIPPED-POSSIBLE-BUG), `audit/verify/R*.md`, `audit/facts/*.md`.
"Decided" = you confirmed it is a bug or told me the intended behaviour. "Reported" = found by code reading, not yet confirmed by you.

## Decided by you (intended behaviour is what the wiki says)
1. Lotus: seeds should only grow on farmland; code accepts grass, dirt and farmland (`LotusBlock.isValidGround`). Wiki: farmland only.
2. Peace Candle ignores slimes (early return in `AsmHandler.overrideSpawnResult`, `AsmHandler.java:169-181`); it should block all hostile (MONSTER) spawns.
3. Peace Candle church generation is plains villages only (`PeaceCandleChurchPool`); it should cover every village. Wiki: "village church".
4. Spectre Lens is keyed per owner UUID in overworld storage and works in any dimension (`SpectreLensHandler.java:25-41`); intended: per player per dimension. Wiki keeps "one lens per dimension".
5. Advanced Item Collector: hard-coded `MAX_RANGE = 16` (`AdvancedItemCollectorTileEntity.java:45`), no config. Decided: documented as 16, no config. (The original had a config capped at 10.)
6. Super Lubricent Tincture recipe takes any potion on 1.14.4; it should only take a Water Bottle (newer branches already do). Wiki recipe shows "Water Bottle".
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
- Imbuing Station, Potion Vaporizer, Inventory Tester do not drop their contents when broken (the original Imbuing Station did).
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
