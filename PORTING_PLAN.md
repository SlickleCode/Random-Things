# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port (this branch, complete) | `docs/migration/plans/1.18.2-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 17 on this branch; JDK 8 on 1.14.4 / 1.16.5), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Audit bug fixes (2026-10-29, unverified)

The 12 fixes of the wiki audit (1.14.4 commit `03be7448`) were mirrored here **without a compiler or Forge jars (no JDK 17 / network in that session) - build, run both suites and retest before trusting them**; every call was copied from existing code on this branch:
Redstone Tool plain-click linking, Online Detector case sensitivity, Lotus Seeds on farmland only, Peace Candle denies slimes, Spectre Lens per player per dimension, Ender Letter store always the
overworld's, Sound Box / Slime Cube powered flags saved, Advanced Redstone Torch `setChanged`, Potion Vaporizer drops its contents, Ender Bucket drain check, Eclipsed Clock target time, Stable Ender Pearl guard.
**Not done:** Peace Candle churches in every village type (blocked). Rows AF1-AF12 in `TESTING_CHECKLIST.md` section G; details in `docs/project/audit-fixes-2026-10-29.md`.

**Round 2 (same day, also unverified):** added the Spectre Sapling conversion, Runic Pattern from paper, Luminous Powder recipe (glow: nothing to port, the 1.18+ glint shaders ignore the lightmap so the glint is always full-bright; see version-notes.md), Numbered Spectre Coils and Blackout Powder;
fixed Floo (unloaded chunk, duplicate names), Stable Ender Pearl dimensions, Position Filter dimensions, Observer reload output and the Analog Emitter input side. Rows AG1-AG11 in `TESTING_CHECKLIST.md` The Magic Bean stalk now breaks blocks in its way (row AG12). What the cloud session could not do: `docs/project/cloud-session-limits-2026-10-29.md`. section H.

**Wave 3 parity fixes (2026-11-13, unverified in game):** Block Breaker now breaks normally and drops in the world (no Magnetic pickaxe, no rear-inventory delivery; test `behavior.block_breaker.mines_front_block_and_drops_it_in_the_world`), rows AW1-AW6 in `TESTING_CHECKLIST.md`: Blazing Fire delay 15 + rand(10), chest boats in the Super Lubricent cap, Fertilized Dirt for sugar cane / cactus (1.21.x), Imbue on-hit through `LivingDamageEvent.Pre` (1.21.x), Spirit `/kill` + real death at expiry (1.20.1+). Not done: Trigger Glass sand/gravel fall-through (needs a FallingBlock coremod), the Magnetic config gate on 1.21.x (data-driven enchantment), Ancient Furnace biome list (kept per version).

**Parity fixes (2026-11-14, server suites green, not looked at in a game):** Trigger Glass lets sand and gravel fall through while triggered again (`FallingBlockTransformer` on `FallingBlock.isFree`, hook `AsmHandler.openTriggerGlass`; test `coremod.trigger_glass.sand_and_gravel_fall_through_it_only_while_triggered`, row AW9). Audit items that were not bugs: Blazing Fire tick delay (already patched by `FireBlockTransformer`), chest boats (a `ChestBoat` is a `Boat`), Ancient Furnace biome list (1.18 removed the hills biomes). Also checked and not bugs: Fertilized Dirt boost (1.21.x already covers sugar cane and cactus), weather cloud / bean stalk / Bean Sprout / Portkey height limits (they follow the world build height, which is what 1.12.2 `getHeight()` meant), Spirit expiry (uses `kill()`, `GENERIC_KILL` accepted), Imbue on-hit (`LivingDamageEvent.Pre` fires at the same point as 1.14.4 `LivingHurtEvent`; direct-entity test equals the old indirect-source exclusion), Rain Shield hook set (version-specific by design).

**Spirit Spawn Egg parity (2026-11-14, server suites green, not looked at in a game):** test `parity.spirit_spawn_egg.sets_a_spawner_and_spawns_from_a_dispenser` (row AW11) checks that the egg switches a Mob Spawner and works from a Dispenser; it passes on this branch (the loader/vanilla egg already does both). Spirit moon bonus: the End (fixed time) no longer counts as night, as on 1.14.4 / 1.12.2 (row AW12). Rain Shield and the Spirit entity were compared on all nine branches: same logic (only the hook locations differ: 1.14.4 `GameRenderer`, 1.16.5-1.21.1 `WorldRenderer`/`LevelRenderer`, 1.21.4+ the unified precipitation lookup). Known gap on every branch: 1.12.2 had a `RainShieldRange` option, the ports use 80 blocks fixed.

