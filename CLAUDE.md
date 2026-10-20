# Random Things — 1.12.2 → Forge port (this branch: 1.20.1)

Porting the Minecraft mod "Random Things" (originally by Lumien) from 1.12.2 to newer Forge versions (one branch per version; this folder is the 1.20.1 branch), on the
fork [SlickleCode/Random-Things](https://github.com/SlickleCode/Random-Things).

Reusable docs: [docs/testing/](docs/testing/README.md) (automated in-game tests, dev-only) and
[docs/migration/](docs/migration/README.md) (migration knowledge base for future version bumps).

**Read [PORTING_PLAN.md](PORTING_PLAN.md)'s "START HERE (session handoff)" section first in any new
session on this repo** — it has the current state, what's in progress, and what's next. Don't re-derive
project state from scratch; that file is kept current after every slice.

## Branches

- `1.14.4` — the 1.14.4 port (Forge 28.2.26, Gradle 5.6.4).
- `1.16.5` — the 1.16.5 port (Forge 36.2.39, ForgeGradle 4.1, Gradle 6.8.3), branched from `1.14.4`; plan in `docs/migration/plans/1.16.5-port-plan.md`.
- `1.18.2` — the 1.18.2 port (Forge 40.3.12, ForgeGradle 5.1, Gradle 7.4.2, **JDK 17**, Mojang names), branched from `1.16.5`; plan in `docs/migration/plans/1.18.2-port-plan.md`.
- `1.19.2` — the 1.19.2 port (Forge 43.5.2, ForgeGradle 5.1, Gradle 7.4.2, JDK 17), branched from `1.18.2`; plan in `docs/migration/plans/1.19.2-port-plan.md`.
- `1.20.1` — the 1.20.1 port (Forge 47.4.28, ForgeGradle 6, Gradle 8.8, JDK 17), branched from `1.19.2`; plan in `docs/migration/plans/1.20.1-port-plan.md`.
- `master` — untouched original 1.12.2-era source, kept as reference (`git show origin/1.12.2:<path>`
  to read the real original of any file).
- `main` — landing-page README only, no code.

## One folder per version (git worktrees)

The repository lives in the parent folder as a bare store with one **worktree folder per version branch** (`<parent>/1.14.4` ... `<parent>/1.20.1`); you
are in one of them. Each has its own `run/`, `build/`, `prod-test/` and checked-out branch, so there is nothing to clean when changing versions:
**open the other folder instead of running `git checkout`**. Use `git worktree list` to see them and `git -C <other-folder> ...` for the other branch.
If you ever do switch the branch inside a folder, clear the old state first (a world from a newer version hangs an older client, an old server world
stops the server at a registry confirmation):

```
rm -rf run/saves/RTAutoTest run/RTServerTest run/logs run/randomthings_*.txt run/screenshots/randomthings_debug run/saves/Scn_*
```

## Toolchain

```
source env/activate.sh   # pins JDK 17 on this branch (JDK 8 on 1.14.4 / 1.16.5)
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
  Jar locations, SRG ids and bytecode reading: the `mapping-lookup` skill (on 1.18.2 names are Mojang names; members are `m_`/`f_` ids in production).
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
- **Mixins cannot be tested via `./gradlew runClient`** in this environment (and on the older Forge builds never
  shipped Mixin's own runtime) — see `PORTING_PLAN.md`'s Mixin-investigation history if that
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

## General coding guidelines (Karpathy-style, from multica-ai/andrej-karpathy-skills)

Behavioral guidelines to reduce common LLM coding mistakes; they bias toward caution over speed (for trivial tasks, use judgment). Merge with the project-specific rules above.

### 1. Think before coding
**Don't assume. Don't hide confusion. Surface tradeoffs.**
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity first
**Minimum code that solves the problem. Nothing speculative.**
- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical changes
**Touch only what you must. Clean up only your own mess.**
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.
- Remove imports/variables/functions that YOUR changes made unused; leave pre-existing dead code unless asked.

The test: every changed line should trace directly to the user's request.

### 4. Goal-driven execution
**Define success criteria. Loop until verified.**
- "Add validation" -> write tests for invalid inputs, then make them pass.
- "Fix the bug" -> write a test that reproduces it, then make it pass.
- "Refactor X" -> ensure tests pass before and after.

For multi-step tasks, state a brief plan, each step with a verify check:
```
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
```

These guidelines are working if there are fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.
