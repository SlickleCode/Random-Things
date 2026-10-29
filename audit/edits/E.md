# Edits group E
- lotus: changed: growth/blossom XP 3-11/seed planting, properties (obtain, config Lotus, growth).
- glowing_mushrooms: changed: removed brewing claim; added spreading/light/soil, properties.
- colored_grass: changed: 16 colours, seeds, plain dirt only, spread/revert, silk touch, properties.
- fertilized_dirt: changed: "factor of 3" replaced by code fact (up to 3 extra growth ticks per random tick), hoe, properties.
- ancient_furnace: changed: fixed radius 56 / 5 min (not configurable), nether star consumed, only listed biomes convert, properties.
- peace_candle: changed: properties only (obtain, range, config); description left as is.
- slime_cube: changed: removed "significantly increased rate"; properties.
- bean: untouched (user decision).
- configuration: NEW page (misc), 20 RTConfig options as properties rows.

SKIPPED-POSSIBLE-BUG:
- peace_candle: wiki "prevents Natural Mob Spawning in a 3 chunk radius". Today's AsmHandler.java:169-179 returns early for EntityType.SLIME before the candle check, so slimes are NOT blocked. Original (1.12.2 AsmHandler.modifyValidSpawningChunks) removed the 7x7 chunks for the whole MONSTER type (slimes are monsters there). Differs and not documented as deliberate -> wiki sentence left, no mention of slimes.
- peace_candle: "33% in a village church": today only plains villages (PeaceCandleChurchPool), original patched the church in every village biome (wiki-feature-status.md:377 per sheet). Sentence left unchanged; scope not written.
- slime_cube: "any height" kept (code forces ALLOW, bypassing vanilla predicate). Original only overrode isSlimeChunk (ClassTransformer.java:367), so height limit probably stayed; possible difference, sentence left.
- scope-decisions.md not found in repo (docs/project missing), so no deliberate-divergence list was available.

HOLD:
- lotus seeds on podzol/coarse dirt (open question 3); not written.
- fertilized_dirt exact growth multiplier (unverified, no test): only "up to 3 extra growth ticks per random tick" (code) written.
- Slime Cube / Peace Candle behaviour with spawners/spawn eggs (open question 1); not written.
- slime cube powered state after reload (no NBT); not written.

REMOVED-CLAIMS:
- glowing_mushrooms: "replacement for glowstone in all brewing recipes" - no brewing integration (grep of src/main; sheet glowing_mushrooms).
- slime_cube: "significantly increased rate" - no rate change in code (AsmHandler.java:169-179) nor in original (only slime-chunk override).
- ancient_furnace: "(configurable)" - radius is constant 56, no config key (AncientFurnaceTileEntity.java:71-76).

NEW-FACTS-ADDED: ~45 (incl. 20 config rows). Config file name randomthings-common.toml cited in sheet WORLD-LEVEL A. "Loot options need reload/restart" from sheet B note (archive #338).
