---
name: branch-mirroring
description: Switch between the per-Minecraft-version branches (1.14.4, 1.16.5, 1.18.2, later ones) and carry fixes, tests and docs across them safely. Use whenever you open another version's folder, port a change from one branch to another, or prepare a commit/push of the same change to several branches.
---

# Branch switching and mirroring

Branches: `1.14.4` (Forge 28.2.26), `1.16.5` (Forge 36.2.39, from 1.14.4), `1.18.2` (Forge 40.3.12, from 1.16.5, Mojang names, JDK 17), `master` = untouched 1.12.2-era reference (`git show origin/1.12.2:<path>`), `main` = README only. Each version branch is a full port.

## Layout and rules
- **Each version branch has its own worktree folder** under the parent (`<parent>/1.14.4`, `1.16.5`, `1.18.2`; repository in `<parent>/.bare`). Work inside the folder of the version you are changing; reach another with `git -C ../<other> ...`. **Never `git checkout` a different version inside a worktree.** Each folder has its own `run/`, `build/`, `prod-test/<ver>/`; suites for two versions can run in parallel, but never two Minecraft windows at once with the playtest helper.
- **Never commit or push unless the user says so** (CLAUDE.md); use the harness's attribution line.
- Toolchain per folder: `source env/activate.sh` (JDK 8 on 1.14.4/1.16.5, **JDK 17 on 1.18.2**).
- The parent folder's `.claude/skills` and `.claude/agents` are junctions into one version folder (currently `1.16.5`); the 1.18.2 skills only apply when a session runs inside `1.18.2/`.

## Carrying a change across
1. Prefer **commit then cherry-pick** for identical-shape changes (docs, plain-logic classes, tests with no version API).
2. Otherwise `git show <sha> -- <path> | git apply --3way`, then compile and fix API differences. **1.16.5 -> 1.18.2 is NOT mechanical**: Mojang names (a 1.16.5 patch uses MCP names; map it with `docs/migration/tools/maps.py`/`Remap.java` or by hand), `BlockEntity` constructors/tickers, shader rendering, `Holder<Biome>`, `PlacedFeature`, datapack dimension type. Rendering and coremod code must be re-derived from the target's bytecode (`mapping-lookup`, `coremod-patching`).
3. On the target branch run the client suite, the server suite (`RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer`), the GUI click test if screens changed (`RT_AUTOTEST_CLICKS=true`), the render scenes (`RT_AUTOTEST_SCENES=true`), then `prod-jar-check` for anything touching coremods/reflection. A test that passed on another branch proves little: each port found its own crashes.
4. Mirror the docs the same pass: `TESTING_CHECKLIST.md`, `PORTING_PLAN.md` handoff, `docs/testing/README.md` + `test-inventory.md`, `docs/project/*`.
5. Record what is NOT mirrored in `PORTING_PLAN.md` ("Current state on this branch").

## Documentation layout (same on every branch)
Root files stay short; detail goes under `docs/` (`docs/project/`, `docs/testing/`, `docs/migration/` incl. `plans/` and `tools/`, `docs/archive/`). New version branch: follow `docs/migration/07-next-migration-checklist.md` and the 1.18.2 plan as the worked example (`docs/migration/plans/1.18.2-port-plan.md`).
