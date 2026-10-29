# Old-world loading check (fixture world)

Checks that data written by one version of the mod is still there when the next version opens the world (`TESTING_CHECKLIST.md` P14).

```bash
RT_AUTOTEST=true RT_FIXTURE=build  ./gradlew runClient   # fresh world RTFixture, fills it, writes rt_fixture_expected.txt into the world folder, quits (saves)
RT_AUTOTEST=true RT_FIXTURE=verify ./gradlew runClient   # opens run/saves/RTFixture, compares it with rt_fixture_expected.txt, writes run/randomthings_fixture.txt, quits
bash docs/testing/fixture-chain.sh                       # the whole chain: every branch builds, the next one in the order verifies
```

To check one upgrade by hand: build on branch A, copy `A/run/saves/RTFixture` to `B/run/saves/RTFixture`, verify on B. The result is also in the game log as `[RT-DEBUG] fixture: OK/FAIL ...` lines.

## What the fixture holds (`debug/FixtureWorld.java`, same keys on every branch)

| Key | Data | Where it is saved |
|---|---|---|
| `dropper.diamonds`, `dropper.stone`, `dropper.redstone_mode` | Iron Dropper inventory (7 diamonds, 3 stone) and its mode | block entity |
| `radar.crystal_biome` | Biome Radar holding a desert Biome Crystal | block entity (item data) |
| `inbox.letters`, `inbox.sender` | an Ender Letter in a player's mailbox inbox | saved data |
| `floo.name` | a Floo fireplace named `fixture_hearth` | saved data |
| `spectre.cube_owners`, `spectre.owner` | the player's Spectre cube | saved data of the Spectre dimension |
| `anchor.registered`, `anchor.forced` | an Ender Anchor and the chunk it keeps force-loaded | saved data + forced chunks |

The area is built next to the world spawn (block entities only run in loaded, ticking chunks, and the anchor registers on its first tick).

## Results (2026-10-28)

| Upgrade | Result |
|---|---|
| 1.14.4 -> 1.16.5 | all but the Spectre cube: **1.14.4 itself never saves the Spectre handler's data** (a same-branch reload loses it too; cause not found) |
| 1.16.5 -> 1.18.2 | **the world does not open**: Minecraft's own fixer (`WorldGenSettingsHeightAndBiomeFix`) throws on the `randomthings:spectre` dimension that 1.16 stores in `level.dat`. Mod data not checked on this hop. **Decision 2026-10-28: not fixed.** The project is past these versions, so a 1.16.5 world is not carried forward to 1.18.2. |
| 1.18.2 -> 1.19.2, 1.19.2 -> 1.20.1 | all ok (after the Floo UUID fix below) |
| 1.20.1 -> 1.20.6 | **item data lost**: stack counts (7 diamonds -> 1), the crystal's biome and the letter's sender. NeoForge's inventory reader expects the 1.20.5 item format and the mod has no migration. **Decision 2026-10-28: not fixed.** Vanilla's data fixers and NeoForge do not convert mod block entity data either, most mods lose it across 1.20.5, and the project is past these versions, so a 1.20.1 world is not carried forward. Floo, Spectre, anchor ok. |
| 1.20.6 -> 1.21.1 -> 1.21.4 -> 1.21.11 | all ok |

Found and fixed with it: on 1.16.5 / 1.18.2 / 1.19.2 / 1.20.1 the Floo fireplace, the Floo brick tile entity and the Block Breaker owner read their saved UUID with `getCompound` although it is saved as an int array, so the load threw and the Floo network started empty after every restart. They now use `util/UuidTag`, which also reads the 1.14.4 form (`M`/`L` longs). On 1.20.1, 1.20.6, 1.21.1, 1.21.4 and 1.21.11 a Spirit never despawned after its lifetime (`kill()` goes through the Spirit's own damage check): now `discard()`.
