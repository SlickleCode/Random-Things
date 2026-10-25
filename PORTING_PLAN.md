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
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 17 on this branch; JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.20.1`, 2026-10-18) - PORT STARTED

- **Branch:** cut from `1.19.2` (`c0962ea`); toolchain Forge 1.20.1-47.4.28 / ForgeGradle 6 / Gradle 8.8 / JDK 17, Mojang names. Plan and milestones: `docs/migration/plans/1.20.1-port-plan.md`. M0-M6 minus the playtest are done: dev server 375/0/3, dev client 377/0/1 (audit 0 failures), GUI click test 28/37/0, render scenes reviewed, production (SRG) server 375/0/3 and client 377/0/1 in `prod-test/1.20.1`. Scripted playtest: all 26 scenarios pass on the dev client. Open: the signed-chat check and a production re-run with the final jar (see the plan's "Still open").
- Everything is uncommitted. The 1.19.2 state this branch started from (all suites green, signed chat unverified) is in `docs/migration/plans/1.19.2-port-plan.md`.

- **Tests mirrored from 1.20.6 (2026-10-21):** `behavior.loot.rolled_chest_biome_crystals_carry_a_biome`, `behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load`, `behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it`; dedicated-server suite now 378/0/3 (of 381), 0 failures. Mutation-checked on 1.20.1 (dropping the random-biome function, the chest pool or the radar update tag each fails its test). The manual world (`/rtdebug manual`, `RT_MANUAL=true`) is on this branch too: `docs/testing/manual-test-world.md`.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`).

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.

## What to do next

1. Signed-chat check on a logged-in production client and a production re-run with the final jar; then add TESTING_CHECKLIST rows for the 1.20 changes (creative tab, tooltips on the old Button screens, datapack worldgen).

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
