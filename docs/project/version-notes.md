# Version notes (branch 1.14.4)

Facts that differ per branch. The shared skills and agents (identical on every branch, see `docs/project/branches.md`) read the section named after them first. The section texts below were moved here unchanged from this branch's former per-branch skills; where a section says it was written for another version, treat versions, jar paths and counts in it as unverified for this branch.

## mapping-lookup

_(former skill heading: Mapping and javap lookup)_

Project rule (CLAUDE.md): never write against a Minecraft/Forge API from memory - read the real jar.

### Mapped jar (dev names), per version
```
~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/<forge-version>_mapped_snapshot_<date>-<mc>/
  1.14.4: forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3-recomp.jar   (+ -sources.jar)
  1.16.5: forge-1.16.5-36.2.39_mapped_snapshot_20210309-1.16.5.jar          (use the one WITHOUT -launcher; there is no -sources next to it)
```
Locate it with `find ~/.gradle/caches/forge_gradle/minecraft_user_repo -name "*mapped_snapshot*.jar"`. Then:
- Signatures: `javap -p -cp "$J" net.minecraft.client.renderer.ItemRenderer | grep -i glint`
- Bytecode (needed for coremods): `javap -p -c -cp "$J" <class>` - read the real instruction shapes, local variable slots, constants.
- Fields/inner classes: `'net.minecraft.block.AbstractBlock$Properties'` (quote the `$`).

### SRG names (production)
- **Methods/fields -> SRG:** MCP zip, e.g. `~/.gradle/caches/forge_gradle/maven_downloader/de/oceanlabs/mcp/mcp_snapshot/<date>-<mc>/mcp_snapshot-<date>-<mc>.zip` -> `methods.csv` / `fields.csv` (`searge,name,...`). Read it with Python `zipfile` (no unzip needed). Only names that have an MCP name are in there; the rest are already `func_...` in dev too.
- **Class/obf layout, signatures with obf types:** `~/.gradle/caches/forge_gradle/mcp_repo/de/oceanlabs/mcp/mcp_config/<mc>-<date>/joined/rename/config/joined.tsrg` (class blocks `obf net/minecraft/...`, members `obf desc func_xxx`). Find a class block with `grep -n " net/minecraft/client/renderer/ItemRenderer$"` then read the lines after it.
- **Runtime reality (1.16 userdev):** methods that HAVE an MCP name show that name at dev runtime, methods without one keep `func_...`; production always has SRG. A transformer that matches by name must accept **both** (`name === "renderItem" || name === "func_229111_a_"`) and match descriptors using the `net/minecraft/...` class names.
- **Avoid hand-matching names in bytecode you inject**: put the call in a small Java static helper (e.g. `ClientAsmHandler.setRenderingStackFromSlot(entity, slot)`) so the compiler/reobf maps the method name; inject only `INVOKESTATIC` of your own class. Injecting `INVOKEVIRTUAL ... func_184582_a` crashed the dev client with `NoSuchMethodError`.

### Handy facts learned
- 1.16: `BlockState.isSolid()` (default true) decides whether a neighbour's face is culled against a block; see-through blocks need `Block.Properties.notSolid()` (the render layer is separate, `RenderLayerSetup`).
- 1.16: world biomes are data-pack instances; look names up through `((ServerWorld) world).func_241828_r().getRegistry(Registry.BIOME_KEY)`, not `ForgeRegistries.BIOMES`.
- 1.16: dimensions in NBT are registry-key strings (`"minecraft:overworld"`), 1.14 uses int ids; attribute `generic.max_health` vs `generic.maxHealth`; structure names for `/locate` are lower case on 1.16.
- Before relying on a Forge event, find who posts it in the sources jar (`HarvestDropsEvent` is never posted).
- Check 1.12.2 behaviour with `git show origin/1.12.2:<path>` before "fixing" anything that looks wrong.

## coremod-patching

_(former skill heading: Coremod patching)_

Mixins cannot be tested via `runClient` and this Forge build never shipped Mixin's runtime - the project converts them to coremods (`META-INF/coremods.json` + `transformer/*.js` + `AsmHandler`/`ClientAsmHandler`). Existing transformers are the best templates; the map of what each patches is `docs/project/coremods.md` (update it when you add one).

