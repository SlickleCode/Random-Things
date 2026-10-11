# Random Things 1.14.4 - Testing Checklist (open items only)

The full numbered sheet with every row's test steps and your results (2026-10-11) is in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md). This file lists only what still needs you. Record a result by filling the Result column here (Pass / Fail + one sentence), or just tell Claude the row number. Root-cause / fix writeups: [`docs/testing/fix-log/`](docs/testing/fix-log/README.md).

**Totals (388 rows):** PASS 227, blank 105, DONE 40, FIXED 12, REMOVED 4

## A. Failing or partial (0)

None.

## B. Fixed, waiting for your retest (12)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener — place, right-click to open its 9-slot GUI, insert a few full Sound Patterns: any of those sounds played by ANYONE within ~20 blocks of the block go silent for everyone in range, not just you | FIXED | |
| 169 | Imbuing Station — put the COBBLESTONE in the center slot and a water bottle + vine + bone meal in the 3 ingredient slots (any order, as in 1.12.2): after 200 ticks, produces Mossy Cobblestone in the output slot and consumes exactly 1 of each input | FIXED | |
| 234 | Fluid Display — right-click with a filled fluid container (bucket, Ender Bucket, etc.): block takes on that fluid's texture/color, still image | FIXED | |
| 238 | Evil Tear — craft: Wither Skeleton Skull + Ghast Tear + Ender Pearl (shapeless, any arrangement) | FIXED | |
| 254 | Magic Hood — wear it as a helmet: your potion-effect particle swirl no longer shows to anyone, whether you're sneaking or not | FIXED | |
| 258 | Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire extinguishing near it - the mechanical effects of rain/snow are suppressed | FIXED | |
| 261 | Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request) | FIXED | |
| 265 | Ancient Furnace — after a multi-minute heat-up, it melts surface snow (and ice, to water) in a large radius around itself, reassigns that area's biome to its warmer counterpart (e.g. Snowy Tundra -> Plains), then explodes and is destroyed | FIXED | |
| 277 | Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the ground it stops despawning and stops glowing (primed); picking it back up before that resets the countdown next time it's dropped | FIXED | |
| 279 | Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - inventory, held, dropped on the ground, item frame), the donor item is returned unconsumed | FIXED | |
| 287 | Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom; sneak-right-click opens an "edit" GUI (9 Position Filter slots, same as the Advanced Interface, plus your normal inventory) to bind up to 9 targets; plain right-click opens a compact "use" GUI with one button per bound target, each firing a 20-tick strength-15 pulse at its position when pressed | FIXED | |
| 293 | Block Destabilizer — a redstone pulse flood-fills every block connected to the one directly in front of it that matches the same exact `BlockState` (not just the same block with different properties) *and is a normal full cube*, up to 50 blocks, then drops the whole matched structure as falling-block entities top row first, nearest-to-the-machine first within each row | FIXED | |

