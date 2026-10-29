# Brief for parity agents (READ-ONLY)

Goal: find behaviour differences between the baseline branch 1.14.4 and the other version branches 1.16.5, 1.18.2, 1.19.2, 1.20.1, 1.20.6,
1.21.1, 1.21.4, 1.21.11 for the features of your pages. All nine branches must behave identically; every difference is a "parity bug" that
goes into a list for the user. You do NOT edit anything except your report. No fetch/commit/push/download, no game, no builds.

Read the branches with git (all already fetched): `git -C /home/user/Random-Things show origin/<branch>:<path>` and
`git -C /home/user/Random-Things ls-tree -r --name-only origin/<branch> | grep <ClassName>`. Class names are mostly stable, packages are under
`src/main/java/lumien/randomthings/`. Baseline source is also extracted at
MOD=/tmp/claude-0/-home-user-Random-Things/f859bde5-7624-5f08-9a19-e526dde76d34/scratchpad/mod1144 (same content as origin/1.14.4).
Already checked mechanically (do NOT redo): class name sets, config keys + defaults (identical on all branches), recipe JSON files (identical
on all branches after tag-name normalisation) EXCEPT super_lubricent_tincture (1.14.4 takes any potion, 1.20.6+ require a Water Bottle).
Known per-branch notes: /tmp/.../mod1144/docs/project/version-notes.md and docs/project/branches.md (read them, they list known differences).

## What to do, per assigned page (read data/features/<id>.json to learn which facts matter: numbers, ranges, durations, conditions, drops)
1. Find the implementing classes on 1.14.4 (see audit/facts/*.md for citations).
2. For each other branch, read the same class and compare the BEHAVIOUR relevant to the wiki facts: constants, conditions, tick rates, ranges,
   damage/durability, drop tables, redstone output values, GUI limits, loot chances, config gating, dimension handling. Ignore pure API
   renames, refactors, mapping names, rendering-only code unless a wiki fact depends on it.
3. Report ONLY real or probable behaviour differences (with branch, file:line on both sides, what differs, which wiki fact it affects), and a
   short line "no difference found" per page otherwise. Say how deep you looked (full class read vs grep).
4. Also note if a feature is missing entirely on a branch (class absent, no registration).

Report to `audit/verify/<your id>.md`. Reply with <= 10 lines: number of differences found, the 3 most important.
Mark confidence: CONFIRMED (both sides read) or SUSPECTED.
