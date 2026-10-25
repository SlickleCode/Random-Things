# Version branches

One branch (and one worktree folder `<parent>/<version>`) per Minecraft version; each is a full port cut from the previous one. **Add a row here when you create a branch**
(this file is shared: change it once and `/mirror` it). Facts that differ per branch go in `version-notes.md`, not in the shared skills.

| Branch | Loader / build | JDK | Names in dev -> production | Cut from | Plan |
|---|---|---|---|---|---|
| `1.14.4` | Forge 28.2.26, Gradle 5.6.4 | 8 | MCP -> SRG | (first port) | - |
| `1.16.5` | Forge 36.2.39, ForgeGradle 4.1, Gradle 6.8.3 | 8 | MCP -> SRG | 1.14.4 | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12, ForgeGradle 5.1, Gradle 7.4.2 | 17 | Mojang -> SRG ids (`m_`/`f_`) | 1.16.5 | `docs/migration/plans/1.18.2-port-plan.md` |
| `1.19.2` | Forge 43.5.2, ForgeGradle 5.1, Gradle 7.4.2 | 17 | Mojang -> SRG ids | 1.18.2 | `docs/migration/plans/1.19.2-port-plan.md` |
| `1.20.1` | Forge 47.4.28, ForgeGradle 6, Gradle 8.8 | 17 | Mojang -> SRG ids | 1.19.2 | `docs/migration/plans/1.20.1-port-plan.md` |
| `1.20.6` | NeoForge 20.6.141, NeoGradle 7, Gradle 8.13 | 21 | Mojang everywhere (no SRG) | 1.20.1 | `docs/migration/plans/1.20.6-port-plan.md`, `docs/project/api-gotchas-1.20.6.md` |
| `1.21.1` | NeoForge 21.1.264, NeoGradle 7, Gradle 8.13 | 21 | Mojang everywhere (no SRG) | 1.20.6 | `docs/migration/plans/1.21.1-port-plan.md`, `docs/project/api-gotchas-1.21.1.md` |
| `1.21.4` | NeoForge 21.4.161, NeoGradle 7, Gradle 8.13 | 21 | Mojang everywhere (no SRG) | 1.21.1 | `docs/migration/plans/1.21.4-port-plan.md`, `docs/project/api-gotchas-1.21.4.md` |
| `1.21.11` | NeoForge 21.11.48, NeoGradle 7, Gradle 8.13 | 21 | Mojang everywhere (no SRG) | 1.21.4 | `docs/migration/plans/1.21.11-port-plan.md`, `docs/project/api-gotchas-1.21.11.md` |

`master` = untouched 1.12.2-era source (`git show origin/1.12.2:<path>`), `main` = README only.

## New branch checklist
1. In the parent folder: `git worktree add <ver> -b <ver> <previous-branch>`; copy `prod-test/desktop`, set up `prod-test/<ver>` (ask before downloading installers).
2. Port (`docs/migration/07-next-migration-checklist.md`, newest plan as the worked example).
3. Write `docs/project/version-notes.md` for the branch (sections: mapping-lookup, coremod-patching, prod-jar-check, scripted-playtest, suites) and add the row above.
4. `python docs/migration/tools/shared-drift.py` must report no differences; `bash docs/migration/tools/suite-all.sh --baseline <ver>` records the first suite counts.
