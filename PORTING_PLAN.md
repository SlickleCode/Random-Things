# Random Things port - START HERE (session handoff)

Porting the Minecraft mod Random Things (Lumien) from 1.12.2 to newer Forge versions, on the fork
`SlickleCode/Random-Things`. Working rules: [`CLAUDE.md`](CLAUDE.md). Everything detailed lives under `docs/` (map below).
**Update this file and `TESTING_CHECKLIST.md` whenever features change.**

## Branches and toolchain

| Branch | What | Docs |
| --- | --- | --- |
| `1.14.4` | Forge 28.2.26 port, complete | this file, `docs/project/` |
| `1.16.5` | Forge 36.2.39 port (this branch) | `docs/migration/plans/1.16.5-port-plan.md` |
| `1.18.2` | Forge 40.3.12 port, complete (JDK 17, Mojang names) | `docs/migration/plans/1.18.2-port-plan.md` |
| `master` | untouched 1.12.2-era source (`git show origin/1.12.2:<path>`) | |
| `main` | landing README only | |

`source env/activate.sh` (pins JDK 8), then `./gradlew build` (`-x test` for a quick check). Details: `docs/project/toolchain.md`.
**Folder layout (since 2026-10-14):** the repository is a bare store in the parent folder (`.bare/`) with one git-worktree folder per version branch (`1.14.4/`, `1.16.5/`, `1.18.2/`), each with its own `run/`, `build/` and `prod-test/`. Work inside a version folder and open the other one for the other branch instead of `git checkout`; nothing needs cleaning between versions (see `CLAUDE.md`, skill `branch-mirroring`). Project skills/agents live in `.claude/` (tracked on every branch).

## Current state on this branch (`1.16.5`, 2026-10-14)

- **Port:** the 1.14.4 feature set runs on Forge 36.2.39 (MatrixStack rendering, code-registered Spectre dimension, worldgen features, coremods ported;
  plan and milestones in `docs/migration/plans/1.16.5-port-plan.md`). Everything under `docs/project/` was written for 1.14.4 and still applies except
  where the plan file says otherwise (e.g. `docs/project/api-gotchas-1.14.4.md` is 1.14.4-only).
- **Automated:** client suite 350 passed / dedicated-server suite 350 passed (1 skip each), resource audit 0 failures, GUI click-through 0 failures with
  17 slot tooltips verified (including both Sound Dampener screens), production (SRG) client and server suites 349 passed (run before the Biome Radar test was added), 26 scripted scenarios on the dev client and the render set on the production client. See `docs/testing/`.
