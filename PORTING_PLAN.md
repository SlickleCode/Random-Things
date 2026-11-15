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
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 21 on this branch; JDK 17 on 1.18.2-1.20.1, JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Audit bug fixes (2026-10-29, unverified)

A wiki audit (branch `wiki`) found behaviour bugs; the 12 fixes of the `1.14.4` commit `03be7448` were mirrored onto this branch **without a compiler or NeoForge jars (cloud session, no build) - build, run both suites and retest before trusting them**:
Redstone Tool plain-click linking, Online Detector case sensitivity, Lotus Seeds on farmland only, Peace Candle denies slimes, Spectre Lens per player per dimension, Ender Letter store always the
overworld's, Sound Box / Slime Cube powered flags saved, Advanced Redstone Torch `setChanged`, Potion Vaporizer drops its contents, Ender Bucket drain check, Eclipsed Clock target time, Stable Ender Pearl guard.
**Not done:** Peace Candle churches in every village type (blocked, see the 1.14.4 note). Rows AF1-AF12 in `TESTING_CHECKLIST.md` section G; details in `docs/project/audit-fixes-2026-10-29.md`.

**Round 2 (same day, also unverified):** added the Spectre Sapling conversion, Runic Pattern from paper, Luminous Powder recipe (glow rendering still missing), Numbered Spectre Coils and Blackout Powder;
- **Luminous Powder glint (2026-10-29, no code change):** the full-bright glint needs no port on this branch; all glint render types are lightmap-independent (details in `docs/project/coremods.md`). Visual check outstanding (checklist row AG3).
fixed Floo (unloaded chunk, duplicate names), Stable Ender Pearl dimensions, Position Filter dimensions, Observer reload output and the Analog Emitter input side. Rows AG1-AG11 in `TESTING_CHECKLIST.md` The Magic Bean stalk now breaks blocks in its way (row AG12). What the cloud session could not do: `docs/project/cloud-session-limits-2026-10-29.md`. section H.

**Wave 3 parity fixes (2026-11-13, unverified in game):** Block Breaker now breaks normally and drops in the world (no Magnetic pickaxe, no rear-inventory delivery; test `behavior.block_breaker.mines_front_block_and_drops_it_in_the_world`), rows AW1-AW6 in `TESTING_CHECKLIST.md`: Blazing Fire delay 15 + rand(10), chest boats in the Super Lubricent cap, Fertilized Dirt for sugar cane / cactus (1.21.x), Imbue on-hit through `LivingDamageEvent.Pre` (1.21.x), Spirit `/kill` + real death at expiry (1.20.1+). Not done: Trigger Glass sand/gravel fall-through (needs a FallingBlock coremod), the Magnetic config gate on 1.21.x (data-driven enchantment), Ancient Furnace biome list (kept per version).

**Parity fixes (2026-11-14, server suites green, not looked at in a game):** Trigger Glass lets sand and gravel fall through while triggered again (`FallingBlockTransformer` on `FallingBlock.isFree`, hook `AsmHandler.openTriggerGlass`; test `coremod.trigger_glass.sand_and_gravel_fall_through_it_only_while_triggered`, row AW9). The `MagneticEnchantment` config now also removes Magnetic from the enchanting table, trades and random loot (data condition `randomthings:magnetic_enabled`, row AW10). Audit items that were not bugs: Blazing Fire tick delay (already patched by `FireBlockTransformer`), chest boats (a `ChestBoat` is a `Boat`), Ancient Furnace biome list (1.18 removed the hills biomes). Also checked and not bugs: Fertilized Dirt boost (1.21.x already covers sugar cane and cactus), weather cloud / bean stalk / Bean Sprout / Portkey height limits (they follow the world build height, which is what 1.12.2 `getHeight()` meant), Spirit expiry (uses `kill()`, `GENERIC_KILL` accepted), Imbue on-hit (`LivingDamageEvent.Pre` fires at the same point as 1.14.4 `LivingHurtEvent`; direct-entity test equals the old indirect-source exclusion), Rain Shield hook set (version-specific by design).

**Spirit Spawn Egg parity (2026-11-14, server suites green, not looked at in a game):** test `parity.spirit_spawn_egg.sets_a_spawner_and_spawns_from_a_dispenser` (row AW11) checks that the egg switches a Mob Spawner and works from a Dispenser; it passes on this branch (the loader/vanilla egg already does both). Spirit moon bonus: the End (fixed time) no longer counts as night, as on 1.14.4 / 1.12.2 (row AW12). Rain Shield and the Spirit entity were compared on all nine branches: same logic (only the hook locations differ: 1.14.4 `GameRenderer`, 1.16.5-1.21.1 `WorldRenderer`/`LevelRenderer`, 1.21.4+ the unified precipitation lookup). Known gap on every branch: 1.12.2 had a `RainShieldRange` option, the ports use 80 blocks fixed.

**Spirit End moon bonus (2026-11-14):** the Spirit moon bonus applies under open sky at night and in the End on every branch (`RandomThings.spiritMoonTime`; the End has a fixed time, so 1.14.4 never gave it and 1.16.5+ gave it by accident, then briefly not at all). Test `parity.spirit.moon_bonus_applies_in_the_end`, row AW12; wiki ectoplasm and configuration pages say so.

