# Random Things 1.21.4 - Testing Checklist

**Ready-made test world:** `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` opens a seed-424242 normal-terrain world with every block on show, set-ups and a stocked inventory - see [`docs/testing/manual-test-world.md`](docs/testing/manual-test-world.md).

Fill in **Result** with `Pass` or `Fail` (leave blank if untested) and a one-sentence note in **Feedback** (what you did, what happened vs. expected).
Row numbers are the original sheet's, so references like "#158" still work. **Every result is blank: nothing on this branch has been tested by hand yet** (the 1.14.4 sheet was; sections 1-5 were carried over from 1.16.5 unchanged, section 6 is new for 1.18.2).

Sections 1-5 list what the automated suites did **not** cover on 1.16.5 (204 rows); the rows covered by the suites and by the scripted scenarios (all 26 re-run and passing on 1.18.2 on 2026-10-15) are listed with their evidence in [`docs/testing/covered-by-tests-1.18.2.md`](docs/testing/covered-by-tests-1.18.2.md); the 44 rows for features that were removed on request are dropped. The full 1.14.4 sheet with its old results is in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md); a suggested order for these checks (rendering rows first) is in `PORTING_PLAN.md` "What to do next".

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
| 94 | Redstone Tool |  | *Auto 2026-10-23:* retest.redstone_tool: two clicks link an observer, glow while linking |
| 98 | Potion Vaporizer — burns fuel correctly |  |  |
| 99 | Potion Vaporizer — bottle-out slot can't be manually filled by the player, only by the machine |  |  |
| 104 | Entity Detector — filter modes (All/Living/Animal/Monster/Player/Items) each trigger correctly |  |  |
| 105 | Entity Detector — Custom mode with an Entity Filter Item in the slot |  |  |
| 106 | Entity Detector — per-axis radius (0–10) is respected |  |  |
| 108 | Entity Filter Item — right-click an entity to capture its exact type |  | *Auto 2026-10-23:* retest.entity_filter: right-click captures the type, filter matches only that type |
| 112 | Advanced Item Collector — longer range, per-axis radius, single-item filter slot |  |  |
| 113 | Filtered Super Lubricent Platform — items matching the filter slot fall through instead of sliding on top |  |  |
| 114 | Notification Interface — pops a toast (title/description/item icon) only for the linked player on a redstone rising edge |  |  |
| 115 | ID Card — right-click binds it to yourself |  | *Auto 2026-10-23:* retest.id_card: right-click binds the user |