**Spirit End moon bonus (2026-11-14):** the Spirit moon bonus applies under open sky at night and in the End on every branch (`RandomThings.spiritMoonTime`; the End has a fixed time, so 1.14.4 never gave it and 1.16.5+ gave it by accident, then briefly not at all). Test `parity.spirit.moon_bonus_applies_in_the_end`, row AW12; wiki ectoplasm and configuration pages say so.

**Village survey (2026-11-14, server-side):** per village type the candle share of churches is 18-54% over 150 assembled villages each (the test builds 50 per type) (plains ~35%, savanna ~33%, snowy ~30%, taiga ~30%, desert ~30% but only ~10-20 churches per 150 villages; vanilla desert and taiga villages almost never hold a church). A dev-only real-world survey (`debug/test/VillageSurvey.java`, run with `bash docs/testing/survey-scripts/village_survey.sh default`; seed 424242, grid probes, fresh world, writes `run/randomthings_villages.txt`) found the candle block standing in the nearest candle church of every type that had one. The survey needed no gradle change: the script sets the system property through `JAVA_TOOL_OPTIONS` and patches `run/server.properties` (restored afterwards).

**Spectre rock blocks (2026-11-14):** the Energy Injector and the five Spectre Coils need a pickaxe to drop, as in 1.12.2 (`Material.ROCK`) (`.setRequiresTool()` + `harvestTool(PICKAXE)` on 1.16.5, `.requiresCorrectToolForDrops()` plus a `minecraft:mineable/pickaxe` tag file from 1.18.2). Test `parity.spectre_rock_blocks.need_a_pickaxe_to_drop`, row AW13. Other "suspected" audit items (Spectre dimension, Rune Dust support, Escape Rope landing, Magnetic tool set) are still unchecked.

**Peace Candle churches in code (2026-11-14, unverified in a real world):** `PeaceCandleChurchPool` now wraps the `houses` and `zombie/houses` pools of all five village biomes and swaps each vanilla `*_temple_*` church for the same template plus a `PeaceCandleProcessor` (registered processor type, so it survives save/reload) that turns one floor air cell into the candle; the cell is chosen from the vanilla template (inside, roofed, most central). `peace_candle_church.nbt` is deleted. Tests: `worldgen.peace_candle.every_village_biome_church_gets_a_candle_variant_with_a_spot_inside` and the reworked pool/assembler tests; row AW8.

## Current state on this branch (`1.18.2`, 2026-10-15) - PORT IN PROGRESS