- **Mirrored from `1.14.4` on 2026-10-12:** Sound Dampener slot-tooltip fix + the click test that guards it, imbue effect icon fix (#388), controlled test
  world (always day, no weather/mobs), Biome Radar crystal spinning inside the block (#389), `GlintColors` + its unit test, Portkey `hasEffect`
  assertions, dev `ParticleProbe`, and the documentation layout (root files short, detail under `docs/`).
- **Also ported (2026-10-12):** enchant-glint colours (#390) via three coremods - `RenderTypeTransformer` (the seven glint `RenderType` getters return a tinted wrapper type, `TintedGlintRenderType`, for stacks with a custom colour), `ItemRendererTransformer` (captures the stack in `renderItem`), `ArmorLayerTransformer` (captures the worn stack) - because 1.16 glint types use `POSITION_TEX` (colour = GL colour at draw time) and each tinted type needs its own entry in the main buffer source's `fixedBuffers` (otherwise "BufferBuilder not started"). The scripted helper now drives the 1.16.5 menus (`SCN_MC=1.16`); dimension tags are registry strings there (`"minecraft:overworld"`), the max-health attribute is `generic.max_health`.
- **Real 1.16.5 crash found by the scripted Biome Radar scenario:** `BiomeRadarTileEntity.tick` threw an NPE on the server (and never found its biome): a world's biomes are data-pack instances, not the `ForgeRegistries.BIOMES` ones, so `getKey` returned null and `==` never matched. It now resolves names through the world's own biome registry; guarded by `behavior.biome_radar.search_finds_the_crystals_biome_and_a_missing_biome_keeps_searching` (verified to fail without the fix).
- **Verified on 1.16.5 (2026-10-12):** all 26 scripted scenarios pass on the dev client (`SCN_MC=1.16`); the production (SRG) client passes the render scenarios (Fluid Display, crafting, dyeing + glint, worn armor, Biome Radar, runes, falling blocks, Lazy/Reset, Remote edit), plus production server and client suites 349/0. The Ancient Furnace scenario needed a ring of ice spots on 1.16 (`/locatebiome` is approximate and biome edges are fuzzy); the furnace itself works (14 of 14 spots melted).
- **Render fix found while doing it (reported by the user: Fluid Display neighbours showing holes):** 1.16 culls a neighbour's face against any block whose `BlockState.isSolid()` is true (default), independent of the render layer in `RenderLayerSetup`; the 1.14.4 `getRenderLayer()` overrides had no 1.16 equivalent, so every see-through/partial block (28 of them) now sets `Block.Properties.notSolid()`. Guarded by `behavior.render.see_through_blocks_are_not_solid_so_neighbours_keep_their_faces`.
- **Checklist (reset 2026-10-14):** `TESTING_CHECKLIST.md` was rebuilt for this branch: every result is blank (nothing here has been tested by hand), rows covered by the suites/scenarios were removed (`docs/testing/covered-by-tests-1.16.5.md` lists them with their evidence), removed-feature rows dropped; 204 rows + a short visual/audio spot-check table remain.
- 2026-10-17: same Ancient Furnace snow/ice bug fixed here (biome cells are 4x4 in 1.16 too; the new heating test failed without the fix). New worldgen tests as on 1.18.2; `worldgen.*` 22/0/0 on the client, server suite 374/0/2.
- 2026-10-17: Bean Sprout now also survives on grass, dirt, coarse dirt, podzol and farmland (1.12.2's plain-type plant rule; CropBlock alone allowed farmland only, so it almost never generated naturally). Test `worldgen.bean_sprout.places_on_air_above_grass_dirt_and_farmland_but_not_on_stone_or_into_a_solid_block`.

- **Tests mirrored from 1.20.6 (2026-10-21):** `behavior.loot.rolled_chest_biome_crystals_carry_a_biome`, `behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load`, `behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it`; dedicated-server suite now 379/0/2 (of 381), 0 failures. Mutation-checked on 1.20.1 (dropping the random-biome function, the chest pool or the radar update tag each fails its test). The manual world (`/rtdebug manual`, `RT_MANUAL=true`) is on this branch too: `docs/testing/manual-test-world.md`.
- **Coremod guards + render probes (2026-10-23):** every transformer script adds a hook-site marker field (`rt$hooks$<Tag>$<count>`) read by the new `coremod.applied.every_transformer_left_its_hook_marker_on_its_target_class` test (found that the Light Redirector transformer silently did not apply on the NeoForge branches 1.20.6/1.21.1: the `ModelData` descriptor still used the Forge package); render hooks call `HookProbe.hit`, and the scene run (`RT_AUTOTEST_SCENES=true`) writes `randomthings_scenes.txt` with per-scene checks (dyed armor/items, light redirector, magic hood nameplate, rain shield visuals; 5/5 on every branch). Mutation-checked on 1.21.1 (re-creating the armor-overload bug fails `armor_dye_applied`). Also mirrored from 1.19.2: client-suite test runner fixes (survival abilities during runs, real client player position, stale-server guard).

## What to do next

1. **Manual pass by the user on this branch**: work through the rebuilt `TESTING_CHECKLIST.md`, rendering rows first (glass-like blocks and the 28 `notSolid()` blocks beside real neighbours, glints, dyes, Rune Base, Diaphanous Blocks), then worldgen in a normal world. Write a 1.16.5 playtest route listing only what the scripts do not cover.
2. (done 2026-10-14) 1.16 API gotchas: `docs/project/api-gotchas-1.16.5.md`.
3. Add a scenario for every bug the manual pass turns up. Production-check (SRG names) after any coremod change (`prod-jar-check` skill).
4. Otherwise **ask the user which area to tackle**. After every change: full `./gradlew build`, update the checklist + this file, leave uncommitted unless told to commit.
5. Open design questions: `docs/migration/open-questions-backlog.md`.

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
