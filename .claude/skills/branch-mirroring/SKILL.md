---
name: branch-mirroring
description: Switch between the per-Minecraft-version branches and carry fixes, tests and docs across them safely. Use whenever you open another version's folder, port a change from one branch to another, or prepare a commit/push of the same change to several branches. `/mirror <sha> <branches>` automates the apply + compile step.
---

# Branch switching and mirroring

## Branches
The branch table (loader, JDK, naming scheme, which branch each was cut from) is in **`docs/project/branches.md`**; add a row there when you create a branch (`/mirror` carries it to the others).

`master` = untouched 1.12.2-era reference (`git show origin/1.12.2:<path>`), `main` = README only. Each version branch is a full port.

## Layout and rules
- **One worktree folder per branch** under the parent (`<parent>/<version>`; repository in `<parent>/.bare`). Work inside the folder of the version you are changing; reach another with `git -C ../<other> ...`. **Never `git checkout` a different version inside a worktree.** Each folder has its own `run/`, `build/`, `prod-test/<ver>/`; suites for two versions can run in parallel, but never two Minecraft windows at once with the playtest helper.
- **Never commit or push unless the user says so** (CLAUDE.md); use the harness's attribution line.
- Toolchain per folder: `source env/activate.sh` (picks the JDK from the matrix).
- `.claude/` (skills, agents, commands, hooks, settings) is tracked per branch but must stay **byte-identical on every branch**: all version-specific facts live in `docs/project/version-notes.md` (the one file per branch that legitimately differs). Change a shared file once, `/mirror` it to the others, and run `python docs/migration/tools/shared-drift.py` (`/drift`) to confirm nothing diverged. The parent folder's `.claude` is a junction into one version folder.

## Carrying a change across
1. Same-shape changes (docs, plain-logic classes, tests with no version API, skills/agents/commands): **`/mirror <sha> <branch...>`**, or `bash docs/migration/tools/mirror.sh <sha> <branch...>`, applies the commit's diff to each target worktree's working tree (no commit), compiles each and prints a per-branch table. Review and commit per branch yourself.
2. A target where the patch does not apply is reported "needs manual port" and left untouched. Hop one version at a time (`1.19.2 -> 1.20.1 -> 1.20.6`): a patch from 1.14.4 will rarely apply to 1.20.x. Port by hand with `git show <sha> -- <path>`, then compile and fix API differences.
3. **Not mechanical across** (each branch's `docs/project/version-notes.md` and `api-gotchas-*` list the specifics): 1.16.5 -> 1.18.2 (Mojang names, `BlockEntity` ctors/tickers, shaders, `Holder<Biome>`, datapack dimension type) and 1.20.1 -> 1.20.6 (data components replace item NBT, payload networking, `RegisterEvent` registration, capabilities, `use` split; see `docs/project/api-gotchas-1.20.6.md`). Rendering and coremod code must be re-derived from the target's bytecode (`mapping-lookup`, `coremod-patching`).
4. On the target run the client suite, the server suite (`RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer`), the GUI click test if screens changed (`RT_AUTOTEST_CLICKS=true`), the render scenes (`RT_AUTOTEST_SCENES=true`), then `prod-jar-check` for anything touching coremods/reflection. A pass on another branch proves little: each port found its own crashes. The `suite-runner` agent runs these without flooding context.
5. Mirror the docs in the same pass: `TESTING_CHECKLIST.md`, `PORTING_PLAN.md` handoff, `docs/testing/README.md` + `test-inventory.md`, `docs/project/*`.
6. Record what is NOT mirrored in `PORTING_PLAN.md` ("Current state on this branch").

## Documentation layout (same on every branch)
Root files stay short; detail goes under `docs/` (`docs/project/`, `docs/testing/`, `docs/migration/` incl. `plans/` and `tools/`, `docs/archive/`). New version branch: follow `docs/migration/07-next-migration-checklist.md` and the newest plan in `docs/migration/plans/` as the worked example; run `git worktree add <ver> <branch>` in the parent and set up `prod-test/<ver>`.
