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
