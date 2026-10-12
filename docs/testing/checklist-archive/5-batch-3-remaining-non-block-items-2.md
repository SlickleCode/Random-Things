## 5. Batch 3 — remaining (non-block) items (continued)

### Slice 25 — Ancient Furnace, Peace Candle (worldgen batch)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 262 | Ancient Furnace — rarely found naturally embedded (top face flush with the ground) in cold biomes (snowy tundra, taiga, mountains, etc.) … | PASS |  |
| 263 | Ancient Furnace — right-clicking the top opening with a Nether Star consumes it (unless creative) and starts it heating … | PASS |  |
| 264 | Ancient Furnace — right-clicking with anything else, or right-clicking one already heating, does nothing | PASS |  |
| 265 | Ancient Furnace — after a multi-minute heat-up, it melts surface snow (and ice, to water) in a large radius around itself, reassigns that area's biome to its warmer counterpart (e.g. Snowy Tundra -> Plains), then explodes and is destroyed | FIXED | Real bug: the biome mutation itself was already correct server-side, but nothing told an already-connected client's own separate copy of the chunk's biome data to update, so the visual grass/foliage/fog color never changed even though the real biome had. Fixed by resending each affected chunk to everyone tracking it. Please retest. |
| 266 | Ancient Furnace — very hard to break with a pickaxe if left un-activated; breaking it before activation drops itself | PASS |  |
| 267 | Ancient Furnace — heating state (and progress) survives a world save/reload while mid-heat-up | PASS |  |
| 268 | Peace Candle — standalone decorative candle, creative-menu obtainable; needs solid ground underneath (auto-breaks and drops otherwise, like a torch) | PASS |  |
| 269 | Peace Candle — no hostile mobs naturally spawn within a 3-chunk radius of one (test by placing one down and waiting/forcing a spawn attempt nearby) | PASS |  |
| 270 | Peace Candle — in a plains village, roughly a third of the tall cobblestone churches are instead the Peace Candle church (`randomthings:peace_candle_church`: same footprint as the vanilla one, custom build with a Peace Candle inside, entrance on the east side of the template); the rest stay vanilla. Needs several plains villages / churches to see both variants; its door must connect to the village street like any vanilla house |  |  |
| 271 | ~~Special Chest (Water variant) — naturally generates in an ocean monument's treasure/core room~~ (removed per request) | DONE |  |
| 272 | Spectre Illuminator — right-click a block to place it … | PASS |  |
| 273 | Spectre Illuminator — once centered, its whole chunk (and only that chunk) lights up to full block-light brightness, and a soft particle trail plays around it | PASS |  |
| 274 | Spectre Illuminator — right-click the placed orb to collect it back as an item; the chunk goes dark again | PASS |  |
| 275 | Portkey — craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center) |  |  |
| 276 | Portkey — right-click a block to bind it there; shift-hold shows the bound dimension/x/y/z in the tooltip (plain hover shows a "hold shift" hint instead), matching `Position Filter`'s own … | PASS |  |
| 277 | Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the ground it stops despawning and stops glowing (primed); picking it back up before that resets the countdown next time it's dropped | FIXED | Real bug: the priming counter lives on the item stack's own NBT (needed for client-side rendering), but mutating that tag in place on the same object the dropped entity already had cached never actually re-syncs to the client - the check that decides whether to resend only trips for a genuinely different object. Fixed by mutating a copy instead. Please retest. |
| 278 | Portkey — once primed and bound, the next player to pick it up (not the one who dropped it, necessarily … | PASS |  |
| 279 | Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - inventory, held, dropped on the ground, item frame), the donor item is returned unconsumed | FIXED | Real bug (second one found in this same area): `ItemRenderer.renderItem` - what this item's renderer is already invoked from - applies its own centering offset before calling the renderer, and both the camo'd and plain branches called back into an overload that applies that exact same offset a second time, stacking. Fixed by cancelling the outer one first. Please retest. |
| 280 | Portkey — an un-camouflaged Portkey still shows its own plain icon correctly (in every context) | PASS |  |
| 281 | Spectre Anchor — craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 Ectoplasm (center) |  |  |
| 282 | Spectre Anchor — combine it with any other single non-stackable item in a crafting table … | PASS |  |
| 283 | Basic Redstone Interface — craft: iron ingots at the 4 corners, redstone dust on the 4 edges, a Stable Ender Pearl in the center … | PASS |  |
| 284 | Basic/Advanced Redstone Interface — whatever redstone signal (weak or strong) is currently hitting the interface block itself gets mirrored onto its target position(s), as if a real redstone source were sitting there - e.g. redstone wire leading away from the target lights up, a comparator reading it responds, etc.; power updates live as the interface's own input changes, and stops immediately if the interface block is broken |  |  |
| 285 | Advanced Redstone Interface — craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-click opens a GUI with 9 Position Filter slots instead of a single tool-bound target - drop up to 9 Position Filter items in to broadcast to all 9 positions simultaneously; removing one stops broadcasting to that position (matches the Basic interface's live power mirroring, just fanned out to multiple targets) |  |  |
| 286 | Redstone Activator — right-click empty air to cycle its duration (2/20/100 ticks, shown both in the tooltip and as a texture swap on the item itself; shift-right-click cycles backwards) … | PASS |  |
| 287 | Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom; sneak-right-click opens an "edit" GUI (9 Position Filter slots, same as the Advanced Interface, plus your normal inventory) to bind up to 9 targets; plain right-click opens a compact "use" GUI with one button per bound target, each firing a 20-tick strength-15 pulse at its position when pressed | FIXED | Reported (with screenshot): the "use" screen looked like a blank white box. Real bug found - it (and the edit screen, and Advanced Redstone Interface's own screen) never drew a title at all. Fixed all three. The plain gray panel look itself is faithful to 1.12.2's own texture, not a bug. See `TESTING_NOTES.md` #287. Please retest - this may also be what the original "no slots visible" report was seeing.<br><br>**10/3/2026 camo icons added:** edit GUI now has the second row of 9 ghost slots (click one holding an item to copy it as that button's icon, click empty-handed to clear); the use GUI buttons are slot-shaped and show the camo item (or the Position Filter if none), with the Position Filter's custom name as tooltip. Please test: set a camo, reopen the use GUI, rename a Position Filter on an anvil, shift-click a Position Filter in/out, and that the remote itself can't be taken from the hotbar while editing. |

### Slice 26 — Magnetic Enchantment, Block Breaker, Block Destabilizer

New this session (a cloud sandbox with no Forge/Mojang Maven access - see the
note at the top of this slice's rows and `PORTING_PLAN.md`'s handoff for what
that means for verification). Both blocks pick up the previously-NOT-STARTED
Magnetic Enchantment/enchantment-package prerequisite and the "new falling-
entity type" prerequisite respectively - see `WIKI_FEATURE_STATUS.md`.

| # | Feature | Result | Feedback |
|---|---|---|---|
| 288 | Magnetic Enchantment — appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mining a block with it puts drops straight in your inventory instead of on the ground |  |  |
| 289 | Block Breaker — craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way facing toward whichever way you were looking |  |  |
| 290 | Block Breaker — left unpowered, continuously mines whatever's directly in front of it at real survival speed (respects the target block's actual hardness/tool-appropriateness via a … | PASS |  |
| 291 | Block Breaker — redstone-powering it pauses mining immediately (crack overlay clears); breaking the breaker block itself while mid-swing also clears the overlay cleanly | PASS |  |
| 292 | Block Destabilizer — craft: obsidian at the 4 corners, sand left/right of a diamond in the middle row, redstone above/below the diamond (ring pattern) |  |  |
| 293 | Block Destabilizer — a redstone pulse flood-fills every block connected to the one directly in front of it that matches the same exact `BlockState` (not just the same block with different properties) *and is a normal full cube*, up to 50 blocks, then drops the whole matched structure as falling-block entities top row first, nearest-to-the-machine first within each row | FIXED | Implemented per request (this restriction never existed in 1.12.2 either, so it's a real behavior change, not a bug fix) - the flood-fill now skips non-full blocks (redstone, torches, levers, etc.) entirely, leaving them behind untouched instead of sweeping them up. Please retest. |
| 294 | Block Destabilizer — GUI: Fuzzy toggle button makes the match ignore block-state variants and match by `Block` type only (e.g. matches every log rotation, every stained-glass color) |  |  |
| 295 | Block Destabilizer — GUI: Lazy toggle remembers which connected blocks *didn't* match last time, so re-triggering doesn't re-scan the whole structure from scratch; Reset clears that memory |  |  |
| 296 | Block Destabilizer — falling blocks landing on an obstruction (another block, a tile-entity-bearing block) vanish without dropping an item, matching 1.12.2's design (this is a demolition tool, not a block-moving one) |  |  |
| 297 | Spectre Key — craft: Spectre Ingot at the 3 corners (top-left, middle-left, bottom-right), Stable Ender Pearl center-left; Spectre Ingot itself crafts from Lapis Lazuli + Gold Ingot + Ectoplasm (or the 9x bulk recipe: Lapis Block + Gold Block + 8x Ectoplasm); Ectoplasm has a 1/55 chance per random tick to drop from Spectre Leaves |  |  |
| 298 | Spectre Key — hold right-click for 5 seconds (glowing cyan particle charge-up) to teleport into your own private Spectre Cube room … | PASS |  |
| 299 | Spectre Cube room — a 16x16 room made of unbreakable/explosion-immune Spectre Block, with a 2x2 unbreakable Spectre Core pedestal at the center … | PASS |  |
| 300 | Spectre Core — right-click with Ectoplasm raises the room's ceiling (consumes the held stack, up to however much height is left before Y 255) … | PASS |  |
| 301 | Spectre Dimension — always dark (no skylight), flat gray-cyan fog, no mob spawns (void biome, empty chunk generator), can't be slept in/respawn-set, wandering outside your own cube (not … | PASS |  |
| 302 | Spectre Dimension — joining/rejoining any world after having visited the Spectre dimension doesn't crash | PASS |  |

### Slice 27 — Spectre Tools

New this session (2026-09-28, local machine, full toolchain access). Picked from the NOT-STARTED wiki
list now that Spectre Ingot exists (crafted in Slice 26's Spectre Key batch). All four items ground-truthed
against the real 1.14.4 `IItemTier`/`ToolItem`/`PickaxeItem`/`AxeItem`/`ShovelItem`/`SwordItem` hierarchy
via `javap`/real source before writing anything - one real simplification fell out of that: vanilla's own
`AxeItem#getDestroySpeed` already does exactly what 1.12.2's `ItemSpectreAxe` custom override did (full
efficiency on wood/plant materials regardless of the per-block list), so no override was needed there at
all. `./gradlew build -x test` clean, plus a `runClient` smoke boot confirming all 4 items/models/textures
load and bake with no `ModelBakery` errors and reach texture-atlas stitching with no missing-texture
placeholder warnings for any of them. Not yet tested in-game (crafting, actual mining/reach/repair feel).

| # | Feature | Result | Feedback |
|---|---|---|---|
| 303 | Spectre Sword — craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) and enchantability (22) than a diamond sword, repairable with Spectre Ingot on an anvil |  |  |
| 304 | Spectre Pickaxe — craft: 3 Spectre Ingot across the top row, Obsidian double-stacked below center … | PASS |  |
| 305 | Spectre Axe — craft: 2 Spectre Ingot + 1 Obsidian in an L (top-left/middle-left/middle-center), Obsidian bottom-center … | PASS |  |
| 306 | Spectre Shovel — craft: 1 Spectre Ingot top-center, Obsidian double-stacked below; same +3 reach | PASS |  |

### Slice 28 — Spectre energy network (Charger/Coils/Injector)

New this session (2026-09-28, local machine, full toolchain access). Scoped down from the full
"Spectre energy network" wiki grouping, per user request: this slice covers the actual FE-network
trio (per-player energy pool + its two "taps" + the wearable charger); **Spectre Lens deferred to a
follow-up slice** since it's a functionally unrelated mechanic (extends a Beacon's buffs to its owner
remotely - see `WIKI_FEATURE_STATUS.md`), not part of the energy network at all despite sharing a wiki
row. `NUMBER`-tier coils also deferred (a config-gated, dungeon-loot-table-only tier in 1.12.2 - needs
real loot-table-injection infrastructure this port hasn't built for anything yet).

Ground-truthed the real 1.14.4 `IEnergyStorage`/`CapabilityEnergy`/`WorldSavedData`/
`DimensionSavedDataManager`/`LazyOptional` APIs via `javap`/real source before writing anything - all
stable, near-unchanged from 1.12.2's own Forge Energy API. **Real discovery while porting the
recipes**: every Coil/Charger tier (bar the always-uncraftable Genesis ones) needs "Spectre String", an
`ItemIngredient` sub-item that a first pass of grepping the real 1.12.2 source for its Java enum
constant name turned up *no* recipe/drop/loot source for at all (even blacklisted from JEI as
"unobtainable") - initially reported to the user as dead/vestigial 1.12.2 content. **That was wrong,
caught before shipping**: a second, more careful look at the actual `recipes/` folder (not just a
Java-source grep) found a real `spectrestring.json` recipe (4x from Ectoplasm + String + Diamond) that
the enum-name grep had simply missed. Ported that recipe too; the user's own call (asked directly,
since this file's own working rule says to report a "matches 1.12.2" finding and wait rather than
silently diverge) was to match 1.12.2 exactly regardless, which turned out to be the easy case since
the ingredient is real and obtainable after all. **Disclosed simplifications**, in order of size:
- Coil/Injector's elaborate hand-modeled Cubik Studio meshes (a small attached nub for Coil; an
  obsidian-pedestal-inside-a-translucent-glass-shell for the Injector, both via Forge's 1.12.2-only
  `forge:multi-layer`/`forge_marker` blockstate format, which doesn't exist in this Forge version)
  replaced with plain full-cube blocks reusing the original's own real (and, it turns out, already
  animated - real `.mcmeta` files copied over unchanged) block textures - matches this port's own
  established "small attached block" simplification precedent (`ContactButtonBlock`/
  `ContactLeverBlock` made the same call already). The real mechanic (must attach to a face that
  exposes an Energy capability, auto-drops otherwise) is preserved exactly, only the 3D shape/collision
  precision is dropped.
