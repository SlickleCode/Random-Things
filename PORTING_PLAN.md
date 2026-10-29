# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete (this branch) | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port, complete in code, pushed | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port, complete (JDK 17, Mojang names) | `docs/migration/plans/1.18.2-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 8), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Audit bug fixes (2026-10-29, unverified)

A wiki audit (branch `wiki`) found behaviour bugs; 12 fixes were written on this branch **without a compiler or Forge jars (cloud session, no JDK 8, no Maven access) - build, run both suites and retest before trusting them**:
Redstone Tool plain-click linking, Online Detector case sensitivity, Lotus Seeds on farmland only, Peace Candle denies slimes, Spectre Lens per player per dimension, Ender Letter store always the
overworld's, Sound Box / Slime Cube powered flags saved, Advanced Redstone Torch `markDirty`, Potion Vaporizer drops its contents, Ender Bucket drain check, Eclipsed Clock target time, Stable Ender Pearl guard.
**Not done:** Peace Candle churches in every village type (needs the vanilla village pool / church piece names of desert, savanna, snowy and taiga villages, which this repo does not reference - read them from the mapped jar).
Rows AF1-AF12 in `TESTING_CHECKLIST.md`; details in `docs/project/audit-fixes-2026-10-29.md`.

**Round 2 (same day, also unverified):** added the Spectre Sapling conversion, Runic Pattern from paper, Luminous Powder recipe (glow rendering still missing), Numbered Spectre Coils and Blackout Powder;
fixed Floo (unloaded chunk, duplicate names), Stable Ender Pearl dimensions, Position Filter dimensions, Observer reload output and the Analog Emitter input side. Rows AG1-AG11 in `TESTING_CHECKLIST.md`. The Magic Bean stalk now breaks blocks in its way (row AG12). What the cloud session could not do: `docs/project/cloud-session-limits-2026-10-29.md`.

## Current state (2026-10-14)

- **Port:** every wiki-documented feature is ported on both branches except the removed / will-not-migrate ones
  (`docs/project/scope-decisions.md`, `docs/project/wiki-feature-status.md`; the Precious Emerald and its rainbow glint are explicitly not ported).
