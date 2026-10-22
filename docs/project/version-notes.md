# Version notes (branch 1.20.6)

Facts that differ per branch. The shared skills and agents (identical on every branch, see `docs/project/branches.md`) read the section named after them first. The section texts below were moved here unchanged from this branch's former per-branch skills; where a section says it was written for another version, treat versions, jar paths and counts in it as unverified for this branch.

## mapping-lookup

_(former skill heading: Mapping and javap lookup (1.18.2: official Mojang names))_

> **Version notes (1.20.6, NeoForge 20.6.141, JDK 21):** this body was written on 1.18.2. On 1.20.6 production uses Mojang names too, so there are no SRG ids and `lookup.py` is not needed; never write `m_`/`f_` ids. Mapped classes for `javap`: `build/neoForm/neoFormJoined1.20.6-*/steps/packRecomp/output.jar`; decompiled sources to grep: `.../steps/patchUserDev/output.jar`; NeoForge sources: `neoforge-20.6.141-sources.jar` in the Gradle cache (`docs/project/api-gotchas-1.20.6.md`, "Toolchain"). Item data lives in data components (`util/StackTag`), registries only in `RegisterEvent`.

Project rule (CLAUDE.md): never write against a Minecraft/Forge API from memory - read the real jar.

### Naming on this branch
- Source, dev runtime and class names use **official Mojang names** (`net.minecraft.world.level.Level`, `BlockEntity`, `getBlockEntity`). Class names are the same in production; **methods and fields are SRG ids in production** (`m_46758_`, `f_58650_`).
- A transformer or reflection call that matches a member by name must accept the Mojang name **and** the SRG id (`name === "isRainingAt" || name === "m_46758_"`), and match descriptors with the `net/minecraft/...` Mojang class names. `ObfuscationReflectionHelper.findField/findMethod` takes the SRG id only.
- Avoid hand-matching names in injected bytecode: put the call in a Java static helper (`ClientAsmHandler`/`AsmHandler`) so the compiler/reobf maps the name; inject only `INVOKESTATIC` of your own class.

