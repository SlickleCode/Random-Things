# Untested items and manual checks (generated 2026-10-03 from TESTING_CHECKLIST.md)

Counts: DONE 40, FAIL 8, FIXED 10, PASS 196, REMOVED 4, UNTESTED 106


## A. Open FAIL rows - need a fix, then a retest (8)


**2. Batch 1 — standalone blocks (no GUI/tile entity)**

- #56 Colored Grass — (SUPERSEDED 2026-09-29: all 16 colors + Grass Seeds now ported, see #342-#344) ships as single white-only variant (no color picker yet)  
  _Last note:_ User Update 9/30/2026: Grass seeds work to place tinted grass, and it spreads. It does not have a preference for the shovel tool, and only says "Colored Grass" in it's tooltip. I would like it to say it's color in it's tooltip as "<COLOR> Grass Block" **Checked - this is intentional, matching 1.12.2…

**3. Batch 2 — tile-entity/GUI-backed blocks / Slice 5**

- #93 Chat Detector — GUI text field, pulses on placer typing the exact configured message  
  _Last note:_ **Investigated, no bug found in the button/click-routing code itself** - the same button pattern works elsewhere (Igniter), and the text field's own click handling correctly yields to the button per vanilla source. See `TESTING_NOTES.md` #93 for what to check on retest (does the button highlight on …

**4. General spot-checks across the whole port**

