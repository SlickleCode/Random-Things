---
description: Run the dedicated-server suite on several version worktrees and print one table - /suites [--baseline] [branch...]
argument-hint: [--baseline] [branch...]
---

Run the cross-version server suite with `docs/migration/tools/suite-all.sh`.

Arguments: `$ARGUMENTS` (optional `--baseline`, then branches; default is all six).

1. Run `bash docs/migration/tools/suite-all.sh $ARGUMENTS` in the background (about 2-3 minutes per branch, sequential) and wait for it; do not start other Gradle or Minecraft processes meanwhile.
2. Report the table verbatim, then each failure with a one-line reason. A failure on one branch only is likely version-specific; check whether `coremod.spawn.peace_candle_denies_monster_spawns_in_range` or the Portkey test flaked (re-run just that branch once before calling it a failure).
3. With `--baseline`, mention any branch whose counts differ from `docs/testing/suite-baseline.txt` and ask before updating that file. Never commit.
