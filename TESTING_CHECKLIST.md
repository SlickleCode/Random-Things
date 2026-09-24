# Random Things 1.14.4 Port — Testing Checklist

Reference for manually testing the port in-game. Nothing here has been run in an actual
client/server — everything below is "should work per the code" until you've clicked it.
Organized so you can work top-down: recent bug fixes first (highest chance of still being
wrong), then Batch 1, then Batch 2, then what's simply not there yet.

Legend: ⚠️ = known, deliberately-disclosed simplification vs. 1.12.2 (not a bug, just different).

---

## 1. Priority: recently fixed bugs (please confirm these first)

These were reported broken by you and then fixed blind (no in-game verification possible here).

- [ ] **Contact Lever** — place it, check the *item* icon in inventory/hand looks like the lever
  (not a weird/blank/stretched texture), and the *block* still looks correct placed both off and on.
- [ ] **Luminous Brick / Stained Brick (luminous variants)** — item icon in inventory should now
  render at full brightness like the block does, for all 16 colors.
- [ ] **Trigger/Redirector/Redstone/etc. plate family item icons** — check a couple of the 9 plate
  types' item icons in inventory look correct (previously all rendering "weird" due to a missing
  `getRenderLayer` override that's now added via the shared `PlateBlock` base).
- [ ] **Trigger Glass transparency** — unpowered Trigger Glass should render translucent glass, not
  an opaque block.
- [ ] **Trigger Glass walk-through when powered** — power a Trigger Glass block with redstone; while
  triggered you should be able to walk straight through it (no collision), and it should **not**
  deal suffocation damage. It should also visibly go more translucent while triggered. ~60 ticks
  after power is removed it should re-solidify (and re-trigger immediately if still powered or if
  the adjacent block is still triggered).
- [ ] **Trigger Glass sand/gravel passthrough** — drop sand or gravel onto a *triggered* Trigger
  Glass block; it should fall through instead of resting on top.
- [ ] **Lapis Glass** — should be **solid to the player** (can stand/walk on top, can't walk
  through it) but **passable to every other entity** (mobs, items, arrows should go straight
  through/fall through it). Standing inside/adjacent to it should **not** cause suffocation damage.
- [ ] **Quartz Glass** — the mirror image: **passable to the player** (walk straight through, no
  collision) but **solid to every other entity** (a mob should not be able to walk through it).
  Should also not cause suffocation damage to the player while standing in it.
- [ ] Double-check Lapis/Quartz Glass under a **falling player** (jump onto Lapis Glass from above)
  and near **mob pathing** (does a zombie correctly path around Quartz Glass instead of trying to
  walk through it?) — these exercise different code paths (movement collision vs. suffocation vs.
  pathfinding) than a simple walk-up-and-bump test.

If any of these are still wrong, the root cause is documented in the class-level javadocs of
[LapisGlassBlock.java](src/main/java/lumien/randomthings/block/LapisGlassBlock.java),
[QuartzGlassBlock.java](src/main/java/lumien/randomthings/block/QuartzGlassBlock.java), and
[TriggerGlassBlock.java](src/main/java/lumien/randomthings/block/TriggerGlassBlock.java) — worth
pasting back with the symptom.

---

## 2. Batch 1 — standalone blocks (no GUI/tile entity) — complete

All of these should be craftable/obtainable via creative and behave correctly. ⚠️ items are
intentionally different from 1.12.2.

- [ ] Lapis Glass, Lapis Lamp, Quartz Glass, Quartz Lamp, Super Lubricent Ice, Super Lubricent
  Platform, Trigger Glass, Compressed Slime Block, Contact Button, Contact Lever, Spectre Block
  (plain decorative, no energy network)
