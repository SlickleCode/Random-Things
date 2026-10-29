# Edit log, group F
- divining_rods: changed: removed mod-ore rods paragraph; vanilla rod = 7 ores (no quartz/nether gold), tag-based, client-only; props + keywords.
- emerald_compass: changed: ID Card not consumed, 1 Hz update, X/Z only (same as original 1.12.2 class, so wiki was just silent), random wobble when target offline; props.
- golden_compass: changed: X/Z only, filter not consumed, random wobble without target (original has no dimension logic either; playtest #137-138 PASS); props.
- position_filter: changed: removed purple cube claim and Highlight Color row; added shift tooltip, HideCoordinates config, stack size.
- entity_filter: changed: exact type match, empty filter matches all; props.
- chunk_analyzer: changed: main hand, results saved on item, counts from bottom to surface incl. caves, sorted by count; "few seconds" -> "short while".
- magic_hood: changed: not indestructible (leather helmet durability, chain protection); blacksmith -> weaponsmith; dungeon 5% / weaponsmith 15%; MagicHood config.
- magnetic_enchantment: changed: added XP/mob drops not affected, Block Breaker, config MagneticEnchantment.
- blaze_and_steel: changed: replaced "significantly faster burn and spread" with "updates twice as often as vanilla fire"; 1 durability per use; durability 64.
- escape_rope: changed: 21 -> 20 uses; needs roofed location, not in water, 1 durability, drops if no way found, same dimension only.
- super_lubricent_boots: changed: 0.35 b/t speed cap, sneaking disables, any living entity, iron-boot stats.
- bottle_of_air: created (creative-only, drink in water refills breath, single-use).
- id_card: created (bind by right click, Emerald Compass, Entity Detector custom, Global Chat Detector whitelist).

SKIPPED-POSSIBLE-BUG: none.
HOLD:
- magnetic_enchantment "roughly the same chance as Silktouch" left as is: sheet marks it [unverified] (rarity RARE, same enchantability); not changed, not extended.
- magnetic_enchantment "pickaxes / axes / shovels": hoe/shears acceptance unverified, left as is.
- divining_rods: lit redstone ore / nether ores tag membership unverified, not written.
- blaze_and_steel: whether fire burns out/spreads faster beyond tick rate (FireBlockTransformer not read) not written.
REMOVED-CLAIMS:
- divining_rods "more rods depending on installed mods" (F sheet divining_rods DIVERGENCE/WIKI-CLAIM-CHECK; no mod rods in ModItems.java:228-238).
- position_filter purple cube highlight + Highlight Color row (only drawCube caller is DiviningRodRenderer.java:74).
- magic_hood "indestructible" (MagicHoodItem.java:105-150), "blacksmith villager" (LootHandler.java:29-30 weaponsmith).
- escape_rope "21 times" (UseItemTests.java:407-423, ModItems.java:339).
NEW-FACTS-ADDED: ~55
