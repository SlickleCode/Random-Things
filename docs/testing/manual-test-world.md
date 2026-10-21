# Manual test world (dev client)

For working through `TESTING_CHECKLIST.md` by hand: a normal-terrain creative world with every mod block on show, a few working set-ups, chests with every mod item and a stocked inventory.

```bash
source env/activate.sh
RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient      # RT_AUTOTEST just switches the helper on; no tests run
```

The client creates (first run) or reopens (later runs) the world `RTManual` - seed **424242**, default world type, structures on, creative - and runs `/rtdebug manual` itself. Run `/rtdebug manual` again at any place in any dev world (needs permission level 2) to rebuild the showroom around you; delete `run/saves/RTManual` to start over. Sound stays on.

## What `/rtdebug manual` builds (`debug/ManualTestWorld.java`)
A smooth-stone sky platform 96 x 70, 30 blocks above the surface next to the player (x east, z south), air cleared above it. You are put on its south side, creative, daytime, clear weather.
- **North half - museum:** every mod block, 14 per row, each with an oak sign carrying its registry name (blocks are placed without neighbour updates, so torches/plants that cannot survive stay visible).
- **South edge - set-ups** (signs say what to do): Biome Radar with its iron-bar antenna and a redstone block beside it, two Fluid Displays (one between sand/stone/glass), Iron Dropper, Item Collector next to a chest, an iron-block pyramid with a beacon for the Spectre Lens.
- **South edge, east end - item chests:** one chest per 27 mod items, every mod item once.
- **Inventory:** hotbar = Biome Crystals (desert, snowy plains, jungle, badlands, mushroom fields), paper, Portkey, water and lava bucket; then Ender/Reinforced Ender Bucket, Redstone Remote and Tool, Position Filters, ID Card, Entity Filter, Time in a Bottle, Magic Hood, Super Lubricent Boots, the Spectre tools/key/anchor/charger, Ender Letters, Sound Recorder/Patterns, Summoning Pendulum, compasses, Floo Powder, golden and weather eggs, two water bottles, imbuing ingredients (cobblestone, vine, bone meal, coal, flint, blaze powder, nether star), iron armor, an enchanted helmet, a Magnetic pickaxe, all 16 dyes, iron, redstone blocks, levers, iron bars, stone.
- This branch has no biome locate command, so the chat does not print distances; the Biome Radar itself finds the biomes (the snowy biome is `snowy_tundra` here). Seed 424242 differs per Minecraft version, so the distances differ too. The Biome Radar search goes to the real nearest biome, so this world is the test for "a distant biome in a non-superflat world".

## Rows this world is meant for
Everything in `TESTING_CHECKLIST.md` that needs blocks or items: right-click interactions, buckets, item data, Spectre tools/Lens, Magic Hood and boots, imbue bottles (craft in the Imbuing Station, ingredients are in the inventory), eggs and dispensers (place a dispenser), Magnetic, creative tab and tooltips, loot chests (needs real structures - fly to a village/dungeon), glass-like blocks and rendering (museum), worldgen rows (explore the normal terrain), Ancient Furnace biome change (place it in a snowy area).