**Village survey (2026-11-14, server-side):** per village type the candle share of churches is 18-54% over 150 assembled villages each (the test builds 50 per type) (plains ~35%, savanna ~33%, snowy ~30%, taiga ~30%, desert ~30% but only ~10-20 churches per 150 villages; vanilla desert and taiga villages almost never hold a church). A dev-only real-world survey (`debug/test/VillageSurvey.java`, run with `bash docs/testing/survey-scripts/village_survey.sh minecraft\:normal`; seed 424242, grid probes, fresh world, writes `run/randomthings_villages.txt`) found the candle block standing in the nearest candle church of every type that had one. The survey needed no gradle change: the script sets the system property through `JAVA_TOOL_OPTIONS` and patches `run/server.properties` (restored afterwards).

**Spectre rock blocks (2026-11-14):** the Energy Injector and the five Spectre Coils need a pickaxe to drop, as in 1.12.2 (`Material.ROCK`) (`.setRequiresTool()` + `harvestTool(PICKAXE)` on 1.16.5, `.requiresCorrectToolForDrops()` plus a `minecraft:mineable/pickaxe` tag file from 1.18.2). Test `parity.spectre_rock_blocks.need_a_pickaxe_to_drop`, row AW13. Other "suspected" audit items (Spectre dimension, Rune Dust support, Escape Rope landing, Magnetic tool set) are still unchecked.

**Peace Candle churches in code (2026-11-14, unverified in a real world):** `PeaceCandleChurchPool` now wraps the `houses` and `zombie/houses` pools of all five village biomes and swaps each vanilla `*_temple_*` church for the same template plus a `PeaceCandleProcessor` (registered processor type, so it survives save/reload) that turns one floor air cell into the candle; the cell is chosen from the vanilla template (inside, roofed, most central). `peace_candle_church.nbt` is deleted. Tests: `worldgen.peace_candle.every_village_biome_church_gets_a_candle_variant_with_a_spot_inside` and the reworked pool/assembler tests; row AW8.

## Current state on this branch (`1.21.1`, 2026-10-22)

- **Branch:** cut from `1.20.6` (`b0291e3`); NeoForge 21.1.264 / NeoGradle 7.0.192 / Gradle 8.13 / JDK 21, Mojang names everywhere. Plan: `docs/migration/plans/1.21.1-port-plan.md`; every API change: `docs/project/api-gotchas-1.21.1.md`.
- **Automated (dev, 2026-10-22):** dedicated-server suite 380 passed / 0 failed / 4 skipped (of 384; the 4th is the real-terrain test that needs a normal world) plus a normal-world pass 1 / 0 / 0; client suite 383 / 0 / 1, resource audit 147 blocks / 241 items / 222 recipes, 0 failures; GUI click test 28 GUIs / 37 clicks / 0 failures; 26 render scenes (checked by eye: dyed armor and items, light redirector, rain shield, blazing fire, magic hood); the manual world (`RT_MANUAL=true`) builds and prints the biome distances.
- **What changed:** see the gotchas file. Main items: `ResourceLocation` factories, the vertex-builder rework, data-driven Magnetic enchantment (config toggle now only disables the effect), plantable API replaced by `canSustainPlant(..., plantState)` + `TriState`, `DimensionTransition` for Spectre/End portal teleports, `CraftingInput` recipes, singular data folders and `pack_format` 34.
- **Found by a render scene only:** NeoForge 21 draws armor through the 12-argument `renderArmorPiece`, so the Dyeing Machine armor tint stayed off with the old hook placement; fixed (both overloads patched, colour is an int now).
- **Terrain-generation test (2026-10-22):** `worldgen.glowing_mushroom.real_terrain_generation_places_patches_underground` loads real chunks (16 areas of 3x3, 9 mushrooms in 5 chunks, mutation-checked) and therefore needs a normal-terrain world: it skips in the flat suite and runs in the second server pass that `suite-all.sh` does when `docs/testing/normal-world-pass.txt` exists (`level-type=minecraft:normal`, seed 424242, `RT_AUTOTEST_NORMAL=true`). Details: `api-gotchas-1.21.1.md`, Tests.
- **Real install (2026-10-22):** NeoForge 21.1.264 installer downloaded to `prod-test/1.21.1/` (untracked); the built jar on the real server: suite 380 / 0 / 4, on the real client 382 / 0 / 2 with the audit clean and 26 render scenes (armor tint correct).
- **Desktop playtest (2026-10-22, `SCN_MC=1.21.1`): all 28 scenarios pass on the dev client.** `scn.py` needed two 1.21 helper fixes (Singleplayer button y=203; summon attribute NBT in the new `attributes:[{id,base}]` form, otherwise the rain-shield pig kept 10 max health); no mod bug turned up.
- **Not done / not verified:** nothing outstanding except the manual checklist rows.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`).

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.
- **Time Accelerator wheel (2026-10-29):** the accelerated block shows the spinning decagon wheel of 1.12.2 on all six faces (rings grow with the time rate) instead of the floating Time in a Bottle icons. Geometry and colours are one version-neutral file, `client/renderer/TimeWheel.java` (identical on every branch); each branch only has its own `TimeAcceleratorEntityRenderer` that transforms the triangles and draws them unlit and translucent. Render scenes `time_wheel` (six rates in a row) and `time_wheel_close`.

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