- **Testing sheet finished by the user (2026-10-11), cleaned 2026-10-14:** 392 rows - 227 PASS, 44 DONE/REMOVED (features dropped), 16 FIXED, 105 never tested, **0 FAIL**.
  90 of the 121 rows that were still open are now covered by the suites/scenarios and were removed from `TESTING_CHECKLIST.md` (`docs/testing/covered-by-tests.md` lists them with their
  evidence); 31 open rows remain (#261, #389, and 29 never-tested ones) plus a short section D of visual/audio spot-checks (glints and dyes, Portkey glow, Fluid Display, Light Redirector, runes, Biome Radar).
- **Automated:** client and dedicated-server suites 348 passed (1 environment-dependent skip), resource audit 0 failures, GUI click-through
  (buttons, text fields, slot tooltips), render-scene screenshots, 26 scripted in-game scenarios (`docs/testing/desktop-helper/`, `python -u scn.py all`), and the same
  suites/scenarios on a *production* (SRG-named) Forge server and client in `prod-test/` (all coremods verified). Everything is dev-only; see `docs/testing/README.md` and `docs/testing/test-inventory.md`.
- **More automated coverage (2026-10-12):** `GlintColors` (side-neutral glint colour logic, unit test `behavior.glint.colour_priority_matches_1_12_2`), Portkey `hasEffect` assertions, dev `ParticleProbe` (Magic Hood), scenarios `redstone_remote_edit`, `block_destabilizer_lazy`, `falling_block_obstruction`, `rune_rendering` (mutation-checked: fails if rune noise is re-rolled per frame).
- **Enchant-glint colours (#390):** new `ClientAsmHandler.enchantmentColorHook` + `ItemRendererTransformer` patch (renderItem stack capture, glint colour constant in `lambda$renderItem$0`): Dyeing Machine glint, Redstone Tool, Spectre Key/Sword, Portkey, Escape Rope. Worn-armor glint hooked via `ArmorLayerTransformer` (second `color4f` in `ArmorLayer.func_215338_a` + stack capture before that call). Client suite 347/0 after the change; verified on the production (SRG) client 2026-10-12: client suite 348/0, server 346/0, and the dyeing, worn-armor, biome-radar and rune scenarios (`SCN_RUN=prod-test/1.14.4/client-run`).
- **Biome Radar crystal render (#389):** now spins in place inside the block like 1.12.2 (was hovering above it).
- **Scripted playtest (2026-10-11):** a deterministic helper drives the real game (`docs/testing/desktop-helper/`, 26 scenarios, `python -u scn.py all`); it verified most of the playtest route (#121, #149-153, #158-160, #169-179, #209-215 except #211/#213, #234, #238, #258/#259, #265, #277, #279, #284, #287, #288, #293/#294, #319, #321-324, #343/#344, #356/#357, #387) and found the missing imbue effect icon (fixed, #388). Still manual: audio/visual checks, #211 (needs an online name lookup), #213 (2 players), #295/#296, Spirit, Runes, Spectre Coil, GUI-bound steps of #287.
- **Worldgen bug found by the tests (2026-10-11):** the Glowing Mushroom dropped itself on its first neighbour update and could not spread (own light 15 vs its "light < 13" rule): light is now 12 (`GlowingMushroomBlock.GLOW_LEVEL`), worldgen uses `canGrowDuringWorldgen`; checklist #387 awaits retest.
- **Worldgen:** Glowing Mushroom and Peace Candle church generation are covered by deterministic `worldgen.*` tests (real terrain + village assembly), so rows #49/#270/#341/#356b need no manual hunting.
- **Nothing uncommitted;** everything is pushed (`origin/1.14.4`). The Sound Dampener tooltip fix, imbue icon fix, glint colours and the documentation layout are mirrored to `1.16.5`.
- 2026-10-17: new worldgen tests (feature registration, bean sprout/lotus/furnace rules, heating, client biome resync via `ClientProbe`, Spectre generator); 1.14.4 biomes are per column so the heating bug of newer versions does not exist here. `worldgen.*` 22/0/0 on the client, server suite 372/0/2.
- 2026-10-17: Bean Sprout now also survives on grass, dirt, coarse dirt, podzol and farmland (1.12.2's plain-type plant rule; CropBlock alone allowed farmland only, so it almost never generated naturally). Test `worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block`.

- **Tests mirrored from 1.20.6 (2026-10-21):** `behavior.loot.rolled_chest_biome_crystals_carry_a_biome`, `behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load`, `behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it`; dedicated-server suite now 377/0/2 (of 379; no biome-radar search test on this branch), 0 failures. Mutation-checked on 1.20.1 (dropping the random-biome function, the chest pool or the radar update tag each fails its test). The manual world (`/rtdebug manual`, `RT_MANUAL=true`) is on this branch too: `docs/testing/manual-test-world.md`.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`). Also ported: `behavior.biome_radar.search...` and `behavior.render.see_through...` (both mutation-checked); client-suite test runner fixes (survival abilities during runs, real client player position, stale-server guard) that 1.19.2 already had.

- **Museum report fixes (2026-10-24):** Super Lubricent blocks use the glass (ice) sound type; Blazing Fire is a cutout block like vanilla fire (already inherited on 1.14.4). Also: Spirit Spawn Egg, Time in a Bottle icons full-bright, Spectre Illuminator client playtest scenario. Checklist rows M1-M6.
- **Time Accelerator wheel (2026-10-29):** the accelerated block shows the spinning decagon wheel of 1.12.2 on all six faces (rings grow with the time rate) instead of the floating Time in a Bottle icons. Geometry and colours are one version-neutral file, `client/renderer/TimeWheel.java` (identical on every branch); each branch only has its own `TimeAcceleratorEntityRenderer` that transforms the triangles and draws them unlit and translucent. Render scenes `time_wheel` (six rates in a row) and `time_wheel_close`.

## What to do next

1. Check `TESTING_CHECKLIST.md` for `FAIL` rows, then the open rows (B, C) and the section D spot-checks; don't re-investigate a `FIXED` row without reading its entry in `docs/testing/fix-log/`.
2. Remaining manual checks are the visual/audio ones and the online/two-player rows listed in `docs/testing/playtest-route-1.14.4.md` ("left for your eyes and ears").
3. Otherwise **ask the user which area to tackle**. After every change: full `./gradlew build`, update the checklist + this file, leave uncommitted unless told to commit (mirror to `1.16.5` with `branch-mirroring`).
4. Open design questions: `docs/migration/open-questions-backlog.md`.

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
