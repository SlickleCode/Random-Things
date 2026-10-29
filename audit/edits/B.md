# Group B edit log
changed: chat_detector - exact-match, 20-tick pulse, fixed owner, 9 ID card slots, properties
changed: creative_player_interface - removed OpenComputers text; creative-menu-only
changed: entity_detector - fixed invert wording (no player in range), radius 0-10 default 5, filter list, weak/strong button
changed: igniter - 2 modes only, fire placed in air block, facing
changed: imbuing_station - Experience +100% (double), ingredients, 200-tick brew, mossy cobblestone, melee only
changed: inventory_tester - whole stack must fit, 2-tick check, keeps last signal
changed: iron_dropper - listed modes/values (Pulse/Repeat Powered every 4 ticks, delay None/5/20, motion, effects)
changed: item_collector - Item Filter replaced by single example-item slot; 7x7x7 cube; default range 5
changed: notification_interface - removed OpenComputers; icon slot, owner only, online only
changed: online_detector - removed OpenComputers; once-per-second, username
changed: player_interface - removed OpenComputers; side mapping
changed: potion_vaporizer - living entities only; normal non-instant potions, first effect only
changed: rain_shield - fixed 80 blocks (not configurable); needs solid block below
changed: biome_radar - crystals in all chests (not dungeon only); spiral search, no max distance
unchanged: block_breaker - only unverified / possible-bug claims (see lists)
changed: block_destabilizer - key + default of limit, full cubes only, tile-entity blocks skipped, one by one
changed: dyeing_machine - removed purple/black glow claims and "only non-self-coloured items" claim; block items not accepted
changed: fluid_display - needs a filled container
new: biome_crystal (item) - chest loot 20% per chests/* table, config BiomeCrystal default true, stores one biome, Radar/Sensor use

SKIPPED-POSSIBLE-BUG:
- item_collector: wiki "range 0 to 10 per direction" kept; code MAX_RANGE=16 hard-coded (AdvancedItemCollectorTileEntity.java:45); original max 10 via config AdvancedItemCollectorMaxRange default 10; not documented as deliberate.
- block_breaker: wiki "all dropped items go into the inventory behind" kept; today this only works when config MagneticEnchantment=true (original used own ItemCatcher, no such dependency).
- block_destabilizer: "Configurable" kept as is (key now in properties).
HOLD:
- rain_shield: rain sound suppression unverified (open question 5); the page now only claims blocks act as if dry, sounds claim removed from text.
- block_breaker: "can't break blocks an iron pickaxe can't" unverified (open question 3), left.
- block_destabilizer: lazy-mode semantics unverified (open question 7), text left; "arrow button" icon untested.
- Imbuing Station / Potion Vaporizer / Inventory Tester contents not dropped on break (open question 1) - not written.
- notification_interface icon-drop duplication (open question 2) - not written.
REMOVED-CLAIMS:
- OpenComputers components: online_detector, notification_interface, creative_player_interface, player_interface mention (scope-decisions.md:5-6; no OC code in J/R).
- igniter "Keep Ignited" mode (IgniterTileEntity.java:26-29, removed by request).
- imbuing_station "50% more experience" (RandomThings.java:226-231 doubles; same as original).
- item_collector "Item Filter" (does not exist; single example slot, AdvancedItemCollectorTileEntity.java:31-43).
- dyeing_machine "purple dye gives default glow", "black dye almost invisible", "only items that don't colour themselves" (no such code, DyeUtil; same in original ContainerDyeingMachine).
- rain_shield "configurable", "particles / sounds" (AsmHandler.java:241; sounds unverified).
- biome_radar "Dungeon Chests" (LootHandler.java:69-74).
NEW-FACTS-ADDED: ~70
