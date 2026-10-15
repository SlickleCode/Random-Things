---
name: branch-mirroring
description: Switch between the per-Minecraft-version branches (1.14.4, 1.16.5, later ones) and carry fixes, tests and docs across them safely. Use whenever you git checkout another version branch, port a change from one branch to another, or prepare a commit/push of the same change to several branches.
---

# Branch switching and mirroring

Branches: `1.14.4` (Forge 28.2.26), `1.16.5` (Forge 36.2.39, branched from 1.14.4), `master` = untouched 1.12.2-era reference (`git show origin/1.12.2:<path>`), `main` = README only. Each version branch is a full port; most files are shared in shape, rendering/registry code differs.

## Rules
- **Never commit or push unless the user says so** (CLAUDE.md). When they do ("commit and push"), do it for the branches they named, with the attribution line from the harness (`Co-Authored-By: ...`).
- **Before every `git checkout` to another version branch, clean `run/`** (a world from a newer version hangs an older client; an old server world stops the server at a registry confirmation):
  `rm -rf run/saves/RTAutoTest run/RTServerTest run/logs run/randomthings_*.txt run/screenshots/randomthings_debug run/saves/Scn_*` (and kill leftover `java`).
- JDK 8: `source env/activate.sh` in every new shell.

## Carrying a change across
1. Prefer a **commit then cherry-pick** (`git cherry-pick <sha>`) for identical-shape changes (docs, plain-logic classes like `GlintColors`, tests with no version API). Docs-only edits can also be carried uncommitted with `git stash; git checkout <b>; git stash pop`.
2. For changes whose files differ between branches, apply the diff with `git show <sha> -- <path> | git apply --3way`, file by file; it usually applies cleanly because both ports share structure. Then compile (`./gradlew compileJava -q`) and fix **API differences** (1.16: `MatrixStack` rendering, `Vector3d` not `Vec3d`, registry keys, `isSolid`/`notSolid`, `IServerWorldInfo`, `func_241828_r()` dynamic registries). Rendering and coremod code is NOT mechanical - re-derive it from the target version's bytecode (`mapping-lookup`, `coremod-patching`).
3. Run on the target branch: client suite, server suite (`RT_AUTOTEST=true RT_AUTOTEST_EXIT=true ./gradlew runServer`), GUI click-through if screens changed (`RT_AUTOTEST_CLICKS=true`), then `prod-jar-check` and `scripted-playtest` for anything visible. Don't assume a test that passed on the other branch passes here - 1.16 found real crashes (Biome Radar NPE) and a culling bug 1.14.4 never had.
4. Mirror the docs the same pass: `TESTING_CHECKLIST.md`, `PORTING_PLAN.md` handoff, `docs/testing/README.md` + `test-inventory.md`, `docs/project/*` (scope decisions, coremods), and keep `docs/testing/desktop-helper/` identical to the live `prod-test/desktop/` copies.
5. Record what is NOT mirrored in `PORTING_PLAN.md` ("Current state on this branch") so the next session sees the gap.

## Documentation layout (same on every branch)
Root files stay short (`PORTING_PLAN.md` handoff, `TESTING_CHECKLIST.md` open rows, `WIKI_FEATURE_STATUS.md`); detail goes under `docs/` (`docs/project/` knowledge, `docs/testing/` automation + checklist archive + fix log, `docs/migration/` reusable migration KB, `docs/archive/`). New version branch: copy the layout, then follow `docs/migration/07-next-migration-checklist.md`.
