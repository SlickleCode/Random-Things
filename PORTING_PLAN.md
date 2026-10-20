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
