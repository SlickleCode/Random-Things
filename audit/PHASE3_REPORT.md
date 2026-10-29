# Phase 3 report (wiki update) - nothing committed or pushed

Date 2026-10-29. All edits are in the working tree of branch `wiki` (uncommitted). ASCII only, no version wording.

## What changed
- Generator: `scripts/generate_html.py`, `scripts/texture_utils.py`, `template-html/page.html`, `docs/style/main.css`, `data/README.md`:
  new optional `recipes` list (each recipe may carry a `note` sentence). Old single `recipe` still works. Needed because many pages
  have several recipes. This is the only non-data change.
- `data/categories.json`: new category `legacy` ("Legacy / Not in the current mod").
- Legacy (10): item_filter, water_walking_boots, obsidian_water_walking_boots, obsidian_skull, lava_charm, lava_waders, pitcher_plant,
  custom_crafting_tables, inventory_rerouter, biome_blocks. Text kept, status line added at the top.
- New pages (7): super_lubricent_ice, bottle_of_air, biome_crystal, id_card, redstone_tool, spectre_ingot, configuration (20 options).
- Text edited on 73 existing pages (details per group in `audit/edits/A..H.md`).
- Recipes: structured `recipes` on 75 pages, built by script from the mod's recipe JSON (`audit/tools/build_recipes.py`). Shapeless
  recipes carry the sentence "Shapeless: the ingredients can be placed anywhere in the crafting grid." Colour families show one example
  (any dye) plus a sentence; wood platforms likewise. Old recipe pictures removed on those pages. Ectoplasm lost its picture (the mod has
  no ectoplasm recipe). Special recipes (compass setters, Portkey camo, Diaphanous set/invert, Spectre Anchor combine) have no grid;
  their existing text stays.
- `docs/` regenerated (107 pages + index). Ingredient icons: only a few textures exist locally, so most slots show text labels. I did
  not run the fetch scripts (they download).

## Left as is, per your decisions
spectre_sapling, runic_dust, luminous_powder, spectre_coils, spectre_illuminator, bean (J1): text AND recipe pictures untouched.

## Possible mod bugs for you to investigate (NOT on the wiki; the wiki sentence was left unchanged where it conflicts)
1. Advanced Item Collector range: code max 16 hard-coded; original max 10 via config `AdvancedItemCollectorMaxRange`. (item_collector)
2. Block Breaker drop routing only works when config `MagneticEnchantment` is on; original had no such dependency.
3. Spectre Lens: code = one lens per player, any dimension; original = per dimension (wiki matches original).
4. Peace Candle ignores slimes (early return) and church swap is plains villages only; original covered all monsters and all villages.
5. Slime Cube: original probably kept the height limit; code forces allow at any height.
6. Redstone Interface red connection lines: today only Observers draw lines; original drew Interface lines too.
7. Redstone Tool linking may be unreachable (GUI opens first); Advanced Interface targets and Observer output after reload.
8. Possible duplication: Notification Interface drops its copied icon stack. Imbuing Station, Potion Vaporizer, Inventory Tester do not drop contents.
9. Reinforced Ender Bucket drains the source before checking the fluid; Eclipsed Clock drop loses target time and sneak from 0 goes
   negative; Floo fireplace may be deleted when its chunk is unloaded; Stable Ender Pearl does not cross dimensions (original did) and a
   tag without UUID throws.
10. Sound Box and Slime Cube `powered` not saved; Advanced Redstone Torch strength setters skip markDirty; Analog Emitter input facing.
11. Missing config keys vs the original: Rain Shield range, Ancient Furnace area (wiki now states the fixed values, as you asked).
12. Missing features vs the old wiki text (pages left as is): Spectre Sapling conversion, Rune Pattern from paper, Luminous Powder glint,
    Numbered Coils, Blackout Powder, Nature Core, Magic Bean obtain path.
13. TeleporterTransformer return trip from the Spectre dimension; Biome Crystal in every chest table (documented as is).

## Decisions I applied by default (you did not answer; easy to revert)
J4 Experience imbue = +100% (original does the same); J7 Super Lubricent page = tincture only plus new Ice page; J8 folded pages stay folded;
J10 legacy list as in the matrix; J11 OpenComputers / mod-ore sentences removed. The sneak-click wipe on Sound Pattern is written (original
does the same: checked in the original source).

