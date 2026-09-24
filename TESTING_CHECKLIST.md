# Random Things 1.14.4 Port — Testing Sheet

One row per individually testable feature. Fill in **Result** with `Pass` or
`Fail` (leave blank if untested), and add a one-sentence note in **Feedback**
— especially for a `Fail`, since I can't run the game myself and need
specifics (what you did, what happened vs. what you expected) to fix it.
Rows are numbered so you can reference a specific one back to me, e.g. "#47
is broken."

---

## 1. Priority: recently fixed bugs (please confirm these first)

These were reported broken previously and fixed blind (no in-game
verification possible on my end) — highest chance of still being wrong.

| #   | Feature                                                                              | Result | Feedback                                                                                                  |
| --- | ------------------------------------------------------------------------------------ | ------ | --------------------------------------------------------------------------------------------------------- |
| 1   | Contact Lever — item icon (inventory/hand) renders correctly                         | PASS   |                                                                                                           |
| 2   | Contact Lever — block appearance, unpowered                                          | PASS   |                                                                                                           |
| 3   | Contact Lever — block appearance, powered/active                                     | FIXED  | Cannot power block — root cause: the lever/button `activate()` trigger was never wired to any input; right-clicking the block they're mounted on now calls it. Please retest. |
| 4   | Luminous Brick — item icon renders at full brightness (check a few of the 16 colors) | FIXED (round 2) | Grout/Underlying Brick texture is transparent when it should be opaque like Stained Brick. Light is okay. — root cause: model only referenced the `_tint` overlay layer, never the opaque `_base` layer underneath it. Round 2: fixing that introduced a new bug — item wasn't rendering as a block in inventory and was huge when held — because the custom two-layer model had no `"parent"` at all, so it inherited none of the standard GUI/hand/ground display-transform scaling every normal block model gets. Added `"parent": "block/block"` back (kept the custom elements). Please retest. |
| 5   | Stained Brick (non-luminous) — item icon renders correctly                           | PASS   |                                                                                                           |
| 6   | Accelerator Plate — item icon                                                        | NOT A BUG | Confirmed with you: the whole plate family is a deliberately mostly-transparent decal in the actual 1.12.2 source (checked `plate_accelerator.json` — only a thin quad using a ~77%-transparent icon texture), meant to show the block underneath through it. Current port matches that design. Let me know if you'd rather redesign these with an opaque baked-in stone look instead. |
| 7   | Bouncy Plate — item icon                                                             | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 8   | Collection Plate — item icon                                                         | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 9   | Corrector Plate — item icon                                                          | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 10  | Directional Accelerator Plate — item icon                                            | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 11  | Item Rejuvenator Plate — item icon                                                   | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 12  | Item Sealer Plate — item icon                                                        | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 13  | Redirector Plate — item icon                                                         | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 14  | Redstone Plate — item icon                                                           | NOT A BUG | Same as #6 — see-through-by-design, matches original. |
| 15  | Trigger Glass — unpowered: renders translucent (not opaque)                          | PASS   |                                                                                                           |
| 16  | Trigger Glass — powered: no collision, can walk through                              | PASS   |                                                                                                           |
| 17  | Trigger Glass — powered: no suffocation damage while standing in it                  | PASS   |                                                                                                           |
| 18  | Trigger Glass — powered: visibly more translucent than unpowered                     | PASS   |                                                                                                           |
| 19  | Trigger Glass — re-solidifies ~60 ticks after power is removed                       | PASS   |                                                                                                           |
| 20  | Trigger Glass — sand/gravel falls through while triggered                            | PASS   |                                                                                                           |
| 21  | Lapis Glass — solid to the player (can stand/walk on top, blocks walking through)    | PASS   |                                                                                                           |
| 22  | Lapis Glass — passable to non-player entities (mobs/items/arrows pass through)       | PASS   |                                                                                                           |
| 23  | Lapis Glass — no suffocation damage to the player                                    | N/A    | Lapis cannot be passed through by the player                                                              |
| 24  | Quartz Glass — passable to the player (walk straight through, no collision)          | PASS   |                                                                                                           |
| 25  | Quartz Glass — solid to non-player entities (a mob can't walk through it)            | PASS   |                                                                                                           |
| 26  | Quartz Glass — no suffocation damage to the player                                   | PASS   |                                                                                                           |
| 27  | Lapis Glass — jumping/falling onto it from above lands correctly                     | PASS   |                                                                                                           |
| 28  | Quartz Glass — a mob correctly paths around it instead of into it                    | PASS   |                                                                                                           |

---

## 2. Batch 1 — standalone blocks (no GUI/tile entity)

| #   | Feature                                                                                                                                                                     | Result | Feedback                                                                                           |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------ | -------------------------------------------------------------------------------------------------- |
| 29  | Lapis Lamp                                                                                                                                                                  | NOT A BUG | Always on when it should respond to redstone — checked the original 1.12.2 `BlockLapisLamp`: it never had redstone control either, `getLightValue` unconditionally returns 15. It's a static always-on light source by design, same as Rainbow Lamp's base glow. No code change made. |
| 30  | Quartz Lamp                                                                                                                                                                 | NOT A BUG | Always on when it should respond to redstone — same as Lapis Lamp above, matches the original's always-on design. No code change made. |
| 31  | Super Lubricent Ice (slippery)                                                                                                                                              | PASS   |                                                                                                    |
| 32  | Super Lubricent Platform (slippery)                                                                                                                                         | PASS   |                                                                                                    |
| 33  | Compressed Slime Block                                                                                                                                                      | FIXED (round 2) | Just called Slime Block in game, no difference in behavior from vanilla block — root cause was two-fold: (1) the block had a stray `asItem()` override that hijacked its identity to vanilla's Slime Block item, and (2) the actual 1.12.2 mechanic — right-click a Slime Block with a shovel to compact it, right-click again to compact further (3 levels, each bouncier) — was never ported at all, so there was no way to ever create one. Round 2: behavior confirmed working, but texture didn't change between compression levels — the original tinted each level a different gray shade via a block-color multiplier, which hadn't been reproduced. Added it (model now has a tintindex, block implements the color interface). Please retest. |
| 34  | Contact Button                                                                                                                                                              | FIXED  | Does not work, texture is not full block like it's supposed to be — same root cause as #3 (unwired `activate()` trigger) plus the model wrongly parented to vanilla's thin button shape instead of the original's full-cube `orientable` shape. Both fixed. Please retest. |
| 35  | Spectre Block (plain decorative)                                                                                                                                            | PASS   |                                                                                                    |
| 36  | Spectre Log                                                                                                                                                                 | FIXED  | Does not craft into Spectre Planks — the crafting recipe file was simply missing from the port. Added (1 log → 4 planks, matching 1.12.2). Please retest. |
| 37  | Spectre Plank                                                                                                                                                               | PASS   |                                                                                                    |
| 38  | Spectre Leaves                                                                                                                                                              | FIXED (round 2) | Does not drop ectoplasm — Ectoplasm was one of several ingredient items deliberately deferred pending the items batch; ported it now as a standalone item (matching the Lotus Blossom/Sakanade Spores pattern) and added the leaf loot table. Round 2: you reported leaves were also dropping saplings when they should only drop Ectoplasm — the original 1.12.2 source actually did include an independent 1/50 sapling chance alongside Ectoplasm, but per your call, removed the sapling entry so leaves now only ever drop Ectoplasm (1/55). Please retest. |
| 39  | Spectre Sapling — grows into a tree (simplified fixed shape, not worldgen-feature-based)                                                                                    | FIXED (round 2) | When it grows, it creates phantom blocks. bugged blocks appear correct upon world save and reload. — root cause: every log/leaf block placed during growth used a `setBlockState` flag that updates the server's world but never syncs the change to the client, so the client kept rendering stale (often air) chunk data until a reload re-synced everything. Fixed to use the standard notify+sync flag. Round 2: growth itself confirmed working, but the held/inventory item icon was rendering as a 3D crossed-plane "sprite" instead of flat pixel art — the item model wrongly parented straight to the block's cross-shaped model instead of a flat `item/generated` icon (same bug also found and fixed on Glowing Mushroom and Pitcher Plant, which share the same cross-shaped block model pattern). Please retest. |
| 40  | Accelerator Plate — function (speeds up entities)                                                                                                                           |        |                                                                                                    |
| 41  | Bouncy Plate — function (bounces entities)                                                                                                                                  |        |                                                                                                    |
| 42  | Collection Plate — function (pulls from neighbor inventory)                                                                                                                 |        |                                                                                                    |
| 43  | Corrector Plate — function                                                                                                                                                  |        |                                                                                                    |
| 44  | Directional Accelerator Plate — function                                                                                                                                    |        |                                                                                                    |
| 45  | Item Rejuvenator Plate — function                                                                                                                                           |        |                                                                                                    |
| 46  | Item Sealer Plate — function                                                                                                                                                |        |                                                                                                    |
| 47  | Redirector Plate — function (redirects entity movement)                                                                                                                     |        |                                                                                                    |
| 48  | Redstone Plate — function                                                                                                                                                   |        |                                                                                                    |
| 49  | Glowing Mushroom                                                                                                                                                            |        | Not yet reported broken, but found and fixed proactively while chasing #39: its item icon had the same wrong cross-model-as-item-parent bug as Spectre Sapling. Worth a quick recheck. |
| 50  | Sided Redstone                                                                                                                                                              |        |                                                                                                    |
| 51  | Pitcher Plant (decorative only — no fluid-bottle/cauldron filling)                                                                                                          |        | Not yet reported broken, but found and fixed proactively while chasing #39: same item-icon bug as Glowing Mushroom/Spectre Sapling. Worth a quick recheck. |
| 52  | Stained Brick — non-luminous variants (spot-check a few of the 16 colors)                                                                                                   |        |                                                                                                    |
| 53  | Stained Brick — luminous variants, glow correctly (spot-check a few of the 16 colors)                                                                                       |        |                                                                                                    |
| 54  | Biome Glass (5 variants: cobble/smooth/brick/cracked/chiseled) — tints by biome                                                                                             |        |                                                                                                    |
| 55  | Biome Stone (5 variants) — tints by biome                                                                                                                                   |        |                                                                                                    |
| 56  | Colored Grass — ships as single white-only variant (no color picker yet)                                                                                                    |        |                                                                                                    |
| 57  | Luminous Block — glows correctly (spot-check a few of the 16 colors)                                                                                                        |        |                                                                                                    |
| 58  | Luminous Translucent Block — glows + is see-through (spot-check a few colors)                                                                                               |        |                                                                                                    |
| 59  | "Beans" item — plants Bean Sprout                                                                                                                                           |        |                                                                                                    |
| 60  | "Lesser Magic Bean" item — plants the lesser Bean Stalk                                                                                                                     |        |                                                                                                    |
| 61  | "Magic Bean" item — plants the fast/strong magic Bean Stalk                                                                                                                 |        |                                                                                                    |
| 62  | Bean Sprout — grows through stages, harvestable at max growth without breaking the plant                                                                                    |        |                                                                                                    |
| 63  | Bean Stalk (lesser) — grows upward over time                                                                                                                                |        |                                                                                                    |
| 64  | Bean Stalk (magic) — grows upward, caps itself with a Pod near build-height limit                                                                                           |        |                                                                                                    |
| 65  | Pod — drops beans when broken                                                                                                                                               |        |                                                                                                    |
| 66  | Lotus Seeds — plants Lotus                                                                                                                                                  |        |                                                                                                    |
| 67  | Lotus — 4 growth stages, harvestable at max age without breaking the plant                                                                                                  |        |                                                                                                    |
| 68  | Sakanade — shearable, hangs under giant mushroom caps                                                                                                                       |        |                                                                                                    |
| 69  | Blazing Fire — spreads noticeably faster than vanilla fire                                                                                                                  |        |                                                                                                    |
| 70  | Blazing Fire — catches flammable blocks (wood/wool) more eagerly than vanilla (Mixin-only tuning — if it behaves identical to vanilla fire, the Mixin likely isn't loading) |        |                                                                                                    |

Known, expected (not a bug): none of Batch 1's blocks have loot tables yet —
breaking any of them in survival currently drops nothing. Already tracked as
a follow-up cleanup slice, no need to re-report it.

---

## 3. Batch 2 — tile-entity/GUI-backed blocks

### Slice 1

| # | Feature | Result | Feedback |
|---|---|---|---|
| 71 | Analog Emitter — reads input side, re-emits configurable 0–15 on other sides | | |
| 72 | Igniter — Toggle mode | | |
| 73 | Igniter — Ignite mode | | |
| 74 | Igniter — Keep Ignited mode (self-relights every ~10 ticks, recovers from being doused) | | |
| 75 | Online Detector — emits redstone while the configured username is online | | |
| 76 | Item Collector — vacuums nearby dropped items into attached inventory | | |
| 77 | Item Collector — 6-way directional model looks correctly oriented on all faces | | |
| 78 | Extraction Plate — pulls items from target inventory, ejects toward cycled output side | | |
| 79 | Processing Plate — extraction+eject timer, plus touch-triggered insert of colliding items | | |

### Slice 2

| # | Feature | Result | Feedback |
|---|---|---|---|
| 80 | Advanced Redstone Repeater — configurable turn-on delay respected | | |
| 81 | Advanced Redstone Repeater — configurable turn-off delay respected (independently of turn-on) | | |

### Slice 3

| # | Feature | Result | Feedback |
|---|---|---|---|
| 82 | Iron Dropper — 9-slot inventory GUI | | |
| 83 | Iron Dropper — 6-way facing on placement | | |
| 84 | Iron Dropper — redstone mode: Pulse | | |
| 85 | Iron Dropper — redstone mode: Repeat Powered | | |
| 86 | Iron Dropper — redstone mode: Repeat | | |
| 87 | Iron Dropper — drops its inventory contents when broken | | |

### Slice 4

| # | Feature | Result | Feedback |
|---|---|---|---|
| 88 | Custom Workbench — opens vanilla crafting GUI, fixed "tools on top" skin | | |

### Slice 5

| # | Feature | Result | Feedback |
|---|---|---|---|
| 89 | Player Interface — exposes placer's online inventory correctly per side (hotbar below, armor above, offhand north, rest elsewhere) | | |
| 90 | Inventory Tester — weak signal reflects whether held item could insert into faced inventory, with invert toggle | | |
| 91 | Inventory Rerouter — right-click cycles a side's capability redirect | | |
| 92 | Inventory Rerouter — two rerouters facing each other don't infinite-loop | | |
| 93 | Chat Detector — GUI text field, pulses on placer typing the exact configured message | | |

### Slice 6

| # | Feature | Result | Feedback |
|---|---|---|---|
| 94 | Redstone Tool — right-click links a Redstone Observer to a distant block, glows while linking | | |
| 95 | Redstone Observer — mirrors the linked target's weak/strong power in real time | | |

### Slice 7

| # | Feature | Result | Feedback |
|---|---|---|---|
| 96 | Potion Vaporizer — correctly detects the enclosed room it faces (flood fill) | | |
| 97 | Potion Vaporizer — applies the stored potion's effect to entities inside that room | | |
| 98 | Potion Vaporizer — burns fuel correctly | | |
| 99 | Potion Vaporizer — bottle-out slot can't be manually filled by the player, only by the machine | | |

### Slice 8

| # | Feature | Result | Feedback |
|---|---|---|---|
| 100 | Special Chest (Nature) — placed block renders custom texture + lid animation | | |
| 101 | Special Chest (Nature) — item icon renders as a 3D chest, not a flat icon | | |
| 102 | Special Chest (Water) — placed block renders custom texture + lid animation | | |
| 103 | Special Chest (Water) — item icon renders as a 3D chest, not a flat icon | | |

### Slice 9 + 10

| # | Feature | Result | Feedback |
|---|---|---|---|
| 104 | Entity Detector — filter modes (All/Living/Animal/Monster/Player/Items) each trigger correctly | | |
| 105 | Entity Detector — Custom mode with an Entity Filter Item in the slot | | |
| 106 | Entity Detector — per-axis radius (0–10) is respected | | |
| 107 | Entity Detector — invert toggle | | |
| 108 | Entity Filter Item — right-click/attack an entity to capture its exact type | | |
| 109 | Filtered Redirector Plate — redirects left/right per-entity based on the two filter slots (second slot wins if both match) | | |

### Slice 11

| # | Feature | Result | Feedback |
|---|---|---|---|
| 110 | Slime Cube — unpowered forces slime spawns to be allowed in its chunk | | |
| 111 | Slime Cube — powered forces slime spawns to be denied in its chunk | | |
| 112 | Advanced Item Collector — longer range, per-axis radius, single-item filter slot | | |

### Slice 12

| # | Feature | Result | Feedback |
|---|---|---|---|
| 113 | Filtered Super Lubricent Platform — items matching the filter slot fall through instead of sliding on top | | |

### Slice 13

| # | Feature | Result | Feedback |
|---|---|---|---|
| 114 | Notification Interface — pops a toast (title/description/item icon) only for the linked player on a redstone rising edge | | |

### Slice 14

| # | Feature | Result | Feedback |
|---|---|---|---|
| 115 | ID Card — right-click binds it to yourself | | |
| 116 | Global Chat Detector — catches/hides messages from any player with a matching ID card in its whitelist | | |
| 117 | Global Chat Detector — server operators bypass the whitelist check | | |

---

## 4. General spot-checks across the whole port

| # | Feature | Result | Feedback |
|---|---|---|---|
| 118 | No raw untranslated `block.randomthings.xxx`-style strings showing up in tooltips/GUI titles | | |
| 119 | Batch 2 blocks all drop correctly when broken (they have loot tables, unlike Batch 1) | | |
| 120 | GUI text-cycle mode buttons (Igniter, Entity Detector, etc.) are legible and functional (plain text, not the original's icon-sprite buttons — cosmetic difference only) | | |

---

## 5. Batch 3 — remaining (non-block) items

| # | Feature | Result | Feedback |
|---|---|---|---|
| 121 | Bean Stew — eat it, restores 8 hunger, leaves you holding an empty bowl afterward | | |
| 122 | Blaze and Steel — right-click a block face to place Blazing Fire instead of vanilla fire; damages the tool by 1 per use | | |
| 123 | Bottle of Air — right-click while standing in water to drink; your air/breath meter should top up while you hold right-click, even past the point you'd normally start drowning | | |
| 124 | Stable Ender Pearl — right-click to bind it to yourself (tooltip should show "Bound to \<your name\>"), then throw it; ~7 seconds (140 ticks) after it lands you should be teleported to where it landed, with an enderman-teleport sound | | |
| 125 | Stable Ender Pearl — thrown while unbound (fresh from creative, no right-click-to-bind first): after landing, should teleport the nearest living entity within 10 blocks to its position instead of you | | |
| 126 | Obsidian Skull — just carry it anywhere in your inventory (not worn/equipped); should give occasional protection from fire damage, more reliably for small burns than big hits | | |
| 127 | Lava Charm — same as Obsidian Skull, but specifically no-sells lava damage (spends internal charge, needs a moment to "recharge" between saves) | | |
| 128 | Lava Wader — worn as boots: fully immune to lava damage as long as its charge isn't depleted (charge drains on each save, regenerates over ~2 seconds after) | | |
| 129 | Lava Wader — also gives the general chance-based fire-damage protection (like Obsidian Skull/Lava Charm), on top of its lava-specific full immunity | | |
| 130 | Water Walking Boots — worn as boots, hold jump while standing on water with clear air above you: should bob/hop across the surface instead of sinking | | |
| 131 | Obsidian Water Walking Boots — same water-walking as above, plus counts as fire-damage protection like Obsidian Skull | | |
| 132 | Obsidian Water Walking Boots — does NOT let you walk on lava (only Lava Wader does) | | |
| 133 | Lava Wader — also lets you walk on LAVA the same way Water Walking Boots lets you walk on water | | |
| 134 | Super Lubricent Boots — worn as boots, negates the slide on Super Lubricent Ice/Platform/Stone (walk normally instead of sliding around) | | |
| 135 | Sneaking while wearing Super Lubricent Boots on a Super Lubricent block — should still be slippery while sneaking (boots only cancel it while standing/walking normally, matching the original) | | |

---

## 6. Not implemented yet — don't test these, they don't exist

Deliberately deferred, each for a stated reason (see the plan file for full
detail) — no point filing a bug against these:

- **Ancient Furnace** — never player-placeable in 1.12.2 either; belongs with a future worldgen batch.
- **Block Diaphanous, Light Redirector, Voxel Projector** — need a generic runtime block-model renderer.
- **Block Destabilizer** — needs a new custom falling-entity type.
- **Sound Recorder / Item Sound Pattern / Sound Box / Sound Dampener** — whole 4-piece subsystem, not started.
- **Imbuing Station / Rune Base** — needs a real custom recipe system.
- **Biome Radar** — needs unported items + a new particle + a new network message.
- **Block Breaker** — needs an enchantment system that doesn't exist yet.
- **Fluid Display** — blocked on unresearched 1.14.4 fluid API questions.
- **Spectre energy network, Ender network, Floo network, wireless Redstone Interface, Rain Shield, Peace Candle worldgen** — all need Mixin/worldgen work, none started.
- Worldgen/biomes, potions, enchantments, most crafting recipes, remaining non-block items — future batches, not started.
