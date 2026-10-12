# Old PORTING_PLAN intro and file map

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

# Random Things 1.12.2 → Forge 1.14.4 port — project state

Condensed 2026-10-04 from a ~420 KB chronological log (full history: `git log -p -- PORTING_PLAN.md`;
per-bug writeups: `TESTING_NOTES.md`; per-feature status: `WIKI_FEATURE_STATUS.md`). This file is the
current truth: state, scope decisions, conventions, API gotchas, coremod map. `CLAUDE.md` has the
working rules. **Update this file whenever features change** (see CLAUDE.md).

## File map

| File | Purpose |
| --- | --- |
| `CLAUDE.md` | Working rules (javap-first, match 1.12.2, no auto-commit, update docs) |
| `PORTING_PLAN.md` | This file: state, decisions, conventions, gotchas |
| `WIKI_FEATURE_STATUS.md` | Authoritative per-feature table (100 wiki pages) + "Not migrating" list |
| `TESTING_CHECKLIST.md` | Numbered test rows with status (`PASS`/`FAIL`/`FIXED`/`UNTESTED`/`REMOVED`); user fills results |
| `TESTING_NOTES.md` | Root-cause / fix writeups keyed by checklist row `#` (not in the checklist's Feedback column) |
| `UNTESTED_AND_MANUAL_CHECKS.md` | Generated summary of open/untested rows |
| `GUI_TEST_LIST.md` | GUI screenshot-review tracker (complete) |
| `docs/testing/` | Automated test setup: running it, writing tests, dev-only isolation |
| `docs/migration/` | Reusable migration knowledge base (pitfalls, strategy, case studies, checklist) |
| `docs/migration/versions/` | Multi-version support catalog and per-version analysis. Scope (2026-10-08): 1.14.4 and up, Forge then NeoForge (Fabric later), 1.14.4 feature set only, branch per version, 1.15.2/1.17.1 skipped, keep coremods |

