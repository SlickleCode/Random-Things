---
name: prod-jar-check
description: Verify the built mod jar on a REAL production (SRG-named) Forge server and client in prod-test/<mc-version>/. Use after any change to coremods (transformer/*.js, ClientAsmHandler/AsmHandler), reflection, registries, or rendering hooks, and before committing a branch, because dev (MCP-name) runs can pass while a release jar breaks.
---

# Production-jar check

Dev runs (`./gradlew runClient/runServer`) use MCP names; a real install uses SRG (`func_...`) names. Coremods and reflection can pass dev and still fail in a release jar. Full background: `docs/testing/README.md` ("Production-jar test").

## Setup (once per Minecraft version; untracked, gitignored `prod-test/<ver>/`)
Check `ls prod-test/<ver>` first - it normally exists (1.14.4 and 1.16.5 do). If not: install a server (`java -jar forge-<ver>-installer.jar --installServer`), a client (`client/launcher_profiles.json` = `{"profiles":{},"version":3}`, then `--installClient client`), copy `docs/testing/prod-test-scripts/{fetch-libs.py,run-client.py}` there and run `python fetch-libs.py`.

## Run
1. `source env/activate.sh` (JDK 8 - **required**; system Java 17+ dies with a `URLClassPath ... "urls" is null` NPE).
2. `./gradlew build -x test`, then copy `build/libs/RandomThings-MC<ver>-*.jar` (pick the right version; old jars may linger) into BOTH `prod-test/<ver>/mods/` and `prod-test/<ver>/client-run/mods/` (delete older RandomThings jars first).
3. Server suite, from `prod-test/<ver>/`: make sure `server.properties` is `level-name=RTServerTest`, `online-mode=true`, `level-type=flat` (it may still point at `RealGen` from the generation check - then no report appears), `rm -rf RTServerTest randomthings_tests.txt`, then
   `java -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.exit=true -jar forge-<ver>.jar nogui` and read `randomthings_tests.txt`.
4. Client suite: `rm -rf client-run/saves/RTAutoTest client-run/randomthings_*.txt`, then
   `python run-client.py -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true`; report is `client-run/randomthings_tests.txt`.
5. Rendering (suites do not draw anything): start the client with `python run-client.py -Drandomthings.debug=true`, then drive scenarios with the `scripted-playtest` skill using `SCN_RUN=<abs path>/prod-test/<ver>/client-run` (and `SCN_MC=1.16` on 1.16.5). Good set for coremod changes: `fluid_display crafting_evil_tear_portkey dyeing_machine worn_armor biome_radar rune_rendering`. `sound_dampener`/`magic_hood` need the dev client (they read `[RT-DEBUG]` lines).
6. Clean up: kill java, `rm -rf client-run/saves/Scn_* RTServerTest client-run/saves/RTAutoTest`.

## Reading results
- Expect the same pass count as the dev suites (1 environment skip). A coremod that silently did not apply shows up as a failing coremod test or a failing render scenario, not as an error - check `client-run/logs/latest.log` for `Failed to` / transformer exceptions too.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop: `jstack <pid>` shows Nashorn `initializeCoreMod#transformer(<eval>:N)` (see the `coremod-patching` skill).
