# 01 — Strategy and workflow

## 1. Know what you are porting before you write code

**Inventory first.** Before porting, build three lists from the original source:

1. *Features* (what a player sees). Here the mod's public wiki, exported to 100 JSON feature pages,
   became the **authoritative scope**; original code with no wiki page was not migrated unless explicitly
   kept. A scope document the owner has agreed to is the single biggest protection against endless
   porting of dead code.
2. *Engine hooks* (what the original patched). 1.12.2 Random Things bytecode-patched exactly 17 vanilla/Forge
   classes through a custom ASM transformer. Reading that transformer's dispatcher *and* each patch body
   *and* the hook implementations gave the complete list of "needs a replacement hook" features up front
   (two early readings of it were wrong; see lesson in §6).
3. *Dependencies on things that no longer exist*: third-party compat (dropped by decision), the old
   ore dictionary, metadata sub-items, the old model/blockstate pipeline, entity registration style.

**Record scope decisions in a file the next session will read** (`WIKI_FEATURE_STATUS.md` with DONE /
DONE-UNTESTED / PARTIAL / NOT STARTED / WILL NOT MIGRATE / REMOVED, plus a "Not migrating" section). Decisions
the owner made (remove X, don't port Y) are binding; re-opening them wastes sessions.

## 2. Order of work that worked

1. **Toolchain and a booting skeleton** — pinned JDK, pinned Forge, a `runClient` that reaches the main
   menu, the mapped jar available for `javap`.
2. **Shared infrastructure** — registration scaffolding, color/tint plumbing, the debug/test hooks.
3. **Batch 1: standalone blocks** (no tile entity, GUI or network). Fast wins, exercises registration, models,
   blockstates, textures, loot, recipes.
4. **Batch 2: tile-entity blocks** with containers/GUIs/network — subsystem by subsystem.
5. **Batch 3: items**, then **deferred subsystems** (things that need new infrastructure: biome radar,
   sound patterns, imbuing/runes, floo network).
6. **Wiki-scoped slices** for what remained, each its own focused change.
7. **Hook-dependent features last** (coremods): by then the pattern for all the ordinary work is settled and
   the *set* of needed hooks is known and can share infrastructure (one shared spawn-dispatch hook served three
   features).
8. **Backfills**: loot tables (99), recipes (45), config toggles, translations.
9. **Polish and audits**: GUI layout audit of all GUIs, naming, tooltips.

Do not try to do everything in one pass. A "slice" = one feature family end to end (block + TE + GUI +
network + recipe + loot + lang + checklist rows).

## 3. The per-slice loop

1. Read the original (`git show origin/<branch>:<path>`): class, its model/blockstate/recipe/loot JSON,
   texture. Check data files directly — they reference numeric metadata that Java greps miss.
2. `javap` every API you are not 100% sure about in the *target* version (mapped jar; read the sources jar
   for behavior).
3. Port with the target version's idioms (extend vanilla classes instead of reimplementing; discrete items
   instead of metadata; data-driven where the version supports it).
4. **Disclose every simplification** in the class javadoc and the checklist ("dropped X because Y").
5. Compile; then *boot the game* when the slice adds registry entries, coremods, capabilities, or models.
6. Add/modify the checklist row (status `FIXED`/`UNTESTED` until a human retests) and the state file, **in
   the same pass**.
7. Leave it uncommitted until the owner asks (the owner's standing rule here).

## 4. Working rules that held up

| Rule | Why |
| --- | --- |
| Ground-truth with `javap`/sources before writing | Prior knowledge of the API was wrong for the exact build used, repeatedly |
| Match the original exactly unless told otherwise; flag divergences | The owner wants the same mod; divergences should be conscious |
| Before "fixing" a suspicious behavior, read the original | Several "bugs" were intentional |
| Don't claim a fix works without confirmation or runtime evidence | Several blind fixes were wrong or crashed |
| Update docs/checklist in the same pass as code | Docs drift fast; stale `BUGGY` tags misled later sessions |
| Ask before building something whose consumers already shipped | Avoids orphaned features (Item Filter) |
| Prefer deleting an already-working-but-unwanted feature cleanly over leaving remnants | Plate family, Special Chest, Biome Glass etc. were removed on request, including assets/lang/registrations |

## 5. Handling the human tester

Playtesting was done by the owner, not the agent. What made it efficient:

- A **numbered checklist** (`#NNN`, one behavior per row, statuses `PASS/FAIL/FIXED/UNTESTED/REMOVED`).
  Root-cause narrative went to a separate `TESTING_NOTES.md` keyed by row number so the owner's feedback
  column stayed theirs.
- **Evidence-first bug reports.** Screenshots, crash logs and log lines turned multi-round guessing into a
  one-round fix (the Spectre Illuminator instrumentation round, §6 of the case studies).
- **Ask precise questions** when a report is ambiguous ("farmland or dirt?", "unbound or bound Portkey?").
  Several "bugs" were a different surface, a stale build or an unbound item.
- **Say when something cannot be verified headlessly** (visual rendering, multiplayer) and ask for a specific
  retest rather than declaring victory.
- **A stale build is a real cause.** "Not craftable" reports for correct recipes were an old client/jar; first
  action: fresh `./gradlew build` and restart.

## 6. Process mistakes worth not repeating

- **Confident "impossible" claims that were wrong.** "No runtime biome-reassignment API" (there was one),
  "village generator is fully data-driven, no hook" (pools were Java-registered and wrappable), "no clean
  injection point" for churches. When a claim says *impossible*, require evidence (bytecode, source) in the
  note, and mark it provisional.
- **Misattributed hooks.** `Block.getLightValue` was first attributed to the Lapis/Quartz lamps; it was the
  Spectre Illuminator. `CheckSpawn` was assumed a clean replacement for a spawn patch; it fires too late to
  ALLOW. Read the *hook implementation* bodies, not just the patch dispatcher.
- **Dropping behavior as "equivalent."** The bean stalk's `isLadder` was dropped because a push-while-moving
  collision tweak "gave a climbable feel" (it did not). Dropping a behavior is a feature change; list it.
- **Fixing one copy of a repeated bug.** The same fix was needed in several siblings (text-field handling in
  4–5 screens, `getUpdateTag`/`onDataPacket` in 6 tile entities, item-collector capability side in 2 classes).
  After any fix, grep for siblings.
- **Letting the running log grow without bound.** The handoff file reached ~420 KB / 4,500 lines. It was
  condensed to ~300 lines (state, decisions, conventions, gotchas, coremod map) with history left in git.
  Do the condensing earlier.
- **Environment drift.** One stretch ran in a cloud sandbox with no Forge/Maven access, so nothing could be
  build-verified; features from that stretch were explicitly tagged "not build-verified" and re-checked later.
  Record the environment's limits in the note, and re-verify afterwards.

## 7. Simplifications that were accepted (and are reasonable to repeat)

- Metadata sub-items → discrete items/blocks per variant (the target version has no metadata).
- Ore-dictionary → vanilla items or `forge:` tags; third-party compat dropped.
- Icon-sprite buttons → text buttons first, restored selectively where clarity demanded it.
- Bespoke particle classes → vanilla particle types with a tint.
- Bespoke rendering frameworks (custom GL magic circles) → dropped or replaced by a tile-entity renderer.
- Hand-copied vanilla algorithms → extend the vanilla class and override the smallest hook (and use a
  coremod for the private parts).

Each was recorded as a *disclosed simplification*. The discipline of writing it down is what lets a later
session decide whether to revisit it.
