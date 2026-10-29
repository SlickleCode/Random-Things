# Final report - wiki audit and update (Phases 0-4)

Date 2026-10-29. Baseline 1.14.4. Everything below is uncommitted in the `wiki` working tree (nothing committed or pushed).

## Deliverables (all in `audit/`)
- `GAP_MATRIX.md` (Phase 2 matrix + decisions J1-J14), `facts/` (Phase 1 fact sheets with file:line), `PHASE3_REPORT.md` (edits, user decisions, playtest round),
  `CHANGES_BY_PAGE.md` (generated: every page, what changed), `edits/A-H.md` (per-group edit logs: removed claims, skipped items, holds),
  `verify/R1-R4.md` (second code read of 75 high-risk pages), `verify/MEDIA.md`, `PARITY_BUGS.md` + `verify/P1-P4.md` + `verify/PARITY_MECH.md`,
  tools in `tools/` (`verify.py`, `changes_by_page.py`, `build_recipes.py`, `parity_mech.py`).

## Result in numbers
108 pages: 94 existing pages changed, 8 new (super_lubricent_ice, bottle_of_air, biome_crystal, id_card, redstone_tool, spectre_ingot, configuration,
ender_mailbox), 6 left as is by your decision (spectre_sapling, runic_dust, luminous_powder, spectre_coils, spectre_illuminator, bean).
10 pages moved to the Legacy category (item_filter, water_walking_boots, obsidian_water_walking_boots, obsidian_skull, lava_charm, lava_waders,
pitcher_plant, custom_crafting_tables, inventory_rerouter, biome_blocks). Categories: block 53, item 40, legacy 10, misc 5.
Structured recipes on 76 pages (from the mod's recipe JSON, shapeless noted). Generator extended with an optional `recipes` list.

## Phase 4 checks
1. `tools/verify.py` (run it again any time): 0 failures. Covers non-ASCII, categories, required fields, property rows, 3x3 recipe shapes, generated page for
   every data file, no orphan pages, no dead links/media in all generated html, every page linked from the index, version wording, and that all 156 registered
   item/block names appear on a page or are an explicit exemption. Mutation-tested (planted errors were caught).
2. Second code read of 75 pages (R1-R4): about 1 wrong sentence in 4 pages, all fixed (a sentence I wrote about Potion Vaporizer rooms, the Block Destabilizer
   fuzzy example, Magnetic + Block Breaker wording, platform collision wording) plus omissions added (Igniter signal edges, per-axis collector range, Spectre Key
   room interior, Biome Radar search rules, Peace Candle hostile-only). All recipe grids on the checked pages match the recipe JSON.
3. Media (MEDIA.md): 53 of 56 screenshots consistent with the new text. 3 weak stale candidates: red link lines from the Redstone Interface
   (redstone-interface-1.png, -2.png, advanced-redstone-interface.png). 18 videos on 15 pages could not be reviewed (list in MEDIA.md); the Divining Rods video may show
   the removed mod-ore rods and needs a human look.
4. Parity (PARITY_BUGS.md): recipes, config keys/defaults and lang keys identical on all branches except the Super Lubricent Tincture recipe;
   12 confirmed and about 15 suspected behaviour differences in the other branches. Nothing of it is on the wiki.

## Still open (needs you) - UPDATED after your answers
(Your answers: Redstone Tool text now says shift right click; the playtest conflicts and the Tincture recipe are recorded in `MOD_BUGS.md` with the wiki on the correct wording; commit and push was approved after regeneration and check.)

(Original list, kept for the record:)
A. Conflicts between your playtests (1.14.4) and what the agents read in code. I followed your playtests on the wiki:
   - Online Detector name: you said case sensitive; P1 read case-insensitive matching.
   - Ender Letters/Mailboxes across dimensions: you said yes; P4 read per-dimension saved data.
   - Rain Shield sounds: you said they stop; P1 found the 1.14.4 hook list only patches the renderer (sounds/particles are patched on 1.16.5+).
   - Redstone Tool: you said linking works; P1 thinks it only links while sneaking (the blocks open their GUI otherwise). The tool page says "right click";
     say if it should say "shift right click".
B. (Resolved) Super Lubricent Tincture: not a difference; 1.14.4 already needs a Water Bottle (the script ignored NBT). The wiki shows "Water Bottle".
C. Possible mod bugs you own (not on the wiki): see PHASE3_REPORT.md (Lotus soil, Peace Candle slimes and plains-only churches, Advanced Item Collector config range,
   Spectre Lens per player, Block Breaker/Magnetic config dependency, Notification Interface no longer a concern, etc.).
D. Recipe icons: texture download was blocked (403 for minecraft.wiki). 109 recipe ingredient keys show as text labels. Allow `minecraft.wiki` and I can rerun it.
E. Commit and push: not done. Say the word, and whether `audit/` goes in.
F. Optional wiki additions I skipped as minor or bug-adjacent: Floo duplicate-sign-name failure, Escape Rope not starting in the Nether/End, Chunk Analyzer main-hand
   requirement, Artificial End Portal structure text (screenshots only), Golden Compass needing a set Position Filter.

## Verified vs not (honest summary)
Verified by tests/playtest (yours or checklist): the facts you confirmed; checklist PASS rows for compasses, Illuminator, Spectre Key/Lens/tools, tree blocks.
Verified by code reading with citations (two independent passes for 75 pages): most page facts. Not verified: anything graded unverifiable in `verify/R*.md`
(for example Dyeing Machine worn-armour colouring, Spectre Tools vs diamond comparison which uses vanilla values), all rendering details beyond your playtest answers,
and the parity SUSPECTED items (no jars; no game run). Vanilla-value claims were kept off the wiki unless the repo or you confirmed them.
