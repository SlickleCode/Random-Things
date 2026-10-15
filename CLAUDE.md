# Random Things — 1.12.2 → Forge 1.14.4 port

Porting the Minecraft mod "Random Things" (originally by Lumien) from 1.12.2 to Forge 1.14.4, on the
fork [SlickleCode/Random-Things](https://github.com/SlickleCode/Random-Things).

Reusable docs: [docs/testing/](docs/testing/README.md) (automated in-game tests, dev-only) and
[docs/migration/](docs/migration/README.md) (migration knowledge base for future version bumps).

**Read [PORTING_PLAN.md](PORTING_PLAN.md)'s "START HERE (session handoff)" section first in any new
session on this repo** — it has the current state, what's in progress, and what's next. Don't re-derive
project state from scratch; that file is kept current after every slice.

## Branches

- `1.14.4` — active port work, checked out by default.
- `master` — untouched original 1.12.2-era source, kept as reference (`git show origin/1.12.2:<path>`
  to read the real original of any file).
- `main` — landing-page README only, no code.

## Switching branches: clean `run/`

Each branch's Minecraft version writes its own world format into the gitignored `run/` directory, and a
newer-format world (e.g. `run/saves/RTAutoTest` from 1.16.5) makes an older client hang on load. **Whenever
you `git checkout` another version branch, clear the old state first** (this is not tracked, so safe):

```
rm -rf run/saves/RTAutoTest run/RTServerTest run/logs run/randomthings_*.txt run/screenshots/randomthings_debug
```

(delete any other test worlds in `run/saves` the same way; the autotest recreates `RTAutoTest`).

## Toolchain

```
source env/activate.sh   # pins JDK 8
./gradlew build          # full build; add -x test to skip tests for a quick compile check
```

`TESTING_CHECKLIST.md` (repo root) is the live in-game testing tracker (open rows only) — check it for new
`FAIL` rows at the start of a session before starting new porting work; add new/changed-feature rows there as
`FIXED`. The full finished sheet is in `docs/testing/checklist-archive/`, root-cause writeups per row in
`docs/testing/fix-log/`. Root markdown files are deliberately short (`PORTING_PLAN.md` handoff,
`WIKI_FEATURE_STATUS.md` scope summary); project knowledge (conventions, coremods, API gotchas, scope decisions,
history) lives in `docs/project/` — put new detail there, not back in the root files.

## Working rules

- **Ground-truth every uncertain Minecraft/Forge API against the real mapped jar with `javap` before writing code that uses it** — never from
  memory of modding guides (this repeatedly caught real bugs: wrong render-layer assumptions, nonexistent methods, APIs that silently do nothing).
  Jar locations per version, SRG names and bytecode reading: the `mapping-lookup` skill.
- **Match 1.12.2 behavior exactly by default**, including reading the actual original source via
  `git show origin/1.12.2:<path>` rather than assuming. When the user explicitly states a preference
  that diverges from the original, follow their call and flag the divergence plainly rather than
  silently matching upstream.
- **Before changing behavior that looks wrong, check the real 1.12.2 source first.** If it matches
  original design, report that back and ask/wait rather than unilaterally "fixing" already-correct
  behavior — several things that looked like bugs (a see-through texture style, an always-on light, a
  Portkey that glows forever unbound) turned out to be intentional in 1.12.2.
- **Don't commit or push automatically** — wait for the user to explicitly say so, even after a big
  verified batch of fixes. Keep doing the work and running full `./gradlew build` verification per
  slice; just leave the result uncommitted until asked.
- **Update `TESTING_CHECKLIST.md` and `PORTING_PLAN.md` whenever project features change** — any new
  feature, behavior change, GUI/layout change, bug fix, or removal gets a checklist row (new, or the
  existing row amended; mark it `FIXED` until the user retests) and a note in `PORTING_PLAN.md`'s
  "START HERE" section, in the same pass as the code change. Don't wait to be asked or batch it for
  later.
- Work in the same local checkout the user runs `./gradlew runClient` in — no need to hand over a
  built jar unless they ask for one directly (e.g. to test on a separate real launcher install).
- **Mixins cannot be tested via `./gradlew runClient`** in this environment, and this Forge build never
  actually shipped Mixin's own runtime — see `PORTING_PLAN.md`'s Mixin-investigation history if that
  ever comes up again. The project converts Mixins to coremods (`META-INF/coremods.json` +
  `transformer/*.js` + `AsmHandler`) instead, which do work self-contained in a shipped jar.

Co-Authored-By line for commits (when the user asks for one to be created): see the harness's own
attribution instructions for the current line to use.

## Automated tests and dev-only tooling

- Run `RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient` (see
  `docs/testing/README.md`) after any slice that touches registration, models, recipes, tile entities, capabilities
  or hooks; read `run/randomthings_audit.txt` and `run/randomthings_tests.txt`. Also run
  `RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer` after anything that adds client-class references (dedicated-server boot + suite). Add a behavior test
  (`debug/test/BehaviorTests`) for new redstone/capability/hook logic.
- All dev tooling must stay behind `DevMode.ENABLED` (`-Drandomthings.debug=true`): no `@Mod.EventBusSubscriber`
  on dev classes, no references to dev classes from game code except the guarded `DevTools.init()` call, no config
  options for it. Use `RTDebug.log(...)` for diagnostics, never an unconditional `LOGGER.info` in gameplay code.
- Before relying on any Forge event, find who posts it in the sources jar (`HarvestDropsEvent` is never posted).

## Project skills (`.claude/skills/`)

Procedures that were repeated by hand are packaged as skills - load them instead of re-deriving: `prod-jar-check` (verify the jar on a real SRG-named
server/client), `scripted-playtest` (drive the real game with `desk.py`/`scn.py`; run or write scenarios), `mapping-lookup` (javap, dev vs SRG names per
version), `coremod-patching` (writing/debugging JS transformers and their traps), `branch-mirroring` (switching version branches, cherry-picking, what to
re-verify and re-document).

Project agents (`.claude/agents/`): `bytecode-scout` (read-only javap/mapping research, returns anchors and signatures, not dumps) and `suite-runner`
(runs the dev/server/click/production suites and reports only counts and failures). Use them when the raw output would flood the context.
