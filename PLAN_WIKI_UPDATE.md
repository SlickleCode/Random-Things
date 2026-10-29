# Plan: audit the Random Things wiki against the mod and update it

Status: PLAN ONLY (written 2026-10-28). Nothing in the wiki has been changed yet.
Commit/push only when the user says so. The wiki now lives INSIDE the mod repo as its own branch/folder: `.` (this file's own folder; copied from an old standalone folder named `RT_Wiki_remake`, which is obsolete and must not be used). `wiki/` is now a git worktree on branch `wiki` (empty root commit `1f24671`, unrelated to the version branches); the wiki files are in it but UNCOMMITTED (untracked). Use `git -C wiki ...`. Do not commit unless asked.

## 0. Path convention (intentional)

All paths in this document are **relative to the folder containing this file** (the `wiki/` worktree), on purpose: the absolute location of the repo differs between machines and sessions, so absolute paths would go stale. `.` is the wiki, `..` is the `Random-Things` parent, `../1.14.4` is the baseline mod worktree. A path with no prefix (`data/`, `docs/`, `scripts/`) always means the wiki's own folder; mod paths always carry the `../<version>/` prefix. Keep this convention when editing the plan or writing audit files, and resolve the paths from this file's location.

## 1. Goal

Make the wiki an accurate, complete description of what the mod actually does today. Method: analyse the mod's real behavior, compare it with each wiki page, then update the wiki. Cover edge cases (config gating, shared pages, removed features, intentional divergences).

## 2. Decisions already made by the user (binding)

1. **Baseline = the `1.14.4` worktree** (`../1.14.4`). Fact sheets come from there.
2. **No version differences in the wiki.** All nine branches must behave identically (memory: "Branch parity"). Never write "since 1.16", "only on 1.14" etc. If a newer branch behaves differently from 1.14.4, that is a *bug to iron out*: collect it in a "parity bugs" list for the user; do not document it and do not fix it as part of this task.
3. **Removed / won't-migrate features** get their pages moved into a **separate section**: add a category (e.g. id `legacy`, label "Legacy / Not in this mod version") to `data/categories.json`. Page text is kept, with a clear status line at the top. Do not delete them.
4. **Features with no wiki page today get new pages** (registered blocks/items/entities/enchantments/potions/structures/commands/config-only features).
5. Intentional divergences from original 1.12.2 get a *short* note on the page (they are deliberate, listed in `scope-decisions.md`).

## 3. Layout and where things live

### Wiki folder: `.` = `wiki/` (git worktree, branch `wiki`)
- `data/features/*.json` - the ONLY place content lives (100 files). Schema in `data/README.md`:
  required `title`, `category`, `description`; optional `keywords`, `images`, `videos`, `recipe_images`, structured `recipe` (3x3 `shape` + `result`; keys resolve to `textures/item|block|models/armor|vanilla/<key>.png`, then another feature id, else a text label), `properties` (`[{label,value}]`).
- `data/categories.json` - sidebar sections: `item`, `block`, `misc` (each with inline SVG icon). New category = one entry here.
- `data/vanilla_texture_names.json` - overrides for vanilla icon fetching.
- `template-html/` (`base.html`, `sidebar.html`, `page.html`, `index.html`), `scripts/generate_html.py` (+ `constants.py`, `texture_utils.py`, `fetch_vanilla_textures.py`, `fetch_lumien_assets.py`), output goes to `docs/*.html` (generated; GitHub Pages serves `docs/`). `docs/images`, `docs/textures`, `docs/js`, `docs/style` are hand-maintained assets. A GitHub Action regenerates `docs/` on push to master.
- Regenerate: `pip install -r scripts/requirements.txt; cd scripts; python generate_html.py`.
- Content rule: **ASCII only** (a prior commit normalised everything to ASCII).
- The fetch scripts need network; run only if the user allows downloads.
- Existing feature ids (100): advanced_redstone_interface, advanced_redstone_repeater, advanced_redstone_torch, analog_emitter, ancient_furnace, artificial_end_portal, bean, biome_blocks, biome_radar, blaze_and_steel, block_breaker, block_destabilizer, block_of_sticks, chat_detector, chunk_analyzer, colored_grass, compressed_slime_block, contact_button_lever, creative_player_interface, custom_crafting_tables, diaphanous_blocks, divining_rods, dyeing_machine, eclipsed_clock, ectoplasm, emerald_compass, ender_bridge, ender_bucket, ender_letter, entity_detector, entity_filter, escape_rope, fertilized_dirt, floo_teleportation, fluid_display, glowing_mushrooms, golden_compass, golden_egg, igniter, imbuing_station, inventory_rerouter, inventory_tester, iron_dropper, item_collector, item_filter, lapis_glass, lapis_lamp, lava_charm, lava_waders, light_redirector, lotus, luminous_blocks, luminous_powder, magic_hood, magnetic_enchantment, notification_interface, obsidian_skull, obsidian_water_walking_boots, online_detector, peace_candle, pitcher_plant, platforms, player_interface, portable_sound_dampener, portkey, position_filter, potion_vaporizer, quartz_glass, quartz_lamp, rain_shield, rainbow_lamp, redstone_activator, redstone_interface, redstone_observer, redstone_remote, runic_dust, sided_redstone, slime_cube, sound_box, sound_dampener, sound_pattern, sound_recorder, spectre_anchor, spectre_charger, spectre_coils, spectre_illuminator, spectre_key, spectre_lens, spectre_sapling, spectre_tools, stable_ender_pearl, stained_bricks, summoning_pendulum, super_lubricent, super_lubricent_boots, super_lubricent_stone, time_in_a_bottle, trigger_glass, water_walking_boots, weather_egg.

### Mod repo: `..` = the `Random-Things` parent folder (worktree layout, NOT a normal checkout)
- `.bare/` is the repository (the `wiki` folder is a sibling of the version folders); one worktree folder per version: `1.14.4`, `1.16.5`, `1.18.2`, `1.19.2`, `1.20.1`, `1.20.6`, `1.21.1`, `1.21.4`, `1.21.11`. Work inside a version folder. Read the parent `CLAUDE.md` and the version folder's `CLAUDE.md` and `PORTING_PLAN.md` first. Do not `git checkout` other versions; open the other folder. Do not commit/push unless told.
- Source: `../1.14.4/src/main/java/lumien/randomthings/` with packages `asm, block, client, config, container, debug, enchantment, entity, handler, item, lib, network, potion, recipes, tileentity, util, worldgen` (+ `RandomThings.java`). ~372 java files. Data: `src/main/resources/data/randomthings/{loot_modifiers,loot_tables,recipes,structures}`, `data/forge/loot_modifiers`, tags, `assets/randomthings/lang/en_us.json`. Coremods: `src/main/resources/transformer/*.js`, `META-INF/coremods.json`.
- Key docs (every path in this list is under `../1.14.4/`; note the mod's `docs/` is NOT the wiki's generated `docs/`):
  - `WIKI_FEATURE_STATUS.md` + `docs/project/wiki-feature-status.md` (613 lines): per-wiki-feature table of 1.12.2 classes, status, ASM. **Its `DONE-UNTESTED` labels are stale; do not use them as evidence.** Contains the "Not migrating" notes.
  - `docs/project/scope-decisions.md`: binding user decisions and the list of deliberate divergences from 1.12.2 (Spectre Leaf drops Ectoplasm only; Floo Powder shapeless; Colored Grass named "<Color> Grass Block"; Potion Vaporizer / Block Destabilizer flood-fills pass non-full blocks; Global Chat Detector has no op bypass; Rune Base left-click clears whole block; `FlatRunes` config; Super Lubricent speed caps 0.35 b/t blocks, 15 deg/t boats; Trigger Glass chain limit config default 20; Magnetic config-off keeps enchant registered but unobtainable; Remote camo row; Ender Letter message body; Precious Emerald/Festival not ported; ItemFilter replaced by single example-item slots; metadata sub-items became discrete items per variant). Also lists things that looked like bugs but are intentional (see-through Plates, Lapis/Quartz Lamp always lit, Portkey glowing while unbound, chest-less Colored Grass recipe, Golden Compass only reading recipe-set NBT).
  - Removed on request: Plate family (11 blocks), Special Chest, Sakanade, Pitcher Plant, Obsidian Skull / Lava Charm / Lava Waders / Water Walking Boots, Biome Glass, Biome Stone, Inventory Rerouter, Custom Crafting Tables. Will not migrate (final): Item Filter, Ender network / Link Orb, Blackout Powder, Collapse potion, modded-ore divining rods + third-party compat, Festival system, Player Soul / Revive Circle / Rez Stone, Dungeon Chest Generator, Voxel Projector, Nature Core, `RTCommand`, Precious Emerald. Verify each against the status doc before moving a page to Legacy (some wiki pages, e.g. Obsidian/Water Walking Boots, Divining Rods, Biome Blocks, might be partly kept).
  - `docs/project/version-notes.md`, `branches.md`, `coremods.md`, `architecture-conventions.md`, `api-gotchas-1.14.4.md`, `pitfalls.md`, `slice-history.md`.
  - `TESTING_CHECKLIST.md`, `docs/testing/` (`test-inventory.md`, `covered-by-tests.md`, `manual-test-world.md`, `playtest-route-1.14.4.md`, `checklist-archive/`, `fix-log/`, `desktop-helper/`): real evidence of what was verified in game.
- Git history for original behavior: `git show origin/1.12.2:<path>` (use before calling anything a wiki error that might be original behavior).
- "The museum" = user's manual test world (RT_MANUAL runClient, every block on signs + item chests).
- Skills/agents available in the project: `mapping-lookup`, `scripted-playtest`, `branch-mirroring`, `coremod-patching`, `prod-jar-check`; agents `bytecode-scout`, `suite-runner`, `Explore`.
- Environment: Windows, PowerShell/Git Bash, JDK 8 for 1.14.4 (`source env/activate.sh` in bash). Reading source needs no build.

## 4. Phases

### Phase 0 - Setup
1. Read both CLAUDE.md files, `scope-decisions.md`, the removed/won't-migrate lists, memory notes.
2. Check `git -C wiki status` (files are untracked on branch `wiki`; do not commit unless asked). Never touch the old `RT_Wiki_remake` folder.
3. Create a scratch folder for the inventory (e.g. `wiki/audit/`, git-ignored or deleted at the end; ask before committing it).

### Phase 1 - Behavior inventory (read-only)
1. **Mechanical registry listing** from the 1.14.4 source: all blocks, items, tile entities, entities, enchantments, potions, particle types, containers/GUIs, structures/worldgen features, loot modifiers, recipes (json), tags, commands, key bindings, config options with defaults, creative tab contents, advancements if any. Source of names: registries in `RandomThings.java`/`ModBlocks`/`ModItems`-style classes and `en_us.json`.
2. **Map registry entries to wiki pages** (and to the Legacy list). Output three lists: (a) registered but no page, (b) page but nothing registered (removed/legacy), (c) matched.
3. **Per-feature fact sheet**, split into ~6 parallel groups (suggested: redstone blocks/interfaces/detectors; spectre family + dimension/worldgen; lamps/glass/luminous/decor blocks; tools, compasses, rods, boots/armor, enchantment, potion; entities/eggs/pearls/ender items/teleport; sound blocks/items, filters, misc/utility). Each sheet records: exact recipe(s) from `data/.../recipes` (shaped/shapeless, tag ingredients, output count), other ways to obtain (loot, worldgen, drops), GUI and every control, redstone input/output and per-side behavior, state/NBT/stored data, dimension/biome/light/time conditions, ranges/cooldowns/durability/stack sizes, config options with defaults and effect, creative-vs-survival and multiplayer/permission differences, interactions with other features, particles/sounds, and deliberate divergences from 1.12.2. Cite file:line for each fact.
4. **Evidence grading** per fact: `code` (read in source), `test` (covered by suite/checklist), `playtest` (checked in game), `unverified`. Facts that rendering/GUI/real-time behavior alone can settle go to a **playtest list**; use the `scripted-playtest` skill only after asking the user for an uninterrupted window (shared desktop; see memory "Playtest automation etiquette").
5. **Parity side-list:** while reading, if `1.16.5`...`1.21.11` obviously differ from 1.14.4 for a feature (cheap check: `../1.14.4/docs/project/version-notes.md`, `/drift`, `/parity` helpers, grep of the same class), note it in the parity-bugs list. Do not document it.

### Phase 2 - Compare
1. For each of the 100 pages (+ new ones), diff wiki text/properties/recipe against the fact sheet. Classify: WRONG, OUTDATED (1.12.2 behavior no longer true), MISSING (fact absent), EXTRA (claims something not in the mod), VAGUE, DIVERGENCE-BY-DESIGN, OK. Severity: high (misleads gameplay), medium, low (wording).
2. Non-text checks: recipe images vs real recipes, `recipe` structured blocks resolve, keywords, images/videos still depict current visuals (flag stale media for the user; do not regenerate), broken `images/` references, category correctness, ASCII-only.
3. Special cases to resolve explicitly: shared pages covering several items (Platforms, Spectre Tools, Luminous Blocks, Divining Rods, Stained Bricks, Biome Blocks, Colored Grass, Beans); config-gated features (record default state); features whose wiki claim came from 1.12.2 but port intentionally changed; features implemented via coremods (verify they work in release; check `coremods.md`); items that became discrete variants instead of metadata.
4. Deliver `audit/GAP_MATRIX.md`: page x finding x severity x evidence, plus the three registry lists, the playtest list and the parity-bugs list. **Show the user the matrix before editing the wiki** if it contains judgement calls (anything classed DIVERGENCE-BY-DESIGN vs WRONG).

### Phase 3 - Update the wiki
1. Edit `data/features/*.json` only (never hand-edit `docs/*.html`). Small commits per group (only if the user says to commit).
2. Add `legacy` category to `categories.json`; move removed/won't-migrate pages there with a status line ("Not in the current mod: removed/not migrated") at the top of `description`.
3. Create new pages for uncovered features following the schema; use structured `recipe` blocks where a real recipe exists; use `properties` rows for quick facts (obtain method, config key, redstone behavior, range).
4. Write in the existing tone (short, player-facing). No version wording. ASCII only. Intentional 1.12.2 divergences: one short note.
5. Regenerate `docs/` with `generate_html.py`; check the output renders (open `docs/index.html` and a few pages in the browser pane); fix texture keys that fall back to text labels (ask before running network fetch scripts).

### Phase 4 - Verify and report
1. Script: every registered feature maps to a page or an explicit exemption; every recipe key resolves; no dead links; no non-ASCII; every `data/features` file has a generated page.
2. Re-read the highest-risk pages against code a second time (divergence pages, coremod-dependent, config-gated, multi-item shared pages).
3. Final report to the user: changes per page, new pages, moved-to-legacy list, unverified/playtest list, stale media list, parity-bugs list (versions that differ from 1.14.4; for the user to fix elsewhere).

## 5. Rules for the executing session
- Be honest about evidence: report what is verified vs not; never state a behavior from memory of the original mod.
- Check the real 1.12.2 source before declaring something a wiki bug that might be original behavior.
- Ask before: committing, pushing, downloading assets, running the game, deleting pages. Don't touch the user's `.idea/`/`.obsidian/` in the mod parent folder.
- Don't edit the mod source in this task (read-only on everything under `..` except `.`); fixes belong to a separate request.
- Use subagents for the parallel inventory (one per feature group), keep their reports short with file:line citations.
