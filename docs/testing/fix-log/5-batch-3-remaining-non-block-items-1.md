## 5. Batch 3 — remaining (non-block) items

### #127 — Lava Charm — same as Obsidian Skull, but specifically no-sells lava damage (spends internal charge, needs a moment to "recharge" between saves)

— found a real bug: unlike Lava Wader (which regenerates charge via `onArmorTick` every tick while worn), Lava Charm had no charge-regeneration logic at all since it's a plain carried item, not armor — its charge NBT tag never even existed, so the protection check always failed. Added the same regeneration via `inventoryTick` instead. Please retest.

### #128 — Lava Wader — worn as boots: fully immune to lava damage as long as its charge isn't depleted (charge drains on each save, regenerates over ~2 seconds after)

Same root cause/fix as #130 (jittery liquid-surface detection) - see that row.

### #130 — Water Walking Boots — worn as boots: walking on water should be automatic/passive; sneaking should let you sink through instead

— this port's water-walking is a plain per-tick Forge event listener, not ASM, so that wasn't actually the cause. Found two real bugs in it: (1) it checked the block at an exact `floor(posY)` snapshot of the player's feet, but once resting right at the liquid surface the feet Y sits exactly on the block boundary, and floating-point noise from the *previous* tick's own upward nudge was enough to flip that floor() between the liquid block and the … (full writeup in git history)

### #131 — Obsidian Water Walking Boots — same water-walking as above, plus counts as fire-damage protection like Obsidian Skull

— water-walking fixed same as #130. The "does not protect from lava" part is correct/expected: this item was only ever meant to walk on lava, not protect against its damage (that's Lava Wader specifically) — matches the real 1.12.2 design.

### #136 — Super Lubricent Stone

Same boat-acceleration bug and fix as #31 - see that row for the full writeup.

### #145 — Chunk Analyzer — right-click to open the GUI, hit the "Scan" button: shows "Scanning Chunk..." with an animated dots suffix, Scan button disables while scanning, then a scrollable list of every distinct block in your current chunk appears sorted by count (highest first), each row showing the block's icon/name/count

