# Automated testing for Random Things (dev-only)

Full list of every test and tool: [test-inventory.md](test-inventory.md).

This project ships a small automated test setup that runs **inside the real game** (client + integrated
server). It exists because most bugs in this port were invisible to the compiler and only showed up at
runtime: registry/ObjectHolder crashes, missing render layers, models rejected by the game's parser,
NBT that does not survive a reload, gameplay hooks that silently never fire. A plain `./gradlew build`
catches none of those.

Everything described here is **developer tooling**. It is switched on by one JVM property and costs
nothing in normal play — see [isolation.md](isolation.md).

| File | What it covers |
| --- | --- |
| `README.md` (this file) | What exists, how to run it, how to read results, what it has found |
| [writing-tests.md](writing-tests.md) | How to add a behavior test / what the test API offers |
| [isolation.md](isolation.md) | How dev tooling is kept out of normal gameplay, and the performance contract |

## The three layers

All three run from one command (below). They write reports into the game directory (`run/` in dev).

1. **Resource audit** — `client/ResourceAudit.java` → `randomthings_audit.txt`
   Static checks of registered content against assets and data:
   - every block has a blockstate whose models exist (parent chains followed),
   - every item has a model (including `overrides`),
   - every referenced texture exists as a PNG and no `#variable` is left unresolved,
   - every mod model is accepted by the **game's own model parser** (`BlockModel.deserialize`) — this
     catches schema mistakes that are valid JSON but rejected at bake time (e.g. a scalar where an array
     is required),
   - the vanilla chest loot tables `LootHandler` injects into exist (a wrong name silently disables the injection),
   - every recipe file under `data/randomthings/recipes` was actually loaded by the `RecipeManager`
     (a parse failure silently drops a recipe), outputs and ingredients are non-empty,
   - every item/block is produced by at least one recipe unless it is in `NO_RECIPE_EXPECTED`
     (items that also had no recipe in 1.12.2: loot-only, creative-only, worldgen, imbuing results...).
2. **Behavior tests** — `debug/test/BehaviorTests.java` → `randomthings_tests.txt`
   Build a small contraption in a scratch area, let the server tick, assert on the outcome. Currently:
   Item Collector, Iron Dropper (pulse edges, repeat mode, into-inventory and into-air), Igniter (toggle and
   ignite modes), Analog Emitter, Redstone Observer, Block Breaker (mining + Magnetic drop redirection + redstone
   pause), the Spectre Anchor special recipe, and a generic "every shaped/shapeless data recipe really crafts its
   declared output" test. `CoremodTests` drives the real patched vanilla code paths: spawn control (Lapis Lamp,
   Peace Candle, Slime Cube), Rain Shield vs `World.isRainingAt`, the Wireless Redstone Interface lighting a lamp at
   its bound target, Spectre Anchor surviving the death-drop routine, and Super Lubricent Boots removing ground
   friction (and sneaking cancelling it).
3. **NBT / capability round-trip tests** — `debug/test/RoundTripTests.java` → same report
   One generated test per registered **tile entity type**, **entity type** and **item**:
   write to NBT → read into a fresh instance → write again → require identical NBT; item / fluid / energy
   capabilities are first filled with test content and must come back unchanged after the reload.

Optionally a fourth, manual-review layer: the **GUI pass** (`/rtdebug allguis`) opens every GUI and
screenshots it to `run/screenshots/randomthings_debug/`. It proves nothing opens-crashes; layout still
needs human eyes.

## Running it

Hands-free, from the repo root (JDK 8 pinned by `env/activate.sh`):

```bash
source env/activate.sh
RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient
```

The client boots, creates/opens the flat creative world `RTAutoTest`, runs the audit, runs the test suite,
(optionally the GUI pass) and quits. Total ≈ 2–3 minutes. Read:

- `run/randomthings_audit.txt` — first line is the summary, then `FAIL`/`WARN` lines
- `run/randomthings_tests.txt` — first line is the summary, then `FAIL`/`SKIP` lines (passes are only counted)
- `run/logs/latest.log` — `[RT-DEBUG]` lines, plus any game-side warnings (grep for `Unable to load model`,
  `Parsing error loading recipe`, `Exception`)

