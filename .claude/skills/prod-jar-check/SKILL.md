---
name: prod-jar-check
description: Verify the built mod jar on a REAL production NeoForge 20.6.141 server and client in prod-test/1.20.6/. Use after any change to coremods (transformer/*.js, ClientAsmHandler/AsmHandler), reflection, registries, or rendering hooks, and before committing a branch, because dev runs (Mojang names for members) can pass while a release jar breaks.
---

# Production-jar check (1.20.6; body below is the 1.18.2 text, see the version note)

> **Version notes (1.20.6):** production = NeoForge 20.6.141 install in `prod-test/1.20.6/` (installer `neoforge-20.6.141-installer.jar`, JDK 21; ask before downloading). There is no SRG production variant: names are Mojang names, so this check now guards the jar packaging, `neoforge.mods.toml`, data-pack paths and coremods, not name mapping. Copy `build/libs/RandomThings-MC1.20.6-*.jar` into `prod-test/1.20.6/mods/` and `client-run/mods/`. Server: `run.sh`/`run.bat` (args file under `libraries/net/neoforged/neoforge/20.6.141/`, `-D` options in `user_jvm_args.txt`); client: `python run-client.py -D...` (`fetch-libs.py`/`run-client.py` are NeoForge-only). Baseline on the real install: server 379/0/3, client 383/0/1 + 26 render scenes.

Dev runs (`./gradlew runClient/runServer`) show Mojang names for methods and fields; a real install runs with SRG ids (`m_...`/`f_...`). Coremods and reflection can pass dev and still fail in a release jar. Background: `docs/testing/README.md` ("Production-jar test").

## Setup (once; untracked, gitignored `prod-test/1.18.2/`)
Check `ls prod-test/1.18.2` first. If missing: download `forge-1.18.2-40.3.12-installer.jar`, install a server (`java -jar <installer> --installServer`) and a client into `client/` (`client/launcher_profiles.json` = `{"profiles":{},"version":3}`, then `--installClient client`), copy `docs/testing/prod-test-scripts/{fetch-libs.py,run-client.py}` there and run `python fetch-libs.py`. Needs network access (ask the user before downloading; ~60 MB of libraries). 1.18.2 needs **JDK 17** for the installer, server and client.

## Run
1. `source env/activate.sh` (JDK 17).
2. `./gradlew build -x test`, then copy `build/libs/RandomThings-MC1.18.2-*.jar` into BOTH `prod-test/1.18.2/mods/` and `prod-test/1.18.2/client-run/mods/` (delete older RandomThings jars first).
3. Server suite, from `prod-test/1.18.2/` (`echo eula=true > eula.txt`; `server.properties`: `level-name=RTServerTest`, `online-mode=true`, `level-type=flat`); `rm -rf RTServerTest randomthings_tests.txt`; then `java -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.exit=true @user_jvm_args.txt @libraries/net/minecraftforge/forge/1.18.2-40.3.12/win_args.txt nogui` (1.18 Forge starts through the args files; the `-D` options must come BEFORE `@win_args.txt`, after it they are parsed as program options and the server dies with `D is not a recognized option`). Read `randomthings_tests.txt`.
4. Client suite: `rm -rf client-run/saves/RTAutoTest client-run/randomthings_*.txt`, then `python run-client.py -Drandomthings.debug=true -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true`; report `client-run/randomthings_tests.txt`. (`run-client.py` now also handles the 1.17+ launcher JSON: it fills `${library_directory}`/`${classpath_separator}`/`${version_name}` in the version json's JVM arguments (module path); `version_name` must be the *vanilla* version `1.18.2`, otherwise the client jar is loaded as a second module and startup dies with `Module minecraft contains package ...`.)
5. Rendering (suites draw nothing): render scenes in the production client (`-Drandomthings.autotest=true -Drandomthings.autotest.scenes=true -Drandomthings.autotest.exit=true`; screenshots land in `client-run/screenshots/randomthings_debug/scenes`) and compare with the dev client's.
6. Clean up: kill java, `rm -rf RTServerTest client-run/saves/RTAutoTest`.

## Results so far (2026-10-15)
Production server suite 349 passed / 0 failed / 2 skipped; production client suite 350 / 0 / 1 (audit clean); 26 render scenes ran without errors (armor recolour, light redirector, rain shield looked right). Installs live in the untracked `prod-test/1.18.2/` (server + `client/` + `client-run/`).

## Reading results
- Expect the same pass count as the dev suites. A coremod that silently did not apply shows up as a failing coremod test or a failing render scene, not as an error - also grep `logs/latest.log` for `Failed to` and transformer exceptions.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop (`jstack <pid>`, see `coremod-patching`).
- Things most likely to differ in production on 1.18.2: reflection by SRG id (`f_`/`m_`), the coremod name checks, `ObfuscationReflectionHelper` lookups, and dev-only tooling being class-loaded.
