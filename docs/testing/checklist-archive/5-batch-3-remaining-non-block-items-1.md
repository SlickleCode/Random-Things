## 5. Batch 3 — remaining (non-block) items

| # | Feature | Result | Feedback |
|---|---|---|---|
| 121 | Bean Stew — eat it, restores 8 hunger, leaves you holding an empty bowl afterward |  |  |
| 122 | Blaze and Steel — right-click a block face to place Blazing Fire instead of vanilla fire; damages the tool by 1 per use | PASS |  |
| 123 | Bottle of Air — right-click while standing in water to drink; should fully refill your air/breath meter in one go and be consumed (count -1) | PASS |  |
| 124 | Stable Ender Pearl — right-click to bind it to yourself (tooltip should show "Bound to \<your name\>"), then throw it … | PASS |  |
| 125 | Stable Ender Pearl — thrown while unbound (fresh from creative, no right-click-to-bind first) … | PASS |  |
| 126 | ~~Obsidian Skull~~ (removed per request) | DONE |  |
| 127 | ~~Lava Charm~~ (removed per request) | DONE |  |
| 128 | ~~Lava Wader~~ (removed per request) | DONE |  |
| 129 | ~~Lava Wader~~ (removed per request) | DONE |  |
| 130 | ~~Water Walking Boots~~ (removed per request) | DONE |  |
| 131 | ~~Obsidian Water Walking Boots~~ (removed per request) | DONE |  |
| 132 | ~~Obsidian Water Walking Boots~~ (removed per request) | DONE |  |
| 133 | ~~Lava Wader~~ (removed per request) | DONE |  |
| 134 | Super Lubricent Boots — worn as boots, makes EVERY surface as slippery as a Super Lubricent block (not just the three actual Super Lubricent blocks), sliding around constantly | PASS |  |
| 135 | Sneaking while wearing Super Lubricent Boots — should walk normally (sneaking cancels the boots' effect, the only way to stop sliding) | PASS |  |
| 136 | Position Filter — right-click a block to store its position … | PASS |  |
| 137 | Golden Compass — freshly crafted (gold ingots + vanilla compass), no target set yet: needle wobbles/spins randomly like an unbound vanilla compass near the Nether, never settles | PASS |  |
| 138 | Golden Compass — combine with a Position Filter that has a stored position at a crafting table … | PASS |  |
| 139 | Emerald Compass — freshly crafted (emeralds + vanilla compass), no target bound yet: needle wobbles randomly, same as an unbound Golden Compass | PASS |  |
| 140 | Emerald Compass — bind an ID Card to yourself (right-click), then combine it with the compass at a crafting table … | PASS |  |
| 141 | Emerald Compass — bound player logs off: needle goes back to a random wobble instead of pointing at their last known position, and resumes tracking automatically once they log back in | PASS |  |
| 142 | Escape Rope — deep underground (no sky visible from where you're standing), hold right-click … | PASS |  |
| 143 | Escape Rope — release right-click partway through the search instead of holding it the whole time … | PASS |  |
| 144 | Escape Rope — used 20 times (its full durability): breaks like a normal tool after the 21st successful use | PASS |  |
| 145 | Chunk Analyzer — right-click to open the GUI, hit the "Scan" button … | PASS |  |
| 146 | Chunk Analyzer — close the GUI and reopen it without rescanning: the previous scan results are still there (persisted in the item's own NBT), no need to rescan | PASS |  |
| 147 | Chunk Analyzer — scan a chunk, then move to your own inventory/hotbar/armor while the scan is running (e.g. pick up or drop an item) … | PASS |  |
| 148 | Chunk Analyzer — scan a very tall/full chunk (lots of terrain, caves, etc.) … | PASS |  |
| 149 | Biome Radar — right-click with an empty hand and a Biome Crystal in your other hand's slot/held: inserting works, and right-clicking again with an empty hand takes the crystal back out |  |  |
| 150 | Biome Radar — build the antenna (iron bars in the specific pattern above the radar — see `BiomeRadarTileEntity.isValid()` or just try a small iron-bars frame 2-3 blocks up), insert a crystal, then power it with redstone: it starts searching, and colored dust particles appear above the antenna as it samples biomes |  |  |
| 151 | Biome Radar — let a search finish (crystal's target biome found): particles change to the target biome's color, then right-click with paper: consumes the paper and gives you a Position Filter pointing at the found location |  |  |
| 152 | Biome Radar — break one of the antenna's iron bars while a search is active (not directly adjacent to the radar, so no immediate neighbor-update): within a few seconds the radar should notice and drop back to idle instead of continuing to search with a broken antenna |  |  |
| 153 | Biome Radar — break the radar block itself while it has a crystal inserted: the crystal drops instead of being deleted |  |  |
| 154 | Sound Recorder — sneak-right-click to start recording (icon changes), walk around hearing various sounds (footsteps, mobs, blocks, etc … | PASS |  |
| 155 | Sound Recorder GUI — put a blank Sound Pattern in the left slot, click a sound name in the list: a "full" Sound Pattern (different icon, tooltip shows the sound's name) appears in the right slot and the blank one is consumed | PASS | Confirmed working by user 2026-10-04 (hover/click, border, scrollbar drag, row padding). |
| 156 | Sound Pattern — sneak-right-click a full (stamped) pattern: clears it back to blank | PASS |  |
| 157 | Sound Box — right-click with a full Sound Pattern to insert it (block's texture changes to its "full" look), then power it with redstone: plays that sound … | PASS |  |
| 158 | Sound Dampener — place, right-click to open its 9-slot GUI, insert a few full Sound Patterns: any of those sounds played by ANYONE within ~20 blocks of the block go silent for everyone in range, not just you | FIXED | **Investigated, no bug found - matches 1.12.2 exactly** (byte-for-byte same `distanceSq < 20*20` check, confirmed via that branch's real source). Note the 20-block check is against the *sound's own origin position*, not your distance from the dampener - if the sound source (a mob, a block) was itself within 20 blocks of the dampener, it mutes regardless of how far you personally were standing when you heard it. If that's not what happened, tell me the exact distance from the sound source (not you) to the dampener and I'll dig further. 2026-10-11 retest: muting works. NEW BUG FOUND + FIXED: hovering a Sound Pattern in the Sound Dampener / Portable Sound Dampener GUI showed no tooltip - `AbstractSoundDampenerScreen` had no `render` override (no `renderHoveredToolTip`, no dimmed background); every other container screen has it, 1.12.2's `GuiContainerBase` did it for all. Retest: hover a pattern in both GUIs. The GUI click test (`RT_AUTOTEST_CLICKS`) now also hovers a filled slot in every container GUI and fails if no item tooltip is requested; verified it catches this bug (2 failures with the fix reverted, 17 slot tooltips pass with it). |
| 159 | Portable Sound Dampener — right-click to open its 9-slot GUI (carried in your own inventory, not placed), insert a few full Sound Patterns: those sounds go silent for you specifically no matter where you are, as long as you're carrying it anywhere in your inventory (not just held) |  |  |
| 160 | Sound Dampener / Portable Sound Dampener — break the block or take the pattern back out of a slot: the muting stops for that sound, and any Sound Patterns inside are dropped/returned rather than deleted |  |  |
| 161 | Rune Dust — right-click the top of a solid block: places a new, invisible-until-decorated Rune Base with one colored pixel in the corner you clicked (a 4x4 grid across the whole block) … | PASS |  |
| 162 | Rune Base — place two side-by-side, put matching-colored pixels on the touching edge of each: a connecting strip of that color bridges the gap between them … | PASS |  |
| 163 | Rune Base — left-click (not fully breaking) anywhere on it: every pixel on the block pops off as its own dropped Rune Dust item, and the block itself disappears immediately | PASS |  |
| 164 | Rune Base — break the block outright (not via left-click-to-remove-one) while it still has pixels: every remaining pixel drops as its matching Rune Dust, not just one | PASS |  |
| 165 | Rune Base — remove the solid block underneath it: the Rune Base itself breaks (and drops its pixels, per #164) | PASS |  |
| 166 | Rune Pattern — right-click an existing Rune Base: copies its pattern onto the Rune Pattern item (tooltip lists each color and how many pixels) … | PASS |  |
| 167 | Rune Pattern — sneak-right-click: turns back into a plain vanilla paper item | PASS |  |

### Slice 15

| # | Feature | Result | Feedback |
|---|---|---|---|
| 168 | Imbuing Station — craft: water bucket + emerald + 2x vine + 2x lily pad + terracotta |  |  |
| 169 | Imbuing Station — put the COBBLESTONE in the center slot and a water bottle + vine + bone meal in the 3 ingredient slots (any order, as in 1.12.2): after 200 ticks, produces Mossy Cobblestone in the output slot and consumes exactly 1 of each input | FIXED | Earlier text had the bottle and cobblestone swapped. Real bug found by `behavior.imbuing_station.*`: the machine consumed the inputs but could never put the result into its output slot (it used `insertItem`, which the output slot rejects on purpose). Fixed; please retest this and #170-173 in-game. |
| 170 | Imbuing Station — water bottle (center) + coal + flint + blaze powder -> Fire Imbue potion |  |  |
| 171 | Imbuing Station — water bottle (center) + spider eye + rotten flesh + red mushroom -> Poison Imbue potion |  |  |
| 172 | Imbuing Station — water bottle (center) + Lesser Magic Bean + lapis lazuli + glowstone dust -> Experience Imbue potion |  |  |
| 173 | Imbuing Station — water bottle (center) + wither skeleton skull + nether brick + ghast tear -> Wither Imbue potion |  |  |
| 174 | Imbuing Station — progress arrow + the three per-slot "fill" animations only animate while a valid recipe is in progress; output slot rejects manual insertion (machine-only) |  |  |
| 175 | Drinking a Fire/Poison/Experience/Wither Imbue potion grants its effect for 20 minutes, leaves an empty glass bottle, and shows its own HUD/inventory icon; drinking a second kind replaces the first rather than stacking |  |  |
| 176 | While Fire Imbue is active, dealing direct melee/attack damage (not projectiles) sets the target on fire for 10s |  |  |
| 177 | While Poison Imbue is active, a direct hit gives the target Poison II for 10s |  |  |
| 178 | While Wither Imbue is active, a direct hit gives the target Wither II for 5s |  |  |
| 179 | While Experience Imbue is active, killing a mob drops double XP |  |  |

### Slice 16

| # | Feature | Result | Feedback |
|---|---|---|---|
| 180 | Floo Powder — craft: Ender Pearl top-left, Redstone top-right, Gunpowder bottom-left, plain Beans bottom-right (2x2, makes 16) | PASS | Confirmed working by user 2026-10-04. |
| 181 | Floo Pouch — craft: leather (8-around) + Floo Powder (center); sneak-right-click tops it up from any Floo Powder in your inventory, up to 128, shown as a durability-bar-style charge meter |  |  |
| 182 | Floo Sign — craft: Floo Powder (8-around) + any wood-type sign (center) |  |  |
| 183 | Floo Token — craft: Floo Powder (8-around) + paper (center), makes 2 |  |  |
| 184 | Floo Sign — right-click a vanilla Bricks block: converts the whole contiguous horizontal group it's touching (up to 20 blocks) into a named fireplace … | PASS |  |
| 185 | Floo Sign — right-clicking a brick that's already part of an existing fireplace fails instead of double-converting it | PASS |  |
| 186 | Breaking any single brick in a fireplace (master or a child) reverts the *entire* group back to plain Bricks | PASS |  |
| 187 | Stand on any brick of a fireplace, hold Floo Powder (or carry a Floo Pouch with charge), and type a chat message … | PASS |  |
| 188 | Typing a name that resolves back to the fireplace you're already standing on shows "You are already at this destination" instead of teleporting | PASS |  |
| 189 | Departure and arrival both show a burst of flame particles, visible to any nearby player, not just the one teleporting | PASS |  |
| 190 | Floo Token — drop it and leave it resting on the ground: after a few seconds it vanishes and a temporary fireplace (flame particles, no name) appears at that spot for a short while | PASS |  |
| 191 | Standing in/near a temporary Floo Token fireplace and typing a chat message teleports for free (no Floo Powder/pouch charge needed) | PASS |  |
| 192 | Dropping a second Floo Token near an already-active temporary fireplace does not spawn a duplicate | PASS |  |

### Slice 17

| # | Feature | Result | Feedback |
|---|---|---|---|
| 193 | Eclipsed Clock — craft: 4x obsidian + 4x gold ingot + ghast tear (center) |  |  |
| 194 | Eclipsed Clock — right-click a wall to hang it; right-click the hung clock to advance its target time by 30s, sneak-right-click to go back 30s; the dial face visually updates to match | PASS |  |
| 195 | Eclipsed Clock — looking at it (or having just clicked it) shows the target time as floating text above it | PASS |  |
| 196 | Eclipsed Clock — right-click it while holding a charged Time in a Bottle … | PASS |  |
| 197 | Eclipsed Clock — breaking it drops the clock item back (preserving nothing - a fresh clock); creative-mode breaking drops nothing | PASS |  |
| 198 | Time in a Bottle — craft: 3x gold ingot + 2x diamond + 2x lapis lazuli + clock + glass bottle |  |  |
| 199 | Time in a Bottle — passively fills with stored time while carried anywhere in inventory (tooltip shows H/M/S); holding two at once drains the one with less time to 0 | PASS |  |
| 200 | Time in a Bottle — right-click a block (not the Eclipsed Clock) with 30+ seconds stored … | PASS |  |
| 201 | Time in a Bottle — right-click an existing Time Accelerator … | PASS |  |
| 202 | Weather Egg (Sun/Rain/Storm) — craft each: obsidian corners + fire charge (center) + the type-specific ingredient (sunflower/potion/sugar) + lapis lazuli or feather per type, makes 2 |  |  |
| 203 | Weather Egg — throwing any type spawns a rising cloud where it lands (damages any entity hit directly) | PASS |  |
| 204 | Weather Egg cloud — rises for ~10s then floats to the world height limit, then sets the weather … | PASS |  |
| 205 | Weather Egg cloud — visibly different particle effects per type while rising (plus a colored accent for Rain/Storm) | PASS | Confirmed working by user 2026-10-04. |

### Slice 18

| # | Feature | Result | Feedback |
|---|---|---|---|
| 206 | Ender Mailbox — craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center) |  |  |
| 207 | Ender Letter — craft: 2x paper (diagonal) + ender pearl (center) |  |  |
| 208 | Ender Letter — right-click (fresh, unaddressed) opens a 9-slot "write a letter" GUI with a receiver-name text field; put items in, type a player's name | PASS |  |
| 209 | Ender Mailbox — place one: it remembers you as its owner |  |  |
| 210 | Ender Mailbox — sneak-right-click any mailbox (yours or anyone else's) while holding an addressed letter: delivers it into the *named recipient's* personal inbox (not the block you clicked), consumes the letter, plays a sound |  |  |
| 211 | Ender Mailbox — sneak-right-click with an addressed letter naming a player who doesn't exist: get a "does not seem to reside in this world" message, letter untouched |  |  |
| 212 | Ender Mailbox — right-click your own placed mailbox (not sneaking): opens your personal inbox - the same inbox no matter which of your placed mailboxes you use |  |  |
| 213 | Ender Mailbox — right-click a mailbox someone else placed (not sneaking): "You are not the owner of this mailbox," no GUI opens |  |  |
| 214 | Ender Mailbox — after mail arrives in your inbox, the mailbox block starts glowing/emitting portal particles within ~10 seconds; stops once you've collected everything |  |  |
| 215 | Receiving a letter: take it from your inbox into your own inventory, right-click it - opens the same letter GUI, output-only this time (can take items out, can't add more), receiver field disabled, tooltip shows sender's name |  |  |
| 216 | Emptying a received letter completely and closing the GUI makes the (now-empty) letter item disappear | PASS |  |
| 217 | A received (unopened) letter shows an enchantment-glow shimmer in your inventory | PASS |  |

### Slice 19

| # | Feature | Result | Feedback |
|---|---|---|---|
| 218 | Ender Bucket — craft: 2x iron ingot (top corners) + ender pearl (center) |  |  |
| 219 | Ender Bucket — right-click while looking near (not necessarily exactly at) a body of water/lava: picks up 1000mB even if the exact block you're pointed at is a flowing edge, not a source … | PASS |  |
| 220 | Ender Bucket — right-click a solid block face while full: places the contained fluid normally | PASS |  |
| 221 | Ender Bucket — held-item tooltip/name shows "Ender Bucket of <Fluid>" while full | PASS |  |
| 222 | Reinforced Ender Bucket — craft: obsidian (side corners) + Ender Bucket (top center) + Stable Ender Pearl (bottom center) | PASS |  |
| 223 | Reinforced Ender Bucket — holds 10x the fluid (10000mB); durability bar shows fill level, tinted to the contained fluid's own color | PASS |  |
| 224 | Reinforced Ender Bucket — sneak while picking up: keeps draining the whole connected body instead of stopping after the first 1000mB, until full or the body runs out | PASS |  |

### Slice 20 — Summoning Pendulum, Golden Egg/Golden Chicken, Fluid Display

| # | Feature | Result | Feedback |
|---|---|---|---|
| 225 | Summoning Pendulum — right-click a passive (non-hostile, non-player) mob: captures it (mob disappears, enderman-teleport sound plays), up to 5 at once | PASS |  |
| 226 | Summoning Pendulum — right-clicking a 6th mob while already holding 5 does nothing but play a firecharge-use sound instead | PASS |  |
| 227 | Summoning Pendulum — durability bar fills as purple, up to 5/5; shows an enchant-glow shimmer once full | PASS |  |
| 228 | Summoning Pendulum — right-click a block: releases the oldest captured mob there (FIFO order); with nothing captured, plays a firecharge-use sound instead | PASS |  |
| 229 | Summoning Pendulum — plain tooltip shows "Holding N Entity/Entities"; holding Shift instead lists each captured mob's name | PASS |  |
| 230 | Golden Egg — throw it (right-click): always hatches a Golden Chicken where it lands (unlike vanilla eggs' 1-in-8 chance), and damages anything it hits directly for 1 | PASS | Confirmed working by user 2026-10-04. |
| 231 | Golden Chicken — behaves like a normal passive chicken (follows held seeds, can be led/bred by seeds) but never lays eggs and has no breeding-goal AI (matches the original's own apparent … | PASS |  |
| 232 | Golden Chicken — periodically drops 3 gold ingots instead of laying an egg … | PASS |  |
| 233 | Fluid Display — craft: 8x glass (ring) + glass bottle (center) | PASS |  |
| 234 | Fluid Display — right-click with a filled fluid container (bucket, Ender Bucket, etc.): block takes on that fluid's texture/color, still image | FIXED |  |
| 235 | Fluid Display — plain right-click (empty/non-fluid item): toggles between the still and flowing texture for the displayed fluid | PASS |  |
| 236 | Fluid Display — sneak-right-click with a non-fluid item: rotates the displayed cube 90 degrees | PASS |  |
| 237 | Fluid Display — breaking it drops itself as an item | PASS |  |

### Slice 21 — Artificial End Portal, Creative Player Interface

| # | Feature | Result | Feedback |
|---|---|---|---|
| 238 | Evil Tear — craft: Wither Skeleton Skull + Ghast Tear + Ender Pearl (shapeless, any arrangement) | FIXED | Changed to `minecraft:crafting_shapeless` per request (no 1.12.2 recipe found to check against either way, so just did it). Please retest. |
| 239 | Build the ritual structure (3x3 air pocket, End Rod above End Stone above it, End Stone floor, a surrounding obsidian ring 2 blocks out … | PASS |  |
| 240 | Right-clicking an End Rod that ISN'T part of a valid structure does nothing (no tear consumed) | PASS |  |
| 241 | The portal charges up for ~10 seconds (purple enchant-table sparkles rising out of it starting a couple seconds in), then shows the swirling end-portal starfield visual once fully charged | PASS |  |
| 242 | Once fully charged, standing in it teleports you to the End (same as a vanilla end portal) - not while riding or being ridden | PASS |  |
| 243 | Breaking any block of the supporting structure while the portal is active makes it disappear | PASS |  |
| 244 | Creative Player Interface — creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves identically to the already-shipped Player Interface (binds to whoever places it, exposes their inventory as a capability for hoppers etc.) |  |  |

### Slice 22 — Ender Bridge, Ender Anchor, Prismarine Ender Bridge

| # | Feature | Result | Feedback |
|---|---|---|---|
| 245 | Ender Anchor — craft: 8x obsidian (ring) + Stable Ender Pearl (center) |  |  |
| 246 | Ender Bridge — craft: 6x end stone + redstone (center) + Stable Ender Pearl (right-middle) |  |  |
| 247 | Ender Bridge — placing it faces you (matches the direction you were looking, including straight up/down) … | PASS |  |
| 248 | Ender Bridge — if the scan reaches an Ender Anchor before hitting anything else solid … | PASS |  |
| 249 | Ender Bridge — if the scan instead hits any other solid block first (or the anchor's chunk isn't loaded), it just goes dark without teleporting anyone | PASS |  |
| 250 | Ender Bridge — cutting redstone power resets it back to idle so it can be triggered again | PASS |  |
| 251 | Prismarine Ender Bridge — craft: 4x Prismarine Shard + 4x Prismarine Crystals (corners/edges) + Ender Bridge (center) … | PASS |  |

### Slice 23 — Magic Hood (Batch 7, slice 3)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 252 | Magic Hood — no crafting recipe (dungeon-chest-only in 1.12.2 too); creative-only for now | PASS |  |
| 253 | Magic Hood — wear it as a helmet: other players can no longer see your nametag above your head, whether you're sneaking or not |  |  |
| 254 | Magic Hood — wear it as a helmet: your potion-effect particle swirl no longer shows to anyone, whether you're sneaking or not | FIXED | Real bug: `shouldHideParticles(true)` doesn't actually suppress particles - that flag is what vanilla itself uses for genuinely ambient effects (Beacon/Conduit), just switching to a rarer, fainter particle type, not zero. The real gate is the synced particle color; forcing it to exactly 0 is what stops spawning entirely. Please retest. |
| 255 | Magic Hood — has durability of leather | PASS |  |

### Slice 24 — Rain Shield (Batch 7, slice 4)

| # | Feature | Result | Feedback |
|---|---|---|---|
| 256 | Rain Shield — craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row) |  |  |
| 257 | Rain Shield — needs solid ground underneath; breaks and drops itself if that support is removed (like a torch) | PASS |  |
| 258 | Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire extinguishing near it - the mechanical effects of rain/snow are suppressed | FIXED | The mechanical suppression itself was already confirmed correct via a bytecode-level check in an earlier session - the "still see rain particles" part of this report was really #261 (the purely visual half, disclosed as not-yet-built at the time). That's now implemented too - see #261. Please retest the mechanical side specifically (wetting/fire-extinguish) if it still seems off after that. |
| 259 | Rain Shield — powered via redstone: shielding turns off, rain/snow affects the area normally again |  |  |
| 260 | Rain Shield — while raining, shows a ring of flame/smoke particles swirling around its candle, regardless of whether the shield is currently active or powered | PASS |  |
| 261 | Rain Shield — the falling rain/snow visual itself IS now locally hidden near the shield (real behavior change from 1.12.2, per explicit request) | FIXED | Implemented via a new coremod redirecting the one `World.getBiome` call inside vanilla's rain-rendering loop, substituting a real no-precipitation biome for columns inside an active shield's radius. Verified via `runClient` that the new coremod transforms cleanly (no VerifyError/crash). Please retest in-game - I can't visually confirm rain/snow rendering without a real interactive session. |

