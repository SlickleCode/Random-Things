---
description: Check that the shared tooling files are identical on every version branch - /drift [--diff]
argument-hint: [--diff]
---

Run `python docs/migration/tools/shared-drift.py $ARGUMENTS`.

Everything under `.claude/`, the four helper scripts in `docs/migration/tools/` (mirror, suite-all, test-parity, shared-drift), `.gitattributes`, `CLAUDE.md` and `docs/project/branches.md` must be identical on all branches (only `docs/project/version-notes.md` differs per branch). For each differing file, say which branch is the odd one and whether the newest branch or the odd one is right, then offer to fix it with `/mirror` or a copy. Do not commit.