- #120 GUI text-cycle mode buttons (Igniter, Entity Detector, etc.) are legible and functional (plain text, not the original's icon-sprite buttons — cosmetic difference only)  
  _Last note:_ Many GUI's need tweaks and updates to display properly. **10/3/2026 audit progress (debug screenshots):** fixed Global Chat Detector (slot rows/ySize didn't match texture; button overlapped slots; now original layout + sprite toggle), Advanced Redstone Interface + Redstone Remote Edit (`advanced.png…

**5. Batch 3 — remaining (non-block) items**

- #155 Sound Recorder GUI — put a blank Sound Pattern in the left slot, click a sound name in the list: a "full" Sound Pattern (different icon, tooltip shows the sound's name) appears in the right slot and t…  
  _Last note:_ GUI has text off the screen, no button or way to select sound to save. **10/3/2026, debug screenshot:** layout/texture were correct; the sound-name text was white on the light-gray panel (nearly invisible, looked like "no way to select"). Rows are now dark text with a dark highlight bar + white text…
- #158 Sound Dampener — place, right-click to open its 9-slot GUI, insert a few full Sound Patterns: any of those sounds played by ANYONE within ~20 blocks of the block go silent for everyone in range, not j…  
  _Last note:_ **Investigated, no bug found - matches 1.12.2 exactly** (byte-for-byte same `distanceSq < 20*20` check, confirmed via that branch's real source). Note the 20-block check is against the *sound's own origin position*, not your distance from the dampener - if the sound source (a mob, a block) was itsel…

**5. Batch 3 — remaining (non-block) items / Slice 16**

- #180 Floo Powder — craft: Ender Pearl top-left, Redstone top-right, Gunpowder bottom-left, plain Beans bottom-right (2x2, makes 16)  
  _Last note:_ Make this crafting recipe shapeless **Fixed 9/30/2026:** recipe is now shapeless (this intentionally diverges from 1.12.2's shaped 2x2). Please retest.

**5. Batch 3 — remaining (non-block) items / Slice 17**

- #205 Weather Egg cloud — visibly different particle effects per type while rising (plus a colored accent for Rain/Storm)  
  _Last note:_ When throwing egg, no egg sprite or billboard is shown, but all other effects happen. **Fixed 9/30/2026:** root cause was the thrown entity lacking `createSpawnPacket()`, so the client never had the entity to render (effects were server-driven). Added `NetworkHooks.getEntitySpawningPacket`. Please r…

**5. Batch 3 — remaining (non-block) items / Slice 20 — Summoning Pendulum, Golden Egg/Golden Chicken, Fluid Display**

- #230 Golden Egg — throw it (right-click): always hatches a Golden Chicken where it lands (unlike vanilla eggs' 1-in-8 chance), and damages anything it hits directly for 1  
  _Last note:_ Does not show sprite or billboard while egg is thrown, still spawns chicken. **Fixed 9/30/2026:** same root cause/fix as #205. Please retest.

## B. FIXED - fix applied, awaiting your retest (10)


**5. Batch 3 — remaining (non-block) items / Slice 20 — Summoning Pendulum, Golden Egg/Golden Chicken, Fluid Display**

- #234 Fluid Display — right-click with a filled fluid container (bucket, Ender Bucket, etc.): block takes on that fluid's texture/color, still image

**5. Batch 3 — remaining (non-block) items / Slice 21 — Artificial End Portal, Creative Player Interface**

- #238 Evil Tear — craft: Wither Skeleton Skull + Ghast Tear + Ender Pearl (shapeless, any arrangement)  
  _Last note:_ Changed to `minecraft:crafting_shapeless` per request (no 1.12.2 recipe found to check against either way, so just did it). Please retest.

**5. Batch 3 — remaining (non-block) items / Slice 23 — Magic Hood (Batch 7, slice 3)**

- #254 Magic Hood — wear it as a helmet: your potion-effect particle swirl no longer shows to anyone, whether you're sneaking or not  
  _Last note:_ Real bug: `shouldHideParticles(true)` doesn't actually suppress particles - that flag is what vanilla itself uses for genuinely ambient effects (Beacon/Conduit), just switching to a rarer, fainter particle type, not zero. The real gate is the synced particle color; forcing it to exactly 0 is what st…

**5. Batch 3 — remaining (non-block) items / Slice 24 — Rain Shield (Batch 7, slice 4)**

- #258 Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire extinguishing near it - the mechanical effects of rain/snow ar…  
  _Last note:_ The mechanical suppression itself was already confirmed correct via a bytecode-level check in an earlier session - the "still see rain particles" part of this report was really #261 (the purely visual half, disclosed as not-yet-built at the time). That's now implemented too - see #261. Please retest…
- #261 Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request)  
  _Last note:_ Implemented via a new coremod redirecting the one `World.getBiome` call inside vanilla's rain-rendering loop, substituting a real no-precipitation biome for columns inside an active shield's radius. Verified via `runClient` that the new coremod transforms cleanly (no VerifyError/crash). Please retes…

**5. Batch 3 — remaining (non-block) items / Slice 25 — Ancient Furnace, Peace Candle (worldgen batch)**

- #265 Ancient Furnace — after a multi-minute heat-up, it melts surface snow (and ice, to water) in a large radius around itself, reassigns that area's biome to its warmer counterpart (e.g. Snowy Tundra -> P…  
  _Last note:_ Real bug: the biome mutation itself was already correct server-side, but nothing told an already-connected client's own separate copy of the chunk's biome data to update, so the visual grass/foliage/fog color never changed even though the real biome had. Fixed by resending each affected chunk to eve…
- #277 Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the ground it stops despawning and stops glowing (primed); picking it…  
  _Last note:_ Real bug: the priming counter lives on the item stack's own NBT (needed for client-side rendering), but mutating that tag in place on the same object the dropped entity already had cached never actually re-syncs to the client - the check that decides whether to resend only trips for a genuinely diff…
- #279 Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - inventory, held, dropped on the ground, item frame), the donor ite…  
  _Last note:_ Real bug (second one found in this same area): `ItemRenderer.renderItem` - what this item's renderer is already invoked from - applies its own centering offset before calling the renderer, and both the camo'd and plain branches called back into an overload that applies that exact same offset a secon…
- #287 Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom; sneak-right-click opens an "edit" GUI (9 Position Filter s…  
  _Last note:_ Reported (with screenshot): the "use" screen looked like a blank white box. Real bug found - it (and the edit screen, and Advanced Redstone Interface's own screen) never drew a title at all. Fixed all three. The plain gray panel look itself is faithful to 1.12.2's own texture, not a bug. See `TESTIN…

**5. Batch 3 — remaining (non-block) items / Slice 26 — Magnetic Enchantment, Block Breaker, Block Destabilizer**

- #293 Block Destabilizer — a redstone pulse flood-fills every block connected to the one directly in front of it that matches the same exact `BlockState` (not just the same block with different properties) …  
  _Last note:_ Implemented per request (this restriction never existed in 1.12.2 either, so it's a real behavior change, not a bug fix) - the flood-fill now skips non-full blocks (redstone, torches, levers, etc.) entirely, leaving them behind untouched instead of sweeping them up. Please retest.

## C. Never tested - no result recorded yet (106)


**2. Batch 1 — standalone blocks (no GUI/tile entity)**

- #49 Glowing Mushroom

**5. Batch 3 — remaining (non-block) items**

- #121 Bean Stew — eat it, restores 8 hunger, leaves you holding an empty bowl afterward
- #149 Biome Radar — right-click with an empty hand and a Biome Crystal in your other hand's slot/held: inserting works, and right-clicking again with an empty hand takes the crystal back out
- #150 Biome Radar — build the antenna (iron bars in the specific pattern above the radar — see `BiomeRadarTileEntity.isValid()` or just try a small iron-bars frame 2-3 blocks up), insert a crystal, then pow…
- #151 Biome Radar — let a search finish (crystal's target biome found): particles change to the target biome's color, then right-click with paper: consumes the paper and gives you a Position Filter pointing…
- #152 Biome Radar — break one of the antenna's iron bars while a search is active (not directly adjacent to the radar, so no immediate neighbor-update): within a few seconds the radar should notice and drop…
- #153 Biome Radar — break the radar block itself while it has a crystal inserted: the crystal drops instead of being deleted
- #159 Portable Sound Dampener — right-click to open its 9-slot GUI (carried in your own inventory, not placed), insert a few full Sound Patterns: those sounds go silent for you specifically no matter where …
- #160 Sound Dampener / Portable Sound Dampener — break the block or take the pattern back out of a slot: the muting stops for that sound, and any Sound Patterns inside are dropped/returned rather than delet…

**5. Batch 3 — remaining (non-block) items / Slice 15**

- #168 Imbuing Station — craft: water bucket + emerald + 2x vine + 2x lily pad + terracotta
- #169 Imbuing Station — put a water bottle in the center slot + vine + bone meal + cobblestone in the 3 ingredient slots (any order): after 200 ticks, produces Mossy Cobblestone and consumes exactly 1 of ea…
- #170 Imbuing Station — water bottle (center) + coal + flint + blaze powder -> Fire Imbue potion
- #171 Imbuing Station — water bottle (center) + spider eye + rotten flesh + red mushroom -> Poison Imbue potion
- #172 Imbuing Station — water bottle (center) + Lesser Magic Bean + lapis lazuli + glowstone dust -> Experience Imbue potion
- #173 Imbuing Station — water bottle (center) + wither skeleton skull + nether brick + ghast tear -> Wither Imbue potion
- #174 Imbuing Station — progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in progress; output slot rejects manual insertion (machine-only)
- #175 Drinking a Fire/Poison/Experience/Wither Imbue potion grants its effect for 20 minutes, leaves an empty glass bottle, and shows its own HUD/inventory icon; drinking a second kind replaces the first ra…
- #176 While Fire Imbue is active, dealing direct melee/attack damage (not projectiles) sets the target on fire for 10s
- #177 While Poison Imbue is active, a direct hit gives the target Poison II for 10s
- #178 While Wither Imbue is active, a direct hit gives the target Wither II for 5s
- #179 While Experience Imbue is active, killing a mob drops double XP

**5. Batch 3 — remaining (non-block) items / Slice 16**

- #181 Floo Pouch — craft: leather (8-around) + Floo Powder (center); sneak-right-click tops it up from any Floo Powder in your inventory, up to 128, shown as a durability-bar-style charge meter
- #182 Floo Sign — craft: Floo Powder (8-around) + any wood-type sign (center)
- #183 Floo Token — craft: Floo Powder (8-around) + paper (center), makes 2

**5. Batch 3 — remaining (non-block) items / Slice 17**

- #193 Eclipsed Clock — craft: 4x obsidian + 4x gold ingot + ghast tear (center)
- #198 Time in a Bottle — craft: 3x gold ingot + 2x diamond + 2x lapis lazuli + clock + glass bottle
- #202 Weather Egg (Sun/Rain/Storm) — craft each: obsidian corners + fire charge (center) + the type-specific ingredient (sunflower/potion/sugar) + lapis lazuli or feather per type, makes 2

**5. Batch 3 — remaining (non-block) items / Slice 18**

- #206 Ender Mailbox — craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center)
- #207 Ender Letter — craft: 2x paper (diagonal) + ender pearl (center)
- #209 Ender Mailbox — place one: it remembers you as its owner
- #210 Ender Mailbox — sneak-right-click any mailbox (yours or anyone else's) while holding an addressed letter: delivers it into the *named recipient's* personal inbox (not the block you clicked), consumes …
- #211 Ender Mailbox — sneak-right-click with an addressed letter naming a player who doesn't exist: get a "does not seem to reside in this world" message, letter untouched
- #212 Ender Mailbox — right-click your own placed mailbox (not sneaking): opens your personal inbox - the same inbox no matter which of your placed mailboxes you use
- #213 Ender Mailbox — right-click a mailbox someone else placed (not sneaking): "You are not the owner of this mailbox," no GUI opens
- #214 Ender Mailbox — after mail arrives in your inbox, the mailbox block starts glowing/emitting portal particles within ~10 seconds; stops once you've collected everything
- #215 Receiving a letter: take it from your inbox into your own inventory, right-click it - opens the same letter GUI, output-only this time (can take items out, can't add more), receiver field disabled, to…

**5. Batch 3 — remaining (non-block) items / Slice 19**

- #218 Ender Bucket — craft: 2x iron ingot (top corners) + ender pearl (center)

**5. Batch 3 — remaining (non-block) items / Slice 21 — Artificial End Portal, Creative Player Interface**

- #244 Creative Player Interface — creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves identically to the already-shipped Player Interface (binds to whoever places it, expose…

**5. Batch 3 — remaining (non-block) items / Slice 22 — Ender Bridge, Ender Anchor, Prismarine Ender Bridge**

- #245 Ender Anchor — craft: 8x obsidian (ring) + Stable Ender Pearl (center)
- #246 Ender Bridge — craft: 6x end stone + redstone (center) + Stable Ender Pearl (right-middle)

**5. Batch 3 — remaining (non-block) items / Slice 23 — Magic Hood (Batch 7, slice 3)**

- #253 Magic Hood — wear it as a helmet: other players can no longer see your nametag above your head, whether you're sneaking or not

**5. Batch 3 — remaining (non-block) items / Slice 24 — Rain Shield (Batch 7, slice 4)**

- #256 Rain Shield — craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row)
- #259 Rain Shield — powered via redstone: shielding turns off, rain/snow affects the area normally again

**5. Batch 3 — remaining (non-block) items / Slice 25 — Ancient Furnace, Peace Candle (worldgen batch)**

- #270 Peace Candle — in a plains village, roughly a third of the tall cobblestone churches are instead the Peace Candle church (`randomthings:peace_candle_church`: same footprint as the vanilla one, custom …
- #275 Portkey — craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center)
- #281 Spectre Anchor — craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 Ectoplasm (center)
- #284 Basic/Advanced Redstone Interface — whatever redstone signal (weak or strong) is currently hitting the interface block itself gets mirrored onto its target position(s), as if a real redstone source we…
- #285 Advanced Redstone Interface — craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-click opens a GUI with 9 Position Filter slots instead of a single tool-bound target …

**5. Batch 3 — remaining (non-block) items / Slice 26 — Magnetic Enchantment, Block Breaker, Block Destabilizer**

- #288 Magnetic Enchantment — appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mining a block with it puts drops straight in your inventory instead of on the ground
- #289 Block Breaker — craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way facing toward whichever way you were looking
- #292 Block Destabilizer — craft: obsidian at the 4 corners, sand left/right of a diamond in the middle row, redstone above/below the diamond (ring pattern)
- #294 Block Destabilizer — GUI: Fuzzy toggle button makes the match ignore block-state variants and match by `Block` type only (e.g. matches every log rotation, every stained-glass color)
- #295 Block Destabilizer — GUI: Lazy toggle remembers which connected blocks *didn't* match last time, so re-triggering doesn't re-scan the whole structure from scratch; Reset clears that memory
- #296 Block Destabilizer — falling blocks landing on an obstruction (another block, a tile-entity-bearing block) vanish without dropping an item, matching 1.12.2's design (this is a demolition tool, not a b…
- #297 Spectre Key — craft: Spectre Ingot at the 3 corners (top-left, middle-left, bottom-right), Stable Ender Pearl center-left; Spectre Ingot itself crafts from Lapis Lazuli + Gold Ingot + Ectoplasm (or th…

**5. Batch 3 — remaining (non-block) items / Slice 27 — Spectre Tools**

- #303 Spectre Sword — craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) and enchantability (22) than a diamond sword, repairable with Spectre Ingot on an anvi…

**5. Batch 3 — remaining (non-block) items / Slice 28 — Spectre energy network (Charger/Coils/Injector)**

- #307 Spectre String — craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4
- #308 Spectre Coil (Normal/Redstone/Ender) — craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre Ingot/Glass; Redstone: Redstone Block/Redstone/Spectre String + a Normal coil; Ender: …
- #309 Spectre Coil (Genesis) — same attach/drop behavior as the other tiers, but generates a flat 10,000,000 FE/tick directly into whatever it's attached to, bypassing the player's pool entirely; creative-t…
- #310 Spectre Energy Injector — craft: Obsidian at the 4 corners, Spectre Lens top-center, a vanilla Beacon center, Spectre String left/right/bottom-center; once placed by a player, exposes that player's Sp…
- #311 Spectre Charger (Normal/Redstone/Ender/Genesis) — craft the first 3 tiers same escalating pattern as the Coils (Genesis has no recipe, creative-only); right-click to toggle on/off; while on and carrie…

**5. Batch 3 — remaining (non-block) items / Slice 29 — Spectre Lens**

- #312 Spectre Lens — craft: Spectre Ingot at the 4 corners, Emerald top/bottom-center, Diamond left/right-center, Glass in the middle; can only be placed directly on top of a vanilla Beacon (auto-breaks and…
- #314 Diaphanous Block — craft (Quartz Glass + 4 colored glass in a ring, see recipe) places a plain Stone-displaying block; combine it with any other single non-tile-entity block item in a crafting grid to…
- #318 Light Redirector — craft (planks + glass ring, see recipe); right-click a face to toggle it open/closed (texture swaps to a dimmer "disabled" look per face)
- #319 Light Redirector — an *open* face shows whatever block is directly on the *opposite* side of the redirector instead of its own texture (the "periscope" effect - e.g. open only the top and bottom faces…
- #320 Dyeing Machine — craft (Green/Red/Blue Dye + White Wool + Crafting Table, see recipe) and place; right-click opens a GUI with an item slot, a dye slot, and 2 result slots (recolor + enchant-glint-colo…
- #321 Dyeing Machine — put any non-block item in the left slot and a vanilla dye in the dye slot; taking the "recolor" result consumes 1 of each ingredient and gives back a copy of the item tinted the dye's…
- #322 Dyeing Machine — taking the "enchant glint" result instead gives back a copy of the item whose enchantment shimmer (only visible if the item is actually enchanted/glowing) renders in the dye's color i…
- #323 Dyeing Machine — a dyed armor piece (from the "recolor" result) also shows tinted while worn on your character model, not just held/in inventory
- #324 Dyeing Machine — closing the GUI while the item/dye slots still hold something drops those items at your feet instead of deleting them
- #325 Spirit — killing any living entity has a small chance to spawn a tiny flying wisp at the death spot (higher at night under a fuller moon, and higher still once you've beaten the Ender Dragon at least …
- #326 Spirit — drifts aimlessly near where it appeared, floats through the air (not on the ground), and despawns on its own after a while if left alone
- #327 Spirit — immune to regular attacks/arrows/fire/etc; only a Spectre Sword (or creative-mode/out-of-world damage) can actually hurt it, and it dies in one hit either way (1 HP)
- #328 Spirit — killing it with a Spectre Sword drops 1-2 Ectoplasm
- #329 Batch 1 block loot tables — break a spread of Batch 1 blocks in survival (stained brick, luminous block, biome stone/glass, lapis/quartz glass+lamp, contact button/lever, spectre log/plank/sapling, gl…
- #330 Bean Sprout — breaking a fully grown (age 7) sprout drops 3-4 beans total; a young sprout drops just 1
- #331 Colored Grass — breaking it drops plain Dirt; with a Silk Touch tool it drops itself
- #332 Compressed Slime Block — breaking it drops a regular vanilla Slime Block
- #333 Blocks that should drop nothing still drop nothing: Blazing Fire, Floo Brick, Rune Base, Bean Stalk/Lesser Bean Stalk, Diaphanous Block (returns its camo block via its own code, not a loot table)
- #334 Chest loot injection — Summoning Pendulum, Magic Hood and Slime Cube occasionally appear in dungeon chests; Magic Hood in village weaponsmith chests, Pendulum in stronghold corridor chests, Slime Cube…
- #335 Chest loot injection — a Biome Crystal shows up in roughly 1 in 5 vanilla chests, already tuned to a random biome (name shows the biome, works with the Biome Radar)
- #336 Config — first launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` boolean options, all defaulting to `true`
- #337 Config — `MagneticEnchantment = false` stops Magnetic appearing at the enchanting table/on books and stops it pulling drops; `ArtificialEndPortal = false` makes Evil Tear do nothing on an End Rod; `Lo…
- #338 Config — `MagicHood`/`SummoningPendulum`/`SlimeCube`/`BiomeCrystal = false` under `[loot]` stops that item being injected into chests (restart the world/server after editing)
- #339 Bean Pod — breaking one drops iron (8-20), gold (4-15), diamonds (1-5), 0-2 emeralds, 4-8 beans and a Golden Egg (in survival, not just creative; use `/give` beans to grow a full stalk if needed)
- #340 Bean Pod — `GoldenEgg = false` in the config removes the egg from the drops; blowing the pod up with TNT drops only some of the stacks
- #341 Glowing Mushrooms — generate a fresh world/new chunks and explore caves: small patches of glowing mushrooms appear underground (never with sky access), roughly one patch per 4 chunks; `GlowingMushroom…
- #342 Grass Seeds — craft one from a Grass Block (shapeless); right-click plain Dirt with it: becomes normal grass, seed consumed (not consumed in creative; other dirt types/blocks do nothing)
- #343 Colored Grass Seeds — Grass Seeds + one vanilla dye (shapeless) gives that color's seeds (16 colors); right-clicking Dirt with them makes grass tinted that color, matching the seed icon's tint
- #344 Colored Grass — spreads its color onto nearby Dirt over time when lit; reverts to Dirt when covered by an opaque block; breaking drops Dirt, and with Silk Touch drops a Colored Grass block that keeps …
- #345 Spirit config — `[numbers]` section: `SpiritLifeTime`, `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceEndIncrease` change Spirit despawn time and spawn odds (e.g. `SpiritChanceNormal = 1.0…
- #346 Runes — placed rune pixels now show the original's noisy texture (each pixel/connection strip slightly different, tinted by the dust color); the pattern stays stable while you look around/move (doesn'…
- #347 Runes — `[visual] FlatRunes = true` in the config switches them to the plain flat texture (takes effect on the next render, no restart needed if the config file is reloaded; otherwise restart)
- #348 Recipes — open the recipe book / a crafting table and confirm the backfilled recipes appear and craft (45 new): Item Collector (ender pearl / hopper / 3 obsidian, left-column shape), Advanced Item Col…
- #349 Recipes — Lapis Glass (x8), Quartz Glass (x8), Lapis/Quartz Lamp, Trigger Glass (x4); Biome Glass and the five Biome Stones (x16 each, ring around a Biome Crystal); Biome Sensor
- #350 Recipes — Super Lubricent: Ice (x16: slimeball/ice/water bucket), Platform (x6), Filtered Platform, new **Super Lubricent Tincture** item (wheat seeds + *water bottle* + beans, x4 — a plain/awkward po…
- #351 Recipes — Stable Ender Pearl, Lesser Magic Bean (8 gold nuggets around Beans), Bean Stew (both orientations: wheat above/below the beans, bowl at the bottom), Blaze and Steel, ID Card (paper + ink sac…
- #352 Recipes — shapes were ported with 1.12.2's exact (left-aligned, right-padded) layouts, so a few look lopsided (Item Collector, Advanced Item Collector, Global Chat Detector, Slime Cube, Trigger Glass,…
- #353 Config — `[visual] HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>` (no dimension/XYZ even with Shift held); Position Filter shows no coordinate tooltip and no "ho…
- #354 Config — `[numbers] BlockDestabilizerLimit`: default 50 blocks per activation as before; raise it (e.g. 500) and a bigger connected mass falls at once; `0` = no limit
- #355 Config — `[numbers] TriggerGlassChainLimit`: default 20 as before; lower it (e.g. 3) and only that many touching Trigger Glass blocks go passable per pulse; `0` = whole connected mass
- #356 Ender Anchor chunk loading — place an Ender Anchor far from any player (e.g. 300+ blocks from an Ender Bridge facing it, or use `/forceload query` at the anchor's chunk), walk away so its chunk would …
- #357 Ender Anchor chunk loading — persists across a save/quit/reload (chunk still force-loaded on reload, no duplicate registration); breaking the anchor (by hand, piston, or explosion) un-forces its chunk…
- #358 Ender Anchor chunk loading — `[features] EnderAnchorChunkloading = false`: newly placed anchors don't force anything, and anchors that had forced a chunk release it the next time they load
- #359 Ender Anchor tooltip — hovering the item (inventory/creative) shows "Keeps the chunk it is in loaded"; the line disappears when `EnderAnchorChunkloading = false`
- #356 Config — `[worldgen] PeaceCandle = false`: no plains village church is replaced any more, new villages generate exactly like vanilla (the wrapper adds no extra randomness); default `true`. Applies per…


## D. Manual checks that aren't single checklist rows

- **All 28 GUIs** - open each once with the debug flag on and review the screenshots: see [GUI_TEST_LIST.md](GUI_TEST_LIST.md) (#120 audit).
- **Worldgen** (can't be triggered headlessly): Lotus, Ancient Furnace, Glowing Mushrooms, Bean Sprout, Peace Candle church in plains villages (fresh worlds, `/locate village`; check the path reaches the church door).
- **Coremods that only a real client can confirm**: Rain Shield visual/mechanical suppression, Lapis Lamp spawning, Blazing Fire / Super Lubricent Boots (#134/#135 - had a prior "still broken" report as a Mixin), Dyeing Machine armor/ItemRenderer recolor, redstone power coremods (Redstone Interface/Activator/Remote).
- **Persistence**: quit and reload a world with Ender Anchors (chunkloading, #356-#358), Spectre dimension (teleport in/out, restart client, rejoin), Redstone Remote bindings.
- **Multiplayer / server**: `runServer` boot, Anchor chunkloading + tooltip read the *client's* config, Spectre energy network and Redstone Remote across clients - none have been tried.
- **Config options** (`randomthings-common.toml`): each of `[worldgen]`, `[loot]`, `[features]`, `[visual]`, `[numbers]`, `[debug]` - toggle and confirm the described effect.
- **Retest after the 10/3 fixes**: thrown Weather/Golden Egg sprites (#205/#230), Floo Powder shapeless (#180), Colored Grass name + shovel (#56), Chat Detector / Global Chat Detector / Advanced Redstone Interface / Remote Edit GUIs, Sound Recorder list colors (#155).
- **Never in-game confirmed (wiki status DONE-UNTESTED)**: roughly 55 of the 100 wiki-documented features; see `WIKI_FEATURE_STATUS.md` (filter for `DONE-UNTESTED`).
- **Known-open questions waiting on you**: #93 and #158 (code matches 1.12.2 - need the exact behavior you saw), #120 (which other GUIs look wrong), duplicate row numbers #134/#136/#356 in the checklist.