New this session (Batch 3 slice 5). Craft: iron bars + iron ingots + glass + stone + dirt — see recipe. Two real bugs found and fixed via user testing: GUI rendered as a giant dirt/black rectangle (AbstractList's inlined menu-chrome), and the scan skipped the topmost block in every column (off-by-one on the height loop).

### #149 — Biome Radar — right-click with an empty hand and a Biome Crystal in your other hand's slot/held: inserting works, and right-clicking again with an empty hand takes the crystal back out

New this session (Batch 3, first deferred-subsystem slice: BiomeRadar). Craft: iron ingots + glass + a Biome Sensor in a ring (III / GSG / III). Biome Sensor and Biome Crystal are creative/give-only, matching the 1.12.2 original (never craftable there either).

### #150 — Biome Radar — build the antenna (iron bars in the specific pattern above the radar — see `BiomeRadarTileEntity.isValid()` or just try a small iron-bars frame 2-3 blocks up), insert a crystal, then power it with redstone: it starts searching, and colored dust particles appear above the antenna as it samples biomes

New this session. **2026-09-27: `onDataPacket` live-sync bug fixed** (see #161's writeup) - the particle spawning reads `this.state` directly from the client TE, so a state change (idle→searching) triggered by redstone power might not have shown up client-side without a relog before this fix. Please retest.

### #151 — Biome Radar — let a search finish (crystal's target biome found): particles change to the target biome's color, then right-click with paper: consumes the paper and gives you a Position Filter pointing at the found location

New this session. Same `onDataPacket` fix as #150 applies to the finished→idle-color transition.

### #152 — Biome Radar — break one of the antenna's iron bars while a search is active (not directly adjacent to the radar, so no immediate neighbor-update): within a few seconds the radar should notice and drop back to idle instead of continuing to search with a broken antenna

New this session — checks the periodic isValid() poll. Same `onDataPacket` fix as #150 applies.

### #153 — Biome Radar — break the radar block itself while it has a crystal inserted: the crystal drops instead of being deleted

New this session.

### #154 — Sound Recorder — sneak-right-click to start recording (icon changes), walk around hearing various sounds (footsteps, mobs, blocks, etc — up to 10 distinct ones), sneak-right-click again to stop, then plain right-click to open its GUI: a list of the recorded sound names appears

New this session (Batch 3, Sound-pattern family slice). Craft: iron bars + glass + iron ingots + lapis lazuli — see recipe.

### #155 — Sound Recorder GUI — put a blank Sound Pattern in the left slot, click a sound name in the list: a "full" Sound Pattern (different icon, tooltip shows the sound's name) appears in the right slot and the blank one is consumed

New this session. Craft blank pattern: string + paper + lapis lazuli in a vertical line.

### #156 — Sound Pattern — sneak-right-click a full (stamped) pattern: clears it back to blank

New this session.

### #157 — Sound Box — right-click with a full Sound Pattern to insert it (block's texture changes to its "full" look), then power it with redstone: plays that sound. Right-click the block again (empty hand) to take the pattern back out

New this session. Craft: wood planks + lapis lazuli in a ring.

### #158 — Sound Dampener — place, right-click to open its 9-slot GUI, insert a few full Sound Patterns: any of those sounds played by ANYONE within ~20 blocks of the block go silent for everyone in range, not just you

New this session. Craft: wood planks + white wool + a Portable Sound Dampener in a ring. **2026-09-27: `onDataPacket` live-sync bug fixed** (see #161's writeup) - GUI slot contents sync via the separate Container system (unaffected), but any other client-side read of this TE's own data directly could have been stale without a relog before this fix.

### #159 — Portable Sound Dampener — right-click to open its 9-slot GUI (carried in your own inventory, not placed), insert a few full Sound Patterns: those sounds go silent for you specifically no matter where you are, as long as you're carrying it anywhere in your inventory (not just held)

New this session. Craft: iron ingots + string + lapis lazuli in a ring. No Baubles slot in this port (dropped third-party compat) — carrying it anywhere in your inventory is enough, matching how Obsidian Skull/Lava Charm already work.

### #160 — Sound Dampener / Portable Sound Dampener — break the block or take the pattern back out of a slot: the muting stops for that sound, and any Sound Patterns inside are dropped/returned rather than deleted

New this session.

### #168 — Imbuing Station — craft: water bucket + emerald + 2x vine + 2x lily pad + terracotta

New this session (Batch 3, Imbuing/Rune slice, Imbuing Station half).

### #169 — Imbuing Station — put a water bottle in the center slot + vine + bone meal + cobblestone in the 3 ingredient slots (any order): after 200 ticks, produces Mossy Cobblestone and consumes exactly 1 of each input

The one non-potion recipe; confirms slot-order-independence.

### #180 — Floo Powder — craft: ender pearl + redstone + gunpowder + plain bean (4-way, makes 16)

New this session (Floo network slice).

### #184 — Floo Sign — right-click a vanilla Bricks block: converts the whole contiguous horizontal group it's touching (up to 20 blocks) into a named fireplace; renaming the sign in an anvil first sets that name

Horizontal-only flood fill, matching the original - a fireplace has to be a flat wall/floor of bricks, not a 3D blob.

### #187 — Stand on any brick of a fireplace, hold Floo Powder (or carry a Floo Pouch with charge), and type a chat message: teleports you to the destination whose name is the closest match (typos still resolve to the intended one) instead of sending the message, consuming 1 Floo Powder or 1 pouch charge

Creative mode: same teleport, no consumption.

### #189 — Departure and arrival both show a burst of flame particles, visible to any nearby player, not just the one teleporting

— raised the burst height from +0.6 (just above the brick) to +1.2 blocks (roughly waist height). Please retest.

### #193 — Eclipsed Clock — craft: 4x obsidian + 4x gold ingot + ghast tear (center)

New this session (New-Entity cluster: Eclipsed Clock/Weather Eggs/Time in a Bottle). Ghast Tear substitutes for the original's "Evil Tear" ingredient, tied to the not-yet-ported Artificial End Portal feature - disclosed substitution.

### #200 — Time in a Bottle — right-click a block (not the Eclipsed Clock) with 30+ seconds stored: spends 30s, plants a Time Accelerator at that block, showing the bottle icon just outside all 6 faces (not just one) and particles spread across the whole block, that ticks its tile entity 1x/game-tick for 30 seconds

**2026-09-27, real crash found and fixed (reported by user): game crashed on use.** Root cause: `TimeAcceleratorEntity.target` was a plain field, never synced to the client. Fixed with a real synced `DataParameter<BlockPos>`. … (full writeup in git history)

### #201 — Time in a Bottle — right-click an existing Time Accelerator: doubles its rate (up to 32x) if enough time is stored, each doubling extends its remaining duration and plays a rising note-block chime

Unblocked - #200 confirmed working. **2026-09-27, per user request:** both this and #200's "not enough stored time" cases now send the player an action-bar-adjacent status message (`item.randomthings.time_in_a_bottle.not_enough_time`, red text) instead of silently doing nothing - matches this port's existing `sendStatusMessage` convention (`EnderMailboxBlock`/`EnderLetterItem`), not something 1.12.2 itself did. Not yet tested.

### #205 — Weather Egg cloud — visibly different particle effects per type while rising (plus a colored accent for Rain/Storm)

Disclosed simplification: the original's custom-tinted smoke particle class is replaced with the same tintable dust particle already used by Potion Vaporizer/Floo Network; the Sun variant's extra sunburst overlay render is dropped - see `WeatherCloudEntity`'s javadoc. **2026-09-28: investigated, inconclusive.** `ThrownWeatherEggEntity` is a near-verbatim `ProjectileItemEntity` (vanilla's own base for `EggEntity`), registered with `SpriteRenderer` the exact same way as the confirmed-working Spectre Illuminator, and its `IRendersAsItem#getItem()` is entirely vanilla machinery - found no code-level difference from vanilla's own (visibly working) thrown egg. Same symptom on #230 (Golden Egg, same base class). Possibly a client-side data-sync timing issue specific to fast, short-lived projectiles, not confirmed without live testing.

### #206 — Ender Mailbox — craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center)

New this session (Ender Letter feature).

### #208 — Ender Letter — right-click (fresh, unaddressed) opens a 9-slot "write a letter" GUI with a receiver-name text field; put items in, type a player's name

**2026-09-28: real race condition found** - the field's contents were only ever sent to the server from `Screen#removed()` (i.e. only when the GUI closes), racing the vanilla close-window packet sent over the same connection; the server could already have swapped `player.openContainer` back to the plain inventory container by the time this custom signal arrived, silently dropping it regardless of Enter. … (full writeup in git history)

### #218 — Ender Bucket — craft: 2x iron ingot (top corners) + ender pearl (center)

New this session. This port's first feature touching Forge's fluid capability API.

### #219 — Ender Bucket — right-click while looking near (not necessarily exactly at) a body of water/lava: picks up 1000mB even if the exact block you're pointed at is a flowing edge, not a source - it searches the connected body for a valid source

**2026-09-28: real bug found, ground-truthed via `javap -c`: this Forge build's own `FluidUtil.tryPickUpFluid`/`getFluidHandler(World, BlockPos, Direction)` only ever resolve a tile-entity-backed fluid capability at the target position - no branch at all for vanilla water/lava (`IBucketPickupHandler`) or a TE-less modded fluid block (`IFluidBlock`), so pickup always failed regardless of what was targeted.** Rewrote pickup to drain directly … (full writeup in git history)

### #221 — Ender Bucket — held-item tooltip/name shows "Ender Bucket of <Fluid>" while full

Disclosed simplification: the original's dynamic base+fluid-tint-overlay icon (a 1.12.2-era Forge model type not carried forward to this version) is replaced with a plain static icon - the name/tooltip still identifies the contained fluid, just not the icon color.

### #223 — Reinforced Ender Bucket — holds 10x the fluid (10000mB); durability bar shows fill level, tinted to the contained fluid's own color

Same issue as #219 - same fix applied here (plus preserved the sneak-drain-whole-body behavior against the new drain path). Please retest.

### #225 — Summoning Pendulum — right-click a passive (non-hostile, non-player) mob: captures it (mob disappears, enderman-teleport sound plays), up to 5 at once

New this session.

### #230 — Golden Egg — throw it (right-click): always hatches a Golden Chicken where it lands (unlike vanilla eggs' 1-in-8 chance), and damages anything it hits directly for 1

**2026-09-28: investigated alongside #205 - see that row, same conclusion (inconclusive, no code-level bug found).**

### #233 — Fluid Display — craft: 8x glass (ring) + glass bottle (center)

New this session. Fluid API blocker (noted in section 6 below previously) is resolved - built directly on this session's Ender Bucket fluid-capability work.

### #234 — Fluid Display — right-click with a filled fluid container (bucket, Ender Bucket, etc.): block takes on that fluid's texture/color, still image

**2026-09-27, real bug found and fixed (reported by user, with screenshot): the rendered cube was twisted/non-cube-shaped and both water and lava appeared almost black instead of their real colors.** Two separate bugs in `FluidDisplayTileEntityRenderer`: (1) the `-Y` (bottom) face's quad had one corner at `y=1` (the top of the cube) instead of `y=0`, folding that face diagonally through the middle of the cube instead of it lying flat - every other face's 4 corners correctly shared their own face's constant axis, this was the one exception. (2) same lighting bug as Rune Base's fix (see #161) - `GL_LIGHTING` is enabled by the dispatcher before any TESR renders, and this renderer's untextured-normal `POSITION_TEX_COLOR` vertices darkened heavily as a result; fixed with `GlStateManager.disableLighting()`/`enableLighting()` around the draw. **Also reported: a freshly-placed display with no fluid poured in yet ("blank") showed a flat pale quad instead of being fully invisible** - the code's early-return for a null/empty `FluidStack` looks correct on inspection (ground-truthed `FluidStack.loadFluidStackFromNBT` on empty NBT returns `EMPTY`, not a fallback fluid), so this might just have been the same folded-bottom-face bug viewed from an angle that made it look like a thin flat panel rather than a twisted cube - not fully explained yet. **2026-09-27, same day, user confirmed the quad shape is fixed but reported two more real bugs**: lighting still looked wrong on the faces, and the top face specifically never showed the fluid texture at all (always transparent). Both root-caused: (1) the `-Y` and `+Y` faces both had their vertex winding backwards relative to the other 4 (confirmed via the right-hand-rule normal of each face's vertex order - every side face pointed outward correctly, top/bottom both pointed inward) - with back-face culling on, a backwards face's visible side is exactly the side that gets culled, explaining why the top was always invisible (and the bottom, though less noticeable from a normal viewing angle, had the same problem). Fixed by reversing both faces' corner order. (2) `disableLighting()` alone wasn't the full lighting fix - it correctly stopped the darkening, but also meant every face rendered at the exact same flat brightness, unlike every real block in the game, which bakes a per-face-direction brightness multiplier into its vertex colors (vanilla can't use `GL_LIGHTING`/normals for this either - `BlockModelRenderer`'s own per-direction constants, ground-truthed: UP 1.0, DOWN 0.5, NORTH/SOUTH 0.8, WEST/EAST 0.6). Added the same per-face multiplier here. Please retest - both bugs should be resolved now.

**2026-09-28, real bug found and fixed: still showed as transparent (with a fluid poured in), only faintly tinted.** Same category as Rune Base's "Real bug #1" - `TileEntityRendererDispatcher` doesn't reset the global GL current-color between TESRs, and per-vertex `BufferBuilder.color(...)` only multiplies against that leftover state rather than replacing it, so the cube was drawing with whatever near-zero-alpha color was left over from the last thing rendered that frame. Fixed by adding an explicit `GlStateManager.blendFunc(...)` and `GlStateManager.color4f(1, 1, 1, 1)` reset before drawing, same fix pattern already applied in `RuneBaseTileEntityRenderer`.

**2026-09-28, same day, real bug found and fixed (reported by user, with screenshot): the empty (no fluid poured in) block was still fully invisible, not just faintly tinted.** Root cause, ground-truthed against the real 1.12.2 `ModelFluidDisplay`: the original falls back to a static `ModelCubeAll` textured with the mod's own `fluidDisplay` "empty tank" sprite when there's no fluid - this port's TESR was simply returning without drawing anything at all in that case, a straight missing feature rather than a simplification (the texture asset, `block/fluid_display`, was already sitting unused in this port's resources, referenced only as the block model's particle texture). Fixed by drawing the same cube geometry with that sprite and no tint (white) when empty, instead of skipping the draw - `FluidDisplayTileEntityRenderer` now covers both the empty and full states, matching the original's actual coverage. Please retest both the fresh-placed empty look and the with-fluid look together.

**2026-09-28, real bug found and fixed (reported by user, with screenshot): a neighboring block (e.g. sand) touching a Fluid Display with a transparent fluid in it showed a hole on the touching face instead of its own texture.** Ground-truthed `Block#shouldSideBeRendered`/`#isSolid`: a neighbor's face gets culled against this block if this block's `isSolid()` is true, which is `blocksMovement && getRenderLayer() == SOLID` - and `getRenderLayer()` defaults to `SOLID` unless overridden, completely independent of this block's own `BlockRenderType.INVISIBLE`/TESR-only rendering (an unrelated method). So the sand's face was being culled as if the Fluid Display were a fully opaque block, even though nothing opaque was ever drawn there. Vanilla's own `GlassBlock` avoids exactly this by overriding `getRenderLayer()` to `CUTOUT` - `FluidDisplayBlock` now does the same. This is a real 1.14.4 API coupling with no 1.12.2 counterpart (1.12.2's culling was keyed off `Material#isOpaque()` instead), not a disclosed simplification. Please retest with an opaque neighbor next to a fluid-filled display.

### #235 — Fluid Display — plain right-click (empty/non-fluid item): toggles between the still and flowing texture for the displayed fluid

**2026-09-27: `onDataPacket` live-sync bug fixed** (see #161's writeup) - this toggle is read straight from the client TE by the TESR, exactly the kind of thing that wouldn't have shown up without a relog before this fix. Please retest.

### #236 — Fluid Display — sneak-right-click with a non-fluid item: rotates the displayed cube 90 degrees

Disclosed simplification: physically rotates the rendered cube in world space (TESR-driven) rather than 1.12.2's UV-remap-only rotation on an otherwise-static cube - visually near-identical for this fully-opaque single-texture case. Same `onDataPacket` fix as #235 applies - please retest without relogging in between.

### #238 — Evil Tear — craft: Wither Skeleton Skull (top) + Ghast Tear (middle) + Ender Pearl (bottom)

New this session.

### #244 — Creative Player Interface — creative-menu-only (no survival crafting recipe, matching 1.12.2), otherwise behaves identically to the already-shipped Player Interface (binds to whoever places it, exposes their inventory as a capability for hoppers etc.)

New this session.

### #245 — Ender Anchor — craft: 8x obsidian (ring) + Stable Ender Pearl (center)

New this session.

### #251 — Prismarine Ender Bridge — craft: 4x Prismarine Shard + 4x Prismarine Crystals (corners/edges) + Ender Bridge (center); behaves identically but scans 10 blocks per tick instead of 1 (effectively instant over normal ranges)

New this session.

### #252 — Magic Hood — no crafting recipe (dungeon-chest-only in 1.12.2 too); creative-only for now

New this session.

### #253 — Magic Hood — wear it as a helmet: other players can no longer see your nametag above your head, whether you're sneaking or not

Needed a coremod (`MagicHoodTransformer.js`) - confirmed via `javap -c` that this Forge version has no clean event for hiding a nametag (`RenderNameplateEvent` doesn't exist until later).

### #254 — Magic Hood — wear it as a helmet: your potion-effect particle swirl no longer shows to anyone, whether you're sneaking or not

Unlike the nametag half, this one used a real existing Forge event (`PotionColorCalculationEvent`) - no coremod needed for this half. **2026-09-28: real bug found, ground-truthed via `javap -c`: `LivingEntity` only recalculates its synced particle-hidden flag (firing the event above) when a private `potionsNeedUpdate` flag is set, which only happens when an effect is added/removed/expires (or every 600 ticks for one already active) - equipping the hood while an effect was already running never touched that flag, so the stale pre-hood value could stay synced for up to 30 seconds.** No public API or clean event forces a recalculation, so added a `LivingEquipmentChangeEvent` listener that reflectively flips that one flag (the same thing vanilla's own effect-changed path does) whenever the head slot's Magic Hood state changes. Please retest.

### #255 — Magic Hood — has no durability bar and can't be damaged/broken

**2026-09-28: this port's own "indestructible, matching 1.12.2" comment was wrong** - ground-truthed via `javap -c` that `ArmorItem`'s constructor unconditionally sets max damage from the armor material (`Item#maxDamage` is `private final` with no override point at all), so this was always taking plain Chain-helmet durability, never actually indestructible. … (full writeup in git history)

### #256 — Rain Shield — craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row)

New this session.

### #257 — Rain Shield — needs solid ground underneath; breaks and drops itself if that support is removed (like a torch)

**2026-09-28:** real gap found comparing against 1.12.2's real `BlockRainShield` - the original explicitly calls `checkForDrop` from `neighborChanged`/`onBlockAdded`; overriding `isValidPosition` alone (which this port had) only blocks survival placement, it never makes an already-placed block react to its support disappearing. … (full writeup in git history)

### #258 — Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire extinguishing near it - the mechanical effects of rain/snow are suppressed

Did not prevent rain or rain particles. Needed a coremod (`RainShieldTransformer.js`, redirecting `World.isRainingAt`) - confirmed via `javap -c` that no cancel-friendly Forge weather event exists in this version. **2026-09-28: investigated, no bug found.** Re-verified the coremod bytecode by actually running it (Nashorn+ASM) against the real `World.class` - all 4 `ireturn` sites are correctly redirected through `AsmHandler#overrideIsRainingAt`, and `FarmlandBlock`/`FireBlock`/`ServerWorld` all call this exact method directly (`invokevirtual`, not through an interface default that could route elsewhere). `RainShieldTileEntity`/`AsmHandler` logic also checked out. Since "rain particles" is explicitly the already-disclosed cosmetic gap (see #261), suspect this report may have been about the visual only - please retest specifically checking mechanical effects (e.g. does farmland actually stay dry, checked by right-clicking it with a hoe or watching its texture, not just watching rain fall visually) and report back if it's still actually broken.

### #261 — Rain Shield — the falling rain/snow visual itself is NOT locally hidden near the shield (you'll still see rain appear to fall through the shielded area even though it has no mechanical effect there)

Disclosed simplification - the client-side visual suppression 1.12.2 had would need patching deep inside a ~300-line vanilla rendering method's own loop body (confirmed via `javap -c`), not a simple return-value wrap like every other Batch 7 redirect - not implemented. Not a bug if reported.

### #262 — Ancient Furnace — rarely found naturally embedded (top face flush with the ground) in cold biomes (snowy tundra, taiga, mountains, etc.) - creative-menu obtainable too, for testing without world-searching

New this session.