(What was fixed and why: the row's entry in `docs/testing/fix-log/`, or the row text in the archive.)

## C. Never tested (105 rows with no result)

Row number, feature, and the archive file holding the full test steps. Mostly crafting-recipe rows (the automated suite crafts every data recipe) and secondary behaviour of features whose main rows passed. Fill any of them in when you do test it.

| # | Feature (short) | Full steps in |
|---|---|---|
| 49 | Glowing Mushroom **[covered by automated worldgen tests]** | 2-batch-1-standalone-blocks-no-gui-tile-.md |
| 121 | Bean Stew - eat it, restores 8 hunger, leaves you holding an empty bowl afterward | 5-batch-3-remaining-non-block-items-1.md |
| 149 | Biome Radar - right-click with an empty hand and a Biome Crystal in your other hand's slot/held: inserting works, a... | 5-batch-3-remaining-non-block-items-1.md |
| 150 | Biome Radar - build the antenna (iron bars in the specific pattern above the radar — see `BiomeRadarTileEntity.isVa... | 5-batch-3-remaining-non-block-items-1.md |
| 151 | Biome Radar - let a search finish (crystal's target biome found): particles change to the target biome's color, the... | 5-batch-3-remaining-non-block-items-1.md |
| 152 | Biome Radar - break one of the antenna's iron bars while a search is active (not directly adjacent to the radar, so... | 5-batch-3-remaining-non-block-items-1.md |
| 153 | Biome Radar - break the radar block itself while it has a crystal inserted: the crystal drops instead of being dele... | 5-batch-3-remaining-non-block-items-1.md |
| 159 | Portable Sound Dampener - right-click to open its 9-slot GUI (carried in your own inventory, not placed), insert a ... | 5-batch-3-remaining-non-block-items-1.md |
| 160 | Sound Dampener / Portable Sound Dampener - break the block or take the pattern back out of a slot: the muting stops... | 5-batch-3-remaining-non-block-items-1.md |
| 168 | Imbuing Station - craft: water bucket + emerald + 2x vine + 2x lily pad + terracotta | 5-batch-3-remaining-non-block-items-1.md |
| 170 | Imbuing Station - water bottle (center) + coal + flint + blaze powder -> Fire Imbue potion | 5-batch-3-remaining-non-block-items-1.md |
| 171 | Imbuing Station - water bottle (center) + spider eye + rotten flesh + red mushroom -> Poison Imbue potion | 5-batch-3-remaining-non-block-items-1.md |
| 172 | Imbuing Station - water bottle (center) + Lesser Magic Bean + lapis lazuli + glowstone dust -> Experience Imbue pot... | 5-batch-3-remaining-non-block-items-1.md |
| 173 | Imbuing Station - water bottle (center) + wither skeleton skull + nether brick + ghast tear -> Wither Imbue potion | 5-batch-3-remaining-non-block-items-1.md |
| 174 | Imbuing Station - progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in pro... | 5-batch-3-remaining-non-block-items-1.md |
| 175 | Drinking a Fire/Poison/Experience/Wither Imbue potion grants its effect for 20 minutes, leaves an empty glass bottl... | 5-batch-3-remaining-non-block-items-1.md |
| 176 | While Fire Imbue is active, dealing direct melee/attack damage (not projectiles) sets the target on fire for 10s | 5-batch-3-remaining-non-block-items-1.md |
| 177 | While Poison Imbue is active, a direct hit gives the target Poison II for 10s | 5-batch-3-remaining-non-block-items-1.md |
| 178 | While Wither Imbue is active, a direct hit gives the target Wither II for 5s | 5-batch-3-remaining-non-block-items-1.md |
| 179 | While Experience Imbue is active, killing a mob drops double XP | 5-batch-3-remaining-non-block-items-1.md |
| 181 | Floo Pouch - craft: leather (8-around) + Floo Powder (center); sneak-right-click tops it up from any Floo Powder in... | 5-batch-3-remaining-non-block-items-1.md |
| 182 | Floo Sign - craft: Floo Powder (8-around) + any wood-type sign (center) | 5-batch-3-remaining-non-block-items-1.md |
| 183 | Floo Token - craft: Floo Powder (8-around) + paper (center), makes 2 | 5-batch-3-remaining-non-block-items-1.md |
| 193 | Eclipsed Clock - craft: 4x obsidian + 4x gold ingot + ghast tear (center) | 5-batch-3-remaining-non-block-items-1.md |
| 198 | Time in a Bottle - craft: 3x gold ingot + 2x diamond + 2x lapis lazuli + clock + glass bottle | 5-batch-3-remaining-non-block-items-1.md |
| 202 | Weather Egg (Sun/Rain/Storm) - craft each: obsidian corners + fire charge (center) + the type-specific ingredient (... | 5-batch-3-remaining-non-block-items-1.md |
| 206 | Ender Mailbox - craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center) | 5-batch-3-remaining-non-block-items-1.md |
| 207 | Ender Letter - craft: 2x paper (diagonal) + ender pearl (center) | 5-batch-3-remaining-non-block-items-1.md |
| 209 | Ender Mailbox - place one: it remembers you as its owner | 5-batch-3-remaining-non-block-items-1.md |
| 210 | Ender Mailbox - sneak-right-click any mailbox (yours or anyone else's) while holding an addressed letter: delivers ... | 5-batch-3-remaining-non-block-items-1.md |
| 211 | Ender Mailbox - sneak-right-click with an addressed letter naming a player who doesn't exist: get a "does not seem ... | 5-batch-3-remaining-non-block-items-1.md |
| 212 | Ender Mailbox - right-click your own placed mailbox (not sneaking): opens your personal inbox - the same inbox no m... | 5-batch-3-remaining-non-block-items-1.md |
| 213 | Ender Mailbox - right-click a mailbox someone else placed (not sneaking): "You are not the owner of this mailbox," ... | 5-batch-3-remaining-non-block-items-1.md |
| 214 | Ender Mailbox - after mail arrives in your inbox, the mailbox block starts glowing/emitting portal particles within... | 5-batch-3-remaining-non-block-items-1.md |
| 215 | Receiving a letter: take it from your inbox into your own inventory, right-click it - opens the same letter GUI, ou... | 5-batch-3-remaining-non-block-items-1.md |
| 218 | Ender Bucket - craft: 2x iron ingot (top corners) + ender pearl (center) | 5-batch-3-remaining-non-block-items-1.md |
| 244 | Creative Player Interface - creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves id... | 5-batch-3-remaining-non-block-items-1.md |
| 245 | Ender Anchor - craft: 8x obsidian (ring) + Stable Ender Pearl (center) | 5-batch-3-remaining-non-block-items-1.md |
| 246 | Ender Bridge - craft: 6x end stone + redstone (center) + Stable Ender Pearl (right-middle) | 5-batch-3-remaining-non-block-items-1.md |
| 253 | Magic Hood - wear it as a helmet: other players can no longer see your nametag above your head, whether you're snea... | 5-batch-3-remaining-non-block-items-1.md |
| 256 | Rain Shield - craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row) | 5-batch-3-remaining-non-block-items-1.md |
| 259 | Rain Shield - powered via redstone: shielding turns off, rain/snow affects the area normally again | 5-batch-3-remaining-non-block-items-1.md |
| 270 | Peace Candle - in a plains village, roughly a third of the tall cobblestone churches are instead the Peace Candle c... **[covered by automated worldgen tests]** | 5-batch-3-remaining-non-block-items-2.md |
| 275 | Portkey - craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center) | 5-batch-3-remaining-non-block-items-2.md |
| 281 | Spectre Anchor - craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 E... | 5-batch-3-remaining-non-block-items-2.md |
| 284 | Basic/Advanced Redstone Interface - whatever redstone signal (weak or strong) is currently hitting the interface bl... | 5-batch-3-remaining-non-block-items-2.md |
| 285 | Advanced Redstone Interface - craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-... | 5-batch-3-remaining-non-block-items-2.md |
| 288 | Magnetic Enchantment - appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mini... | 5-batch-3-remaining-non-block-items-2.md |
| 289 | Block Breaker - craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way ... | 5-batch-3-remaining-non-block-items-2.md |
| 292 | Block Destabilizer - craft: obsidian at the 4 corners, sand left/right of a diamond in the middle row, redstone abo... | 5-batch-3-remaining-non-block-items-2.md |
| 294 | Block Destabilizer - GUI: Fuzzy toggle button makes the match ignore block-state variants and match by `Block` type... | 5-batch-3-remaining-non-block-items-2.md |
| 295 | Block Destabilizer - GUI: Lazy toggle remembers which connected blocks *didn't* match last time, so re-triggering d... | 5-batch-3-remaining-non-block-items-2.md |
| 296 | Block Destabilizer - falling blocks landing on an obstruction (another block, a tile-entity-bearing block) vanish w... | 5-batch-3-remaining-non-block-items-2.md |
| 297 | Spectre Key - craft: Spectre Ingot at the 3 corners (top-left, middle-left, bottom-right), Stable Ender Pearl cente... | 5-batch-3-remaining-non-block-items-2.md |
| 303 | Spectre Sword - craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) a... | 5-batch-3-remaining-non-block-items-2.md |
| 307 | Spectre String - craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4 | 5-batch-3-remaining-non-block-items-2.md |
| 308 | Spectre Coil (Normal/Redstone/Ender) - craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre I... | 5-batch-3-remaining-non-block-items-2.md |
| 309 | Spectre Coil (Genesis) - same attach/drop behavior as the other tiers, but generates a flat 10,000,000 FE/tick dire... | 5-batch-3-remaining-non-block-items-2.md |
| 310 | Spectre Energy Injector - craft: Obsidian at the 4 corners, Spectre Lens top-center, a vanilla Beacon center, Spect... | 5-batch-3-remaining-non-block-items-2.md |
| 311 | Spectre Charger (Normal/Redstone/Ender/Genesis) - craft the first 3 tiers same escalating pattern as the Coils (Gen... | 5-batch-3-remaining-non-block-items-2.md |
| 312 | Spectre Lens - craft: Spectre Ingot at the 4 corners, Emerald top/bottom-center, Diamond left/right-center, Glass i... | 5-batch-3-remaining-non-block-items-3.md |
| 314 | Diaphanous Block - craft (Quartz Glass + 4 colored glass in a ring, see recipe) places a plain Stone-displaying blo... | 5-batch-3-remaining-non-block-items-3.md |
| 318 | Light Redirector - craft (planks + glass ring, see recipe); right-click a face to toggle it open/closed (texture sw... | 5-batch-3-remaining-non-block-items-3.md |
| 319 | Light Redirector - an *open* face shows whatever block is directly on the *opposite* side of the redirector instead... | 5-batch-3-remaining-non-block-items-3.md |
| 320 | Dyeing Machine - craft (Green/Red/Blue Dye + White Wool + Crafting Table, see recipe) and place; right-click opens ... | 5-batch-3-remaining-non-block-items-3.md |
| 321 | Dyeing Machine - put any non-block item in the left slot and a vanilla dye in the dye slot; taking the "recolor" re... | 5-batch-3-remaining-non-block-items-3.md |
| 322 | Dyeing Machine - taking the "enchant glint" result instead gives back a copy of the item whose enchantment shimmer ... | 5-batch-3-remaining-non-block-items-3.md |
| 323 | Dyeing Machine - a dyed armor piece (from the "recolor" result) also shows tinted while worn on your character mode... | 5-batch-3-remaining-non-block-items-3.md |
| 324 | Dyeing Machine - closing the GUI while the item/dye slots still hold something drops those items at your feet inste... | 5-batch-3-remaining-non-block-items-3.md |
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
| 341 | Glowing Mushrooms - generate a fresh world/new chunks and explore caves: small patches of glowing mushrooms appear ... **[covered by automated worldgen tests]** | 5-batch-3-remaining-non-block-items-3.md |
| 342 | Grass Seeds - craft one from a Grass Block (shapeless); right-click plain Dirt with it: becomes normal grass, seed ... | 5-batch-3-remaining-non-block-items-3.md |
| 343 | Colored Grass Seeds - Grass Seeds + one vanilla dye (shapeless) gives that color's seeds (16 colors); right-clickin... | 5-batch-3-remaining-non-block-items-3.md |
| 344 | Colored Grass - spreads its color onto nearby Dirt over time when lit; reverts to Dirt when covered by an opaque bl... | 5-batch-3-remaining-non-block-items-3.md |
| 345 | Spirit config - `[numbers]` section: `SpiritLifeTime`, `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceE... | 5-batch-3-remaining-non-block-items-3.md |
| 346 | Runes - placed rune pixels now show the original's noisy texture (each pixel/connection strip slightly different, t... | 5-batch-3-remaining-non-block-items-3.md |
| 347 | Runes - `[visual] FlatRunes = true` in the config switches them to the plain flat texture (takes effect on the next... | 5-batch-3-remaining-non-block-items-3.md |
| 348 | Recipes - open the recipe book / a crafting table and confirm the backfilled recipes appear and craft (45 new): Ite... | 5-batch-3-remaining-non-block-items-3.md |
| 349 | Recipes - Lapis Glass (x8), Quartz Glass (x8), Lapis/Quartz Lamp, Trigger Glass (x4); Biome Glass and the five Biom... | 5-batch-3-remaining-non-block-items-3.md |
| 350 | Recipes - Super Lubricent: Ice (x16: slimeball/ice/water bucket), Platform (x6), Filtered Platform, new **Super Lub... | 5-batch-3-remaining-non-block-items-3.md |
| 351 | Recipes - Stable Ender Pearl, Lesser Magic Bean (8 gold nuggets around Beans), Bean Stew (both orientations: wheat ... | 5-batch-3-remaining-non-block-items-3.md |
| 352 | Recipes - shapes were ported with 1.12.2's exact (left-aligned, right-padded) layouts, so a few look lopsided (Item... | 5-batch-3-remaining-non-block-items-3.md |
| 353 | Config - `[visual] HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>` (no dimens... | 5-batch-3-remaining-non-block-items-3.md |
| 354 | Config - `[numbers] BlockDestabilizerLimit`: default 50 blocks per activation as before; raise it (e.g. 500) and a ... | 5-batch-3-remaining-non-block-items-3.md |
| 355 | Config - `[numbers] TriggerGlassChainLimit`: default 20 as before; lower it (e.g. 3) and only that many touching Tr... | 5-batch-3-remaining-non-block-items-3.md |
| 356 | Ender Anchor chunk loading - place an Ender Anchor far from any player (e.g. 300+ blocks from an Ender Bridge facin... | 5-batch-3-remaining-non-block-items-3.md |
| 357 | Ender Anchor chunk loading - persists across a save/quit/reload (chunk still force-loaded on reload, no duplicate r... | 5-batch-3-remaining-non-block-items-3.md |
| 358 | Ender Anchor chunk loading - `[features] EnderAnchorChunkloading = false`: newly placed anchors don't force anythin... | 5-batch-3-remaining-non-block-items-3.md |
| 359 | Ender Anchor tooltip - hovering the item (inventory/creative) shows "Keeps the chunk it is in loaded"; the line dis... | 5-batch-3-remaining-non-block-items-3.md |
| 356b | Config - `[worldgen] PeaceCandle = false`: no plains village church is replaced any more, new villages generate exa... **[covered by automated worldgen tests]** | 5-batch-3-remaining-non-block-items-3.md |