| Environment variable | Effect |
| --- | --- |
| `RT_AUTOTEST=true` | Enables the hands-free run (world open → audit → tests → ...) |
| `RT_AUTOTEST_NO_GUIS=true` | Skip the GUI screenshot pass at the end (recommended for quick runs) |
| `RT_AUTOTEST_FILTER=<text>` | Only run tests whose name contains the text (case-insensitive), e.g. `block_breaker`, `nbt.item`, `iron_dropper` |
| `RT_AUTOTEST_EXIT=true` | Quit the client when done |
| `RT_AUTOTEST_SCENES=true` | Render screenshot pass (`debug/RenderScenes` + `client/SceneShots`): builds each scene (`/rtdebug scene <id>`), saves HUD-less screenshots to `run/screenshots/randomthings_debug/scenes/<id>.png`, then pages through the Random Things creative tab (`items_N.png`). Review the images by eye. |
| `RT_AUTOTEST_SCENES_ONLY=id,id` | With `_SCENES`: only these scene ids (skips the creative-tab pages) |
| `RT_AUTOTEST_CLICKS=true` | GUI click-through (`client/GuiClickTest`): opens every GUI, hovers and clicks every button through `mouseClicked`, compares server-side block/item NBT before and after, types into every text field and reopens the GUI to check the text was saved. Report: `run/randomthings_clicks.txt`. Slots are not exercised yet. |

The audit always runs; the filter only applies to the in-game tests.

### Production-jar test (coremod / reflection names)

The dev environment uses MCP names, a real install uses SRG names, so coremods and reflection can pass `runClient`/`runServer` and
still break in a release jar (backlog Q13). To check: `./gradlew build`, then in the untracked `prod-test/<mc-version>/` folder
(gitignored) install a real server once (`java -jar forge-<ver>-installer.jar --installServer`), copy `build/libs/*.jar` into its
`mods/`, put `eula=true` and `server.properties` (`level-name=RTServerTest`, `online-mode=true`, `level-type=flat`) there, and run
`java -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.exit=true -jar forge-<ver>.jar nogui`; the report is
`randomthings_tests.txt` in that folder. Delete its `RTServerTest` world when switching versions.

#### Production client (render coremods)

The client-only coremods (dye on items/armor, rain suppression, light redirector) need a production *client*. Once per version, in
`prod-test/<ver>/`: `java -jar forge-<ver>-installer.jar --installClient client` (after creating `client/launcher_profiles.json` with
`{"profiles":{},"version":3}`), copy `docs/testing/prod-test-scripts/{fetch-libs.py,run-client.py}` next to `client/` (they work for any version), run `python fetch-libs.py` (downloads the vanilla libraries and Windows natives
the installer skips, sha1-checked), put the built jar in `client-run/mods/`, then
`python run-client.py -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true` (suite) or
`-Drandomthings.autotest.scenes=true` (screenshots in `client-run/screenshots/randomthings_debug/scenes/`). Put `pauseOnLostFocus:false` in `client-run/options.txt` or the game pauses when its window is not focused and the screenshots show the pause menu. Needs the Gradle caches (assets,
vanilla `version.json`) from having built the project once.

#### Real-world generation check

`docs/testing/prod-test-scripts/realgen.py` (copy it into `prod-test/<ver>/` next to the installed server, mod jar in `mods/`) starts the production server on a real default overworld,
force-loads ~1,700 chunks, saves and counts region-file chunks that contain a Glowing Mushroom. It caught that new 1.16.5 worlds never generated mushrooms (0 chunks) while the
in-test cave and terrain checks first looked fine; use it after touching natural generation.

### Switching branches

`run/` is gitignored and shared by every branch. Before running the tests after switching to a different
Minecraft version branch, delete the old test worlds and reports (`rm -rf run/saves/RTAutoTest run/RTServerTest run/logs
run/randomthings_*.txt run/screenshots/randomthings_debug`); a world saved by another version hangs the client or stops the
dedicated server at a confirmation prompt, and stale reports/screenshots can be mistaken for the new run's.

### Interactive use

With the dev switch on (`runClient` does this) and operator permission, in the `RTAutoTest` flat world:

```
/rtdebug test                 run everything
/rtdebug test iron_dropper    run tests whose name contains "iron_dropper"
/rtdebug gui <id> [sneak]     open any block/item GUI
/rtdebug allguis [delay]      open every GUI in turn (screenshots if enabled)
```

The test command **refuses to run outside a flat world** because it wipes a build area between tests.
The build area is a 15×9×15 box at Y=100, 14 blocks east of the player; nothing else is touched.

### Using it on a real (launcher) install

(`RT_NO_DEBUG=true ./gradlew runClient` does the opposite: launches the dev client with the switch off.)

Add `-Drandomthings.debug=true` (and `-Drandomthings.autotest=true` for the hands-free run) to the JVM
arguments. Without that property none of this code is registered or loaded.

### Running the whole suite on a dedicated server (about 1 minute)

```bash
source env/activate.sh
RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer      # RT_AUTOTEST_FILTER=<text> narrows it
cat run/randomthings_tests.txt
```