- [ ] Spectre tree set: Spectre Log, Spectre Plank, Spectre Leaves, Spectre Sapling (sapling should
  grow into a simplified fixed-shape tree ⚠️ — not the original's worldgen-feature tree)
- [ ] Plate family (9): Accelerator, Bouncy, Collection, Corrector, Directional Accelerator, Item
  Rejuvenator, Item Sealer, Redirector, Redstone Plate — ⚠️ no directional/color texture overlay,
  flat texture regardless of facing
- [ ] Glowing Mushroom, Sided Redstone, Pitcher Plant (⚠️ fluid-bottle/cauldron filling dropped —
  it's decorative only, doesn't interact with water/cauldrons)
- [ ] Stained Brick — all 32 variants (16 colors × luminous/non). ⚠️ Luminous variants use only the
  tint texture layer, not the original's base+tint 2-layer composite — should still look correct,
  just simpler under the hood.
- [ ] Biome Glass, Biome Stone (5 variants: cobble/smooth/brick/cracked/chiseled) — should tint
  based on the biome you place them in (⚠️ uses vanilla's grass-color biome tinting, not the
  original's bespoke biome-category heuristic — colors will differ somewhat by biome but should
  still visibly vary place to place)
- [ ] Colored Grass — ⚠️ ships as a single default-white block only; the 16-color-variant picker
  from 1.12.2 doesn't exist yet (needs a crafting recipe, not yet ported)
- [ ] Luminous Block / Luminous Translucent Block — 16 `DyeColor` variants each (32 total), should
  glow and (translucent variant) be see-through
- [ ] Bean family: `beans`, `lesser_magic_bean`, `magic_bean` items plant Bean Sprout / Bean Stalk
  (fast+strong "magic" vs. slow "lesser" variant) / Pod. Sprout should be harvestable without
  breaking the plant at max growth. ⚠️ dropped the original's self-destroy-into-pod-when-blocked
  edge case and custom ladder-climb feel (should still feel climbable via normal collision).
- [ ] Lotus (`lotus_seeds` plants it, 4 growth stages, harvest-without-breaking at max age), Sakanade
  (shearable, hangs under giant mushroom caps). ⚠️ Sakanade's "collapse" potion-effect-on-harvest is
  dropped (that potion isn't ported).
- [ ] Blazing Fire — should spread noticeably faster than vanilla fire (tick rate halved: 15 vs.
  vanilla's 30) and the other two 1.12.2 tuning tweaks (4x catch chance, faster age growth) should
  also be active via the Mixin — worth specifically testing catch chance against flammable blocks
  (wood, wool) since that's the Mixin-only piece with no fallback if the Mixin silently failed to
  load. If Blazing Fire behaves identically to vanilla fire, the Mixin isn't firing — flag that
  immediately, it's this port's only Mixin-based feature so far.
- [ ] None of Batch 1's blocks have loot tables yet ⚠️ (known gap) — breaking any Batch 1 block in
  survival currently drops **nothing**. This is expected for now, not a new bug — don't re-report it,
  it's tracked as a follow-up cleanup slice.

---

## 3. Batch 2 — tile-entity/GUI-backed blocks — complete for non-subsystem scope

### Slice 1
- [ ] Analog Emitter — reads redstone on its facing side, GUI sets 0–15 output re-emitted on other sides
- [ ] Igniter — 3-mode cycle (Toggle/Ignite/Keep Ignited), lights/extinguishes the block in front on
  redstone transitions; Keep Ignited should self-relight every ~10 ticks even if doused with water
- [ ] Online Detector — GUI text field for a username, emits redstone while that player is online
- [ ] Item Collector — no GUI, mountable nub, vacuums nearby dropped items into the inventory it's
  attached to; ⚠️ its 6-way directional model rotation is a best-effort derivation, never visually
  confirmed — check it actually looks correctly oriented on all 6 faces, not just functions correctly
- [ ] Extraction Plate — pulls items from the inventory below/behind, sneak-click cycles output side
- [ ] Processing Plate — same extraction+eject, plus touch-triggered insert of colliding dropped
  items into the inventory below. ⚠️ cosmetic item-bounce/redirect animation on collision is dropped,
  only the actual transfer happens.

### Slice 2
- [ ] Advanced Redstone Repeater — GUI sets separate turn-on/turn-off delay (2–10000 ticks each);
  test both directions of transition actually respect their configured delay independently

### Slice 3
- [ ] Iron Dropper — 9-slot inventory GUI, 6-way facing (place like a dispenser), 3 redstone modes
  (Pulse/Repeat Powered/Repeat), pickup delay, random motion toggle; should insert into a
  capability-exposing neighbor if present, else eject as a dropped item; breaking it should drop its
  contents

### Slice 4
- [ ] Custom Workbench — opens vanilla's own crafting-table GUI. ⚠️ no dynamic per-wood-type frame
  recoloring — ships as one fixed skin (this mod's "tools on top" art) regardless of what wood it's
  crafted from

### Slice 5
- [ ] Player Interface — no GUI; links to whoever placed it, exposes their online inventory as a
  capability (hotbar from below, armor from above, offhand from north, rest of main inventory from
  every other side) — test with hoppers/pipes on multiple faces
- [ ] Inventory Tester — item slot + facing + invert toggle, weak redstone signal based on whether
  its held item could be inserted into the faced inventory. ⚠️ uses a real slot (consumes the item),
  not the original's "ghost slot" that never consumes it — be aware the test item is genuinely held,
  not just configured
- [ ] Inventory Rerouter — right-click a non-facing side to cycle which real side of the faced
  inventory it maps to; two rerouters aimed at each other should NOT infinite-loop (cycle-detection
  guard). ⚠️ no per-face arrow-decal indicating current mapping — looks identical regardless of
  mapping, you'll need to check function not appearance
- [ ] Chat Detector — GUI text field for one exact message, links to placer, emits a 20-tick pulse
  when they type it, optional consume/hide from chat

### Slice 6
- [ ] Redstone Tool (item) — right-click links a Redstone Observer to a distant block (glows while
  in "linking" mode)
- [ ] Redstone Observer — read-only GUI shows current target X/Y/Z or "No Target"; should mirror the
  linked block's weak/strong power in real time whenever the target changes

### Slice 7
- [ ] Potion Vaporizer — 3-slot GUI (fuel/potion/bottle-out), facing like a dispenser; burns fuel
  and applies the potion's effect to living entities inside the enclosed room it faces (capped
  100-block flood fill — test in both a small sealed room and check it doesn't affect entities
  outside the room); bottle-out slot should be player-insert-locked (can't manually shove items in,
  only pulls out what the machine produces)

### Slice 8
- [ ] Special Chest (both `special_chest_nature` and `special_chest_water` variants) — opens
  vanilla's chest GUI/animation/sound, but should render its own custom texture (not a vanilla
  chest skin) both as a placed block (lid animates on open) and as a held/inventory item icon (3D
  chest, not a flat icon)

### Slice 9 + 10
- [ ] Entity Detector — GUI: filter mode (All/Living/Animal/Monster/Player/Items/Custom), per-axis
  radius 0–10, invert toggle, strong-output toggle, + a filter-item slot for Custom mode
- [ ] Entity Filter Item — right-click or attack a living entity to capture its exact type; then
  place that item in an Entity Detector's Custom slot and verify only that entity type triggers it
- [ ] Filtered Redirector Plate — two filter-item slots (left-turn/right-turn); an entity matching
  the second slot should redirect that way even if it also matches the first (second slot wins).
  ⚠️ reuses the plain Redirector Plate's texture/model — 1.12.2 itself never shipped unique art for
  this block, this isn't a regression

### Slice 11
- [ ] Slime Cube — unpowered = force-allow slime spawns in its chunk, powered = force-deny. ⚠️ this
  affects *any* slime spawn attempt located in that chunk (not just the specific cave-spawn RNG
  check the original ASM patch targeted) — test by toggling power and watching slime spawn rates
  change in that chunk over time, not expecting byte-identical spawn-roll behavior
- [ ] Advanced Item Collector — longer range than Item Collector, configurable per-axis radius, one
  example-item filter slot. ⚠️ single-item filter only, not the original's full whitelist/blacklist
  9-slot filter system

### Slice 12
- [ ] Filtered Super Lubricent Platform — extends the slippery platform, dropped items matching the
  filter slot should fall straight through instead of sliding on top

### Slice 13
- [ ] Notification Interface — links to placer, on redstone rising edge should pop a real toast
  notification (title/description/item icon) on the linked player's screen — test that it only
  shows for the linked player, not everyone online

### Slice 14
- [ ] ID Card (item) — right-click to bind to yourself
- [ ] Global Chat Detector — 9-slot ID-card whitelist GUI; should catch/hide messages from *any*
  player whose matching ID card is in the whitelist (or from any server op), unlike the single-owner
  Chat Detector from Slice 5

---

## 4. Not implemented yet — don't expect these to work

Deliberately deferred, each for a stated reason (see the plan file for full detail):

- **Ancient Furnace** — was never player-placeable in 1.12.2 either; belongs with a future worldgen
  batch alongside the structure that places it.
- **Block Diaphanous, Light Redirector, Voxel Projector** — need a generic runtime block-model
  renderer, more complex than what Special Chest's custom renderer does.
- **Block Destabilizer** — needs a new custom falling-entity type with arbitrary fall direction.
- **Sound Recorder / Item Sound Pattern / Sound Box / Sound Dampener** — whole interdependent
  4-piece subsystem with its own searchable GUI, not started.
- **Imbuing Station / Rune Base** — needs a real custom recipe system, belongs with a future
  Recipes batch.
- **Biome Radar** — needs unported items (Biome Crystal, Position Filter) + a new particle + a new
  network message.
- **Block Breaker** — needs an enchantment system that doesn't exist yet in this port.
- **Fluid Display** — blocked on unresearched 1.14.4 fluid API questions (same open question as
  Pitcher Plant's dropped fluid-filling mechanic in Batch 1).
- **Spectre energy network** (Spectre Coil, Energy Injector, Lens), **Ender network** (Anchor,
  Bridge, Mailbox, Prismarine Ender Bridge, Link Orb), **Floo network** (Floo Brick), **wireless
  Redstone Interface**, **Rain Shield**, **Peace Candle worldgen** — all need Mixin work and/or
  worldgen infrastructure, none started beyond the ASM-mapping analysis in the plan.
- Everything outside "blocks + tile entities" scope entirely: worldgen/biomes, potions,
  enchantments, most crafting recipes, remaining non-block items — future batches, not started.

---

## 5. General things worth spot-checking across the whole port

- [ ] Every new block/item has a lang entry (no raw `block.randomthings.xxx` untranslated strings
  showing up in tooltips/GUI titles)
- [ ] Every Batch 2 block drops correctly when broken (they all got loot tables, unlike Batch 1)
- [ ] GUI text-cycle buttons (mode toggles on Igniter, Entity Detector, etc.) — these are plain text
  buttons rather than the original's icon-sprite buttons ⚠️, purely cosmetic, just confirm they're
  legible and functional