## 4. General spot-checks across the whole port (3)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 118 | No raw untranslated `block.randomthings.xxx`-style strings showing up in tooltips/GUI titles |  |  |
| 119 | Batch 2 blocks all drop correctly when broken (they have loot tables, unlike Batch 1) |  | *Auto 2026-10-23:* retest.loot.every_mod_block_drops_itself...: every block drops itself except the documented ones |
| 120 | GUI text-cycle mode buttons (Igniter, Entity Detector, etc.) are legible and functional (plain text, not the original's icon-sprite buttons — cosmetic difference only) |  |  |

## 5. Batch 3 — remaining (non-block) items (79)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 122 | Blaze and Steel — right-click a block face to place Blazing Fire instead of vanilla fire; damages the tool by 1 per use |  |  |
| 123 | Bottle of Air — right-click while standing in water to drink; should fully refill your air/breath meter in one go and be consumed (count -1) |  |  |
| 124 | Stable Ender Pearl — right-click to bind it to yourself (tooltip should show "Bound to \<your name\>"), then throw it … |  | *Auto 2026-10-23:* retest.stable_ender_pearl: right-click binds name+uuid (the throw itself is not covered) |
| 125 | Stable Ender Pearl — thrown while unbound (fresh from creative, no right-click-to-bind first) … |  |  |
| 136 | Position Filter — right-click a block to store its position … |  | *Auto 2026-10-23:* retest.position_filter: stores the clicked position and dimension |
| 137 | Golden Compass — freshly crafted (gold ingots + vanilla compass), no target set yet: needle wobbles/spins randomly like an unbound vanilla compass near the Nether, never settles |  |  |
| 138 | Golden Compass — combine with a Position Filter that has a stored position at a crafting table … |  | *Auto 2026-10-23:* retest.recipes.golden_compass_takes_the_position_of_a_position_filter |
| 139 | Emerald Compass — freshly crafted (emeralds + vanilla compass), no target bound yet: needle wobbles randomly, same as an unbound Golden Compass |  |  |
| 140 | Emerald Compass — bind an ID Card to yourself (right-click), then combine it with the compass at a crafting table … |  | *Auto 2026-10-23:* retest.recipes.emerald_compass_takes_the_player_of_a_bound_id_card |
| 141 | Emerald Compass — bound player logs off: needle goes back to a random wobble instead of pointing at their last known position, and resumes tracking automatically once they log back in |  |  |
| 145 | Chunk Analyzer — right-click to open the GUI, hit the "Scan" button … |  |  |
| 146 | Chunk Analyzer — close the GUI and reopen it without rescanning: the previous scan results are still there (persisted in the item's own NBT), no need to rescan |  |  |
| 147 | Chunk Analyzer — scan a chunk, then move to your own inventory/hotbar/armor while the scan is running (e.g. pick up or drop an item) … |  |  |
| 148 | Chunk Analyzer — scan a very tall/full chunk (lots of terrain, caves, etc.) … |  |  |
| 154 | Sound Recorder — sneak-right-click to start recording (icon changes), walk around hearing various sounds (footsteps, mobs, blocks, etc … |  |  |
| 155 | Sound Recorder GUI — put a blank Sound Pattern in the left slot, click a sound name in the list: a "full" Sound Pattern (different icon, tooltip shows the sound's name) appears in the right slot and the blank one is consumed |  |  |
| 156 | Sound Pattern — sneak-right-click a full (stamped) pattern: clears it back to blank |  | *Auto 2026-10-23:* retest.sound_pattern: sneak-right-click clears a stamped pattern |
| 161 | Rune Dust — right-click the top of a solid block: places a new, invisible-until-decorated Rune Base with one colored pixel in the corner you clicked (a 4x4 grid across the whole block) … |  | *Auto 2026-10-23:* retest.rune_dust: places a Rune Base with one pixel in the clicked corner, refuses side faces and a repeated pixel |
| 163 | Rune Base — left-click (not fully breaking) anywhere on it: every pixel on the block pops off as its own dropped Rune Dust item, and the block itself disappears immediately |  | *Auto 2026-10-23:* retest.rune_base: removal drops every pixel (the left-click path itself is not covered) |
| 164 | Rune Base — break the block outright (not via left-click-to-remove-one) while it still has pixels: every remaining pixel drops as its matching Rune Dust, not just one |  | *Auto 2026-10-23:* retest.rune_base.breaking_it_drops_every_pixel_as_its_own_dust |
| 165 | Rune Base — remove the solid block underneath it: the Rune Base itself breaks (and drops its pixels, per #164) |  | *Auto 2026-10-23:* retest.rune_base.breaks_and_drops_its_pixels_when_the_block_below_is_removed |
| 166 | Rune Pattern — right-click an existing Rune Base: copies its pattern onto the Rune Pattern item (tooltip lists each color and how many pixels) … |  |  |
| 167 | Rune Pattern — sneak-right-click: turns back into a plain vanilla paper item |  |  |
| 174 | Imbuing Station — progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in progress; output slot rejects manual insertion (machine-only) |  |  |
| 184 | Floo Sign — right-click a vanilla Bricks block: converts the whole contiguous horizontal group it's touching (up to 20 blocks) into a named fireplace … |  | *Auto 2026-10-23:* retest.floo: the sign converts the whole brick group, more than 21 bricks is refused |
| 185 | Floo Sign — right-clicking a brick that's already part of an existing fireplace fails instead of double-converting it |  | *Auto 2026-10-23:* retest.floo: a brick that is already part of a fireplace is refused |
| 186 | Breaking any single brick in a fireplace (master or a child) reverts the *entire* group back to plain Bricks |  | *Auto 2026-10-23:* retest.floo: breaking one brick reverts the whole group |
| 188 | Typing a name that resolves back to the fireplace you're already standing on shows "You are already at this destination" instead of teleporting |  |  |
| 189 | Departure and arrival both show a burst of flame particles, visible to any nearby player, not just the one teleporting |  |  |
| 190 | Floo Token — drop it and leave it resting on the ground: after a few seconds it vanishes and a temporary fireplace (flame particles, no name) appears at that spot for a short while |  |  |
| 191 | Standing in/near a temporary Floo Token fireplace and typing a chat message teleports for free (no Floo Powder/pouch charge needed) |  |  |
| 192 | Dropping a second Floo Token near an already-active temporary fireplace does not spawn a duplicate |  |  |
| 194 | Eclipsed Clock — right-click a wall to hang it; right-click the hung clock to advance its target time by 30s, sneak-right-click to go back 30s; the dial face visually updates to match |  | *Auto 2026-10-23:* retest.eclipsed_clock.right_click_adds_30_seconds...: target time (the dial look is not covered) |
| 195 | Eclipsed Clock — looking at it (or having just clicked it) shows the target time as floating text above it |  |  |
| 196 | Eclipsed Clock — right-click it while holding a charged Time in a Bottle … |  | *Auto 2026-10-23:* retest.eclipsed_clock.a_charged_time_in_a_bottle_fast_forwards...; also fixed a dedicated-server crash here |
| 197 | Eclipsed Clock — breaking it drops the clock item back (preserving nothing - a fresh clock); creative-mode breaking drops nothing |  | *Auto 2026-10-23:* retest.eclipsed_clock.breaking_drops_a_clock_except_for_a_creative_player |
| 199 | Time in a Bottle — passively fills with stored time while carried anywhere in inventory (tooltip shows H/M/S); holding two at once drains the one with less time to 0 |  | *Auto 2026-10-23:* retest.time_in_a_bottle: stores 20 ticks a second, the lesser of two is drained (the tooltip is not covered) |
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
| 219 | Ender Bucket — right-click while looking near (not necessarily exactly at) a body of water/lava: picks up 1000mB even if the exact block you're pointed at is a flowing edge, not a source … |  | *Auto 2026-10-23:* retest.ender_bucket.picking_up_water_from_a_stack...: pickup of 1000 mB; also fixed a stack duplication |
| 220 | Ender Bucket — right-click a solid block face while full: places the contained fluid normally |  | *Auto 2026-10-23:* retest.ender_bucket.a_full_bucket_places_its_fluid... |
| 221 | Ender Bucket — held-item tooltip/name shows "Ender Bucket of <Fluid>" while full |  |  |
| 223 | Reinforced Ender Bucket — holds 10x the fluid (10000mB); durability bar shows fill level, tinted to the contained fluid's own color |  |  |
| 224 | Reinforced Ender Bucket — sneak while picking up: keeps draining the whole connected body instead of stopping after the first 1000mB, until full or the body runs out |  |  |
| 225 | Summoning Pendulum — right-click a passive (non-hostile, non-player) mob: captures it (mob disappears, enderman-teleport sound plays), up to 5 at once |  | *Auto 2026-10-23:* retest.summoning_pendulum.captures_passive_mobs_up_to_five... |
| 226 | Summoning Pendulum — right-clicking a 6th mob while already holding 5 does nothing but play a firecharge-use sound instead |  | *Auto 2026-10-23:* retest.summoning_pendulum.captures_passive_mobs_up_to_five... (the sixth stays, no capture) |
| 227 | Summoning Pendulum — durability bar fills as purple, up to 5/5; shows an enchant-glow shimmer once full |  | *Auto 2026-10-23:* retest.summoning_pendulum.captures...: full pendulum shimmers and hides its bar |
| 228 | Summoning Pendulum — right-click a block: releases the oldest captured mob there (FIFO order); with nothing captured, plays a firecharge-use sound instead |  | *Auto 2026-10-23:* retest.summoning_pendulum.releases_the_oldest_captured_mob_first; also fixed a duplication on release |
| 229 | Summoning Pendulum — plain tooltip shows "Holding N Entity/Entities"; holding Shift instead lists each captured mob's name |  |  |
| 230 | Golden Egg — throw it (right-click): always hatches a Golden Chicken where it lands (unlike vanilla eggs' 1-in-8 chance), and damages anything it hits directly for 1 |  |  |
| 231 | Golden Chicken — behaves like a normal passive chicken (follows held seeds, can be led/bred by seeds) but never lays eggs and has no breeding-goal AI (matches the original's own apparent … |  |  |
| 232 | Golden Chicken — periodically drops 3 gold ingots instead of laying an egg … |  | *Auto 2026-10-23:* retest.golden_chicken.gold_ore_starts_a_timer...; also fixed 1 ingot instead of 3 |
| 237 | Fluid Display — breaking it drops itself as an item |  |  |
| 239 | Build the ritual structure (3x3 air pocket, End Rod above End Stone above it, End Stone floor, a surrounding obsidian ring 2 blocks out … |  |  |
| 240 | Right-clicking an End Rod that ISN'T part of a valid structure does nothing (no tear consumed) |  |  |
| 241 | The portal charges up for ~10 seconds (purple enchant-table sparkles rising out of it starting a couple seconds in), then shows the swirling end-portal starfield visual once fully charged |  |  |
| 242 | Once fully charged, standing in it teleports you to the End (same as a vanilla end portal) - not while riding or being ridden |  |  |
| 243 | Breaking any block of the supporting structure while the portal is active makes it disappear |  |  |
| 244 | Creative Player Interface — creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves identically to the already-shipped Player Interface (binds to whoever places it, exposes their inventory as a capability for hoppers etc.) |  |  |
| 247 | Ender Bridge — placing it faces you (matches the direction you were looking, including straight up/down) … |  | *Auto 2026-10-23:* not covered (facing on placement) |
| 248 | Ender Bridge — if the scan reaches an Ender Anchor before hitting anything else solid … |  | *Auto 2026-10-23:* retest.ender_bridge.powered_it_scans_along_its_facing_and_sends_nearby_items_above_an_ender_anchor |
| 249 | Ender Bridge — if the scan instead hits any other solid block first (or the anchor's chunk isn't loaded), it just goes dark without teleporting anyone |  | *Auto 2026-10-23:* retest.ender_bridge.hitting_another_solid_block_first_just_goes_dark... |
| 250 | Ender Bridge — cutting redstone power resets it back to idle so it can be triggered again |  | *Auto 2026-10-23:* retest.ender_bridge...: cutting and re-applying power triggers it again |
| 252 | Magic Hood — no crafting recipe (dungeon-chest-only in 1.12.2 too); creative-only for now |  |  |
| 255 | Magic Hood — has durability of leather |  | *Auto 2026-10-23:* retest.magic_hood.has_the_durability_of_a_leather_helmet |
| 257 | Rain Shield — needs solid ground underneath; breaks and drops itself if that support is removed (like a torch) |  | *Auto 2026-10-23:* retest.rain_shield.breaks_and_drops_itself_when_the_block_below_is_removed |
| 260 | Rain Shield — while raining, shows a ring of flame/smoke particles swirling around its candle, regardless of whether the shield is currently active or powered |  |  |
| 261 | Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request) |  |  |

## 5. Batch 3 — remaining (non-block) items (continued) (57)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 262 | Ancient Furnace — rarely found naturally embedded (top face flush with the ground) in cold biomes (snowy tundra, taiga, mountains, etc.) … |  |  |
| 263 | Ancient Furnace — right-clicking the top opening with a Nether Star consumes it (unless creative) and starts it heating … |  | *Auto 2026-10-23:* retest.ancient_furnace.a_nether_star_starts_the_heat_up_and_nothing_else_does |
| 264 | Ancient Furnace — right-clicking with anything else, or right-clicking one already heating, does nothing |  | *Auto 2026-10-23:* retest.ancient_furnace.a_nether_star_starts_the_heat_up_and_nothing_else_does |
| 266 | Ancient Furnace — very hard to break with a pickaxe if left un-activated; breaking it before activation drops itself |  | *Auto 2026-10-23:* retest.ancient_furnace.is_very_hard_to_break (hardness 50) |
| 268 | Peace Candle — standalone decorative candle, creative-menu obtainable; needs solid ground underneath (auto-breaks and drops otherwise, like a torch) |  | *Auto 2026-10-23:* retest.peace_candle.breaks_and_drops_itself_when_the_block_below_is_removed |
| 272 | Spectre Illuminator — right-click a block to place it … |  | *Auto 2026-10-23:* retest.spectre_illuminator.placing_it_centers_it_in_the_chunk... |
| 273 | Spectre Illuminator — once centered, its whole chunk (and only that chunk) lights up to full block-light brightness, and a soft particle trail plays around it |  |  |
| 274 | Spectre Illuminator — right-click the placed orb to collect it back as an item; the chunk goes dark again |  | *Auto 2026-10-23:* retest.spectre_illuminator.placing_it_centers_it_in_the_chunk...: collecting it back |
| 276 | Portkey — right-click a block to bind it there; shift-hold shows the bound dimension/x/y/z in the tooltip (plain hover shows a "hold shift" hint instead), matching `Position Filter`'s own … |  |  |
| 280 | Portkey — an un-camouflaged Portkey still shows its own plain icon correctly (in every context) |  |  |
| 286 | Redstone Activator — right-click empty air to cycle its duration (2/20/100 ticks, shown both in the tooltip and as a texture swap on the item itself; shift-right-click cycles backwards) … |  | *Auto 2026-10-23:* retest.redstone_activator.right_click_cycles_2_20_100_and_sneak_goes_backwards (the texture swap is not covered) |
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
| 316 | Diaphanous Block — combine a lone Diaphanous Block by itself in a crafting grid to toggle "inverted" … |  | *Auto 2026-10-23:* retest.recipes.diaphanous_block_takes_a_block_and_toggles_inverted... |
| 317 | Diaphanous Block — breaking it drops an item stack that remembers both the displayed block and the inverted flag (place it again elsewhere, same appearance/inverted state) |  |  |
| 325 | Spirit — killing any living entity has a small chance to spawn a tiny flying wisp at the death spot (higher at night under a fuller moon, and higher still once you've beaten the Ender Dragon at least once) - test by killing plenty of mobs, ideally at night with a bright moon |  |  |
| 326 | Spirit — drifts aimlessly near where it appeared, floats through the air (not on the ground), and despawns on its own after a while if left alone |  |  |
| 327 | Spirit — immune to regular attacks/arrows/fire/etc; only a Spectre Sword (or creative-mode/out-of-world damage) can actually hurt it, and it dies in one hit either way (1 HP) |  | *Auto 2026-10-23:* retest.spirit.ignores_ordinary_damage_and_dies_in_one_hit_from_a_spectre_sword |
| 328 | Spirit — killing it with a Spectre Sword drops 1-2 Ectoplasm |  | *Auto 2026-10-23:* retest.spirit.killed_with_a_spectre_sword_drops_one_or_two_ectoplasm |
| 329 | Batch 1 block loot tables — break a spread of Batch 1 blocks in survival (stained brick, luminous block, biome stone/glass, lapis/quartz glass+lamp, contact button/lever, spectre log/plank/sapling, glowing mushroom, super lubricent ice/stone/platform); each drops itself |  | *Auto 2026-10-23:* retest.loot.every_mod_block_drops_itself_except_the_documented_exceptions |
| 330 | Bean Sprout — breaking a fully grown (age 7) sprout drops 3-4 beans total; a young sprout drops just 1 |  | *Auto 2026-10-23:* retest.loot.bean_sprout_drops_one_bean_when_young_and_three_to_four_when_fully_grown |
| 331 | Colored Grass — breaking it drops plain Dirt; with a Silk Touch tool it drops itself |  | *Auto 2026-10-23:* retest.loot.colored_grass_drops_dirt_and_only_itself_with_silk_touch; also fixed (always dropped itself) |
| 332 | Compressed Slime Block — breaking it drops a regular vanilla Slime Block |  | *Auto 2026-10-23:* retest.loot.every_mod_block_drops_itself... (drops a slime block) |
| 333 | Blocks that should drop nothing still drop nothing: Blazing Fire, Floo Brick, Rune Base, Bean Stalk/Lesser Bean Stalk, Diaphanous Block (returns its camo block via its own code, not a loot table) |  | *Auto 2026-10-23:* retest.loot.every_mod_block_drops_itself... (nothing) |
| 334 | Chest loot injection — Summoning Pendulum, Magic Hood and Slime Cube occasionally appear in dungeon chests; Magic Hood in village weaponsmith chests, Pendulum in stronghold corridor chests, Slime Cube in jungle temple chests (rare-ish; use `/loot` or open many chests) |  |  |
| 335 | Chest loot injection — a Biome Crystal shows up in roughly 1 in 5 vanilla chests, already tuned to a random biome (name shows the biome, works with the Biome Radar) |  |  |
| 336 | Config — first launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` boolean options, all defaulting to `true` |  |  |
| 337 | Config — `MagneticEnchantment = false` stops Magnetic appearing at the enchanting table/on books and stops it pulling drops; `ArtificialEndPortal = false` makes Evil Tear do nothing on an End Rod; `Lotus`/`AncientFurnace = false` stop new ones generating in fresh chunks |  |  |
| 338 | Config — `MagicHood`/`SummoningPendulum`/`SlimeCube`/`BiomeCrystal = false` under `[loot]` stops that item being injected into chests (restart the world/server after editing) |  |  |
| 339 | Bean Pod — breaking one drops iron (8-20), gold (4-15), diamonds (1-5), 0-2 emeralds, 4-8 beans and a Golden Egg (in survival, not just creative; use `/give` beans to grow a full stalk if needed) |  | *Auto 2026-10-23:* retest.loot.bean_pod_drops_ores_beans_and_a_golden_egg_within_the_documented_ranges |
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

## 6. New for 1.18.2 (15)

| # | Feature | Result | Feedback |
|---|---|---|---|
| N1 | Rune Base - pixels show their dye colours (not white) next to each other and connect |  | *Why:* regression found and fixed on 1.18.2 (wrong shader) |
| N2 | Fluid Display - fluid renders with its colour |  | *Why:* same shader fix as the Rune Base |
| N3 | Redstone Observer + Redstone Tool - red lines to the bound target are visible while holding the tool |  | *Why:* line renderer got a new shader; not seen yet |
| N4 | Enchant glint tint - Dyeing Machine glint result, worn armor glint, Portkey/Spectre Key glint show the chosen colour |  | *Why:* shader-based glint types; only loaded, not seen |
| N5 | Spectre dimension - sky/fog look, entering and leaving through the Spectre Core, room generation |  | *Why:* dimension type is now datapack JSON; effects via reflection |
| N6 | Ancient Furnace - after the heat-up the biome colour of the area changes and the client sees it without relogging |  | *Why:* biomes are written per section palette; chunk resent |
| N7 | Peace Candle - in a real plains village roughly a third of the churches contain a Peace Candle |  | *Why:* verified only on assembled villages in the suite |
| N8 | Glowing Mushroom - in a normal world they appear in caves between y 0 and the surface (not in the deepslate layer) |  | *Why:* suite measures 4% of chunks, same as 1.16.5 |
| N9 | Tools - Fertilized Dirt/Colored Grass mine fast with a shovel, Platforms/Block of Sticks with an axe |  | *Why:* now via mineable tags |
| N10 | Portkey item renders (plain and camouflaged) in inventory, hand and on the ground |  | *Why:* new item render properties |
| N11 | Diaphanous Block - breaking it shows particles of the displayed block; the see-through states render correctly |  | *Why:* break particles moved to IBlockRenderProperties |
| N12 | Durability bars - Floo Pouch, Reinforced Ender Bucket and Summoning Pendulum show the right fill and colour |  | *Why:* bar API changed (0..13 width) |
| N13 | Block entity data syncs live to the client without relogging: Observer target, Sound Dampener list, Biome Radar crystal, Redstone Interface |  | *Why:* empty update tags now arrive as null (one crash found) |
| N14 | GUIs - every screen looks right (buttons, tooltips, scroll lists): spot-check the Chunk Analyzer, Sound Recorder, Block Destabilizer lists |  | *Why:* click test passes; layout not eyeballed |
| N15 | World height - build/dig near y -64 and y 320 with the mod blocks; lighting sweep of the Spectre Illuminator still lights an area |  | *Why:* range changed from 0..255 |

## Visual / audio spot-checks that the scripts only measure

The scenarios assert state and pixel measurements for these, but only your eyes/ears confirm they look right (a quick look is enough).

| # | Look/listen for | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener: the muted sound is really silent near the block and audible again beyond 20 blocks |  |  |
| 234 | Fluid Display: takes the poured fluid's texture/colour, still vs flowing looks right, no holes beside neighbouring blocks (sand, stone) - this was the 1.16 culling bug |  |  |
| 149-153 | Biome Radar: crystal spins in the middle of the radar; antenna particles change colour when the search finishes |  |  |
| 254 | Magic Hood: no potion swirl around you in F5 view (and other players see no nametag, two-player check) |  |  |
| 265 | Ancient Furnace: the area around it visibly turns warm biome colours and the snow melts after the explosion (FIXED on 1.19.2: only 1 in 16 columns used to melt) |  |  |
| 277 | Portkey: dropped item shimmers, stops glowing about 5 s later; disguised Portkey (#279) looks like the donor item in hand/ground/item frame |  |  |
| 319 | Light Redirector: open face shows the block on the opposite side (periscope picture) |  |  |
| 321-323 | Dyeing Machine: recolored items are tinted held/in inventory/on the ground/worn; the enchant-glint result glows in the dye colour on items AND worn armor |  |  |
| 346 | Runes: noisy texture, same-colour pixels join across the base edge, nothing flickers while you move |  |  |
| all glass-like blocks | Trigger/Lapis/Quartz Glass, Diaphanous, Spectre and the other 28 `notSolid()` blocks: no see-through holes in the faces of blocks next to them |  |  |
| 1.18+ torch | Advanced Redstone Torch (red/green, floor and wall): no black box around the texture (`RenderLayerSetup`: cutout) - FIXED (mirrored from 1.18.2, scenario `render_fixes_18` passes 2/2 on this branch 2026-10-18) |  |  |
| 1.18+ recorder | Sound Recorder: the row under the mouse gets the dark highlight bar - FIXED (mirrored from 1.18.2, scenario `render_fixes_18` passes 2/2 on this branch 2026-10-18) |  |  |
| 1.18+ fluid display | Fluid Display with a fluid is hidden behind blocks in front of it - FIXED (mirrored from 1.18.2, scenario `render_fixes_18` passes 2/2 on this branch 2026-10-18) |  |  |

## 7. New for 1.20.6 (NeoForge 20.6.141) - all FIXED/new, nothing tested by hand

These are the behaviours whose implementation changed in the 1.20.6 port (data components, payloads, capabilities, new item/block interaction API). The suites pass; these are the things that are worth a quick manual look.

| # | Check | Result | Feedback |
|---|---|---|---|
| P1 | Right-click interactions: every GUI block still opens (Iron Dropper, Igniter, Item Collectors, Detectors, Ender Mailbox, Imbuing Station, ...), Biome Radar takes/returns a Biome Crystal, Sound Box takes/returns a pattern, Ancient Furnace accepts a Nether Star, Spectre Core still works with an empty and a full hand |  | *Why:* `Block.use` is now `useItemOn`/`useWithoutItem` |
| P2 | Ender Bucket / Reinforced Ender Bucket pick up and place water and lava, stack to 16 while empty, show the fluid in the name, durability-style bar on the reinforced one |  | *Why:* fluid is a custom data component now |
| P3 | Items that keep data: Portkey (bind, camouflage recipe, prime after a drop), Golden/Emerald Compass (recipes with Position Filter/ID Card), Position Filter, ID Card, Entity Filter, Sound Pattern (shift-click clears), Sound Recorder (shift toggles, stops at 10), Time in a Bottle (charges while carried, accelerates), Redstone Tool (two clicks link), Redstone Remote, Ender Letter, Spectre Anchor survives death |  | *Why:* stack data moved to `custom_data` |
| P4 | Spectre Pickaxe/Axe/Shovel: +3 block reach while held, tooltip line "Block Interaction Range"; Spectre tools keep their damage/speed |  | *Why:* tools use the new attribute modifier components |
| P5 | Magic Hood: own green texture on the head, hides the potion particles (also when put on while an effect is active), nametag hidden for other players |  | *Why:* particle hiding is `EffectParticleModificationEvent`; armor texture is a material layer |
| P6 | Super Lubricent Boots: wearing them shows the boots texture and you slide |  | *Why:* armor material layer texture |
| P7 | Imbue bottles (fire/poison/experience/wither): drinking applies the effect for 20 minutes, a second kind replaces the first, hits ignite/poison/wither and experience drops grow |  | *Why:* effects are holders now |
| P8 | Spectre Lens above a Beacon applies the beacon effects to the owner everywhere |  | *Why:* beacon fields are holders now |
| P9 | Dispensers shoot Golden Eggs and Weather Eggs |  | *Why:* `ProjectileItem` dispense behaviour |
| P10 | Magnetic enchantment appears in the enchanting table/anvil on digger tools and works (drops go into the inventory); Block Breaker still mines with it |  | *Why:* enchantment definition rewritten |
| P11 | Creative tab "Random Things" lists everything; item tooltips show (Biome Crystal biome, Ender Letter, Time in a Bottle, Spectre Charger, Floo Pouch...) |  | *Why:* tooltips take `TooltipContext` |
| P12 | Multiplayer (two clients): Notification Interface toast, Biome Radar antenna particles, Redstone Remote, Sound Recorder, Eclipsed Clock hands, chat features |  | *Why:* all packets are `CustomPacketPayload`s now (the Eclipsed Clock packet was never registered before) |
| P13 | Chests in dungeons/strongholds/villages/jungle temples can contain Summoning Pendulum, Magic Hood, Slime Cube and Biome Crystals |  | *Why:* suite proves the pools are injected, not that they roll |
| P14 | Load a world saved by the 1.20.1 version: block entity data (Floo network, Ender Mailbox letters, Spectre cubes) survives; stacks keep their data only if saved by this version (item NBT is not converted) |  | *Why:* new world format; no automatic conversion of custom item data |
| P15 | Blazing Fire catches flammable blocks about 4 times as often as vanilla fire (the coremod now actually applies) |  | *Why:* `checkBurnOut` rename |

## 8. New for 1.21.1 (NeoForge 21.1.264) - all FIXED/new, nothing tested by hand

Behaviours whose implementation changed in the 1.21 port. The suites, the GUI click test and the render scenes pass; these are worth a quick look in the manual world.

| # | Check | Result | Feedback |
|---|---|---|---|
| Q1 | Magnetic enchantment: appears on the enchanting table / enchanted books for pickaxes, axes and shovels, anvil cost and name "Magnetic" correct; a Magnetic tool sends block drops straight into the inventory; the Block Breaker still picks up with it |  | *Why:* the enchantment is a data-pack enchantment now (config toggle only disables the effect) |
| Q2 | Dyeing Machine armor: dyed iron/leather armor on a player and on an armor stand shows the dye colour on every piece; dyed items (sword, diamond) are tinted in hand, in the inventory and on the ground |  | *Why:* the armor tint hook moved (NeoForge 21 armor render overloads) |
| Q3 | Lotus Seeds and Spectre Sapling plant on dirt/grass/farmland and on Fertilized Dirt (tilled and untilled), not on stone; Fertilized Dirt still grows crops, stems and desert plants as before |  | *Why:* `IPlantable`/`PlantType` are gone, soil checks use the plant block state |
| Q4 | Spectre key / Spectre dimension: entering and leaving the Spectre dimension (key, Spectre Anchor) and the End via the Artificial End Portal teleports you to the right place with no portal sound/animation glitches |  | *Why:* `changeDimension` takes a `DimensionTransition` now |
| Q5 | Eclipsed Clock frame hangs on a wall with the right size and drops its item when broken; Golden Chicken and Spirit drops/experience, Spectre Illuminator lights up and un-lights when removed |  | *Why:* hanging-entity bounding box, mob drop/experience and removal hooks were renamed |
| Q6 | Imbue potions (fire, poison, wither, ...): melee hits apply the effect, arrows and thrown potions do not |  | *Why:* now `LivingIncomingDamageEvent` |
| Q7 | Crafting-grid special recipes still work: Diaphanous set/invert, Portkey camouflage, Spectre Anchor combine, Golden/Emerald Compass position/target |  | *Why:* recipes take `CraftingInput` now |

## 9. New for 1.21.4 (NeoForge 21.4.161) - all FIXED/new, nothing tested by hand

Behaviours whose implementation changed in the 1.21.4 port (item model definitions, render states, equipment assets, new coremod anchors) and the bugs found while retesting rows. The suites, the GUI click test, the render scenes, the 28 scripted scenarios and a real NeoForge 21.4.161 install pass; these are worth a quick look in the manual world.

| # | Check | Result | Feedback |
|---|---|---|---|
| R1 | Items look right everywhere (inventory, hand, ground, item frame): tinted items (Dyeing Machine colours, Luminous/Stained Brick, Rune Dust, Biome Crystal), the Portkey (plain and camouflaged), compasses and the Time in a Bottle |  | *Why:* all item models are item definitions now (`assets/randomthings/items`), custom tints/ranges are registered in `RTItemModels` |
| R2 | Enchant glint colour on items: a Redstone Tool, Spectre Key, Portkey, Spectre Sword and a Dyeing-Machine glint show their colour in the hand, inventory and on the ground (worn armor too) |  | *Why:* new `ItemStackRenderTransformer` hands the stack to the glint hook |
| R3 | Spectre Pickaxe/Axe/Shovel/Sword, Magic Hood and Super Lubricent Boots: models, worn textures, repair with Spectre Ingots, +3 reach |  | *Why:* tool/armor materials and equipment assets changed |
| R4 | Summoning Pendulum: releasing a captured mob takes it out of the pendulum (it used to be released but stay captured) |  | *Why:* fixed 2026-10-23, test `retest.summoning_pendulum.releases_the_oldest_captured_mob_first` |
| R5 | Ender Bucket: picking up from a stack of empty buckets turns exactly one into a filled one (used to give one and keep the whole stack) |  | *Why:* fixed 2026-10-23, test `retest.ender_bucket...` |
| R6 | Colored Grass without Silk Touch drops plain Dirt, with Silk Touch itself (it always dropped itself) |  | *Why:* the loot table used the pre-1.20.5 tool predicate; fixed 2026-10-23 |
| R7 | Golden Chicken drops 3 gold ingots (it dropped 1, three blocks up) |  | *Why:* `spawnAtLocation(item, 3)` takes a height, not a count; fixed 2026-10-23 |
| R8 | Dedicated server: right-clicking an Eclipsed Clock with a charged Time in a Bottle fast-forwards the time and does not crash the server |  | *Why:* the animation packet class loaded `ClientLevel`; fixed 2026-10-23 |
| R9 | Every block item shows its real name (inventory, hotbar, creative tab, chests), e.g. Spectre Lens, Rain Shield, Dyeing Machine |  | *Why:* 1.21.4 block items need `useBlockDescriptionPrefix()`; they showed raw `item.randomthings.*` keys. Fixed 2026-10-23, test `retest.lang...` |
| R10 | Biome Radar: right-click with an empty hand takes the crystal out, also after the search finished (the radar goes idle) |  | *Why:* deliberate divergence from 1.12.2, which only allowed it while idle |
| R11 | Spectre Illuminator: place it; after ~12 s the orb settles in the middle of the chunk and the whole chunk lights up over about a minute; picking it up darkens it again |  | *Why:* the light hook moved to `BlockLightEngine` (1.21.4 reads emission there); fixed 2026-10-23, test `retest.spectre_illuminator...` |
| R12 | Filtered Super Lubricent Platform sits as low in its item box as the other platforms |  | *Why:* its item model lacked the gui display transform |
