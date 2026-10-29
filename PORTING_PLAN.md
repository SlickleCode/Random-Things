# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port (complete) | `docs/migration/plans/1.18.2-port-plan.md` |
| `1.19.2` | Forge 43.5.2 port, IN PROGRESS (M0-M2 done; server suite 375/0/3; client suite 377/0/1; GUI click test and production (SRG) server/client suites pass) | `docs/migration/plans/1.19.2-port-plan.md` |
| `1.20.1` | Forge 47.4.28 port (complete apart from the signed-chat check) | `docs/migration/plans/1.20.1-port-plan.md` |
| `1.20.6` | NeoForge 20.6.141 port (complete; JDK 21, NeoGradle 7, Gradle 8.13, Mojang names also in production) | `docs/migration/plans/1.20.6-port-plan.md`, `docs/project/api-gotchas-1.20.6.md` |
| `1.21.1` | NeoForge 21.1.264 port (complete; same toolchain as 1.20.6) | `docs/migration/plans/1.21.1-port-plan.md`, `docs/project/api-gotchas-1.21.1.md` |
| `1.21.4` | NeoForge 21.4.161 port (this branch, complete: compiles, server suite 424/0/4 + normal pass 1/0/0, client suite 383/0/2, audit clean, GUI click test 28 GUIs/0 failures, render hook checks 5/5; real NeoForge 21.4.161 install passes server 381/0/4 and client 383/0/2; all 28 scripted scenarios pass on the dev client) | `docs/migration/plans/1.21.4-port-plan.md`, `docs/project/api-gotchas-1.21.4.md` |
| `1.21.11` | NeoForge 21.11.48 port (this branch: compiles, dev server suite 427/0/4, client suite 429/0/2, audit clean, GUI click test 28 GUIs/37 clicks/0 failures, render hook checks 6/6 (coloured glint included, 2026-10-28); **open:** scripted playtest; the real NeoForge 21.11.48 install passes: server 427/0/4, client 429/0/2) | `docs/migration/plans/1.21.11-port-plan.md`, `docs/project/api-gotchas-1.21.11.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 21 on this branch; JDK 17 on 1.18.2-1.20.1, JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.21.11`, 2026-10-23)

- **Branch:** cut from `1.21.4` (`f857bd7`); NeoForge 21.11.48 / NeoGradle 7.0.192 / Gradle 8.13 / JDK 21, Mojang names everywhere. Plan: `docs/migration/plans/1.21.11-port-plan.md`; every API change: `docs/project/api-gotchas-1.21.11.md`.
- **Automated (dev):** dedicated-server suite 427 / 0 / 4, client suite 429 / 0 / 2 with the resource audit at 0 failures, GUI click test 28 GUIs / 37 clicks / 0 failures, render hook checks 6 of 6 (re-run 2026-10-28: items and armor draw the tinted glint, `glint_modulated` fires with the right colours).
- **Biggest changes:** NeoForge dropped the JavaScript coremods (all 14 transformers are Java class processors now, `asm/transform/`), the item/fluid/energy handler capabilities became the transaction based transfer API (`itemhandler/` keeps the old slot API on top), block entities and entities save through `ValueInput`/`ValueOutput` (`util/TagIO` bridges the old `CompoundTag` code), rendering is two step (extract state, submit) with pipeline based render types, `ResourceLocation` is `Identifier`.
- **Open:** Divining Rod cubes are depth tested now, scripted playtest, the manual checklist rows.

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`); the custom-glint items (Spectre Key/Sword, Redstone Tool, Escape Rope) no longer render faded (`ClientAsmHandler.appendGlintColor`); the creative-tab scene parks the cursor so no tooltip covers items. Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario, GUI text colours with alpha (1.21.6+). Checklist rows M1-M7.
- **Time Accelerator wheel (2026-10-29):** the accelerated block shows the spinning decagon wheel of 1.12.2 on all six faces (rings grow with the time rate) instead of the floating Time in a Bottle icons. Geometry and colours are one version-neutral file, `client/renderer/TimeWheel.java` (identical on every branch); each branch only has its own `TimeAcceleratorEntityRenderer` that transforms the triangles and draws them unlit and translucent. Render scenes `time_wheel` (six rates in a row) and `time_wheel_close`.

- **Audit bug fixes (2026-10-29, unverified):** a wiki audit (branch `wiki`) found 12 behaviour bugs, fixed on 1.14.4 (`03be7448`) and mirrored here **without a compiler or NeoForge jars (no JDK, no network) - build, run both suites and retest before trusting them**: Redstone Tool plain-click linking (`useItemOn` returns `PASS` for the tool), Online Detector case sensitivity, Lotus Seeds on farmland only, Peace Candle denies slimes, Spectre Lens per player per dimension, Ender Letter store always the overworld's, Sound Box / Slime Cube powered flags saved, Advanced Redstone Torch `setChanged`, Potion Vaporizer drops its contents, Ender Bucket drain check, Eclipsed Clock target time, Stable Ender Pearl guard. Rows AF1-AF12 in `TESTING_CHECKLIST.md` section G; details in `docs/project/audit-fixes-2026-10-29.md`.

- **Round 2 (same day, also unverified):** added the Spectre Sapling conversion, Runic Pattern from paper, Luminous Powder recipe (glow rendering still missing), Numbered Spectre Coils and Blackout Powder;
  fixed Floo (unloaded chunk, duplicate names), Stable Ender Pearl dimensions, Position Filter dimensions, Observer reload output and the Analog Emitter input side. Rows AG1-AG11 in `TESTING_CHECKLIST.md` The Magic Bean stalk now breaks blocks in its way (row AG12). What the cloud session could not do: `docs/project/cloud-session-limits-2026-10-29.md`. section H.

## What to do next

1. Manual checks: `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` (`docs/testing/manual-test-world.md`), TESTING_CHECKLIST section 8 (1.21 rows).

## Where things are

| Path | Contents |
| --- | --- |
| `CLAUDE.md` | working rules for Claude sessions |
| `TESTING_CHECKLIST.md` | what the user still has to test (open rows only) |
| `WIKI_FEATURE_STATUS.md` | one-screen feature scope summary |
| `docs/project/` | project knowledge: toolchain, scope decisions, architecture, coremods, API gotchas, pitfalls, slice and change-set history, full wiki feature table ([index](docs/project/README.md)) |
| `docs/testing/` | automated tests: how to run, inventory, writing tests, isolation; `checklist-archive/` = full finished sheet; `fix-log/` = root-cause writeups per row |
| `docs/migration/` | reusable migration knowledge base (strategy, pitfalls, per-version notes, plans, open questions) |
| `docs/archive/` | superseded documents |
