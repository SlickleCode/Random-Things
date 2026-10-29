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
| `1.20.6` | NeoForge 20.6.141 port (this branch; JDK 21, NeoGradle 7, Gradle 8.13, Mojang names also in production) | `docs/migration/plans/1.20.6-port-plan.md`, `docs/project/api-gotchas-1.20.6.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 21 on this branch; JDK 17 on 1.18.2-1.20.1, JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.20.6`, 2026-10-20)

- **Branch:** cut from `1.20.1` (`fec80ba`); toolchain NeoForge 20.6.141 / NeoGradle 7.0.192 / Gradle 8.13 / **JDK 21** (`~/.jdks/temurin-21`, found by `env/activate.sh`), Mojang names everywhere (NeoForge 20.5+ does not reobfuscate, so there is no separate SRG production variant). Plan, milestones and open items: `docs/migration/plans/1.20.6-port-plan.md`; every API change and trap: `docs/project/api-gotchas-1.20.6.md`. Everything is uncommitted.
- **Automated (dev, 2026-10-20):** dedicated-server suite 379 passed / 0 failed / 3 skipped (the skips are the same environment ones as before); client suite 381 / 0 / 1, resource audit 147 blocks / 241 items / 222 recipes, 0 failures; GUI click test 28 GUIs / 37 clicks / 0 failures / 17 slot tooltips verified; all 26 render scenes render (armor recolour, glint, Magic Hood, Fluid Display, Rune Base looked right). `./gradlew build` produces `build/libs/RandomThings-MC1.20.6-5.0.jar`.
- **What changed (details in the gotchas file):** item NBT -> `minecraft:custom_data` through `util/StackTag` (copy-on-read: `edit`/`set` to write; the only typed component is the buckets' `FLUID`), `@ObjectHolder`/`IForgeRegistry` replaced by `lib/ObjectHolder`+`ObjectHolderInjector`+`RTRegistry`, capabilities registered in `lib/ModCapabilities` and read through `util/CapUtil`, networking as `CustomPacketPayload`s (`IRTMessage` keeps `read/write`), `Block.use` split (`useWithoutItem`/`useItemOn`), `HolderLookup.Provider` on block entity/saved data/handler (de)serialization, data pack formats (`id`, `c:` tags, `neoforge:components`, `set_components`, `neoforge/` paths), enchantment/armor material/tool tier/projectile item definitions, `EffectParticleModificationEvent` for the Magic Hood, tick events split.
- **Fixed on the way (worth mirroring to older branches):** `FireBlockTransformer` matched `tryCatchFire` but the Mojang name is `checkBurnOut`, so Blazing Fire's catch chance/ageing tweak never applied on 1.18.2-1.20.1 (its test skipped); `ArmorLayerTransformer` colour edit rewritten for 1.20.6; `EclipsedClockAnimationMessage` had never been registered.
- **Copy-on-read bugs found while converting (all fixed, one guarded by a new test):** `StackTag.get(...).put(...)` style writes are lost; `StackTag.set(stack, new CompoundTag())` removes the component (Redstone Tool NPE, caught by `behavior.stack_data.redstone_tool_...`); Emerald Compass, Sound Recorder and Time in a Bottle wrote into copies. New tests: `behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load` and three `behavior.stack_data.*`.
- **Real install (2026-10-20):** NeoForge 20.6.141 installer downloaded to `prod-test/1.20.6/` (untracked), server installed; the built jar on that real server: suite 379 passed / 0 failed / 3 skipped (same as dev). The client was installed too (`run-client.py` and `fetch-libs.py` in `docs/testing/prod-test-scripts/` now target NeoForge installs and the Gradle-cached assets; vanilla libraries fetched from Mojang): production client suite 383 passed / 0 failed / 1 skipped, audit 0 failures, all 26 render scenes render without exceptions.
- **Desktop playtest (2026-10-20, `SCN_MC=1.20.6`): all 28 scenarios pass on the dev client** (in two sittings; the second after the fixes below). `scn.py` supports 1.20.6 (`sdata`/`stack_nbt` helpers for components, `active_effects`, fluid NBT, Save and Quit at y 383, chat commands must stay under 256 characters; copy kept in `docs/testing/desktop-helper/`). Real port bugs the playtest found that no suite had, all fixed: the ItemRenderer coremod anchor (dyed items were not tinted; now anchors on `FastColor.ARGB32.alpha`); `randomTick` no longer calls `tick` since 1.20.5 (Colored Grass, Fertilized Dirt, Glowing Mushroom, Lotus, Spectre Sapling were inert; forwarded explicitly); `DyeingMachineScreen` left the swatch tint set so every inventory item drew tinted (reported by the owner while watching); the Redstone Remote scenario setup exceeded the 256-character chat limit.
- **Nothing else outstanding** except the manual TESTING_CHECKLIST rows and committing. `behavior.portkey...teleports_the_picker_upper` failed once, right after a server-watchdog-killed run left debris in the reused test world; not reproducible in 6 further full runs (see the plan file). The Blazing Fire block draws as a solid cube in the block scene (also on 1.20.1).
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`).

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.
- **Time Accelerator wheel (2026-10-29):** the accelerated block shows the spinning decagon wheel of 1.12.2 on all six faces (rings grow with the time rate) instead of the floating Time in a Bottle icons. Geometry and colours are one version-neutral file, `client/renderer/TimeWheel.java` (identical on every branch); each branch only has its own `TimeAcceleratorEntityRenderer` that transforms the triangles and draws them unlit and translucent. Render scenes `time_wheel` (six rates in a row) and `time_wheel_close`.

## What to do next

1. Manual checks: `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` builds the manual test world (`docs/testing/manual-test-world.md`). Review/commit when the owner says so; then the TESTING_CHECKLIST section 7 rows by hand (the scripted playtest is done).
2. Optionally mirror the three fixes above to `1.18.2`/`1.19.2`/`1.20.1` (`branch-mirroring` skill).

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