- **Branch:** cut from `1.16.5` (`c4a6a04`) on 2026-10-15; toolchain Forge 1.18.2-40.3.12 / ForgeGradle 5.1 / Gradle 7.4.2 / JDK 17 (`source env/activate.sh` finds `~/.jdks/temurin-17*`), official Mojang mappings. Plan, milestone status and every adaptation: `docs/migration/plans/1.18.2-port-plan.md`; the rename/conversion tools: `docs/migration/tools/README.md`.
- **Compiles and `./gradlew build -x test` passes.** The dedicated server boots with the mod and the dev suite runs there: **349 passed / 0 failed / 2 skipped** (the same 2 environment skips as 1.16.5), including the Peace Candle church swap in 60 villages assembled through vanilla's own village start (29 churches, 11 of them Peace Candle).
- **Client (first boot 2026-10-15):** starts, loads the flat test world and runs the suite hands-free: resource audit 0 failures (147 blocks, 241 items, 222 recipes), dev suite 348 passed / 2 failed (the village tests, fixed afterwards on the server) / 1 skipped. One real bug found and fixed: the boat world-tick listener cast `ClientLevel` to `ServerLevel` (1.18 posts `WorldTickEvent` on the client too).
- **GUI click test (2026-10-15):** 28 GUIs, 37 clicks, 0 failures, 17 slot tooltips verified. **Render scenes:** all 26 run without errors (`run/screenshots/randomthings_debug/scenes`); compared side by side with the same scenes on 1.16.5 (blocks_0, rune_base, blazing_fire, diaphanous_near, biome_radar): identical except one regression, fixed - the Rune Base and Fluid Display renderers used the colour-less position/texture shader with a colour vertex format (runes were white); they now use `getPositionTexColorShader`. The black flat blocks in `blocks_0` are the same on 1.16.5 (not a bug). Also set shaders for the Redstone Observer line renderer (`rendertype_lines` + normals) and `RenderUtils` (colour shader), which had none - not seen on screen yet. Real bug found by the scenes: 1.18 delivers an empty block-entity update tag as null (`pkt.getTag()`), which crashed the Redstone Observer's `onDataPacket`; all 8 overrides now load an empty tag instead.
- **Not done / not verified:** how everything the scenarios do not measure LOOKS (screens' layout, the Spectre sky), no manual pass. The 1.16.5 rows of `TESTING_CHECKLIST.md` are carried over and unverified on this branch.
- **Scripted playtest on 1.18.2 (2026-10-15): all 26 scenarios pass** after adapting `scn.py` (`SCN_MC=1.18`; see skill `scripted-playtest`). It found two real bugs the suites had missed: `ItemRendererTransformer` targeted `ItemEntityRenderer` (a rename script had been applied twice; dye/glint tint never applied) and `DyeColor.getColorValue` (no 1.18 equivalent; `DyeUtil.rgb` rebuilds it) - fixed. New offline tool `docs/migration/tools/CoremodCheck.java` verifies every transformer applies.
- **Production (SRG) check done 2026-10-15** (`prod-test/1.18.2`): server suite 349/0/2, client suite 350/0/1, 26 render scenes without errors - the 15 retargeted coremods and the SRG reflection ids work in the release jar.
- Everything is uncommitted.
- 2026-10-17: Ancient Furnace heating melted snow/ice in only ~1 in 16 columns (a 4x4 biome cell converted on its first column hid the other 15 from the cold check); fixed in `AncientFurnaceTileEntity.warmSurroundings` (classify all columns, then write). New worldgen tests (feature registration, bean sprout/lotus/furnace rules, heating, client biome resync via `ClientProbe`, Spectre generator); `worldgen.*` 22/0/0 on the client, server suite 373/0/3.
- 2026-10-17: Bean Sprout now also survives on grass, dirt, coarse dirt, podzol and farmland (1.12.2's plain-type plant rule; CropBlock alone allowed farmland only, so it almost never generated naturally). Test `worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block`.
- 2026-10-18 (user-reported, fixed, checked by screenshots and playtest scenario `render_fixes_18`): Advanced Redstone Torch had a black box (no render layer; now cutout in `RenderLayerSetup`); Sound Recorder rows never highlighted (the custom `SoundNameList.render` skips vanilla's private `hovered`; each row now hit-tests itself); Fluid Display drew over blocks in front of it (renderer now sets depth test/func/mask explicitly; root cause not isolated, the explicit state fixes it). Not yet run on 1.19.2/1.20.1 or the production jar.

- **Tests mirrored from 1.20.6 (2026-10-21):** `behavior.loot.rolled_chest_biome_crystals_carry_a_biome`, `behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load`, `behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it`; dedicated-server suite now 378/0/3 (of 381), 0 failures. Mutation-checked on 1.20.1 (dropping the random-biome function, the chest pool or the radar update tag each fails its test). The manual world (`/rtdebug manual`, `RT_MANUAL=true`) is on this branch too: `docs/testing/manual-test-world.md`.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`). Also mirrored from 1.19.2: client-suite test runner fixes (survival abilities during runs, real client player position, stale-server guard).

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (`RenderLayerSetup`). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.
- **Time Accelerator wheel (2026-10-29):** the accelerated block shows the spinning decagon wheel of 1.12.2 on all six faces (rings grow with the time rate) instead of the floating Time in a Bottle icons. Geometry and colours are one version-neutral file, `client/renderer/TimeWheel.java` (identical on every branch); each branch only has its own `TimeAcceleratorEntityRenderer` that transforms the triangles and draws them unlit and translucent. Render scenes `time_wheel` (six rates in a row) and `time_wheel_close`.

## What to do next

1. **Boot the client** (needs the owner's go-ahead for an uninterrupted window): `RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`; fix whatever the first boot shows (model baking, screens, entity/block-entity renderers, the tinted glint render types, Spectre sky registration).
2. Resource audit, GUI click-through, render scenes, scripted scenarios on the 1.18.2 client; (the production SRG jar check is done, see above).
3. Re-run the client suite (village tests were fixed after the client run), then the manual pass.
4. After every change: full `./gradlew build`, update the plan + this file, leave uncommitted unless told to commit.

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
