# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port (complete) | `docs/migration/plans/1.18.2-port-plan.md` |
| `1.19.2` | Forge 43.5.2 port, IN PROGRESS (M0-M2 done; server suite 375/0/3; client suite 377/0/1; GUI click test and production (SRG) server/client suites pass) | `docs/migration/plans/1.19.2-port-plan.md` |
| `1.20.1` | Forge 47.4.28 port (complete apart from the signed-chat check) | `docs/migration/plans/1.20.1-port-plan.md` |
| `1.20.6` | NeoForge 20.6.141 port (complete; JDK 21, NeoGradle 7, Gradle 8.13, Mojang names also in production) | `docs/migration/plans/1.20.6-port-plan.md`, `docs/project/api-gotchas-1.20.6.md` |
| `1.21.1` | NeoForge 21.1.264 port (complete; same toolchain as 1.20.6) | `docs/migration/plans/1.21.1-port-plan.md`, `docs/project/api-gotchas-1.21.1.md` |
| `1.21.4` | NeoForge 21.4.161 port (this branch, complete: compiles, server suite 424/0/4 + normal pass 1/0/0, client suite 383/0/2, audit clean, GUI click test 28 GUIs/0 failures, render hook checks 5/5; real NeoForge 21.4.161 install passes server 381/0/4 and client 383/0/2; all 28 scripted scenarios pass on the dev client) | `docs/migration/plans/1.21.4-port-plan.md`, `docs/project/api-gotchas-1.21.4.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 21 on this branch; JDK 17 on 1.18.2-1.20.1, JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.21.1`, 2026-10-22)

- **Branch:** cut from `1.20.6` (`b0291e3`); NeoForge 21.1.264 / NeoGradle 7.0.192 / Gradle 8.13 / JDK 21, Mojang names everywhere. Plan: `docs/migration/plans/1.21.1-port-plan.md`; every API change: `docs/project/api-gotchas-1.21.1.md`.
- **Automated (dev, 2026-10-22):** dedicated-server suite 380 passed / 0 failed / 4 skipped (of 384; the 4th is the real-terrain test that needs a normal world) plus a normal-world pass 1 / 0 / 0; client suite 383 / 0 / 1, resource audit 147 blocks / 241 items / 222 recipes, 0 failures; GUI click test 28 GUIs / 37 clicks / 0 failures; 26 render scenes (checked by eye: dyed armor and items, light redirector, rain shield, blazing fire, magic hood); the manual world (`RT_MANUAL=true`) builds and prints the biome distances.
- **What changed:** see the gotchas file. Main items: `ResourceLocation` factories, the vertex-builder rework, data-driven Magnetic enchantment (config toggle now only disables the effect), plantable API replaced by `canSustainPlant(..., plantState)` + `TriState`, `DimensionTransition` for Spectre/End portal teleports, `CraftingInput` recipes, singular data folders and `pack_format` 34.
- **Found by a render scene only:** NeoForge 21 draws armor through the 12-argument `renderArmorPiece`, so the Dyeing Machine armor tint stayed off with the old hook placement; fixed (both overloads patched, colour is an int now).
- **Terrain-generation test (2026-10-22):** `worldgen.glowing_mushroom.real_terrain_generation_places_patches_underground` loads real chunks (16 areas of 3x3, 9 mushrooms in 5 chunks, mutation-checked) and therefore needs a normal-terrain world: it skips in the flat suite and runs in the second server pass that `suite-all.sh` does when `docs/testing/normal-world-pass.txt` exists (`level-type=minecraft:normal`, seed 424242, `RT_AUTOTEST_NORMAL=true`). Details: `api-gotchas-1.21.1.md`, Tests.
- **Real install (2026-10-22):** NeoForge 21.1.264 installer downloaded to `prod-test/1.21.1/` (untracked); the built jar on the real server: suite 380 / 0 / 4, on the real client 382 / 0 / 2 with the audit clean and 26 render scenes (armor tint correct).
- **Desktop playtest (2026-10-22, `SCN_MC=1.21.1`): all 28 scenarios pass on the dev client.** `scn.py` needed two 1.21 helper fixes (Singleplayer button y=203; summon attribute NBT in the new `attributes:[{id,base}]` form, otherwise the rain-shield pig kept 10 max health); no mod bug turned up.
- **Not done / not verified:** nothing outstanding except the manual checklist rows.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`).

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.

## What to do next

1. Manual checks: `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` (`docs/testing/manual-test-world.md`), TESTING_CHECKLIST section 8 (1.21 rows).

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
