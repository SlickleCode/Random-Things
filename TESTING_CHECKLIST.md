# Random Things 1.14.4 - Testing Checklist (open items only)

The full numbered sheet with every row's test steps and your results (2026-10-11) is in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md). This file lists only what still needs you. Record a result by filling the Result column here (Pass / Fail + one sentence), or just tell Claude the row number. Root-cause / fix writeups: [`docs/testing/fix-log/`](docs/testing/fix-log/README.md).

**Totals (original sheet, 392 rows):** PASS 227, never tested 105, DONE/REMOVED 44, FIXED 16. **90 of the 121 open rows were removed on 2026-10-14 because the automated suites/scripted scenarios now cover them** (list with evidence: [`docs/testing/covered-by-tests.md`](docs/testing/covered-by-tests.md)); 31 remain below.

**Ordered playtest route (about 90 minutes, with commands): [`docs/testing/playtest-route-1.14.4.md`](docs/testing/playtest-route-1.14.4.md).**

## A. Failing or partial (0)

None.

## B. Fixed, waiting for your retest (2)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 261 | Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request) | FIXED | |
| 389 | **Biome Radar crystal hovered above the block (reported while watching the scripted run):** the renderer floated and bobbed the crystal ~1 block above the radar; 1.12.2 pinned the item's bob phase so it spins in place inside the block (`RenderBiomeRadar`). Now rendered at about 0.4 blocks up, no bobbing, spinning in place. | FIXED | Check by eye: insert a crystal, it spins in the middle of the radar, also while searching. |

