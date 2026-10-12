# Slice history

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

- **Batch 1 (standalone blocks, 9 slices):** Lapis/Quartz glass+lamp, Super Lubricent family, Trigger Glass,
  Compressed Slime, Contact Button/Lever, Spectre Block, spectre tree set, Glowing Mushroom, Sided Redstone,
  Stained/Luminous bricks (16 colors), Bean family, Lotus, Blazing Fire, color infrastructure; later loot-table
  backfill (99 tables) and recipe backfill (45).
- **Batch 2 (tile-entity blocks, 14 slices):** Analog Emitter, Igniter, Online Detector, Item Collector,
  Adv. Redstone Repeater (bytecode-decoded scheduling), Iron Dropper, Custom Workbench, Player Interface,
  Inventory Tester, Chat Detector, Redstone Observer + Tool, Potion Vaporizer, Entity Detector + Entity
  Filter, Adv. Item Collector, Filtered Super Lubricent Platform, Notification Interface (first targeted
  packet + toast), ID Card + Global Chat Detector.
- **Batch 3 (items):** Bean Stew, Blaze and Steel, Bottle of Air, Stable Ender Pearl, Golden/Emerald Compass
  (+ Position Filter, first `SpecialRecipe`), Escape Rope, Chunk Analyzer (first item-only GUI).
- **Deferred subsystems:** Biome Radar; Sound-pattern family (Recorder/Pattern/Box/Dampeners); Imbuing
  Station + Rune Base/Dust/Pattern; Floo network (first custom entity).
- **Wiki-scoped slices (2026-09-26/28):** Eclipsed Clock + Weather Eggs + Time in a Bottle; Ender Letter/
  Mailbox; Ender Bucket(s); Summoning Pendulum, Golden Egg/Chicken, Fluid Display; Artificial End Portal;
  Ender Bridge/Anchor/Prismarine; Spectre Illuminator; Portkey (+ camo TEISR); Spectre Anchor; Redstone
  Wireless cluster (Basic/Advanced Interface, Activator, Remote); Magnetic Enchantment, Block Breaker, Block
  Destabilizer; Spectre Dimension (Key/Core/Ingot); Spectre Tools; Spectre energy network + Lens; Diaphanous
  Blocks + Light Redirector; Dyeing Machine; Spirit mob.
- **Batch 7 (coremods):** Mixin→coremod conversion; Slime Cube/Lapis Lamp spawn control; Magic Hood; Rain
  Shield; worldgen (Ancient Furnace, Peace Candle + churches, Ender Anchor chunk loading, Glowing Mushroom).
- **Config + polish (2026-09-29→10-04):** `RTConfig` toggles; HideCoordinates; Rune noise texture; Colored
  Grass completion + Grass Seeds; debug tooling; GUI audit passes; testing-pass removals; GUI polish (see
  Current state).
- **Bug-fix sagas worth knowing exist** (all in `TESTING_NOTES.md`): Mixin bootstrap investigation; Super
  Lubricent speed cap/boats; Spectre Illuminator relight (PADDING 15, lazy job queue, client-tick drain);
  TE live-sync; Lapis/Quartz Lamp mechanism flip-flop; Fluid Display render faces/lighting; Rune Base GL state;
  Special Chest `ChestType` crash; Block Breaker FakePlayer NPE; Spectre dimension registry crash;
  Time in a Bottle target sync.