## HOLD (not written, needs a playtest or your call)
Cross-dimension storage (Floo, Ender Letter, Spectre coil/charger buffer), Redstone Tool link steps, rain sound suppression, Block Breaker
tool limits, Destabilizer lazy mode, Diaphanous fade distances, Returning Block of Sticks full inventory, Time in a Bottle wheel, Ender Anchor
reload, Magnetic hoe/rarity, Blaze fire speed beyond tick rate. Full list: matrix section 8 and `audit/edits/*.md`.
Note: redstone_interface / redstone_observer got "Linking: Redstone Tool" property rows that only repeat the existing (HOLD) claim.

## Checks run
Script over all 107 data files: 0 non-ASCII, 0 invalid categories, 0 missing generated pages, all recipe shapes 3x3, properties well formed.
Version-word scan: 2 hits, both the harmless phrase "no longer". Rendered floo_teleportation and item_filter in a headless browser: recipes,
shapeless note and Legacy status display correctly.
NOT done: parity check of branches 1.16.5..1.21.11; media (screenshots/videos) staleness not judged (e.g. screenshots may show removed GUI
options such as the Igniter's third mode; biome_radar text still refers to "Screenshot 1"); recipe icons are mostly text labels; second
re-read of the highest-risk pages against code; scope-decisions.md was not available to the edit agents inside the worktree, so divergence
notes were added by me only for Colored Grass, Potion Vaporizer and Global Chat Detector.

## Follow-up round (your answers on the possible-bug list)
- Advanced Item Collector: documented as range 0 to 16 per direction (default 5), no config option. (item_collector)
- Spectre Lens: wording left as is (your decision: it should be per player per dimension; the mod behaviour is the bug to fix).
- Peace Candle: wiki unchanged (it should not ignore slimes; plains-only churches are a bug).
- Block Breaker: documented that collecting drops into the inventory behind it needs config `MagneticEnchantment` (default true).
- Redstone Tool: linking is reachable, so the link steps are now on `redstone_tool` (right click Observer / Interface, then the target).
- Notification Interface duplication: not a bug (you verified); nothing was on the wiki.
- New page `ender_mailbox` (block); the mailbox recipe moved there from `ender_letter`.
- Texture downloader: ran `scripts/fetch_vanilla_textures.py`; the network policy returned 403 Forbidden for minecraft.wiki on every request
  (45 attempted, 0 downloaded, then stopped). No texture files were added; recipe slots keep text labels. To allow it, add `minecraft.wiki`
  under Allowed domains in the environment's Network access settings, then rerun the script and regenerate.
- Bug list items 1, 3 (partly), 4, 5, 6 of PHASE3_REPORT.md are resolved or reclassified; items 2/3 remain open for the mod.

## Playtest round (your answers) folded into the wiki
Spectre Key return position kept even after death; Ender Letters/mailboxes work across dimensions; Floo is same-dimension only; Spectre
energy buffer shared across dimensions; Advanced Redstone Interface / Observer survive reload; slime block compresses one step per click;
Block Breaker does nothing on blocks an iron pickaxe cannot break; Diaphanous fade distances about 3.6 / 6.2 blocks; Online Detector name is case
sensitive; Chunk Analyzer scan "several seconds"; Rain Shield suppresses rain particles and sounds; Sound Dampener/Recorder work in multiplayer;
Magnetic not on hoes/shears and as rare as Silk Touch; Blaze and Steel fire burns out and spreads faster (original sentence restored);
Lotus not on podzol/coarse dirt; Lapis Lamp, Peace Candle, Slime Cube affect spawner mobs but not spawn-egg mobs.
Not documented by your request: Returning Block of Sticks losing the item with a full inventory.
Open: Lotus answer said "only tilled soil" but the code accepts grass, dirt or farmland; the page keeps the code-based list and adds "not on
podzol or coarse dirt". Tell me if only farmland is right.

## Lotus soil (resolved)
You confirmed Lotus should grow on farmland only. The code (LotusBlock.isValidGround: grass block, dirt or farmland) is therefore a mod bug
for you to fix; the wiki now says "farmland (tilled soil)" only.
