# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port (this branch, in progress) | `docs/migration/plans/1.18.2-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 8), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.18.2`, 2026-10-15) - PORT IN PROGRESS

- **Branch:** cut from `1.16.5` (`c4a6a04`) on 2026-10-15; toolchain Forge 1.18.2-40.3.12 / ForgeGradle 5.1 / Gradle 7.4.2 / JDK 17 (`source env/activate.sh` finds `~/.jdks/temurin-17*`), official Mojang mappings. Plan, milestone status and every adaptation: `docs/migration/plans/1.18.2-port-plan.md`; the rename/conversion tools: `docs/migration/tools/README.md`.
- **Compiles and `./gradlew build -x test` passes.** The dedicated server boots with the mod and the dev suite runs there: **349 passed / 0 failed / 2 skipped** (the same 2 environment skips as 1.16.5), including the Peace Candle church swap in 60 villages assembled through vanilla's own village start (29 churches, 11 of them Peace Candle).
- **Client (first boot 2026-10-15):** starts, loads the flat test world and runs the suite hands-free: resource audit 0 failures (147 blocks, 241 items, 222 recipes), dev suite 348 passed / 2 failed (the village tests, fixed afterwards on the server) / 1 skipped. One real bug found and fixed: the boat world-tick listener cast `ClientLevel` to `ServerLevel` (1.18 posts `WorldTickEvent` on the client too).
- **GUI click test (2026-10-15):** 28 GUIs, 37 clicks, 0 failures, 17 slot tooltips verified. **Render scenes:** all 26 run without errors (`run/screenshots/randomthings_debug/scenes`); compared side by side with the same scenes on 1.16.5 (blocks_0, rune_base, blazing_fire, diaphanous_near, biome_radar): identical except one regression, fixed - the Rune Base and Fluid Display renderers used the colour-less position/texture shader with a colour vertex format (runes were white); they now use `getPositionTexColorShader`. The black flat blocks in `blocks_0` are the same on 1.16.5 (not a bug). Also set shaders for the Redstone Observer line renderer (`rendertype_lines` + normals) and `RenderUtils` (colour shader), which had none - not seen on screen yet. Real bug found by the scenes: 1.18 delivers an empty block-entity update tag as null (`pkt.getTag()`), which crashed the Redstone Observer's `onDataPacket`; all 8 overrides now load an empty tag instead.
- **Not done / not verified:** how everything the scenarios do not measure LOOKS (screens' layout, the Spectre sky), no manual pass. The 1.16.5 rows of `TESTING_CHECKLIST.md` are carried over and unverified on this branch.
- **Scripted playtest on 1.18.2 (2026-10-15): all 26 scenarios pass** after adapting `scn.py` (`SCN_MC=1.18`; see skill `scripted-playtest`). It found two real bugs the suites had missed: `ItemRendererTransformer` targeted `ItemEntityRenderer` (a rename script had been applied twice; dye/glint tint never applied) and `DyeColor.getColorValue` (no 1.18 equivalent; `DyeUtil.rgb` rebuilds it) - fixed. New offline tool `docs/migration/tools/CoremodCheck.java` verifies every transformer applies.
- **Production (SRG) check done 2026-10-15** (`prod-test/1.18.2`): server suite 349/0/2, client suite 350/0/1, 26 render scenes without errors - the 15 retargeted coremods and the SRG reflection ids work in the release jar.
- Everything is uncommitted.

## What to do next

1. **Boot the client** (needs the owner's go-ahead for an uninterrupted window): `RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`; fix whatever the first boot shows (model baking, screens, entity/block-entity renderers, the tinted glint render types, Spectre sky registration).
2. Resource audit, GUI click-through, render scenes, scripted scenarios on the 1.18.2 client; (the production SRG jar check is done, see above).
3. Re-run the client suite (village tests were fixed after the client run), then the manual pass.
4. After every change: full `./gradlew build`, update the plan + this file, leave uncommitted unless told to commit.

## Where things are

| Path | Contents |
| --- | --- |
| `CLAUDE.md` | working rules for Claude sessions |
| `TESTING_CHECKLIST.md` | what the user still has to test (open rows only) |
| `WIKI_FEATURE_STATUS.md` | one-screen feature scope summary |
| `docs/project/` | project knowledge: toolchain, scope decisions, architecture, coremods, API gotchas, pitfalls, slice and change-set history, full wiki feature table ([index](docs/project/README.md)) |
| `docs/testing/` | automated tests: how to run, inventory, writing tests, isolation; `checklist-archive/` = full finished sheet; `fix-log/` = root-cause writeups per row |
| `docs/migration/` | reusable migration knowledge base (strategy, pitfalls, per-version notes, plans, open questions) |
| `docs/archive/` | superseded documents |
