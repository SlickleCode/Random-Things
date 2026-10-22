---
name: prod-jar-check
description: Verify the built mod jar on a REAL production server and client in prod-test/<mc-version>/. Use after any change to coremods (transformer/*.js, ClientAsmHandler/AsmHandler), reflection, registries, or rendering hooks, and before committing a branch, because dev runs can pass while a release jar breaks.
---

# Production-jar check

Dev runs (`./gradlew runClient/runServer`) can differ from a real install: member names (SRG ids on most branches), module/jar packaging, data-pack paths, class loading of dev-only tooling. Coremods and reflection can pass dev and still fail in a release jar. Background: `docs/testing/README.md` ("Production-jar test").

**Everything version-specific (installer and jar names, JDK, how to start the server/client, system properties, baseline counts) is in `docs/project/version-notes.md`, section "prod-jar-check". Read it first.**

## Flow
1. Setup once, untracked and gitignored in `prod-test/<ver>/` (`ls prod-test/<ver>` first). If missing: download the loader's installer, install a server and a client, copy `docs/testing/prod-test-scripts/*` there. **Ask the user before downloading** (state file name, source, size).
2. `source env/activate.sh`, `./gradlew build -x test`, copy `build/libs/RandomThings-MC<ver>-*.jar` into BOTH the server `mods/` and `client-run/mods/` (delete older RandomThings jars first).
3. Server suite from `prod-test/<ver>/`, then the client suite via `run-client.py` (both with the `-Drandomthings.*` options in the notes); read `randomthings_tests.txt` in each.
4. Rendering (the suites draw nothing): run the render scenes in the production client and compare with the dev client's.
5. Clean up: kill java, remove the test worlds.

## Reading results
- Expect the same pass count as the dev suites. A coremod that silently did not apply shows up as a failing coremod test or render scene, not as an error - also grep `logs/latest.log` for `Failed to` and transformer exceptions.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop (`jstack <pid>`, see `coremod-patching`).