### Workflow
1. Find the target with the `mapping-lookup` skill; read the real bytecode (`javap -p -c`) - local variable slots, constants and call order are what you anchor on. Prefer anchoring on a unique call or constant, not an absolute instruction index.
2. Put the logic in a **static Java method** (`AsmHandler` for code that also loads on a dedicated server, `ClientAsmHandler` for anything touching client classes - a server must never load it). The transformer only inserts `INVOKESTATIC lumien/randomthings/asm/<Handler>.<method>`.
3. Transformer skeleton: `initializeCoreMod()` returns `{name: {target: {type:'CLASS', name:'net.minecraft...'}, transformer: function(classNode){...; return classNode;}}}`; add the file to `coremods.json`.
4. Match methods by **both** dev and SRG name, and by full descriptor (see `mapping-lookup`). Never inject an `INVOKEVIRTUAL` of a vanilla method by SRG name (dev runtime has the MCP name); wrap it in your Java helper.
5. Test in this order: build -> dev suite (`RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`) -> `prod-jar-check` -> a rendering scenario via `scripted-playtest` if it draws anything. Add a `CoremodTests` behaviour test for logic.

### Traps that cost real time
- **Insert-before loops must advance past the inserted node.** `insertBefore(node, ...)` puts the new instruction at index i and `node` at i+1; a `for` loop that does not `i++`/skip re-matches the same `RETURN`/`ARETURN` forever and the client **hangs at startup** right after `Developer tools enabled` (`jstack` shows `Nashorn ... initializeCoreMod#transformer(<eval>:N)`). Always `i++` after inserting (or collect targets first, then insert).
- `break` after patching one method stops the loop - use `continue` when one transformer patches several methods.
- A transformer that patches nothing fails silently; verify by observable behaviour (a test or scenario), never by "no error".
- Replacing a call by changing `owner/name/desc` of the same `MethodInsnNode` (same descriptor) is the simplest redirect and keeps the stack intact.
- Stack-neutral injections only: push args, call a `void`/matching-type static, never leave extra values.
- Client-only hooks must clean up per-call state (`setRenderingStack` ... `clearRenderingStack` before every `RETURN`) so stale state cannot leak into unrelated rendering.
- 1.16 glint: item/armor glint types use `POSITION_TEX` (colour = GL colour at draw time) and every custom `RenderType` needs its own entry in the main buffer source's `fixedBuffers`, otherwise `BufferBuilder not started` (see `TintedGlintRenderType`).

## prod-jar-check

_(former skill heading: Production-jar check)_

Dev runs (`./gradlew runClient/runServer`) use MCP names; a real install uses SRG (`func_...`) names. Coremods and reflection can pass dev and still fail in a release jar. Full background: `docs/testing/README.md` ("Production-jar test").

### Setup (once per Minecraft version; untracked, gitignored `prod-test/<ver>/`)
Check `ls prod-test/<ver>` first - it normally exists (1.14.4 and 1.16.5 do). If not: install a server (`java -jar forge-<ver>-installer.jar --installServer`), a client (`client/launcher_profiles.json` = `{"profiles":{},"version":3}`, then `--installClient client`), copy `docs/testing/prod-test-scripts/{fetch-libs.py,run-client.py}` there and run `python fetch-libs.py`.

### Run
1. `source env/activate.sh` (JDK 8 - **required**; system Java 17+ dies with a `URLClassPath ... "urls" is null` NPE).
2. `./gradlew build -x test`, then copy `build/libs/RandomThings-MC<ver>-*.jar` (pick the right version; old jars may linger) into BOTH `prod-test/<ver>/mods/` and `prod-test/<ver>/client-run/mods/` (delete older RandomThings jars first).
3. Server suite, from `prod-test/<ver>/`: make sure `server.properties` is `level-name=RTServerTest`, `online-mode=true`, `level-type=flat` (it may still point at `RealGen` from the generation check - then no report appears), `rm -rf RTServerTest randomthings_tests.txt`, then
   `java -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.exit=true -jar forge-<ver>.jar nogui` and read `randomthings_tests.txt`.
4. Client suite: `rm -rf client-run/saves/RTAutoTest client-run/randomthings_*.txt`, then
   `python run-client.py -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true`; report is `client-run/randomthings_tests.txt`.
5. Rendering (suites do not draw anything): start the client with `python run-client.py -Drandomthings.debug=true`, then drive scenarios with the `scripted-playtest` skill using `SCN_RUN=<abs path>/prod-test/<ver>/client-run` (and `SCN_MC=1.16` on 1.16.5). Good set for coremod changes: `fluid_display crafting_evil_tear_portkey dyeing_machine worn_armor biome_radar rune_rendering`. `sound_dampener`/`magic_hood` need the dev client (they read `[RT-DEBUG]` lines).
6. Clean up: kill java, `rm -rf client-run/saves/Scn_* RTServerTest client-run/saves/RTAutoTest`.

