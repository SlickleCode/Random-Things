# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete (this branch) | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port, complete in code, pushed | `docs/migration/plans/1.16.5-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 8), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Switching branches: clean the old test worlds first** (`rm -rf run/saves/RTAutoTest run/RTServerTest run/logs run/randomthings_*.txt run/screenshots/randomthings_debug`, see `CLAUDE.md`).

## Current state (2026-10-11)

- **Port:** every wiki-documented feature is ported on both branches except the removed / will-not-migrate ones
  (`docs/project/scope-decisions.md`, `docs/project/wiki-feature-status.md`).
- **Testing sheet finished by the user:** 388 rows - 227 PASS, 40 DONE + 4 REMOVED (features dropped), 12 FIXED waiting for a retest,
  105 never tested (mostly crafting-recipe rows the automated suite already covers), **0 FAIL**. Open items: `TESTING_CHECKLIST.md`.
- **Automated:** client and dedicated-server suites 344 passed (1 environment-dependent skip), resource audit 0 failures, GUI click-through
  (buttons, text fields, slot tooltips), render-scene screenshots, and the same suites on a *production* (SRG-named) Forge server and client in
  `prod-test/` (all coremods verified). Everything is dev-only; see `docs/testing/README.md` and `docs/testing/test-inventory.md`.
- **Worldgen:** Glowing Mushroom and Peace Candle church generation are covered by deterministic `worldgen.*` tests (real terrain + village assembly), so rows #49/#270/#341/#356b need no manual hunting.
- **Uncommitted on this branch (until the user says commit):** Sound Dampener GUI tooltip fix (`AbstractSoundDampenerScreen.render`), the slot-tooltip
  check in `GuiClickTest`, and the documentation restructuring below.

## What to do next

1. Check `TESTING_CHECKLIST.md` for `FAIL` rows, then the 12 `FIXED` retests; don't re-investigate a `FIXED` row without reading its entry in `docs/testing/fix-log/`.
2. Mirror the doc layout and the Sound Dampener fix + slot-tooltip test to `1.16.5` (same bug exists there).
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