(What was fixed and why: the row's entry in `docs/testing/fix-log/`, or the row text in the archive.)

## C. Never tested (29 rows with no result)

Row number, feature, and the archive file holding the full test steps. Mostly crafting-recipe rows (the automated suite crafts every data recipe) and secondary behaviour of features whose main rows passed. Fill any of them in when you do test it.

| # | Feature (short) | Full steps in |
|---|---|---|
| 174 | Imbuing Station - progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in pro... | 5-batch-3-remaining-non-block-items-1.md |
| 211 | Ender Mailbox - sneak-right-click with an addressed letter naming a player who doesn't exist: get a "does not seem ... | 5-batch-3-remaining-non-block-items-1.md |
| 213 | Ender Mailbox - right-click a mailbox someone else placed (not sneaking): "You are not the owner of this mailbox," ... | 5-batch-3-remaining-non-block-items-1.md |
| 244 | Creative Player Interface - creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves id... | 5-batch-3-remaining-non-block-items-1.md |
| 309 | Spectre Coil (Genesis) - same attach/drop behavior as the other tiers, but generates a flat 10,000,000 FE/tick dire... | 5-batch-3-remaining-non-block-items-2.md |
| 325 | Spirit - killing any living entity has a small chance to spawn a tiny flying wisp at the death spot (higher at nigh... | 5-batch-3-remaining-non-block-items-3.md |
| 326 | Spirit - drifts aimlessly near where it appeared, floats through the air (not on the ground), and despawns on its o... | 5-batch-3-remaining-non-block-items-3.md |
| 327 | Spirit - immune to regular attacks/arrows/fire/etc; only a Spectre Sword (or creative-mode/out-of-world damage) can... | 5-batch-3-remaining-non-block-items-3.md |
| 328 | Spirit - killing it with a Spectre Sword drops 1-2 Ectoplasm | 5-batch-3-remaining-non-block-items-3.md |
| 329 | Batch 1 block loot tables - break a spread of Batch 1 blocks in survival (stained brick, luminous block, biome ston... | 5-batch-3-remaining-non-block-items-3.md |
| 330 | Bean Sprout - breaking a fully grown (age 7) sprout drops 3-4 beans total; a young sprout drops just 1 | 5-batch-3-remaining-non-block-items-3.md |
| 331 | Colored Grass - breaking it drops plain Dirt; with a Silk Touch tool it drops itself | 5-batch-3-remaining-non-block-items-3.md |
| 332 | Compressed Slime Block - breaking it drops a regular vanilla Slime Block | 5-batch-3-remaining-non-block-items-3.md |
| 333 | Blocks that should drop nothing still drop nothing: Blazing Fire, Floo Brick, Rune Base, Bean Stalk/Lesser Bean Sta... | 5-batch-3-remaining-non-block-items-3.md |
| 334 | Chest loot injection - Summoning Pendulum, Magic Hood and Slime Cube occasionally appear in dungeon chests; Magic H... | 5-batch-3-remaining-non-block-items-3.md |
| 335 | Chest loot injection - a Biome Crystal shows up in roughly 1 in 5 vanilla chests, already tuned to a random biome (... | 5-batch-3-remaining-non-block-items-3.md |
| 336 | Config - first launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` boolean o... | 5-batch-3-remaining-non-block-items-3.md |
| 337 | Config - `MagneticEnchantment = false` stops Magnetic appearing at the enchanting table/on books and stops it pulli... | 5-batch-3-remaining-non-block-items-3.md |
| 338 | Config - `MagicHood`/`SummoningPendulum`/`SlimeCube`/`BiomeCrystal = false` under `[loot]` stops that item being in... | 5-batch-3-remaining-non-block-items-3.md |
| 339 | Bean Pod - breaking one drops iron (8-20), gold (4-15), diamonds (1-5), 0-2 emeralds, 4-8 beans and a Golden Egg (i... | 5-batch-3-remaining-non-block-items-3.md |
| 340 | Bean Pod - `GoldenEgg = false` in the config removes the egg from the drops; blowing the pod up with TNT drops only... | 5-batch-3-remaining-non-block-items-3.md |
| 345 | Spirit config - `[numbers]` section: `SpiritLifeTime`, `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceE... | 5-batch-3-remaining-non-block-items-3.md |
| 347 | Runes - `[visual] FlatRunes = true` in the config switches them to the plain flat texture (takes effect on the next... | 5-batch-3-remaining-non-block-items-3.md |
| 348 | Recipes - open the recipe book / a crafting table and confirm the backfilled recipes appear and craft (45 new): Ite... | 5-batch-3-remaining-non-block-items-3.md |
| 353 | Config - `[visual] HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>` (no dimens... | 5-batch-3-remaining-non-block-items-3.md |
| 354 | Config - `[numbers] BlockDestabilizerLimit`: default 50 blocks per activation as before; raise it (e.g. 500) and a ... | 5-batch-3-remaining-non-block-items-3.md |
| 355 | Config - `[numbers] TriggerGlassChainLimit`: default 20 as before; lower it (e.g. 3) and only that many touching Tr... | 5-batch-3-remaining-non-block-items-3.md |
| 358 | Ender Anchor chunk loading - `[features] EnderAnchorChunkloading = false`: newly placed anchors don't force anythin... | 5-batch-3-remaining-non-block-items-3.md |
| 359 | Ender Anchor tooltip - hovering the item (inventory/creative) shows "Keeps the chunk it is in loaded"; the line dis... | 5-batch-3-remaining-non-block-items-3.md |

## D. Visual / audio spot-checks that the scripts only measure

The scenarios assert state and pixel measurements for these (rows removed from B/C above), but only your eyes/ears confirm they look right - a quick look is enough.

| # | Look/listen for | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener: the muted sound is really silent near the block and audible again beyond 20 blocks |  |  |
| 234 | Fluid Display: takes the poured fluid's texture/colour, still vs flowing looks right |  |  |
| 254 | Magic Hood: no potion swirl around you in F5 view (and other players see no nametag, two-player check) |  |  |
| 265 | Ancient Furnace: the area around it visibly turns warm biome colours and the snow melts after the explosion |  |  |
| 277/279 | Portkey: dropped item shimmers then stops glowing about 5 s later; a disguised Portkey looks like the donor item in hand/ground/item frame |  |  |
| 319 | Light Redirector: open face shows the block on the opposite side (periscope picture) |  |  |
| 321-323, 390 | Dyeing Machine: recolored items are tinted held/in inventory/on the ground/worn; the enchant-glint result glows in the dye colour on items AND worn armor |  |  |
| 346 | Runes: noisy texture, same-colour pixels join across the base edge, nothing flickers while you move |  |  |
| 149-153 | Biome Radar particle colours; (#389 above) the crystal spins in the middle of the radar |  |  |