### Reading results
- Expect the same pass count as the dev suites (1 environment skip). A coremod that silently did not apply shows up as a failing coremod test or a failing render scenario, not as an error - check `client-run/logs/latest.log` for `Failed to` / transformer exceptions too.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop: `jstack <pid>` shows Nashorn `initializeCoreMod#transformer(<eval>:N)` (see the `coremod-patching` skill).

## scripted-playtest

_(former skill heading: Scripted playtest (desk.py + scn.py))_

Source of truth: `docs/testing/desktop-helper/{desk.py,scn.py,README.md}` and the scenario list in `docs/testing/README.md`. Live copies run from the untracked `prod-test/desktop/` (copy changes back to `docs/testing/desktop-helper/` and keep them identical). Windows only (ctypes + Win32).

### Running
- Start the client first (`./gradlew runClient`, or the production client - see `prod-jar-check`), wait for the title screen, then from `prod-test/desktop/`:
  `SCN_MC=1.16 python -u scn.py all` (or scenario names; `list` shows them; `new` = everything after the first four). **Always `python -u`** when redirecting output. 1.14.4 needs no `SCN_MC`. Production client: also `SCN_RUN=<abs path>/prod-test/<ver>/client-run`.
- Each run creates a fresh seed-424242 world per world kind (flat first, then the default-world scenarios), wipes between scenarios, and deletes `Scn_*` worlds at the end. Results: stdout and `prod-test/desktop/scenario_results.txt`; screenshots in `prod-test/desktop/shots/` (read them with the Read tool - they settle most doubts).
- **The user needs Minecraft in front the whole run** (~15 min for `all`). The helper pauses when focus is lost and **aborts if it happens while typing**. Ask the user for an uninterrupted window first. Stop keys: hold Ctrl+Alt+Shift, Pause, Scroll Lock, F12, or `python desk.py stop` (`resume` removes the STOP file). If the game is gone/crashed the run hangs in "paused" - check `tasklist | grep java` and `run/crash-reports/`.
- After an aborted run the game is still inside `Scn_*`: `python desk.py key escape`, `python desk.py click 426 347` (Save and Quit), then `rm -rf run/saves/Scn_*`.

### Writing a scenario (`@scenario` in scn.py)
- Everything that can be a command is a command: `g.tp`/`aim_at(g, x,y,z, frm=...)` for exact aim (reach is 5 blocks - use `frm` for far targets), `/setblock`, `/fill`, `g.slot(n, item)` (`/replaceitem` into exact hotbar slots so GUI click coordinates are constant), `dv(g, 'block x y z', 'path')` for NBT values, `block_is`/`block_isnt`/`entity_count`, `inv(g)`.
- **Negative checks must prove the command ran**: use `block_isnt` (`execute unless block` -> "Test passed"), never `not block_is(...)` (a command typed into the wrong screen "passes").
- **Every GUI open must be guarded** with `gui_open(g, w, h, 'name')` (it aborts the run on failure). Pressing Escape without a GUI opens the pause menu and later clicks hit "Save and Quit".
- GUI slot pixels: `gui_slot(w,h,sx,sy)`, `hotbar_xy(w,h,n)` (854x480 client = GUI scale 2). Commands cannot be typed while a container GUI is open - close with Escape, read state, reopen.
- Pixels: use `desk.grab('Minecraft')` + `desk.frame_px(frame,x,y)`. Thousands of `desk.pixel()` calls take minutes and look like a hang. Hide the HUD/chat with F1 while measuring.
- Measure rendering by **subtracting a reference** (same pose, plain item/armor) rather than absolute colours, and include a **control** (e.g. empty state must show nothing; the unmodified glint must differ) so a pass cannot be vacuous. After writing a visual check, **mutation-test it**: break the thing on purpose, confirm the scenario fails, restore.
- Version differences are handled in scn.py (`MC16`, `DIM` registry-string dimension tag, `generic.max_health`, world-creation clicks, `locatebiome`). Add new ones there rather than forking scenarios.
- The sound check reads `[RT-DEBUG] muted sound ...` and the Magic Hood check reads `ParticleProbe` lines (file `run/rt_probe` enables it) - dev client only.
- Add the scenario's rows to `TESTING_CHECKLIST.md` as `[verified by scripted playtest scenario <date>]` and to the scenario list in `docs/testing/README.md`.

## suites

- Normal skips: environment-dependent ones (ender mailbox unknown receiver, online detector fake player, client-only checks on the dedicated server); counts in `docs/testing/suite-baseline.txt`.
- Known flake: `coremod.spawn.peace_candle_denies_monster_spawns_in_range` flaked once on an older branch and passed on re-run.
- After a killed run delete `run/RTServerTest` and `run/saves/RTAutoTest` (a stale world hangs the next run).
