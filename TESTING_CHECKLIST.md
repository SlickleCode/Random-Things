# Random Things 1.16.5 - Testing Checklist

**Ready-made test world:** `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` opens a seed-424242 normal-terrain world with every block on show, set-ups and a stocked inventory - see [`docs/testing/manual-test-world.md`](docs/testing/manual-test-world.md). with `Pass` or `Fail` (leave blank if untested) and a one-sentence note in **Feedback** (what you did, what happened vs. expected).
Row numbers are the original sheet's, so references like "#158" still work. **Every result is blank: nothing on this branch has been tested by hand yet** (the 1.14.4 sheet was).

This sheet lists only what the automated suites and scripted scenarios do **not** cover: 204 rows. The 140 rows they do cover are listed with their evidence in [`docs/testing/covered-by-tests-1.16.5.md`](docs/testing/covered-by-tests-1.16.5.md); the 44 rows for features that were removed on request are dropped. The full 1.14.4 sheet with its old results is in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md); a suggested order for these checks (rendering rows first) is in `PORTING_PLAN.md` "What to do next".

## 1. Icons, Trigger/Lapis/Quartz Glass, lamps (19) - check these first, they were the 1.14.4 priority fixes

| # | Feature | Result | Feedback |
|---|---|---|---|
| 1 | Contact Lever — item icon (inventory/hand) renders correctly |  |  |
| 2 | Contact Lever — block appearance, unpowered |  |  |
| 3 | Contact Lever — block appearance, powered/active |  |  |
| 4 | Luminous Brick — item icon renders at full brightness (check a few of the 16 colors) |  |  |
| 5 | Stained Brick (non-luminous) — item icon renders correctly |  |  |
| 15 | Trigger Glass — unpowered: renders translucent (not opaque) |  |  |
| 16 | Trigger Glass — powered: no collision, can walk through |  |  |
| 17 | Trigger Glass — powered: no suffocation damage while standing in it |  |  |
| 18 | Trigger Glass — powered: visibly more translucent than unpowered |  |  |
| 19 | Trigger Glass — re-solidifies ~60 ticks after power is removed |  |  |
| 20 | Trigger Glass — sand/gravel falls through while triggered |  |  |
| 21 | Lapis Glass — solid to the player (can stand/walk on top, blocks walking through) |  |  |
| 22 | Lapis Glass — passable to non-player entities (mobs/items/arrows pass through) |  |  |
| 23 | Lapis Glass — no suffocation damage to the player |  |  |
| 24 | Quartz Glass — passable to the player (walk straight through, no collision) |  |  |
| 25 | Quartz Glass — solid to non-player entities (a mob can't walk through it) |  |  |
| 26 | Quartz Glass — no suffocation damage to the player |  |  |
| 27 | Lapis Glass — jumping/falling onto it from above lands correctly |  |  |
| 28 | Quartz Glass — a mob correctly paths around it instead of into it |  |  |

## 2. Batch 1 — standalone blocks (no GUI/tile entity) (27)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 29 | Lapis Lamp |  |  |
| 30 | Quartz Lamp |  |  |
| 31 | Super Lubricent Ice |  |  |
| 32 | Super Lubricent Platform |  |  |
| 136 | Super Lubricent Stone |  |  |
| 33 | Compressed Slime Block |  |  |
| 34 | Contact Button |  |  |
| 35 | Spectre Block (plain decorative) |  |  |
| 36 | Spectre Log |  |  |
| 37 | Spectre Plank |  |  |
| 38 | Spectre Leaves |  |  |
| 39 | Spectre Sapling — grows into a tree (simplified fixed shape, not worldgen-feature-based) |  |  |
| 50 | Sided Redstone |  |  |
| 52 | Stained Brick — non-luminous variants (spot-check a few of the 16 colors) |  |  |
| 53 | Stained Brick — luminous variants, glow correctly (spot-check a few of the 16 colors) |  |  |
| 56 | Colored Grass — all 16 colors via Grass Seeds, tinted, spreads, shovel preference, "<Color> Grass Block" name |  |  |
| 57 | Luminous Block — glows correctly (spot-check a few of the 16 colors) |  |  |
| 58 | Luminous Translucent Block — glows + is see-through (spot-check a few colors) |  |  |
| 59 | "Beans" item — plants Bean Sprout |  |  |
| 60 | "Lesser Magic Bean" item — plants the lesser Bean Stalk |  |  |
| 61 | "Magic Bean" item — plants the fast/strong magic Bean Stalk |  |  |
| 62 | Bean Sprout - also plantable on grass/dirt (FIXED 2026-10-17; used to need farmland) |  |  |
| 63 | Bean Stalk (lesser) |  |  |
| 64 | Bean Stalk (magic) |  |  |
| 65 | Pod — drops beans when broken |  |  |
| 66 | Lotus Seeds — plants Lotus |  |  |
| 67 | Lotus — 4 growth stages, harvestable at max age without breaking the plant<br> |  |  |

## 3. Batch 2 — tile-entity/GUI-backed blocks (19)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 77 | Item Collector — 6-way directional model looks correctly oriented on all faces |  |  |
| 80 | Advanced Redstone Repeater — configurable turn-on delay respected |  |  |
| 81 | Advanced Redstone Repeater — configurable turn-off delay respected (independently of turn-on) |  |  |
| 82 | Iron Dropper — 9-slot inventory GUI |  |  |
| 83 | Iron Dropper — 6-way facing on placement |  |  |
| 87 | Iron Dropper — drops its inventory contents when broken |  |  |
| 89 | Player Interface — exposes placer's online inventory correctly per side (hotbar below, armor above, offhand north, rest elsewhere) |  |  |
| 90 | Inventory Tester — weak signal reflects whether held item could insert into faced inventory, with invert toggle |  |  |
| 94 | Redstone Tool |  |  |
| 98 | Potion Vaporizer — burns fuel correctly |  |  |
| 99 | Potion Vaporizer — bottle-out slot can't be manually filled by the player, only by the machine |  |  |
| 104 | Entity Detector — filter modes (All/Living/Animal/Monster/Player/Items) each trigger correctly |  |  |
| 105 | Entity Detector — Custom mode with an Entity Filter Item in the slot |  |  |
| 106 | Entity Detector — per-axis radius (0–10) is respected |  |  |
| 108 | Entity Filter Item — right-click an entity to capture its exact type |  |  |
| 112 | Advanced Item Collector — longer range, per-axis radius, single-item filter slot |  |  |
| 113 | Filtered Super Lubricent Platform — items matching the filter slot fall through instead of sliding on top |  |  |
| 114 | Notification Interface — pops a toast (title/description/item icon) only for the linked player on a redstone rising edge |  |  |
| 115 | ID Card — right-click binds it to yourself |  |  |

## 4. General spot-checks across the whole port (3)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 118 | No raw untranslated `block.randomthings.xxx`-style strings showing up in tooltips/GUI titles |  |  |
| 119 | Batch 2 blocks all drop correctly when broken (they have loot tables, unlike Batch 1) |  |  |
| 120 | GUI text-cycle mode buttons (Igniter, Entity Detector, etc.) are legible and functional (plain text, not the original's icon-sprite buttons — cosmetic difference only) |  |  |

## 5. Batch 3 — remaining (non-block) items (79)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 122 | Blaze and Steel — right-click a block face to place Blazing Fire instead of vanilla fire; damages the tool by 1 per use |  |  |
| 123 | Bottle of Air — right-click while standing in water to drink; should fully refill your air/breath meter in one go and be consumed (count -1) |  |  |
| 124 | Stable Ender Pearl — right-click to bind it to yourself (tooltip should show "Bound to \<your name\>"), then throw it … |  |  |
| 125 | Stable Ender Pearl — thrown while unbound (fresh from creative, no right-click-to-bind first) … |  |  |
| 136 | Position Filter — right-click a block to store its position … |  |  |
| 137 | Golden Compass — freshly crafted (gold ingots + vanilla compass), no target set yet: needle wobbles/spins randomly like an unbound vanilla compass near the Nether, never settles |  |  |
| 138 | Golden Compass — combine with a Position Filter that has a stored position at a crafting table … |  |  |
| 139 | Emerald Compass — freshly crafted (emeralds + vanilla compass), no target bound yet: needle wobbles randomly, same as an unbound Golden Compass |  |  |
| 140 | Emerald Compass — bind an ID Card to yourself (right-click), then combine it with the compass at a crafting table … |  |  |
| 141 | Emerald Compass — bound player logs off: needle goes back to a random wobble instead of pointing at their last known position, and resumes tracking automatically once they log back in |  |  |
| 145 | Chunk Analyzer — right-click to open the GUI, hit the "Scan" button … |  |  |
| 146 | Chunk Analyzer — close the GUI and reopen it without rescanning: the previous scan results are still there (persisted in the item's own NBT), no need to rescan |  |  |
| 147 | Chunk Analyzer — scan a chunk, then move to your own inventory/hotbar/armor while the scan is running (e.g. pick up or drop an item) … |  |  |
| 148 | Chunk Analyzer — scan a very tall/full chunk (lots of terrain, caves, etc.) … |  |  |
| 154 | Sound Recorder — sneak-right-click to start recording (icon changes), walk around hearing various sounds (footsteps, mobs, blocks, etc … |  |  |
| 155 | Sound Recorder GUI — put a blank Sound Pattern in the left slot, click a sound name in the list: a "full" Sound Pattern (different icon, tooltip shows the sound's name) appears in the right slot and the blank one is consumed |  |  |
| 156 | Sound Pattern — sneak-right-click a full (stamped) pattern: clears it back to blank |  |  |
| 161 | Rune Dust — right-click the top of a solid block: places a new, invisible-until-decorated Rune Base with one colored pixel in the corner you clicked (a 4x4 grid across the whole block) … |  |  |
| 163 | Rune Base — left-click (not fully breaking) anywhere on it: every pixel on the block pops off as its own dropped Rune Dust item, and the block itself disappears immediately |  |  |
| 164 | Rune Base — break the block outright (not via left-click-to-remove-one) while it still has pixels: every remaining pixel drops as its matching Rune Dust, not just one |  |  |
| 165 | Rune Base — remove the solid block underneath it: the Rune Base itself breaks (and drops its pixels, per #164) |  |  |
| 166 | Rune Pattern — right-click an existing Rune Base: copies its pattern onto the Rune Pattern item (tooltip lists each color and how many pixels) … |  |  |
| 167 | Rune Pattern — sneak-right-click: turns back into a plain vanilla paper item |  |  |
| 174 | Imbuing Station — progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in progress; output slot rejects manual insertion (machine-only) |  |  |
| 184 | Floo Sign — right-click a vanilla Bricks block: converts the whole contiguous horizontal group it's touching (up to 20 blocks) into a named fireplace … |  |  |
| 185 | Floo Sign — right-clicking a brick that's already part of an existing fireplace fails instead of double-converting it |  |  |
| 186 | Breaking any single brick in a fireplace (master or a child) reverts the *entire* group back to plain Bricks |  |  |
| 188 | Typing a name that resolves back to the fireplace you're already standing on shows "You are already at this destination" instead of teleporting |  |  |
| 189 | Departure and arrival both show a burst of flame particles, visible to any nearby player, not just the one teleporting |  |  |
| 190 | Floo Token — drop it and leave it resting on the ground: after a few seconds it vanishes and a temporary fireplace (flame particles, no name) appears at that spot for a short while |  |  |
| 191 | Standing in/near a temporary Floo Token fireplace and typing a chat message teleports for free (no Floo Powder/pouch charge needed) |  |  |
| 192 | Dropping a second Floo Token near an already-active temporary fireplace does not spawn a duplicate |  |  |
| 194 | Eclipsed Clock — right-click a wall to hang it; right-click the hung clock to advance its target time by 30s, sneak-right-click to go back 30s; the dial face visually updates to match |  |  |
| 195 | Eclipsed Clock — looking at it (or having just clicked it) shows the target time as floating text above it |  |  |
| 196 | Eclipsed Clock — right-click it while holding a charged Time in a Bottle … |  |  |
| 197 | Eclipsed Clock — breaking it drops the clock item back (preserving nothing - a fresh clock); creative-mode breaking drops nothing |  |  |
| 199 | Time in a Bottle — passively fills with stored time while carried anywhere in inventory (tooltip shows H/M/S); holding two at once drains the one with less time to 0 |  |  |
| 200 | Time in a Bottle — right-click a block (not the Eclipsed Clock) with 30+ seconds stored … |  |  |
| 201 | Time in a Bottle — right-click an existing Time Accelerator … |  |  |
| 203 | Weather Egg — throwing any type spawns a rising cloud where it lands (damages any entity hit directly) |  |  |
| 204 | Weather Egg cloud — rises for ~10s then floats to the world height limit, then sets the weather … |  |  |
| 205 | Weather Egg cloud — visibly different particle effects per type while rising (plus a colored accent for Rain/Storm) |  |  |
| 208 | Ender Letter — right-click (fresh, unaddressed) opens a 9-slot "write a letter" GUI with a receiver-name text field; put items in, type a player's name |  |  |
| 211 | Ender Mailbox — sneak-right-click with an addressed letter naming a player who doesn't exist: get a "does not seem to reside in this world" message, letter untouched |  |  |
| 213 | Ender Mailbox — right-click a mailbox someone else placed (not sneaking): "You are not the owner of this mailbox," no GUI opens |  |  |
| 216 | Emptying a received letter completely and closing the GUI makes the (now-empty) letter item disappear |  |  |
| 217 | A received (unopened) letter shows an enchantment-glow shimmer in your inventory |  |  |
| 219 | Ender Bucket — right-click while looking near (not necessarily exactly at) a body of water/lava: picks up 1000mB even if the exact block you're pointed at is a flowing edge, not a source … |  |  |
| 220 | Ender Bucket — right-click a solid block face while full: places the contained fluid normally |  |  |
| 221 | Ender Bucket — held-item tooltip/name shows "Ender Bucket of <Fluid>" while full |  |  |
| 223 | Reinforced Ender Bucket — holds 10x the fluid (10000mB); durability bar shows fill level, tinted to the contained fluid's own color |  |  |
| 224 | Reinforced Ender Bucket — sneak while picking up: keeps draining the whole connected body instead of stopping after the first 1000mB, until full or the body runs out |  |  |
| 225 | Summoning Pendulum — right-click a passive (non-hostile, non-player) mob: captures it (mob disappears, enderman-teleport sound plays), up to 5 at once |  |  |
| 226 | Summoning Pendulum — right-clicking a 6th mob while already holding 5 does nothing but play a firecharge-use sound instead |  |  |
| 227 | Summoning Pendulum — durability bar fills as purple, up to 5/5; shows an enchant-glow shimmer once full |  |  |
| 228 | Summoning Pendulum — right-click a block: releases the oldest captured mob there (FIFO order); with nothing captured, plays a firecharge-use sound instead |  |  |
| 229 | Summoning Pendulum — plain tooltip shows "Holding N Entity/Entities"; holding Shift instead lists each captured mob's name |  |  |
| 230 | Golden Egg — throw it (right-click): always hatches a Golden Chicken where it lands (unlike vanilla eggs' 1-in-8 chance), and damages anything it hits directly for 1 |  |  |
| 231 | Golden Chicken — behaves like a normal passive chicken (follows held seeds, can be led/bred by seeds) but never lays eggs and has no breeding-goal AI (matches the original's own apparent … |  |  |
| 232 | Golden Chicken — periodically drops 3 gold ingots instead of laying an egg … |  |  |
| 237 | Fluid Display — breaking it drops itself as an item |  |  |
| 239 | Build the ritual structure (3x3 air pocket, End Rod above End Stone above it, End Stone floor, a surrounding obsidian ring 2 blocks out … |  |  |
| 240 | Right-clicking an End Rod that ISN'T part of a valid structure does nothing (no tear consumed) |  |  |
| 241 | The portal charges up for ~10 seconds (purple enchant-table sparkles rising out of it starting a couple seconds in), then shows the swirling end-portal starfield visual once fully charged |  |  |
| 242 | Once fully charged, standing in it teleports you to the End (same as a vanilla end portal) - not while riding or being ridden |  |  |
| 243 | Breaking any block of the supporting structure while the portal is active makes it disappear |  |  |
| 244 | Creative Player Interface — creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves identically to the already-shipped Player Interface (binds to whoever places it, exposes their inventory as a capability for hoppers etc.) |  |  |
| 247 | Ender Bridge — placing it faces you (matches the direction you were looking, including straight up/down) … |  |  |
| 248 | Ender Bridge — if the scan reaches an Ender Anchor before hitting anything else solid … |  |  |
| 249 | Ender Bridge — if the scan instead hits any other solid block first (or the anchor's chunk isn't loaded), it just goes dark without teleporting anyone |  |  |
| 250 | Ender Bridge — cutting redstone power resets it back to idle so it can be triggered again |  |  |
| 252 | Magic Hood — no crafting recipe (dungeon-chest-only in 1.12.2 too); creative-only for now |  |  |
| 255 | Magic Hood — has durability of leather |  |  |
| 257 | Rain Shield — needs solid ground underneath; breaks and drops itself if that support is removed (like a torch) |  |  |
| 260 | Rain Shield — while raining, shows a ring of flame/smoke particles swirling around its candle, regardless of whether the shield is currently active or powered |  |  |
| 261 | Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request) |  |  |

## 5. Batch 3 — remaining (non-block) items (continued) (57)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 262 | Ancient Furnace — rarely found naturally embedded (top face flush with the ground) in cold biomes (snowy tundra, taiga, mountains, etc.) … |  |  |
| 263 | Ancient Furnace — right-clicking the top opening with a Nether Star consumes it (unless creative) and starts it heating … |  |  |
| 264 | Ancient Furnace — right-clicking with anything else, or right-clicking one already heating, does nothing |  |  |
| 266 | Ancient Furnace — very hard to break with a pickaxe if left un-activated; breaking it before activation drops itself |  |  |
| 268 | Peace Candle — standalone decorative candle, creative-menu obtainable; needs solid ground underneath (auto-breaks and drops otherwise, like a torch) |  |  |
| 272 | Spectre Illuminator — right-click a block to place it … |  |  |
| 273 | Spectre Illuminator — once centered, its whole chunk (and only that chunk) lights up to full block-light brightness, and a soft particle trail plays around it |  |  |
| 274 | Spectre Illuminator — right-click the placed orb to collect it back as an item; the chunk goes dark again |  |  |
| 276 | Portkey — right-click a block to bind it there; shift-hold shows the bound dimension/x/y/z in the tooltip (plain hover shows a "hold shift" hint instead), matching `Position Filter`'s own … |  |  |
| 280 | Portkey — an un-camouflaged Portkey still shows its own plain icon correctly (in every context) |  |  |
| 286 | Redstone Activator — right-click empty air to cycle its duration (2/20/100 ticks, shown both in the tooltip and as a texture swap on the item itself; shift-right-click cycles backwards) … |  |  |
| 290 | Block Breaker — left unpowered, continuously mines whatever's directly in front of it at real survival speed (respects the target block's actual hardness/tool-appropriateness via a … |  |  |
| 298 | Spectre Key — hold right-click for 5 seconds (glowing cyan particle charge-up) to teleport into your own private Spectre Cube room … |  |  |
| 299 | Spectre Cube room — a 16x16 room made of unbreakable/explosion-immune Spectre Block, with a 2x2 unbreakable Spectre Core pedestal at the center … |  |  |
| 300 | Spectre Core — right-click with Ectoplasm raises the room's ceiling (consumes the held stack, up to however much height is left before Y 255) … |  |  |
| 301 | Spectre Dimension — always dark (no skylight), flat gray-cyan fog, no mob spawns (void biome, empty chunk generator), can't be slept in/respawn-set, wandering outside your own cube (not … |  |  |
| 302 | Spectre Dimension — joining/rejoining any world after having visited the Spectre dimension doesn't crash |  |  |
| 304 | Spectre Pickaxe — craft: 3 Spectre Ingot across the top row, Obsidian double-stacked below center … |  |  |
| 305 | Spectre Axe — craft: 2 Spectre Ingot + 1 Obsidian in an L (top-left/middle-left/middle-center), Obsidian bottom-center … |  |  |
| 306 | Spectre Shovel — craft: 1 Spectre Ingot top-center, Obsidian double-stacked below; same +3 reach |  |  |
| 309 | Spectre Coil (Genesis) — same attach/drop behavior as the other tiers, but generates a flat 10,000,000 FE/tick directly into whatever it's attached to, bypassing the player's pool entirely; creative-tab only, no recipe (matches 1.12.2 exactly) |  |  |
| 313 | Spectre Lens — while placed on an active (fully powered pyramid) Beacon, the owning player keeps receiving that beacon's current primary/secondary effects wherever they are, any dimension … |  |  |
| 315 | Diaphanous Block — displayed block fades in as you get farther away (mostly invisible up close, looks fully solid past ~33 blocks) and has no collision while not inverted (walk straight … |  |  |
| 316 | Diaphanous Block — combine a lone Diaphanous Block by itself in a crafting grid to toggle "inverted" … |  |  |
| 317 | Diaphanous Block — breaking it drops an item stack that remembers both the displayed block and the inverted flag (place it again elsewhere, same appearance/inverted state) |  |  |
| 325 | Spirit — killing any living entity has a small chance to spawn a tiny flying wisp at the death spot (higher at night under a fuller moon, and higher still once you've beaten the Ender Dragon at least once) - test by killing plenty of mobs, ideally at night with a bright moon |  |  |
| 326 | Spirit — drifts aimlessly near where it appeared, floats through the air (not on the ground), and despawns on its own after a while if left alone |  |  |
| 327 | Spirit — immune to regular attacks/arrows/fire/etc; only a Spectre Sword (or creative-mode/out-of-world damage) can actually hurt it, and it dies in one hit either way (1 HP) |  |  |
| 328 | Spirit — killing it with a Spectre Sword drops 1-2 Ectoplasm |  |  |
| 329 | Batch 1 block loot tables — break a spread of Batch 1 blocks in survival (stained brick, luminous block, biome stone/glass, lapis/quartz glass+lamp, contact button/lever, spectre log/plank/sapling, glowing mushroom, super lubricent ice/stone/platform); each drops itself |  |  |
| 330 | Bean Sprout — breaking a fully grown (age 7) sprout drops 3-4 beans total; a young sprout drops just 1 |  |  |
| 331 | Colored Grass — breaking it drops plain Dirt; with a Silk Touch tool it drops itself |  |  |
| 332 | Compressed Slime Block — breaking it drops a regular vanilla Slime Block |  |  |
| 333 | Blocks that should drop nothing still drop nothing: Blazing Fire, Floo Brick, Rune Base, Bean Stalk/Lesser Bean Stalk, Diaphanous Block (returns its camo block via its own code, not a loot table) |  |  |
| 334 | Chest loot injection — Summoning Pendulum, Magic Hood and Slime Cube occasionally appear in dungeon chests; Magic Hood in village weaponsmith chests, Pendulum in stronghold corridor chests, Slime Cube in jungle temple chests (rare-ish; use `/loot` or open many chests) |  |  |
| 335 | Chest loot injection — a Biome Crystal shows up in roughly 1 in 5 vanilla chests, already tuned to a random biome (name shows the biome, works with the Biome Radar) |  |  |
| 336 | Config — first launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` boolean options, all defaulting to `true` |  |  |
| 337 | Config — `MagneticEnchantment = false` stops Magnetic appearing at the enchanting table/on books and stops it pulling drops; `ArtificialEndPortal = false` makes Evil Tear do nothing on an End Rod; `Lotus`/`AncientFurnace = false` stop new ones generating in fresh chunks |  |  |
| 338 | Config — `MagicHood`/`SummoningPendulum`/`SlimeCube`/`BiomeCrystal = false` under `[loot]` stops that item being injected into chests (restart the world/server after editing) |  |  |
| 339 | Bean Pod — breaking one drops iron (8-20), gold (4-15), diamonds (1-5), 0-2 emeralds, 4-8 beans and a Golden Egg (in survival, not just creative; use `/give` beans to grow a full stalk if needed) |  |  |
| 340 | Bean Pod — `GoldenEgg = false` in the config removes the egg from the drops; blowing the pod up with TNT drops only some of the stacks |  |  |
| 345 | Spirit config — `[numbers]` section: `SpiritLifeTime`, `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceEndIncrease` change Spirit despawn time and spawn odds (e.g. `SpiritChanceNormal = 1.0` makes every mob kill spawn one) |  |  |
| 347 | Runes — `[visual] FlatRunes = true` in the config switches them to the plain flat texture (takes effect on the next render, no restart needed if the config file is reloaded; otherwise restart) |  |  |
| 348 | Recipes — open the recipe book / a crafting table and confirm the backfilled recipes appear and craft (45 new): Item Collector (ender pearl / hopper / 3 obsidian, left-column shape), Advanced Item Collector, Igniter, Iron Dropper, Contact Button/Lever, Chat/Global Chat/Online/Entity Detector, Inventory Tester (x2)/Rerouter, Notification Interface, Redstone Observer/Tool, Sided Redstone, Analog Emitter, Advanced Redstone Repeater, Potion Vaporizer, Player Interface, Slime Cube |  |  |
| 353 | Config — `[visual] HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>` (no dimension/XYZ even with Shift held); Position Filter shows no coordinate tooltip and no "hold Shift" hint; default `false` leaves both as before |  |  |
| 354 | Config — `[numbers] BlockDestabilizerLimit`: default 50 blocks per activation as before; raise it (e.g. 500) and a bigger connected mass falls at once; `0` = no limit |  |  |
| 355 | Config — `[numbers] TriggerGlassChainLimit`: default 20 as before; lower it (e.g. 3) and only that many touching Trigger Glass blocks go passable per pulse; `0` = whole connected mass |  |  |
| 358 | Ender Anchor chunk loading — `[features] EnderAnchorChunkloading = false`: newly placed anchors don't force anything, and anchors that had forced a chunk release it the next time they load |  |  |
| 359 | Ender Anchor tooltip — hovering the item (inventory/creative) shows "Keeps the chunk it is in loaded"; the line disappears when `EnderAnchorChunkloading = false` |  |  |
| 361 | Ender Letter message body — the GUI title reads "Ender Letter" again, with a multiline message box under the item slots (4 visible lines, word-wrap, Enter = newline, arrows/Home/End/click/mouse-wheel/Ctrl+V work, hard cap of 200 characters shown as `n/200`); it survives close/reopen, is trimmed server-side, and a delivered letter shows it read-only. **New feature, not in 1.12.2** |  |  |
| 362 | Ender Letter tooltip shows "Sneak + right-click an Ender Mailbox to send" on unsent letters (not on received ones); Ender Mailbox GUI shows "Incoming Mail" on its title row |  |  |
| 363 | Notification Interface icon slot — now a copy/ghost slot like the Redstone Remote's camo slots: click with an item to copy one as the icon, click empty-handed to clear, shift-click does nothing; slot moved 3px right so it's centered under the "Icon" label |  |  |
| 364 | Redstone Observer / Basic Redstone Interface — "Target" label shown at the top only when a target is set (unset keeps the plain red "No Target" screen). Redstone Remote use screen — hovering a button shows the displayed (camo) item's anvil name, falling back to the Position Filter's name |  |  |
| 365 | Advanced Item Collector GUI — 24px of empty space removed (filter slot and inventory moved up, texture trimmed to match); radius buttons and filter slot all still work |  |  |
| 366 | Fluid Display — inventory/hand/dropped item renders as an isometric full cube (was a flat square of the block texture) |  |  |
| 381 | Portkey item looks normal size in the inventory, in hand (both views), on the ground and in item frames (was tiny: it used block-style display scales) |  |  |
| 383 | Dedicated server with a real client: Spectre dimension entry, exit back to the same overworld position, and unload |  |  |

## Visual / audio spot-checks that the scripts only measure

The scenarios assert state and pixel measurements for these, but only your eyes/ears confirm they look right (a quick look is enough).

| # | Look/listen for | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener: the muted sound is really silent near the block and audible again beyond 20 blocks |  |  |
| 234 | Fluid Display: takes the poured fluid's texture/colour, still vs flowing looks right, no holes beside neighbouring blocks (sand, stone) - this was the 1.16 culling bug |  |  |
| 149-153 | Biome Radar: crystal spins in the middle of the radar; antenna particles change colour when the search finishes |  |  |
| 254 | Magic Hood: no potion swirl around you in F5 view (and other players see no nametag, two-player check) |  |  |
| 265 | Ancient Furnace: the area around it visibly turns warm biome colours and the snow melts after the explosion (FIXED: only 1 in 16 columns used to melt; heating + client resync tests added) |  |  |
| 277 | Portkey: dropped item shimmers, stops glowing about 5 s later; disguised Portkey (#279) looks like the donor item in hand/ground/item frame |  |  |
| 319 | Light Redirector: open face shows the block on the opposite side (periscope picture) |  |  |
| 321-323 | Dyeing Machine: recolored items are tinted held/in inventory/on the ground/worn; the enchant-glint result glows in the dye colour on items AND worn armor |  |  |
| 346 | Runes: noisy texture, same-colour pixels join across the base edge, nothing flickers while you move |  |  |
| all glass-like blocks | Trigger/Lapis/Quartz Glass, Diaphanous, Spectre and the other 28 `notSolid()` blocks: no see-through holes in the faces of blocks next to them |  |  |

## Museum report 2026-10-24 (all branches)

| # | Check | Result | Notes |
| --- | --- | --- | --- |
| M1 | Super Lubricent Stone, Ice, Platform and Filtered Platform: placing and breaking them plays the ice (glass) sounds, not stone sounds - FIXED (museum report 2026-10-24) |  |  |
| M2 | Blazing Fire on netherrack or another non-flammable block looks like vanilla fire (flame shape and height, not a solid box) - FIXED (`BlazingFireBlock` was missing from `RenderLayerSetup` cutout; render scene `blazing_fire` checked 2026-10-24) |  |  |
| M4 | Spirit Spawn Egg exists in the creative tab (black egg, blue spots as in 1.12.2) and spawns a Spirit when used on a block - FIXED (client suite test `retest.spirit_spawn_egg...`) |  |  |
| M5 | Time in a Bottle: the floating clock icons on the faces of the accelerated block are bright, not dark - FIXED (`TimeAcceleratorEntityRenderer` draws full-bright; 1.14.4 renders differently and was not changed, check it) |  |  |
| M6 | Spectre Illuminator: the chunk lights up after placing and goes dark again after removing the orb (client side, with screenshots) - playtest scenario `spectre_illuminator`; not reproducible as a bug on 1.21.11 (lit 107 -> dark again 40, 34 s after removal) |  |  |
