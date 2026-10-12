# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port (this branch) | `docs/migration/plans/1.16.5-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 8), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Switching branches: clean the old test worlds first** (`rm -rf run/saves/RTAutoTest run/RTServerTest run/logs run/randomthings_*.txt run/screenshots/randomthings_debug`, see `CLAUDE.md`).

## Current state on this branch (`1.16.5`, 2026-10-12)

- **Port:** the 1.14.4 feature set runs on Forge 36.2.39 (MatrixStack rendering, code-registered Spectre dimension, worldgen features, coremods ported;
  plan and milestones in `docs/migration/plans/1.16.5-port-plan.md`). Everything under `docs/project/` was written for 1.14.4 and still applies except
  where the plan file says otherwise (e.g. `docs/project/api-gotchas-1.14.4.md` is 1.14.4-only).
- **Automated:** client suite 348 passed / dedicated-server suite 348 passed (1 skip each), resource audit 0 failures, GUI click-through 0 failures with
  17 slot tooltips verified (including both Sound Dampener screens), production (SRG) client and server verified earlier. See `docs/testing/`.
- **Mirrored from `1.14.4` on 2026-10-12:** Sound Dampener slot-tooltip fix + the click test that guards it, imbue effect icon fix (#388), controlled test
  world (always day, no weather/mobs), Biome Radar crystal spinning inside the block (#389), `GlintColors` + its unit test, Portkey `hasEffect`
  assertions, dev `ParticleProbe`, and the documentation layout (root files short, detail under `docs/`).
- **NOT ported to 1.16.5 yet:**
  1. **Enchant-glint colours (#390):** 1.16 draws the glint through `RenderType` glint states with vertex builders, so the 1.14.4 `ItemRenderer`/`ArmorLayer`
     coremods do not apply. `GlintColors.colorFor` (the colour logic) is in place and tested; the render hook is still to be built. Until then the Dyeing
     Machine's glint result keeps the tag but the glint stays purple on this branch.
  2. **Scripted playtest helper:** `docs/testing/desktop-helper/` drives the 1.14.4 menus (world-creation screen coordinates); the 1.16.5 create-world screen is
     different, so its `new_world()` needs re-calibration before the scenarios can run here. In-world coordinates and GUI layouts are the same.
- **Checklist:** `TESTING_CHECKLIST.md` is the 1.14.4 sheet (all rows tested there); rows are re-verified on 1.16.5 per milestone, not assumed.

## What to do next

1. Build the 1.16.5 glint hook (#390) and re-calibrate the helper's world creation; then run `python -u scn.py all` here.
2. Production-check (SRG names) the 1.16.5 jar again after any coremod change (`docs/testing/README.md`).
3. Otherwise **ask the user which area to tackle**. After every change: full `./gradlew build`, update the checklist + this file, leave uncommitted.
4. Open design questions: `docs/migration/open-questions-backlog.md`.

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
