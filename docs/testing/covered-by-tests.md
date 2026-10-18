# Open rows removed from the 1.14.4 checklist because tests cover them (2026-10-14)

These rows were still open in the root `TESTING_CHECKLIST.md` (FIXED awaiting retest, or never tested) and are now asserted by a suite test or a scripted scenario. What remains for your eyes is in the checklist's section D. Evidence names refer to `docs/testing/test-inventory.md` and the scenario list in `docs/testing/README.md`. The old results of the 227 PASS rows are untouched in `docs/testing/checklist-archive/`.

| # | Feature (short) | Covered by |
|---|---|---|
| 49 | Glowing Mushroom | worldgen tests (`worldgen.glowing_mushroom.*`, `worldgen.peace_candle.*`: registration, real terrain, village assembly, config switches) |
| 121 | Bean Stew - eat it, restores 8 hunger, leaves you holding an empty bowl afterward | scripted scenario `bean_stew` |
| 149 | Biome Radar - right-click with an empty hand and a Biome Crystal in your other hand's slot/held: inserting works, a... | scripted scenario `biome_radar + radar test` |
| 150 | Biome Radar - build the antenna (iron bars in the specific pattern above the radar — see `BiomeRadarTileEntity.isVa... | scripted scenario `biome_radar + radar test` |
| 151 | Biome Radar - let a search finish (crystal's target biome found): particles change to the target biome's color, the... | scripted scenario `biome_radar + radar test` |
| 152 | Biome Radar - break one of the antenna's iron bars while a search is active (not directly adjacent to the radar, so... | scripted scenario `biome_radar + radar test` |
| 153 | Biome Radar - break the radar block itself while it has a crystal inserted: the crystal drops instead of being dele... | scripted scenario `biome_radar + radar test` |
| 158 | Sound Dampener — place, right-click to open its 9-slot GUI, insert a few full Sound Patterns: any of those sounds played by ANYONE within ~2 | scripted scenario `sound_dampener` |
| 159 | Portable Sound Dampener - right-click to open its 9-slot GUI (carried in your own inventory, not placed), insert a ... | scripted scenario `sound_dampener` |
| 160 | Sound Dampener / Portable Sound Dampener - break the block or take the pattern back out of a slot: the muting stops... | scripted scenario `sound_dampener` |
| 168 | Imbuing Station - craft: water bucket + emerald + 2x vine + 2x lily pad + terracotta | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 169 | Imbuing Station — put the COBBLESTONE in the center slot and a water bottle + vine + bone meal in the 3 ingredient slots (any order, as in 1 | scripted scenario `imbuing` |
| 170 | Imbuing Station - water bottle (center) + coal + flint + blaze powder -> Fire Imbue potion | scripted scenario `imbuing` |
| 171 | Imbuing Station - water bottle (center) + spider eye + rotten flesh + red mushroom -> Poison Imbue potion | scripted scenario `imbuing` |
| 172 | Imbuing Station - water bottle (center) + Lesser Magic Bean + lapis lazuli + glowstone dust -> Experience Imbue pot... | scripted scenario `imbuing` |
| 173 | Imbuing Station - water bottle (center) + wither skeleton skull + nether brick + ghast tear -> Wither Imbue potion | scripted scenario `imbuing` |
| 175 | Drinking a Fire/Poison/Experience/Wither Imbue potion grants its effect for 20 minutes, leaves an empty glass bottl... | scripted scenario `imbue_effects + imbue test` |
| 176 | While Fire Imbue is active, dealing direct melee/attack damage (not projectiles) sets the target on fire for 10s | scripted scenario `imbue_effects + imbue test` |
| 177 | While Poison Imbue is active, a direct hit gives the target Poison II for 10s | scripted scenario `imbue_effects + imbue test` |
| 178 | While Wither Imbue is active, a direct hit gives the target Wither II for 5s | scripted scenario `imbue_effects + imbue test` |
| 179 | While Experience Imbue is active, killing a mob drops double XP | scripted scenario `imbue_effects + imbue test` |
| 181 | Floo Pouch - craft: leather (8-around) + Floo Powder (center); sneak-right-click tops it up from any Floo Powder in... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 182 | Floo Sign - craft: Floo Powder (8-around) + any wood-type sign (center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 183 | Floo Token - craft: Floo Powder (8-around) + paper (center), makes 2 | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 193 | Eclipsed Clock - craft: 4x obsidian + 4x gold ingot + ghast tear (center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 198 | Time in a Bottle - craft: 3x gold ingot + 2x diamond + 2x lapis lazuli + clock + glass bottle | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 202 | Weather Egg (Sun/Rain/Storm) - craft each: obsidian corners + fire charge (center) + the type-specific ingredient (... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 206 | Ender Mailbox - craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 207 | Ender Letter - craft: 2x paper (diagonal) + ender pearl (center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 209 | Ender Mailbox - place one: it remembers you as its owner | scripted scenario `ender_mailbox` |
| 210 | Ender Mailbox - sneak-right-click any mailbox (yours or anyone else's) while holding an addressed letter: delivers ... | scripted scenario `ender_mailbox` |
| 212 | Ender Mailbox - right-click your own placed mailbox (not sneaking): opens your personal inbox - the same inbox no m... | scripted scenario `ender_mailbox` |
| 214 | Ender Mailbox - after mail arrives in your inbox, the mailbox block starts glowing/emitting portal particles within... | scripted scenario `ender_mailbox` |
| 215 | Receiving a letter: take it from your inbox into your own inventory, right-click it - opens the same letter GUI, ou...  (letter delivery, ta | scripted scenario `ender_mailbox` |
| 218 | Ender Bucket - craft: 2x iron ingot (top corners) + ender pearl (center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 234 | Fluid Display — right-click with a filled fluid container (bucket, Ender Bucket, etc.): block takes on that fluid's texture/color, still ima | scripted scenario `fluid_display` |
| 238 | Evil Tear — craft: Wither Skeleton Skull + Ghast Tear + Ender Pearl (shapeless, any arrangement) | scripted scenario `crafting_evil_tear_portkey` |
| 245 | Ender Anchor - craft: 8x obsidian (ring) + Stable Ender Pearl (center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 246 | Ender Bridge - craft: 6x end stone + redstone (center) + Stable Ender Pearl (right-middle) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 253 | Magic Hood - wear it as a helmet: other players can no longer see your nametag above your head, whether you're snea... | test `behavior.magic_hood.hides_the_nametag_and_the_potion_particles_only_while_worn` + scenario `magic_hood` (counts swirl particles) |
| 254 | Magic Hood — wear it as a helmet: your potion-effect particle swirl no longer shows to anyone, whether you're sneaking or not | test `behavior.magic_hood.hides_the_nametag_and_the_potion_particles_only_while_worn` + scenario `magic_hood` (counts swirl particles) |
| 256 | Rain Shield - craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 258 | Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire ex | scripted scenario `rain_shield + coremod test` |
| 259 | Rain Shield - powered via redstone: shielding turns off, rain/snow affects the area normally again | scripted scenario `rain_shield + coremod test` |
| 265 | Ancient Furnace — after a multi-minute heat-up, it melts surface snow (and ice, to water) in a large radius around itself, reassigns that ar | scripted scenario `ancient_furnace` |
| 270 | Peace Candle - in a plains village, roughly a third of the tall cobblestone churches are instead the Peace Candle c... | worldgen tests (`worldgen.glowing_mushroom.*`, `worldgen.peace_candle.*`: registration, real terrain, village assembly, config switches) |
| 275 | Portkey - craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center) | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 277 | Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the groun | scripted scenario `portkey_drop` |
| 279 | Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - invent | scripted scenario `crafting_evil_tear_portkey` |
| 281 | Spectre Anchor - craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 E... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 284 | Basic/Advanced Redstone Interface - whatever redstone signal (weak or strong) is currently hitting the interface bl... | scripted scenario `redstone_interface` |
| 285 | Advanced Redstone Interface - craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 287 | Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom | scripted scenario `redstone_remote + redstone_remote_edit` |
| 288 | Magnetic Enchantment - appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mini... | scripted scenario `magnetic` |
| 289 | Block Breaker - craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way ... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 292 | Block Destabilizer - craft: obsidian at the 4 corners, sand left/right of a diamond in the middle row, redstone abo... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 293 | Block Destabilizer — a redstone pulse flood-fills every block connected to the one directly in front of it that matches the same exact `Bloc | scripted scenario `block_destabilizer + block_destabilizer_lazy + falling_block_obstruction` |
| 294 | Block Destabilizer - GUI: Fuzzy toggle button makes the match ignore block-state variants and match by `Block` type... | scripted scenario `block_destabilizer + block_destabilizer_lazy + falling_block_obstruction` |
| 295 | Block Destabilizer - GUI: Lazy toggle remembers which connected blocks *didn't* match last time, so re-triggering d... **[verified by script | scripted scenario `block_destabilizer + block_destabilizer_lazy + falling_block_obstruction` |
| 296 | Block Destabilizer - falling blocks landing on an obstruction (another block, a tile-entity-bearing block) vanish w... **[verified by script | scripted scenario `block_destabilizer + block_destabilizer_lazy + falling_block_obstruction` |
| 297 | Spectre Key - craft: Spectre Ingot at the 3 corners (top-left, middle-left, bottom-right), Stable Ender Pearl cente... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 303 | Spectre Sword - craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) a... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 307 | Spectre String - craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4 | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 308 | Spectre Coil (Normal/Redstone/Ender) - craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre I... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 310 | Spectre Energy Injector - craft: Obsidian at the 4 corners, Spectre Lens top-center, a vanilla Beacon center, Spect... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 311 | Spectre Charger (Normal/Redstone/Ender/Genesis) - craft the first 3 tiers same escalating pattern as the Coils (Gen... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 312 | Spectre Lens - craft: Spectre Ingot at the 4 corners, Emerald top/bottom-center, Diamond left/right-center, Glass i... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 314 | Diaphanous Block - craft (Quartz Glass + 4 colored glass in a ring, see recipe) places a plain Stone-displaying blo... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 318 | Light Redirector - craft (planks + glass ring, see recipe); right-click a face to toggle it open/closed (texture sw... | scripted scenario `light_redirector` |
| 319 | Light Redirector - an *open* face shows whatever block is directly on the *opposite* side of the redirector instead...  (block state/NBT tog | scripted scenario `light_redirector` |
| 320 | Dyeing Machine - craft (Green/Red/Blue Dye + White Wool + Crafting Table, see recipe) and place; right-click opens ... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 321 | Dyeing Machine - put any non-block item in the left slot and a vanilla dye in the dye slot; taking the "recolor" re...  (rtDye tag; the in-w | scripted scenario `dyeing_machine + worn_armor` |
| 322 | Dyeing Machine - taking the "enchant glint" result instead gives back a copy of the item whose enchantment shimmer ...  (enchantmentColor ta | scripted scenario `dyeing_machine + worn_armor` |
| 323 | Dyeing Machine - a dyed armor piece (from the "recolor" result) also shows tinted while worn on your character mode...  (rtDye tag only; wor | scripted scenario `dyeing_machine + worn_armor` |
| 324 | Dyeing Machine - closing the GUI while the item/dye slots still hold something drops those items at your feet inste... | scripted scenario `dyeing_machine + worn_armor` |
| 341 | Glowing Mushrooms - generate a fresh world/new chunks and explore caves: small patches of glowing mushrooms appear ... | worldgen tests (`worldgen.glowing_mushroom.*`, `worldgen.peace_candle.*`: registration, real terrain, village assembly, config switches) |
| 342 | Grass Seeds - craft one from a Grass Block (shapeless); right-click plain Dirt with it: becomes normal grass, seed ... | scripted scenario `colored_grass` |
| 343 | Colored Grass Seeds - Grass Seeds + one vanilla dye (shapeless) gives that color's seeds (16 colors); right-clickin... | scripted scenario `colored_grass` |
| 344 | Colored Grass - spreads its color onto nearby Dirt over time when lit; reverts to Dirt when covered by an opaque bl... | scripted scenario `colored_grass` |
| 346 | Runes - placed rune pixels now show the original's noisy texture (each pixel/connection strip slightly different, t... **[verified by script | scripted scenario `rune_rendering` |
| 349 | Recipes - Lapis Glass (x8), Quartz Glass (x8), Lapis/Quartz Lamp, Trigger Glass (x4); Biome Glass and the five Biom... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 350 | Recipes - Super Lubricent: Ice (x16: slimeball/ice/water bucket), Platform (x6), Filtered Platform, new **Super Lub... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 351 | Recipes - Stable Ender Pearl, Lesser Magic Bean (8 gold nuggets around Beans), Bean Stew (both orientations: wheat ... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 352 | Recipes - shapes were ported with 1.12.2's exact (left-aligned, right-padded) layouts, so a few look lopsided (Item... | test `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output` (+ resource audit: every recipe loads) |
| 356 | Ender Anchor chunk loading - place an Ender Anchor far from any player (e.g. 300+ blocks from an Ender Bridge facin... | scripted scenario `ender_anchor` |
| 356b | Config - `[worldgen] PeaceCandle = false`: no plains village church is replaced any more, new villages generate exa... | worldgen tests (`worldgen.glowing_mushroom.*`, `worldgen.peace_candle.*`: registration, real terrain, village assembly, config switches) |
| 357 | Ender Anchor chunk loading - persists across a save/quit/reload (chunk still force-loaded on reload, no duplicate r... | scripted scenario `ender_anchor` |
| 387 | **Glowing Mushroom survival and spreading (found by the new `worldgen.*` tests):** a placed mushroom dropped itself on its first neighbour u | worldgen tests (`worldgen.glowing_mushroom.*`) + scenario `glowing_mushroom_cave` (stays after placement, spreads in a sealed dark room) |
| 388 | **Imbue potions showed no HUD/inventory icon (found by the scripted playtest):** drinking an imbue potion used the 5-argument `EffectInstanc | test `behavior.imbue.drinking_applies_a_20_minute_effect_with_icon_and_no_particles_and_replaces_the_previous_one` + scenario `imbuing` |
| 390 | **Enchant-glint colours were never rendered (found by the scripted dyeing test):** the Dyeing Machine's "enchant glint" result stored `encha | test `behavior.glint.colour_priority_matches_1_12_2` + scenarios `dyeing_machine`, `worn_armor` (measured glint colour on items and worn armor) |
| 142 | Escape Rope — deep underground, hold right-click: charges, searches, teleports to daylight | test `use_item.escape_rope.charges_then_teleports_out_through_the_doorway_and_costs_one_durability` (also `..._a_sealed_room_has_no_way_out_so_the_rope_is_dropped`, `..._refuses_to_start_under_open_sky`) |
| 143 | Escape Rope — release right-click partway | test `use_item.escape_rope.letting_go_before_the_search_finishes_cancels_the_teleport` |
| 144 | Escape Rope — full durability (20): breaks on the **20th** successful use (`damageItem` breaks at damage >= max; 1.12.2 broke on the 21st) | test `use_item.escape_rope.the_twentieth_successful_use_breaks_it` |
