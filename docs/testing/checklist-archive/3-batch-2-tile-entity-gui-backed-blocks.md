## 3. Batch 2 — tile-entity/GUI-backed blocks

### Slice 1

| # | Feature | Result | Feedback |
|---|---|---|---|
| 71 | Analog Emitter — reads input side, re-emits configurable 0–15 on other sides | PASS |  |
| 72 | Igniter — Toggle mode | PASS |  |
| 73 | Igniter — Ignite mode | PASS |  |
| 74 | ~~Igniter — Keep Ignited mode~~ (removed per request) | DONE |  |
| 75 | Online Detector — emits redstone while the configured username is online | PASS |  |
| 76 | Item Collector — vacuums nearby dropped items into attached inventory | PASS |  |
| 77 | Item Collector — 6-way directional model looks correctly oriented on all faces | PASS | Real bug: this port's blockstate rotation values for the 4 horizontal facings didn't match 1.12.2's real numbers at all … |
| 78 | ~~Extraction Plate — pulls items from target inventory, ejects toward cycled output side~~ (removed per request) | DONE |  |
| 79 | ~~Processing Plate — extraction+eject timer, plus touch-triggered insert of colliding items~~ (removed per request) | DONE |  |

### Slice 2

| # | Feature | Result | Feedback |
|---|---|---|---|
| 80 | Advanced Redstone Repeater — configurable turn-on delay respected | PASS |  |
| 81 | Advanced Redstone Repeater — configurable turn-off delay respected (independently of turn-on) | PASS |  |
| 81.1 | Advanced Redstone Repeater — breaking the "off" state block shows a normal break particle effect, not the pink/black missing-texture one | PASS | **2026-09-28: real bug found - same stale-vanilla-texture-name class as #80/81's original fix, one instance that pass … |

### Slice 3

| # | Feature | Result | Feedback |
|---|---|---|---|
| 82 | Iron Dropper — 9-slot inventory GUI | PASS |  |
| 83 | Iron Dropper — 6-way facing on placement | PASS |  |
| 84 | Iron Dropper — redstone mode: Pulse | PASS |  |
| 85 | Iron Dropper — redstone mode: Repeat Powered | PASS |  |
| 86 | ~~Iron Dropper — redstone mode: Repeat~~ (removed per request) | DONE |  |
| 87 | Iron Dropper — drops its inventory contents when broken | PASS |  |

### Slice 4

| # | Feature | Result | Feedback |
|---|---|---|---|
| 88 | ~~Custom Workbench~~ (removed per request) | DONE |  |

### Slice 5

| # | Feature | Result | Feedback |
|---|---|---|---|
| 89 | Player Interface — exposes placer's online inventory correctly per side (hotbar below, armor above, offhand north, rest elsewhere) | PASS | Real bug: the armor-side capability wrapped the raw player inventory directly, whose `isItemValidForSlot` always … |
| 90 | Inventory Tester — weak signal reflects whether held item could insert into faced inventory, with invert toggle | PASS |  |
| 91 | Inventory Rerouter — right-click cycles a side's capability redirect, and now tells you in an action-bar message exactly which direction that side redirects to (or "disabled") every time … | REMOVED | Feature deliberately removed per user request 10/3/2026 (code, assets, recipes, lang all deleted). |
| 92 | Inventory Rerouter — two rerouters facing each other don't infinite-loop | REMOVED | Feature deliberately removed per user request 10/3/2026 (code, assets, recipes, lang all deleted). |
| 93 | Chat Detector — GUI text field, pulses on placer typing the exact configured message | PASS | Confirmed working by user 2026-10-04. |

### Slice 6

| # | Feature | Result | Feedback |
|---|---|---|---|
| 94 | Redstone Tool | PASS |  |
| 95 | Redstone Observer | PASS |  |

### Slice 7

| # | Feature | Result | Feedback |
|---|---|---|---|
| 96 | Potion Vaporizer — correctly detects the enclosed room it faces (flood fill) | PASS |  |
| 97 | Potion Vaporizer — applies the stored potion's effect to entities inside that room | PASS |  |
| 98 | Potion Vaporizer — burns fuel correctly | PASS |  |
| 99 | Potion Vaporizer — bottle-out slot can't be manually filled by the player, only by the machine | PASS |  |
| 99.1 | Potion Vaporizer - Ignore Non-full blocks like torches | PASS |  |

### Slice 8

| # | Feature | Result | Feedback |
|---|---|---|---|
| 100 | ~~Special Chest (Nature) — placed block renders custom texture + lid animation~~ (removed per request) | DONE |  |
| 101 | ~~Special Chest (Nature) — item icon renders as a 3D chest, not a flat icon~~ (removed per request) | DONE |  |
| 102 | ~~Special Chest (Water) — placed block renders custom texture + lid animation~~ (removed per request) | DONE |  |
| 103 | ~~Special Chest (Water) — item icon renders as a 3D chest, not a flat icon~~ (removed per request) | DONE |  |

### Slice 9 + 10

| # | Feature | Result | Feedback |
|---|---|---|---|
| 104 | Entity Detector — filter modes (All/Living/Animal/Monster/Player/Items) each trigger correctly | PASS |  |
| 105 | Entity Detector — Custom mode with an Entity Filter Item in the slot | PASS |  |
| 106 | Entity Detector — per-axis radius (0–10) is respected | PASS |  |
| 107 | Entity Detector — invert toggle | PASS |  |
| 108 | Entity Filter Item — right-click an entity to capture its exact type | PASS | **Investigated, no bug found** - the capture hook (`itemInteractionForEntity`) matches 1.12.2's own mechanism exactly … |
| 109 | ~~Filtered Redirector Plate — redirects left/right per-entity based on the two filter slots (second slot wins if both match)~~ (removed per request) | DONE |  |

### Slice 11

| # | Feature | Result | Feedback |
|---|---|---|---|
| 110 | Slime Cube — unpowered forces slime spawns to be allowed in its chunk | PASS |  |
| 111 | Slime Cube — powered forces slime spawns to be denied in its chunk | PASS |  |
| 112 | Advanced Item Collector — longer range, per-axis radius, single-item filter slot | PASS |  |

### Slice 12

| # | Feature | Result | Feedback |
|---|---|---|---|
| 113 | Filtered Super Lubricent Platform — items matching the filter slot fall through instead of sliding on top | PASS |  |

### Slice 13

| # | Feature | Result | Feedback |
|---|---|---|---|
| 114 | Notification Interface — pops a toast (title/description/item icon) only for the linked player on a redstone rising edge | PASS |  |

### Slice 14

| # | Feature | Result | Feedback |
|---|---|---|---|
| 115 | ID Card — right-click binds it to yourself | PASS |  |
| 116 | Global Chat Detector — catches/hides messages from any player with a matching ID card in its whitelist | PASS |  |
| 117 | ~~Global Chat Detector — server operators bypass the whitelist check~~ (removed per request) | DONE |  |

---

