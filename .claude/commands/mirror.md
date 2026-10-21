---
description: Apply a commit to other version worktrees (no commit) and compile each - /mirror <sha> <branch...>
argument-hint: <sha> <branch> [<branch>...]
---

Mirror a commit onto other version branches using the `branch-mirroring` skill.

Arguments: `$ARGUMENTS` (a commit sha from this worktree, then one or more target branches such as `1.20.1 1.19.2`).

1. If the arguments are missing, ask for the sha (show `git log --oneline -5`) and the target branches.
2. Run `bash docs/migration/tools/mirror.sh <sha> <branches...>` from this worktree (`source env/activate.sh` is done per target by the script). Compiling can take minutes per branch: run it in the background if there are several targets.
3. Report the per-branch table. For every "needs manual port" branch say which files conflict (`git show <sha> --stat` against the target) and offer to port it by hand, one version hop at a time.
4. Do NOT commit or push on any branch; the changes stay in the target working trees for the user to review. Remind the user of that and of the docs to mirror in the same pass (`TESTING_CHECKLIST.md`, `PORTING_PLAN.md` handoff).
