# Wiki feature scope (summary)

The public wiki's 100 feature pages are this port's authoritative scope. Everything on them is ported to 1.14.4 (and 1.16.5) except:

- **Removed on request (do not re-port):** Plate family (11 blocks), Special Chest, Sakanade, Pitcher Plant, Obsidian Skull / Lava Charm / Lava Waders / Water Walking Boots, Biome Glass, Biome Stone, Inventory Rerouter.
- **Will not migrate (final):** Item Filter, Ender network / Link Orb, Blackout Powder, Collapse potion, modded-ore divining rods and all third-party mod compat, Festival system, Player Soul / Revive Circle / Rez Stone, Dungeon Chest Generator, Voxel Projector, Nature Core, `RTCommand`.

Per-feature table (1.12.2 classes, ASM yes/no, historical status labels) and the full "Not migrating" notes:
[`docs/project/wiki-feature-status.md`](docs/project/wiki-feature-status.md). Its `DONE-UNTESTED` labels are from before the user's playtest and are out of date;
actual test results are in `TESTING_CHECKLIST.md` / `docs/testing/checklist-archive/`.
