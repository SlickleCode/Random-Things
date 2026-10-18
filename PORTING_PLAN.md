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

## Current state on this branch (`1.19.2`, 2026-10-15) - PORT STARTED

- **Branch:** cut from `1.18.2` (`2a69d65`); toolchain Forge 1.19.2-43.5.2 / ForgeGradle 5.1 / Gradle 7.4.2 / JDK 17, Mojang names. Plan and milestones: `docs/migration/plans/1.19.2-port-plan.md`. M0 (toolchain) and M1 (mechanical API moves) done: `./gradlew build -x test` passes. The dedicated server boots and the dev suite passes 357/0/2 (2026-10-15); client, worldgen/Peace Candle tests and chat features are open - see the plan's "Known gaps"; test/status text below the 1.18.2 history is carried over.
- Everything is uncommitted.
- 2026-10-17: Bean Sprout now also survives on grass, dirt, coarse dirt, podzol and farmland (1.12.2's plain-type plant rule; CropBlock alone allowed farmland only, so it almost never generated naturally). Test `worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block`.

## What to do next

1. M3-M6 in order (chat, rendering/client, coremods, boot + suites); update the plan + this file after each. M2 (worldgen + restored worldgen tests) is done, see the plan's "M2 result".

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
