# Repo and toolchain

_Moved from `PORTING_PLAN.md` (2026-10-11 doc refactor); the 1.18.2 section below is current for this branch. Index: [README.md](README.md)._

## 1.18.2 (this branch)

- **1.20.6 branch:** NeoForge **20.6.141**, NeoGradle 7.0.192 (`net.neoforged.gradle.userdev`), Gradle **8.13**, **JDK 21** (`~/.jdks/temurin-21*`, `RT_JDK21_HOME` overrides), Mojang names also in production (no `reobfJar`); mod jar `RandomThings-MC1.20.6`; `META-INF/neoforge.mods.toml`, `pack_format` 32. Run configs are the NeoGradle `runs {}` block in `build.gradle`; `settings.gradle` adds the NeoForged maven for plugin resolution.
- (1.18.2 pins below) Forge **1.18.2-40.3.12**, ForgeGradle 5.1.x, Gradle 7.4.2, **JDK 17** (`source env/activate.sh` finds `~/.jdks/temurin-17*`, `RT_JDK17_HOME` overrides; any other default JDK, e.g. 21, fails inside Gradle 7.4). Official Mojang mappings (`channel: 'official'`); mod jar `RandomThings-MC1.18.2`; `pack_format` 8; `loaderVersion [40,)`.
- Mapped jar for `javap`: `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.18.2-40.3.12_mapped_official_1.18.2/forge-1.18.2-40.3.12_mapped_official_1.18.2.jar` (no vanilla source jar exists). Helper scripts: `docs/migration/tools/` (`lookup.py`, `jpm.py`; the rename tools used for the port).
- Dev tooling and the autotest switches are unchanged (see below); additionally `RT_AUTOTEST_CLICKS=true` (GUI click-through) and `RT_AUTOTEST_SCENES=true` (render screenshots) on the client, `runServer` with `RT_AUTOTEST=true` for the dedicated-server suite. Latest results: `PORTING_PLAN.md`.

## Older versions (history)

- Fork: `SlickleCode/Random-Things`. Branches: `1.14.4` = all port work (default); `master` = untouched
  1.12.2-era source (`git show origin/1.12.2:<path>` for any original file); `main` = README only.
- `source env/activate.sh` (pins JDK 8) then `./gradlew build` (`-x test` for a quick check). Forgot to
  source it → Gradle dies with a Groovy `ReflectionCache` error (picked up JDK 21).
- Forge pinned to **1.14.4-28.2.26** (bumped from 28.0.49 for ModLauncher 4.x; only break was
  `ItemEntity.getEntityData()` → `Entity.getPersistentData()`). Gradle 5.6.4 is the ceiling for
  ForgeGradle 3.0.197.
- Mapped jar for `javap`: `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3/forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3-recomp.jar` (+ `-sources.jar`). `javap -c` bytecode disassembly is how every uncertain behavior gets settled.
- **Dev tooling & automated tests** (all dev-only; full docs in `docs/testing/`): enabled solely by the JVM
  property `-Drandomthings.debug=true` (`runClient` sets it; the old `[debug]` config options were removed).
  With it off nothing is registered or class-loaded (`debug/DevMode` -> `debug/DevTools`, see
  `docs/testing/isolation.md`). `RT_AUTOTEST=true [RT_AUTOTEST_NO_GUIS=true] [RT_AUTOTEST_FILTER=<text>]
  [RT_AUTOTEST_EXIT=true] ./gradlew runClient` opens the flat creative world "RTAutoTest" and runs, hands-free:
  (1) `client/ResourceAudit` -> `run/randomthings_audit.txt` (blockstates/models/textures incl. the game's own model
  parser, recipe loading + coverage, allowlist `NO_RECIPE_EXPECTED`), (2) `/rtdebug test` -> `run/randomthings_tests.txt`
  (`debug/test/BehaviorTests` machines + special recipes, `RoundTripTests` NBT/capability round trips for every tile
  entity/entity/item), (3) unless NO_GUIS, `/rtdebug allguis` screenshots to `run/screenshots/randomthings_debug/`.
  Interactive: `/rtdebug test [filter]`, `/rtdebug gui <id> [sneak]`, `/rtdebug allguis [delayTicks]`. `[RT-DEBUG]`
  lines in `run/logs/latest.log`; use `RTDebug.log(...)` for new diagnostics (no-op unless dev). Latest run
  (2026-10-07): audit 0 failures; tests 334 (all pass; 1 environment skip on each side) as of 2026-10-09; the Spectre cube lookup bug (#380) was found and fixed. The GUI pass only shows default states.
- **Knowledge base for future migrations:** `docs/migration/` (strategy, pitfall catalog, version notes, coremod
  guide, verification lessons, case studies, next-migration checklist, open-questions backlog).
- Always `runClient`-smoke-test new `@ObjectHolder` fields and coremods: a plain build won't catch them.