### Mapped jar (for javap)
```
~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.18.2-40.3.12_mapped_official_1.18.2/forge-1.18.2-40.3.12_mapped_official_1.18.2.jar
```
- Signatures: `javap -p -cp "$J" net.minecraft.client.renderer.ItemRenderer | grep -i render`; quote `$` inner classes (`'net.minecraft.core.Holder$Reference'`).
- Bytecode (coremods): `javap -p -c`. The branch helper `docs/migration/tools/jpm.py <class> <method> [regex]` prints one method's bytecode (optionally only matching lines) - use it instead of dumping whole classes.
- There is **no vanilla source** (the `-sources.jar` next to Forge only holds Forge's own classes); bytecode is the ground truth.

### SRG ids
- `python docs/migration/tools/lookup.py net.minecraft.world.level.Level.isRainingAt <more...>` prints the `m_`/`f_` id for a Mojang class+member (needs `client_mappings.txt` and `mcp_mappings.tsrg` in `~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.2/`, present after the first build). Methods are matched by obf descriptor, so overloads resolve to one id; fields by name.
- Mojang and SRG ids are stable for a member across 1.16 -> 1.18 only when vanilla did not refactor it; always re-check in the jar.

### Handy facts learned (1.18.2)
- `Biome` lookups return `Holder<Biome>` (`.value()`); world biomes are data-pack instances, resolve names through `level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)`.
- `DimensionType`'s constructor is private: the Spectre type is `data/randomthings/dimension_type/spectre.json`. Registries are frozen once the server starts: swap `Holder.Reference.value` (SRG `f_205752_`) by reflection to replace an entry (Peace Candle pool).
- Empty block-entity update tags arrive as `null` in `ClientboundBlockEntityDataPacket.getTag()`; `BlockEntity.getUpdateTag()` default is empty (override it to `saveWithoutMetadata()` for synced data).
- `TickEvent.WorldTickEvent` fires for client levels too: guard `event.world.isClientSide` before casting to `ServerLevel`.
- Shaders: a vertex format needs the matching shader (`POSITION_TEX_COLOR` -> `getPositionTexColorShader`), otherwise colours are silently ignored.
- Generator steps are futures and `StructureFeatureManager`/`Blender` query chunks 7-8 chunks out; a hand-driven generation test needs a wide region and a no-structures manager (see `WorldgenTests`). Use `ConfiguredStructureFeature.generate` for real village starts.
- Before relying on a Forge event, find who posts it; check 1.12.2 behaviour with `git show origin/1.12.2:<path>` before "fixing" anything that looks wrong.

## coremod-patching

_(former skill heading: Coremod patching (Forge 40.3.12 text; see version note, JS coremods))_

> **Version notes (1.20.6):** JS coremods still work unchanged in the NeoForge jar (14 transformers, Mojang names in dev AND production, so match the Mojang name only; no SRG ids). A transformer whose anchor vanished silently does nothing: on 1.20.6 three did (ItemRenderer `renderQuadList` anchor now follows `INVOKESTATIC alpha` after `ILOAD 11`; ArmorLayer patches the private `renderModel`; FireBlock `checkBurnOut`). Re-derive each anchor from `steps/packRecomp/output.jar` and confirm with the coremod tests and the render scenes. Registration, JS sandbox rules and the 1.18 glint notes below still apply.

Mixins cannot be tested via `runClient` in this setup - the project uses coremods (`META-INF/coremods.json` + `transformer/*.js` + `AsmHandler`/`ClientAsmHandler`). Existing transformers are the best templates; `docs/project/coremods.md` maps what each patches (update it when you add one; note it was written for 1.14.4 and the 1.18.2 retargets are listed in `docs/migration/plans/1.18.2-port-plan.md`).

### Workflow
1. Find the target with the `mapping-lookup` skill; read the real bytecode (`python docs/migration/tools/jpm.py <class> <method> "<regex>"`) - local variable slots, constants and call order are what you anchor on. Prefer a unique call or constant, not an instruction index. **Re-read the anchor on this version**: 1.18.2 moved things (e.g. `VertexLighterFlat` colour array is local 4, `HumanoidArmorLayer.renderModel` is `invokevirtual`, biome lookups return `Holder`).
2. Put the logic in a **static Java method** (`AsmHandler` for code that also loads on a dedicated server, `ClientAsmHandler` for client classes). The transformer only inserts `INVOKESTATIC lumien/randomthings/asm/<Handler>.<method>`; descriptors use the Mojang `net/minecraft/...` names of your handler's signature.
3. Skeleton: `initializeCoreMod()` returns `{name: {target: {type:'CLASS', name:'net.minecraft.world.level.Level'}, transformer: function(classNode){...; return classNode;}}}`; add the file to `coremods.json`.
4. Match methods by **both** the Mojang name and the SRG id (`m_...`) and by full descriptor. Never inject an `INVOKEVIRTUAL` of a vanilla method by name; wrap it in your Java helper.
5. **Run `docs/migration/tools/CoremodCheck.java` first** (offline; every transformer must report `ok`: a wrong target class or a non-matching anchor otherwise fails silently - see Traps). Then test in this order: build -> dev suite (`RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`) -> `prod-jar-check` -> a rendering scenario if it draws anything. Add a `CoremodTests` behaviour test for logic.

### Traps that cost real time
- **A transformer whose target class name is wrong applies to nothing and no suite notices** (1.18.2: a rename script run twice turned `ItemRenderer` into `ItemEntityRenderer`; found only by the scripted dyeing-machine scenario).
- **Insert-before loops must advance past the inserted node** (`i++` after `insertBefore`, or collect targets first), otherwise the client hangs at startup after `Developer tools enabled` (`jstack` shows Nashorn `initializeCoreMod#transformer`).
- `break` after patching one method stops the loop - use `continue` when one transformer patches several methods.
- A transformer that patches nothing fails silently; verify by observable behaviour, never by "no error".
- Redirecting a call by changing `owner/name/desc` of the same `MethodInsnNode` keeps the stack intact and is the simplest form.
- Stack-neutral injections only. Client-only hooks must clean up per-call state (`setRenderingStack` ... `clearRenderingStack` before every `RETURN`).
- Since 1.18.2 the JS sandbox blocks `Java.type('java.lang.System')`; use only the ASM/coremod API classes (`print(...)` for diagnostics).
- 1.18 glint: the glint `RenderType`s are shader-based (colour via `RenderSystem.setShaderColor` at draw time); every custom `RenderType` still needs its own `fixedBuffers` entry or you get `BufferBuilder not started` (see `TintedGlintRenderType`). Verified on 1.18.2 by the dyeing-machine scenario (pixel measurement of the tinted glint).

## prod-jar-check

_(former skill heading: Production-jar check (1.20.6; body below is the 1.18.2 text, see the version note))_

> **Version notes (1.20.6):** production = NeoForge 20.6.141 install in `prod-test/1.20.6/` (installer `neoforge-20.6.141-installer.jar`, JDK 21; ask before downloading). There is no SRG production variant: names are Mojang names, so this check now guards the jar packaging, `neoforge.mods.toml`, data-pack paths and coremods, not name mapping. Copy `build/libs/RandomThings-MC1.20.6-*.jar` into `prod-test/1.20.6/mods/` and `client-run/mods/`. Server: `run.sh`/`run.bat` (args file under `libraries/net/neoforged/neoforge/20.6.141/`, `-D` options in `user_jvm_args.txt`); client: `python run-client.py -D...` (`fetch-libs.py`/`run-client.py` are NeoForge-only). Baseline on the real install: server 379/0/3, client 383/0/1 + 26 render scenes.

Dev runs (`./gradlew runClient/runServer`) show Mojang names for methods and fields; a real install runs with SRG ids (`m_...`/`f_...`). Coremods and reflection can pass dev and still fail in a release jar. Background: `docs/testing/README.md` ("Production-jar test").

### Setup (once; untracked, gitignored `prod-test/1.18.2/`)
Check `ls prod-test/1.18.2` first. If missing: download `forge-1.18.2-40.3.12-installer.jar`, install a server (`java -jar <installer> --installServer`) and a client into `client/` (`client/launcher_profiles.json` = `{"profiles":{},"version":3}`, then `--installClient client`), copy `docs/testing/prod-test-scripts/{fetch-libs.py,run-client.py}` there and run `python fetch-libs.py`. Needs network access (ask the user before downloading; ~60 MB of libraries). 1.18.2 needs **JDK 17** for the installer, server and client.

### Run
1. `source env/activate.sh` (JDK 17).
2. `./gradlew build -x test`, then copy `build/libs/RandomThings-MC1.18.2-*.jar` into BOTH `prod-test/1.18.2/mods/` and `prod-test/1.18.2/client-run/mods/` (delete older RandomThings jars first).
3. Server suite, from `prod-test/1.18.2/` (`echo eula=true > eula.txt`; `server.properties`: `level-name=RTServerTest`, `online-mode=true`, `level-type=flat`); `rm -rf RTServerTest randomthings_tests.txt`; then `java -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.exit=true @user_jvm_args.txt @libraries/net/minecraftforge/forge/1.18.2-40.3.12/win_args.txt nogui` (1.18 Forge starts through the args files; the `-D` options must come BEFORE `@win_args.txt`, after it they are parsed as program options and the server dies with `D is not a recognized option`). Read `randomthings_tests.txt`.
4. Client suite: `rm -rf client-run/saves/RTAutoTest client-run/randomthings_*.txt`, then `python run-client.py -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true`; report `client-run/randomthings_tests.txt`. (`run-client.py` now also handles the 1.17+ launcher JSON: it fills `${library_directory}`/`${classpath_separator}`/`${version_name}` in the version json's JVM arguments (module path); `version_name` must be the *vanilla* version `1.18.2`, otherwise the client jar is loaded as a second module and startup dies with `Module minecraft contains package ...`.)
5. Rendering (suites draw nothing): render scenes in the production client (`-Drandomthings.autotest=true -Drandomthings.autotest.scenes=true -Drandomthings.autotest.exit=true`; screenshots land in `client-run/screenshots/randomthings_debug/scenes`) and compare with the dev client's.
6. Clean up: kill java, `rm -rf RTServerTest client-run/saves/RTAutoTest`.

### Results so far (2026-10-15)
Production server suite 349 passed / 0 failed / 2 skipped; production client suite 350 / 0 / 1 (audit clean); 26 render scenes ran without errors (armor recolour, light redirector, rain shield looked right). Installs live in the untracked `prod-test/1.18.2/` (server + `client/` + `client-run/`).

### Reading results
- Expect the same pass count as the dev suites. A coremod that silently did not apply shows up as a failing coremod test or a failing render scene, not as an error - also grep `logs/latest.log` for `Failed to` and transformer exceptions.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop (`jstack <pid>`, see `coremod-patching`).
- Things most likely to differ in production on 1.18.2: reflection by SRG id (`f_`/`m_`), the coremod name checks, `ObfuscationReflectionHelper` lookups, and dev-only tooling being class-loaded.

## scripted-playtest

_(former skill heading: Scripted playtest (desk.py + scn.py))_

> **Version notes (1.20.6):** `SCN_MC=1.20.6` selects the 1.20.6 helper paths (`sdata`/`stack_nbt` for data-component commands, `EFFECTS_PATH`, `FLUID_WATER`, MC205 flag; pause-menu Save and Quit at y=383; chat commands are limited to 256 chars). All 28 scenarios pass on the dev client and the real install. Needs an unlocked desktop with Minecraft in front; ask for an uninterrupted window first.

Source of truth: `docs/testing/desktop-helper/{desk.py,scn.py,README.md}` and the scenario list in `docs/testing/README.md`. Live copies run from the untracked `prod-test/desktop/` (copy changes back to `docs/testing/desktop-helper/` and keep them identical). Windows only (ctypes + Win32).

### 1.18.2 status: adapted, all 26 scenarios pass (2026-10-15, dev client)
`SCN_MC=1.18` sets `MC18` (and, like 1.16, `MC16`: registry-string dimensions, `generic.max_health`, `/locatebiome`). What differs from 1.16 and is handled in scn.py:
- **`/replaceitem` no longer exists** (removed in 1.17): `item_cmd(slot, item)` emits `/item replace entity @s <slot> with <item>`.
- **Superflat ground is at y -61** (world bottom -64) while every scenario assumes ground at y 3: `controlled_setup` lays a 180x180 grass floor at y 3 in flat worlds.
- **Biome name** `minecraft:snowy_plains` (was `snowy_tundra`).
- Menus (title, world list, create-world, More World Options) are pixel-identical to 1.16, so the click coordinates did not change. The window title is `Minecraft* 1.18.2` (the helper matches on "Minecraft").
- Window runs: start the dev client first (`./gradlew runClient` without autotest flags), wait for the title screen; start a fresh client when the mod code changed.
What the playtest found on 1.18.2 that no suite had: the Item renderer coremod targeted the wrong class (dye/glint tint never applied), dye colour values (`DyeColor.getColorValue` has no 1.18 equivalent, see `DyeUtil.rgb`), and the particle probe's reflection filter.

### Running
- Start the client first (`./gradlew runClient`, or the production client - see `prod-jar-check`), wait for the title screen, then from `prod-test/desktop/`: `SCN_MC=<ver> python -u scn.py all` (or scenario names; `list` shows them). **Always `python -u`** when redirecting output. Production client: also `SCN_RUN=<abs path>/prod-test/<ver>/client-run`.
- Each run creates a fresh seed-424242 world per world kind, wipes between scenarios and deletes `Scn_*` worlds at the end. Results: stdout and `prod-test/desktop/scenario_results.txt`; screenshots in `prod-test/desktop/shots/` (read them with the Read tool).
- **The user needs Minecraft in front the whole run** (~15 min for `all`). The helper pauses when focus is lost and **aborts if it happens while typing**. Ask for an uninterrupted window first. Stop keys: Ctrl+Alt+Shift, Pause, Scroll Lock, F12, or `python desk.py stop` (`resume` removes the STOP file). If the game crashed the run hangs in "paused" - check `tasklist | grep java` and `run/crash-reports/`.
- After an aborted run the game is still inside `Scn_*`: `python desk.py key escape`, `python desk.py click 426 347` (Save and Quit), then `rm -rf run/saves/Scn_*`.

### Writing a scenario (`@scenario` in scn.py)
- Everything that can be a command is a command: `g.tp`/`aim_at(g, x,y,z, frm=...)`, `/setblock`, `/fill`, `g.slot(n, item)` into exact hotbar slots, `dv(g, 'block x y z', 'path')` for NBT values, `block_is`/`block_isnt`/`entity_count`, `inv(g)`.
- **Negative checks must prove the command ran**: `block_isnt`, never `not block_is(...)`.
- **Every GUI open must be guarded** with `gui_open(g, w, h, 'name')`; Escape without a GUI opens the pause menu and later clicks hit "Save and Quit".
- GUI slot pixels: `gui_slot(w,h,sx,sy)`, `hotbar_xy(w,h,n)` (854x480 client = GUI scale 2). Commands cannot be typed while a container GUI is open.
- Pixels: `desk.grab('Minecraft')` + `desk.frame_px(frame,x,y)`; thousands of `desk.pixel()` calls look like a hang. Hide the HUD with F1 while measuring.
- Measure rendering by **subtracting a reference** and include a **control**; **mutation-test** every visual check (break the thing, confirm the scenario fails, restore).
- Version differences go into scn.py flags, not forked scenarios.
- The sound check reads `[RT-DEBUG] muted sound ...` and the Magic Hood check reads `ParticleProbe` lines - dev client only.
- Add the scenario's rows to `TESTING_CHECKLIST.md` as `[verified by scripted playtest scenario <date>]` and to `docs/testing/README.md`.

## suites

- Server: `run/server.properties` needs `max-tick-time=-1` or the watchdog kills the long village test (60 s).
- Normal skips: 3 (ender mailbox unknown receiver, online detector fake player, and the client-only ancient-furnace check on the dedicated server). Baseline `docs/testing/suite-baseline.txt`.
- Known flakes: `coremod.spawn.peace_candle_denies_monster_spawns_in_range` flaked once (passed on re-run); the Portkey test failed once after a killed run left a stale world, not reproducible in six more runs. After a killed run delete `run/RTServerTest` and `run/saves/RTAutoTest`.
- Gradle needs the network for its first NeoForm download: `--offline` fails until the caches exist (`suite-all.sh` retries online).
- Item data pitfall: `StackTag.get/getOrCreate` return COPIES; write with `StackTag.edit`/`set` (an empty tag removes the component). Three tests (`behavior.stack_data.*`) guard this.