A fake player starts the suite after 100 ticks (`debug/test/ServerAutoTest.java`) and the server stops itself. The gradle
task switches `run/server.properties` to a flat world named `RTServerTest` for the run and restores the file afterwards.
The test area's chunks are force-loaded on purpose: **a dedicated-server world with no real player and no forced chunk stops
ticking entities and tile entities after 300 ticks**, which silently starves long tests (the Block Destabilizer test
looked like a mod bug until this was found). The resource audit and the GUI pass are client-only and do not run here.

### Dedicated-server smoke test (no GUI, about 30 s)

```bash
source env/activate.sh
./gradlew runServer     # wait for: Done (...)! For help, type "help", then stop it with Ctrl+C
```

Run this after any change that adds a class referencing `net.minecraft.client`/`net.minecraftforge.client` or a
client-only event. It loads the mod without the dev switch (a normal server). It only proves the mod *boots* on a server;
it does not play anything. Already caught: the mod crashing every dedicated server (see the table below).

## Reading results — conventions

- Test names are `area.subject.what_it_proves`, lowercase with dots/underscores, so filters work:
  `behavior.block_breaker.mines_front_block_into_rear_inventory`, `nbt.tile_entity.iron_dropper`,
  `nbt.item.ender_bucket`.
- `SKIP` means "not applicable" (e.g. a tile entity type with no block, an entity that cannot be created
  without context) and is reported separately from failures. Investigate skips occasionally; they can hide
  a registration gap.
- A failure message always names the first failing assertion with expected vs actual values.

## What the setup has already found (keep this list growing)

| Finding | Found by | Class of problem |
| --- | --- | --- |
| 64 recipes (16 colors × Stained Brick / Luminous Stained Brick / Luminous Block / Translucent Luminous Block) existed in 1.12.2 but were missing from the port | resource audit (recipe coverage) | Content silently dropped during migration |
| Fluid Display inventory item rendered as a flat square, not an isometric block | user playtest, then fixed | Wrong item model parent |
| A fix for that (`display.scale` as a number) made the model fail to load: 1.14.4 requires an array | in-game log `Unable to load model`; the audit now parses every mod model with the game's own parser | Valid JSON, invalid schema |
| **Magnetic Enchantment did nothing and the Block Breaker's drops fell on the ground** — `BlockEvent.HarvestDropsEvent` is never fired in this Forge build, so the port's hook was dead code | behavior test `block_breaker.mines_front_block_into_rear_inventory` | Event that exists in the API but is never posted |
| Fluid Display tile entity NBT changed on a save/load cycle (`{}` became an explicit empty stack) | NBT round-trip test | Non-idempotent serialization |
| **Redstone Interface never refreshed when redstone next to it changed** - it listened to `NeighborNotifyEvent` (position = the changed block) and compared with its own position | behavior test `coremod.redstone_interface...` | Event semantics misread |
| **The mod crashed every dedicated server** - the main class, a client-only event listener and two entities referenced client-only classes; the client never shows it | dedicated-server smoke test (`./gradlew runServer`) | Client/server dist leak |

| **Imbuing Station consumed its inputs but never produced anything** - it delivered the result with `insertItem` into a slot that rejects insertion | behavior test `imbuing_station.brews_mossy_cobblestone...` (found while adding tests on the dedicated server) | Validated handler vs machine output |
| The Peace Candle baseline test was flaky on the client once more tests ran before it (light engine not settled after 8 ticks) | repeated full runs | Test timing; wait raised to 30 ticks |
| **Spectre cubes after the first were never recognised as the owner's**, so the anti-trespass check kept pinning players to their spawn tile | behavior test `behavior.spectre.*` | Wrong coordinate space in a lookup (a previous 'fix' of the original was itself wrong) |

## Notes for 1.16.x (added on the 1.16.5 branch)

- The dedicated-server suite uses `RTTestPlayer` (a `FakePlayer` that reports its real position and gets a real detached network handler); plain `FakePlayer` cannot be teleported or located.
- The client autotest answers the "experimental settings" / backup dialogs itself and logs them (`AutoTest: confirming ... dialog`).
- The world is created with the 1.16 `createWorld` flow (flat generator); an existing `run/saves/RTAutoTest` is simply reopened.

## Known limits

- Not covered: rendering correctness, sounds, particles, multiplayer sync, GUI feel. The screenshot pass only
  proves GUIs open.
- Tests assert behavior the author thought of. They are excellent at regressions and weak at unknown unknowns.
- Coremod effects are only exercised where a test drives the patched code path (e.g. Block Breaker touches
  harvest paths). Add a behavior test per coremod-backed feature when practical.
- The behavior suite covers a representative subset of machines. Adding coverage is cheap — see
  [writing-tests.md](writing-tests.md).
