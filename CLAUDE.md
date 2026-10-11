# Random Things — 1.12.2 → Forge 1.14.4 port

Porting the Minecraft mod "Random Things" (originally by Lumien) from 1.12.2 to Forge 1.14.4, on the
fork [SlickleCode/Random-Things](https://github.com/SlickleCode/Random-Things).

Reusable docs: [docs/testing/](docs/testing/README.md) (automated in-game tests, dev-only) and
[docs/migration/](docs/migration/README.md) (migration knowledge base for future version bumps).

**Read [PORTING_PLAN.md](PORTING_PLAN.md)'s "START HERE (session handoff)" section first in any new
session on this repo** — it has the current state, what's in progress, and what's next. Don't re-derive
project state from scratch; that file is kept current after every slice.

## Branches

- `1.14.4` — the 1.14.4 port (Forge 28.2.26, Gradle 5.6.4).
- `1.16.5` — the 1.16.5 port (Forge 36.2.39, ForgeGradle 4.1, Gradle 6.8.3), branched from `1.14.4`; plan in `docs/migration/plans/1.16.5-port-plan.md`.
- `master` — untouched original 1.12.2-era source, kept as reference (`git show origin/1.12.2:<path>`
  to read the real original of any file).
- `main` — landing-page README only, no code.

## Toolchain

```
source env/activate.sh   # pins JDK 8
./gradlew build          # full build; add -x test to skip tests for a quick compile check
```

`TESTING_CHECKLIST.md` (repo root) is the live in-game testing tracker — check it for new `FAIL` rows
at the start of a session before starting new porting work. `WIKI_FEATURE_STATUS.md` tracks which
wiki-documented features are DONE/NOT STARTED.

## Working rules

- **Ground-truth every uncertain 1.14.4/Forge API against the real mapped jar with `javap` before
  writing code that uses it** — don't guess from training-data recall of Minecraft modding APIs. This
  early Forge 1.14.4 build (28.2.26) has real gaps and renames versus later versions and versus what
  most online guides assume, and this habit has repeatedly caught real bugs before they shipped (wrong
  render-layer assumptions, nonexistent methods, APIs that exist but silently don't do what their name
  implies — e.g. `FluidUtil.tryPickUpFluid` in this build has no real code path for a plain fluid
  block at all). The mapped jar is at
  `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3/forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3-recomp.jar`
  (also has `-sources.jar` alongside it).
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