- All 4 Coil tiers share one texture (no per-tier tint) - the original's per-tier color was a runtime
  render tint on the now-dropped custom mesh, not a separate baked texture per tier, and this port has
  no `BlockColor`/`ItemColor` tint-callback infrastructure yet; building one for this alone wasn't
  worth it. Tiers are still distinguishable by name/tooltip.
- Coil's own tooltip (real 1.12.2 shows the FE/t transfer or generate rate) isn't ported - minor, the
  tier name alone already conveys which one it is.
- 1.12.2's `ItemSpectreCharger` was a Baubles belt item; Baubles is dropped project-wide (as already
  noted for the Spectre Charger's own `WIKI_FEATURE_STATUS.md` row), so this uses the same "just has to
  be carried anywhere in the inventory" fallback already established for Portable Sound Dampener/
  Obsidian Skull/Lava Charm.
- The Injector's own real recipe needs a Spectre Lens (deferred this slice) - registered with no
  recipe for now, same treatment as the Genesis tiers, until the Lens slice lands.

`./gradlew build -x test` clean. `runClient` smoke-verified further than previous slices: this boot
actually loaded a real world (not just the main menu) and reached `RecipeManager` datapack loading with
no parse errors for any of the 7 new recipes, and `ModelBakery`/texture-atlas stitching with no
missing-model/missing-texture warnings for any of the new blocks/items either. Not yet tested in-game
(placing a Coil against a real energy-capable machine needs another FE-capable mod installed, which
this environment doesn't have - see the feedback column below).

| # | Feature | Result | Feedback |
|---|---|---|---|
| 307 | Spectre String — craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4 |  |  |
| 308 | Spectre Coil (Normal/Redstone/Ender) — craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre Ingot/Glass; Redstone: Redstone Block/Redstone/Spectre String + a Normal coil; Ender: Stable Ender Pearl/Ender Pearl/Spectre String + a Redstone coil); can only be placed against a face that exposes a Forge Energy capability (auto-breaks and drops itself otherwise); once placed, continuously drains the placing player's Spectre energy pool (1024/4096/20480 FE per tick respectively) into whatever it's attached to |  |  |
| 309 | Spectre Coil (Genesis) — same attach/drop behavior as the other tiers, but generates a flat 10,000,000 FE/tick directly into whatever it's attached to, bypassing the player's pool entirely; creative-tab only, no recipe (matches 1.12.2 exactly) |  |  |
| 310 | Spectre Energy Injector — craft: Obsidian at the 4 corners, Spectre Lens top-center, a vanilla Beacon center, Spectre String left/right/bottom-center; once placed by a player, exposes that player's Spectre energy pool as a receive-only Forge Energy capability on every side, so an external FE-producing machine can push energy into it |  |  |
| 311 | Spectre Charger (Normal/Redstone/Ender/Genesis) — craft the first 3 tiers same escalating pattern as the Coils (Genesis has no recipe, creative-only); right-click to toggle on/off; while on and carried anywhere in your inventory, drains your Spectre energy pool each tick to top up any other Forge-Energy-capable item stack in that same inventory (instant/unlimited for Genesis) |  |  |

