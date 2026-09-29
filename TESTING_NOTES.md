# Random Things 1.14.4 Port — Testing Fix/Investigation Log


Investigation notes, root-cause writeups, and fix history that used to live in
`TESTING_CHECKLIST.md`'s Feedback column, moved here so that column stays free
for the user's own test notes. Rows are keyed to the same `#` numbering as the
checklist. Each investigated bug still gets its `Result` status (`FIXED`, `DONE`,
`NOT A BUG`, etc.) recorded in the checklist itself — only the narrative moved.

---

## 1. Priority: recently fixed bugs (please confirm these first)

### #6 — ~~Accelerator Plate — item icon~~ (removed per request)

Plate family not valuable enough to keep migrating — removed entirely.

### #7 — ~~Bouncy Plate — item icon~~ (removed per request)

See #6.

### #8 — ~~Collection Plate — item icon~~ (removed per request)

See #6.

### #9 — ~~Corrector Plate — item icon~~ (removed per request)

See #6.

### #10 — ~~Directional Accelerator Plate — item icon~~ (removed per request)

See #6.

### #11 — ~~Item Rejuvenator Plate — item icon~~ (removed per request)

See #6.

### #12 — ~~Item Sealer Plate — item icon~~ (removed per request)

See #6.

### #13 — ~~Redirector Plate — item icon~~ (removed per request)

See #6.

### #14 — ~~Redstone Plate — item icon~~ (removed per request)

See #6.

## 2. Batch 1 — standalone blocks (no GUI/tile entity)

### #29 — Lapis Lamp

**UNBLOCKED, 2026-09-26**: root cause was structural, not a code bug - `MonsterEntity`'s own registered spawn-placement predicate (light-level check) exits early and returns `false` roughly 165 bytecode instructions *before* `ForgeHooks.canEntitySpawn` ever fires the `LivingSpawnEvent.CheckSpawn` listener this used to rely on, so an ALLOW override never got a chance to run (DENY, e.g. Quartz Lamp #30, worked fine - narrowing an already-allowed spawn late is something the event genuinely can do; only widening an already-rejected one back to allowed is structurally too late). Fixed by intercepting one level higher instead: `EntitySpawnPlacementRegistry`'s own single, shared dispatch point that every registered entity's placement predicate result flows through (one call, one `ireturn`, confirmed via fresh `javap -c`) - a new coremod (`transformer/SpawnPlacementTransformer.js` + `AsmHandler#overrideSpawnResult`) rewrites that one boolean right before it returns, covering every mob type at once. The old `LivingSpawnEvent.CheckSpawn` listener for this (in `RandomThings`'s constructor) was removed entirely as redundant, not kept alongside. Verified two ways: a real `runClient` boot (this class isn't touched until a world actually starts spawning mobs, so it wasn't in that log - see #110's note for how that gap was covered) and a standalone bytecode-verification harness (loaded the real `EntitySpawnPlacementRegistry.class`, ran the actual shipped transformer JS against it via Nashorn, re-serialized, and ran `CheckClassAdapter.verify` - zero errors). Actual in-game behavior (does a Lapis Lamp really stop nearby hostile spawns now) still needs a real playtest. **2026-09-27, real bug found and fixed (reported by user): mobs were spawning even on Peaceful and getting instantly deleted.** Root cause: the coremod's unconditional force-ALLOW bypassed vanilla's placement predicate entirely, including the natural spawn cycle's own peaceful-difficulty gate - so a spawn could be attempted (and immediately despawned by vanilla's separate peaceful check) in a place the game would never have tried spawning at all otherwise. **Mechanism replaced entirely**, back to what 1.12.2 actually did (ground-truthed from the real `BlockLapisLamp`/`BlockQuartzLamp` source - neither ever used ASM for this): `LapisLampBlock` now overrides `getLightValue(BlockState)` to report 15 to the client and 0 to the server (`EffectiveSide.get()`, the 1.14.4-era equivalent of 1.12.2's `FMLCommonHandler#getEffectiveSide`), so the *real* placement predicate's own light check naturally allows spawns nearby - every other vanilla rule, peaceful included, is respected because nothing is bypassed anymore. The `AsmHandler#overrideSpawnResult` coremod override for both lamps was removed (Slime Cube ALLOW and Peace Candle DENY still use it, unrelated). Needs a fresh in-game test, including specifically confirming Peaceful no longer spawns-then-deletes mobs near one. **2026-09-27, reverted again same day (reported by user): the light-value trick made Lapis Lamp block spawns and Quartz Lamp stop blocking them - completely backwards.** Root cause, found by reading `EffectiveSide`'s own source: it returns `LogicalSide.CLIENT` for any thread that isn't part of FML's own `SidedThreadGroup`, and 1.14.4's block-light propagation for a placed block doesn't reliably run on that specific thread - so the "server" branch of the trick silently never fired where it mattered, regardless of how faithfully it matched 1.12.2's real source. **Back to the coremod force-ALLOW/DENY scan** (`AsmHandler#overrideSpawnResult`, restored), but now with an explicit `Difficulty.PEACEFUL` guard on the ALLOW branch (`world.getWorldInfo().getDifficulty() != Difficulty.PEACEFUL`) - fixes the original Peaceful bug's actual root cause (a missing check) without depending on side-detection that's proven unreliable in this Forge version. Both `LapisLampBlock`/`QuartzLampBlock` are back to plain static `Block.Properties` (Lapis: `lightValue(15)`; Quartz: none). **CONFIRMED WORKING, 2026-09-27 (user): both lamps behave correctly now** - Lapis Lamp allows spawns (and no longer spawns-then-deletes on Peaceful), Quartz Lamp blocks them.

### #30 — Quartz Lamp

Was PASS under the original coremod-based force-DENY mechanism, then broken (stopped blocking spawns) by the same-day light-value-trick detour described in #29, now reverted back to the original coremod mechanism (unchanged from what previously passed - no peaceful-related fix needed on the DENY side, forcing DENY in Peaceful is harmless since nothing would spawn there anyway). **CONFIRMED WORKING, 2026-09-27 (user).**

### #31 — Super Lubricent Ice

**2026-09-28, real bug found and fixed (reported by user): a boat resting on this block (or Super Lubricent Stone) accelerates without bound until it crashes the game, unless something physically stops it.** Root cause: the existing speed cap (`SuperLubricentPhysics.capHorizontalSpeed`) only ever ran from a `LivingUpdateEvent` listener, which exclusively fires for `LivingEntity` - a `BoatEntity` is a plain `Entity`, so it was never capped at all. Ground-truthed `BoatEntity#updateMotion`/`#getBoatGlide()`: a boat resting ON_LAND multiplies its *existing* velocity by the average slipperiness of the blocks underneath every single tick (`momentum = boatGlide; motion *= momentum`) - unlike a walking/sprinting entity's acceleration-vs-friction balance (which settles toward a steady state), this has no equilibrium at all. These blocks' slipperiness (`1F / 0.91F`, chosen to exactly cancel the fixed 0.91 friction multiplier living entities decay by) is just above 1.0, so a boat's per-tick momentum multiplier is *also* just above 1.0 - with nothing offsetting it, that compounds every tick unconditionally, even with no paddle input held, until the velocity overflows. Fixed by adding a second, `Entity`-typed listener (`TickEvent.WorldTickEvent`, since no generic "any entity ticked" Forge event exists in this version) that sweeps the world's own entity list once per tick and applies the exact same cap to every grounded non-living entity - not just boats specifically, matching the user's explicit "for all entities" request. The underfoot-block check itself was pulled out into a new shared `SuperLubricentPhysics.isOnLubricentBlock` used by both listeners. Confirmed via a real `runClient` boot that the new listener registers and the client reaches the main menu with no exceptions/crash reports - actual in-game boat behavior on Ice/Platform/Stone still needs a real playtest (headless boot alone can't exercise it).

**2026-09-28, same day, user reported the fix above didn't work at all - a player in a boat still accelerates without bound, and even just turning the boat in place runs away too.** Two real problems with the `WorldTickEvent` sweep from the first attempt: (1) it gated on `entity.onGround`, but a boat resting still (not falling) may never actually set that flag the way a walking entity does - so the cap likely never engaged in the first place; (2) even if it had engaged, it only ever clamped `Entity#getMotion()` (linear velocity) - `BoatEntity`'s turn-rate field (`deltaRotation`) is a completely separate `private` field with no public getter/setter at all, so nothing outside `BoatEntity` could have touched it regardless, which is exactly why turning alone was still unbounded. **Real fix**: `Block#getSlipperiness(BlockState, IWorldReader, BlockPos, Entity)` is a real (non-deprecated) Forge extension point for per-entity slipperiness, and it's the *exact* call `BoatEntity#getBoatGlide()` itself makes (ground-truthed from its source: `blockstate.getSlipperiness(this.world, pos, this)`) to compute both `momentum` (linear) and, from the same value, `deltaRotation`'s own per-tick multiplier. All three Super Lubricent blocks now override that method: every other entity still gets the true zero-friction value (`1F / 0.91F`), but a `BoatEntity` specifically gets capped to `0.989F` - vanilla's own `Blocks.BLUE_ICE` value, the highest slipperiness vanilla itself ever ships and the material real ice-boat speedruns already rely on as proven-stable (ground-truthed from `Blocks.java`: `ICE`/`PACKED_ICE`/`FROSTED_ICE` are `0.98F`, `BLUE_ICE` alone is `0.989F`, nothing vanilla reaches `1.0F`). This fixes motion *and* rotation at the true shared source, with no listener, no `onGround` guesswork, and no reflection needed for the private field. The now-useless `WorldTickEvent` sweep was removed entirely - ground-truthing showed no entity type other than `LivingEntity`/`BoatEntity` reads block slipperiness for its own movement at all, so there was nothing else left for a sweep to usefully protect. Build clean; `getSlipperiness`'s `@Override` compiling successfully confirms the signature match against the real API. Please retest boat behavior (both driving and turning-in-place) on all three blocks.

**2026-09-28, same day, per explicit user request:** the `0.989F`-slipperiness fix above worked, but the user asked for boats to instead match how living entities already behave on these blocks - true zero friction (speed never decays on its own) with an *external* cap on top, not a slightly-lossy slipperiness value. Reverted all three blocks back to reporting the real zero-friction slipperiness unconditionally (no per-entity override at all). Added back a boat-specific `WorldTickEvent` listener - same "no generic per-entity tick event in this Forge version" reasoning as the first attempt, but this time correctly *not* gated on `onGround` (confirmed unreliable for a resting boat in the first attempt), and calling a new `SuperLubricentPhysics.capBoatMotion` that clamps *both* things `BoatEntity#updateMotion` multiplies by slipperiness: linear speed (reusing `capHorizontalSpeed`, same 0.35 blocks/tick cap living entities get) and turn rate (`deltaRotation`, reflected via a lazily-cached `setAccessible(true)` `Field` - same established pattern as `lumien.randomthings.util.PotionMetadataUtil` elsewhere in this project, since that field has no public accessor at all). New turn-rate cap: 15 degrees/tick (hand-picked - no vanilla or community reference value exists for boat spin rate the way sprint speed has one for `MAX_HORIZONTAL_SPEED`; tune freely same as that constant). Confirmed via a real `runClient` boot that the new listener registers and the reflective field lookup doesn't fail (the field name was also re-verified against a fresh `javap -p` of the real class) - no exceptions, no new crash reports. Please retest: boat should now feel exactly like a player here - speed and turning both build up and hold at their cap rather than ever decaying on their own, and never run away unbounded.

**2026-09-28, same day, user reported still completely uncapped on both turning and moving.** Root cause: `TickEvent.WorldTickEvent` never fires for the client world *at all* in this Forge version - `BasicEventHooks#onPostWorldTick` hardcodes `LogicalSide.SERVER` regardless of which `World` is actually passed in (confirmed straight from its own source: `new TickEvent.WorldTickEvent(LogicalSide.SERVER, Phase.END, world)`, no branch on `world.isRemote` anywhere). So the listener above only ever capped the *server's* own boat instance - never what the controlling player's client independently simulates and actually renders on screen frame to frame, which is what looked completely uncapped. **This exact bug, with this exact fix, already happened once before in this same file** - `SpectreIlluminatorRelight`'s own client-side draining hit it first (see that `ClientTickEvent` listener's comment, a little further down in `RandomThings`'s constructor, for the original writeup) - missed re-applying that already-documented lesson here the first time around. Fixed by splitting into two listeners: the existing `WorldTickEvent` one stays (still useful for keeping the server's own authoritative copy sane), plus a new `ClientTickEvent` (`Phase.END`) listener that does the identical sweep against `Minecraft.getInstance().world.getAllEntities()` - same pattern `SpectreIlluminatorRelight`'s client-side drain already established for "needs to run every tick on the client specifically." Confirmed via a real `runClient` boot that both listeners register with no exceptions and no new crash reports. Please retest.

**2026-09-28, same day, per explicit user feedback: "the boat still speeds up at the smallest motion... I would like the boat to not speed up at all and instead cruise at whatever speed it's at, up to the max speed."** The external cap above was masking a real remaining problem rather than a rare edge case: `1F / 0.91F` is *not* true zero friction for a boat the way it is for a `LivingEntity`. Ground-truthed why: `LivingEntity.travel` multiplies motion by `slipperiness * 0.91F` - an extra hardcoded `0.91` baked into *that specific* formula - so slipperiness needs to be `1F / 0.91F` to cancel it to a net `1.0F`. `BoatEntity#updateMotion` has no equivalent extra constant at all - `momentum = boatGlide` (straight from `getSlipperiness()`) is used *directly* as the multiplier. So a boat on these blocks actually had momentum `~1.0989`, not `1.0` - real (if slower) exponential growth from any nonzero residual velocity, which is exactly "speeds up at the smallest motion." **Real fix**: the per-entity `getSlipperiness` override is back on all three blocks, but this time returning exactly `1.0F` for a `BoatEntity` (multiplying by `1.0F` is lossless in IEEE754 - genuine "neither grows nor decays," not an approximation), while every other entity keeps getting the block's normal zero-friction value unchanged. A boat still gains real speed exactly the way paddling normally works - `BoatEntity#controlBoat` adds a fixed velocity nudge per tick of held input, additively, completely independent of `momentum` - and the existing `capBoatMotion` external cap stays in place unmodified, now purely as the "holds forward long enough to climb past the limit" backstop rather than compensating for self-driven growth. Matches "cruise... up to the max speed" precisely. Build clean, confirmed via a real `runClient` boot with no exceptions/new crash reports. Please retest.

### #40 — ~~Accelerator Plate — function (speeds up entities)~~ (removed per request)

Plate family not valuable enough to keep migrating — removed entirely.

### #41 — ~~Bouncy Plate — function (bounces entities)~~ (removed per request)

See #40.

### #42 — ~~Collection Plate — function (pulls from neighbor inventory)~~ (removed per request)

See #40.

### #43 — ~~Corrector Plate — function~~ (removed per request)

See #40.

### #44 — ~~Directional Accelerator Plate — function~~ (removed per request)

See #40.

### #45 — ~~Item Rejuvenator Plate — function~~ (removed per request)

See #40.

### #46 — ~~Item Sealer Plate — function~~ (removed per request)

See #40.

### #47 — ~~Redirector Plate — function (redirects entity movement)~~ (removed per request)

See #40.

### #48 — ~~Redstone Plate — function~~ (removed per request)

See #40.

### #56 — Colored Grass — ships as single white-only variant (no color picker yet)

— checked the real 1.12.2 source: it was never craftable at all, only obtainable by planting a colored Grass Seeds item that isn't ported yet (16 dye-color variants, real future-slice scope, not a quick fix). Hid it from the creative menu for now since that's genuinely unreachable content otherwise. Full color/seed support still pending.

# #67.1
Cannot consume item to get a small amount of experience — genuine gap: it was registered as a completely plain vanilla `Item` with zero eat/XP behavior. Checked 1.12.2's source (it was a special case inside the old multi-sub-item `ItemIngredient` system, not its own class) - ported the real mechanic into a new dedicated `LotusBlossomItem`: 10-tick eat animation, then 3-12 XP on finish, split across orbs the same way vanilla's own XP items do. Please retest.

### #68 — ~~Sakanade — shearable, hangs under giant mushroom caps~~ (removed per request)

Not valuable enough to keep migrating — removed entirely (block, spores item, all registrations/assets/lang).

### #70 — Blazing Fire — catches flammable blocks (wood/wool) more eagerly than vanilla (coremod-only tuning — if it behaves identical to vanilla fire, the coremod likely isn't applying)

**UNBLOCKED, 2026-09-26**: the Mixin this depended on could never fully bootstrap from a mods-folder jar on this Forge version (see the old note this replaced, preserved in the plan file's Batch 7 history). Re-implemented the exact same three ground-truthed bytecode redirects as a Forge coremod instead (`transformer/FireBlockTransformer.js` + `lumien.randomthings.asm.AsmHandler`) - coremods don't have Mixin's launch-plugin-registration problem. Confirmed via `runClient` that the coremod loads, and that `FireBlock` itself still loads/verifies/links cleanly after the bytecode transform (no `VerifyError`/`LinkageError`) - actual in-game fire-spread-speed behavior still needs a real playtest.

## 3. Batch 2 — tile-entity/GUI-backed blocks

### #74 — ~~Igniter — Keep Ignited mode~~ (removed per request)

— removed the mode entirely (now just Toggle/Ignite), plus the recurring self-reschedule tick loop that only existed to serve it.

### #75 — Online Detector — emits redstone while the configured username is online

**2026-09-28, real bug found and fixed: hitting the inventory keybind while the username field was focused still closed the GUI, and typed text wasn't being saved.** Ground-truthed `INestedGuiEventHandler`/`Widget`/`TextFieldWidget`: `Screen#setFocusedDefault` only sets the *screen's* own input-routing pointer (`getFocused()`, used to route `keyPressed`/`charTyped` events to the right widget) - it does not touch the widget's own separate internal `isFocused()` boolean (only ever set by `TextFieldWidget#mouseClicked`, i.e. an actual mouse click into the field). `TextFieldWidget#keyPressed`/`#charTyped` both refuse to do anything unless their own `isFocused()` is true, and this screen's own `keyPressed` override gates its "route to the field instead of closing" logic on that exact same flag - so typing immediately after opening the GUI, without first clicking into the field, fell straight through every guard and hit `ContainerScreen`'s default "unhandled key = close on the inventory keybind" check. This also explains "doesn't save unless you hit Enter": the field never actually received any typed characters to save in the first place. Fixed by calling `usernameField.setFocused2(true)` right after `setFocusedDefault` in `init()`, so the field is genuinely focused (not just screen-routed) the instant the GUI opens. Please retest by opening the GUI and typing a username immediately, without clicking into the field first.

### #76 — Item Collector — vacuums nearby dropped items into attached inventory

— root cause found for the vacuum half: the port called `getCapability(ITEM_HANDLER_CAPABILITY, facing.getOpposite())` to fetch the target inventory's handler, but ground-truthing 1.12.2's `TileEntityItemCollector.update()` shows it fetches with plain `facing` (only the existence check used `.getOpposite()`) — most inventories ignore the side and would still work, but ones that filter by side (hoppers, some modded chests) would silently return no handler, matching "did not see items collected". Fixed in `ItemCollectorTileEntity` and the same bug in `AdvancedItemCollectorTileEntity`. The texture half is NOT explained by anything in the checked-in assets: blockstate/model/texture for `item_collector` are byte-identical to the original 1.12.2 files and wired the same way (`block/block` parent, matching UVs), so a missing-texture render implies either a stale/cached build at the time of testing or something not reproducible from static source review. Please retest after a completely fresh `./gradlew build`/`runClient` (not just a hot-reload) and report back if the checkered texture persists.

### #78 — ~~Extraction Plate — pulls items from target inventory, ejects toward cycled output side~~ (removed per request)

Plate family not valuable enough to keep migrating — removed entirely.

### #79 — ~~Processing Plate — extraction+eject timer, plus touch-triggered insert of colliding items~~ (removed per request)

See #78.

### #86 — ~~Iron Dropper — redstone mode: Repeat~~ (removed per request)

— removed (now just Pulse/Repeat Powered).

### #88 — ~~Custom Workbench~~ (removed per request)

— block, item, and all its resources fully removed.

### #93 — Chat Detector — GUI text field, pulses on placer typing the exact configured message

**2026-09-28, real bug found and fixed (same as #75's writeup - same missing `setFocused2(true)` call, same fix): typing the chat message immediately after opening the GUI, without clicking into the field first, wasn't being captured.** Applied the identical fix here (`messageField.setFocused2(true)` right after `setFocusedDefault` in `init()`).

**The "buttons in the GUI are not clickable" part is still unresolved.** Traced the full click chain against the real 1.14.4 API end to end - `Widget#mouseClicked`'s bounds check (`x/y/width/height`, no overlap with the message field, which sits well above it), `active`/`visible` (both default `true`, never overridden), `isValidClickButton` (left-click only, matches), `AbstractButton#onClick` → `Button#onPress` → the registered `IPressable` lambda → `ChatDetectorContainer#handle(1, ...)` (server-side toggle) → `detectAndSendChanges()` syncing `consume` back to the client for the label redraw - every link in that chain reads correct against the real source, and none of this project's coremods touch anything in the `Screen`/`Widget`/`Button` classes. No bug found by static review. One likely explanation worth ruling out first: 1.12.2's original GUI used a small 20x20 icon toggle button in the *top-right corner* of the panel (`guiLeft+112, guiTop+5`, drawn from the same background texture, matching the little colored-arrow icons visible in `chat_detector.png`) - this port replaced it with a full-width plain-text button *below* the message field instead (`guiLeft+8, guiTop+44`, 160x20, matching this project's established "text buttons over icon sprites" convention). If you were clicking where the icon used to be in 1.12.2 rather than the new button below the field, that would look exactly like "not clickable." Please retest clicking the large gray text button directly under the message field, and if it's still unresponsive there, let me know whether there's no reaction at all (no click sound either) or whether something happens but the label text doesn't update.

### #100 — ~~Special Chest (Nature) — placed block renders custom texture + lid animation~~ (removed per request)

Not valuable enough to keep migrating — removed entirely, including the ocean-monument worldgen coremod added this same session.

### #101 — ~~Special Chest (Nature) — item icon renders as a 3D chest, not a flat icon~~ (removed per request)

See #100.

### #102 — ~~Special Chest (Water) — placed block renders custom texture + lid animation~~ (removed per request)

See #100.

### #103 — ~~Special Chest (Water) — item icon renders as a 3D chest, not a flat icon~~ (removed per request)

See #100.

### #109 — ~~Filtered Redirector Plate — redirects left/right per-entity based on the two filter slots (second slot wins if both match)~~ (removed per request)

Plate family not valuable enough to keep migrating — removed entirely.

### #117 — ~~Global Chat Detector — server operators bypass the whitelist check~~ (removed per request)

— removed; `checkMessage` no longer grants ops an automatic pass, only a matching ID card does.

## 5. Batch 3 — remaining (non-block) items

### #127 — Lava Charm — same as Obsidian Skull, but specifically no-sells lava damage (spends internal charge, needs a moment to "recharge" between saves)

— found a real bug: unlike Lava Wader (which regenerates charge via `onArmorTick` every tick while worn), Lava Charm had no charge-regeneration logic at all since it's a plain carried item, not armor — its charge NBT tag never even existed, so the protection check always failed. Added the same regeneration via `inventoryTick` instead. Please retest.

### #128 — Lava Wader — worn as boots: fully immune to lava damage as long as its charge isn't depleted (charge drains on each save, regenerates over ~2 seconds after)

Same root cause/fix as #130 (jittery liquid-surface detection) - see that row.

### #130 — Water Walking Boots — worn as boots: walking on water should be automatic/passive; sneaking should let you sink through instead

— this port's water-walking is a plain per-tick Forge event listener, not ASM, so that wasn't actually the cause. Found two real bugs in it: (1) it checked the block at an exact `floor(posY)` snapshot of the player's feet, but once resting right at the liquid surface the feet Y sits exactly on the block boundary, and floating-point noise from the *previous* tick's own upward nudge was enough to flip that floor() between the liquid block and the air block above it from tick to tick - the "am I on liquid" check kept flickering true/false, which is the jitter. Now checks slightly below the feet position instead, which doesn't flicker at the boundary. (2) it only ever nudged position upward and never touched the player's own vertical velocity, so normal per-tick gravity kept accumulating downward speed underneath the nudge and fighting it every tick - now zeroes downward velocity before nudging. Untested on my end (no way to run the game) - please retest walking onto and across open water.

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

**2026-09-27, real crash found and fixed (reported by user): game crashed on use.** Root cause: `TimeAcceleratorEntity.target` was a plain field, never synced to the client. Fixed with a real synced `DataParameter<BlockPos>`. **User confirmed working**, then asked for a visible billboard (added via `SpriteRenderer`, same as Spectre Illuminator) → reported it clipped into solid blocks (fixed by offsetting toward the clicked face) → then asked for icons on every face instead of just one, closer to the surface, and particles spread across the whole block. Final design: marker is back at the block center; a new custom `TimeAcceleratorEntityRenderer` (replacing `SpriteRenderer`) draws the icon as a billboard on all 6 faces close to each surface (the per-icon transform sequence is copied from `SpriteRenderer`'s own `doRender`, ground-truthed via `javap -c`); particles now spread across the full block volume instead of a tight radius. The "already exists here" detection matches by `getTarget()` rather than position, so it never needed to change again through any of this. Not yet re-tested.

### #201 — Time in a Bottle — right-click an existing Time Accelerator: doubles its rate (up to 32x) if enough time is stored, each doubling extends its remaining duration and plays a rising note-block chime

Unblocked - #200 confirmed working. **2026-09-27, per user request:** both this and #200's "not enough stored time" cases now send the player an action-bar-adjacent status message (`item.randomthings.time_in_a_bottle.not_enough_time`, red text) instead of silently doing nothing - matches this port's existing `sendStatusMessage` convention (`EnderMailboxBlock`/`EnderLetterItem`), not something 1.12.2 itself did. Not yet tested.

### #205 — Weather Egg cloud — visibly different particle effects per type while rising (plus a colored accent for Rain/Storm)

Disclosed simplification: the original's custom-tinted smoke particle class is replaced with the same tintable dust particle already used by Potion Vaporizer/Floo Network; the Sun variant's extra sunburst overlay render is dropped - see `WeatherCloudEntity`'s javadoc. **2026-09-28: investigated, inconclusive.** `ThrownWeatherEggEntity` is a near-verbatim `ProjectileItemEntity` (vanilla's own base for `EggEntity`), registered with `SpriteRenderer` the exact same way as the confirmed-working Spectre Illuminator, and its `IRendersAsItem#getItem()` is entirely vanilla machinery - found no code-level difference from vanilla's own (visibly working) thrown egg. Same symptom on #230 (Golden Egg, same base class). Possibly a client-side data-sync timing issue specific to fast, short-lived projectiles, not confirmed without live testing.

### #206 — Ender Mailbox — craft: 2x ender pearl + hopper (top row) + 3x iron ingot (middle row) + oak fence (bottom center)

New this session (Ender Letter feature).

### #208 — Ender Letter — right-click (fresh, unaddressed) opens a 9-slot "write a letter" GUI with a receiver-name text field; put items in, type a player's name

**2026-09-28: real race condition found** - the field's contents were only ever sent to the server from `Screen#removed()` (i.e. only when the GUI closes), racing the vanilla close-window packet sent over the same connection; the server could already have swapped `player.openContainer` back to the plain inventory container by the time this custom signal arrived, silently dropping it regardless of Enter. Fixed by syncing on every text change instead (in `tick()`), so the server has the latest value long before the GUI ever starts closing. Please retest.

### #218 — Ender Bucket — craft: 2x iron ingot (top corners) + ender pearl (center)

New this session. This port's first feature touching Forge's fluid capability API.

### #219 — Ender Bucket — right-click while looking near (not necessarily exactly at) a body of water/lava: picks up 1000mB even if the exact block you're pointed at is a flowing edge, not a source - it searches the connected body for a valid source

**2026-09-28: real bug found, ground-truthed via `javap -c`: this Forge build's own `FluidUtil.tryPickUpFluid`/`getFluidHandler(World, BlockPos, Direction)` only ever resolve a tile-entity-backed fluid capability at the target position - no branch at all for vanilla water/lava (`IBucketPickupHandler`) or a TE-less modded fluid block (`IFluidBlock`), so pickup always failed regardless of what was targeted.** Rewrote pickup to drain directly against each interface's own self-contained method (`IFluidBlock#drain`/`IBucketPickupHandler#pickupFluid`) instead of going through the broken Forge helper. Please retest.

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

**2026-09-28: this port's own "indestructible, matching 1.12.2" comment was wrong** - ground-truthed via `javap -c` that `ArmorItem`'s constructor unconditionally sets max damage from the armor material (`Item#maxDamage` is `private final` with no override point at all), so this was always taking plain Chain-helmet durability, never actually indestructible. Since swapping to `ArmorMaterial.LEATHER` outright would also change defense/toughness/enchantability/repair-item, added a small delegating `IArmorMaterial` that copies every other stat from Chain and only overrides durability with Leather's value. Please retest.

### #256 — Rain Shield — craft: Flint (top) + Blaze Rod (middle) + 3x Netherrack (bottom row)

New this session.

### #257 — Rain Shield — needs solid ground underneath; breaks and drops itself if that support is removed (like a torch)

**2026-09-28:** real gap found comparing against 1.12.2's real `BlockRainShield` - the original explicitly calls `checkForDrop` from `neighborChanged`/`onBlockAdded`; overriding `isValidPosition` alone (which this port had) only blocks survival placement, it never makes an already-placed block react to its support disappearing. Fixed by having `neighborChanged` check `isValidPosition` itself and drop the block, matching Peace Candle's already-correct version of the same pattern. Please retest.

### #258 — Rain Shield — unpowered: within an 80-block horizontal radius (ignoring height entirely), no rain-wetting, no snow/ice formation, no fire extinguishing near it - the mechanical effects of rain/snow are suppressed

Did not prevent rain or rain particles. Needed a coremod (`RainShieldTransformer.js`, redirecting `World.isRainingAt`) - confirmed via `javap -c` that no cancel-friendly Forge weather event exists in this version. **2026-09-28: investigated, no bug found.** Re-verified the coremod bytecode by actually running it (Nashorn+ASM) against the real `World.class` - all 4 `ireturn` sites are correctly redirected through `AsmHandler#overrideIsRainingAt`, and `FarmlandBlock`/`FireBlock`/`ServerWorld` all call this exact method directly (`invokevirtual`, not through an interface default that could route elsewhere). `RainShieldTileEntity`/`AsmHandler` logic also checked out. Since "rain particles" is explicitly the already-disclosed cosmetic gap (see #261), suspect this report may have been about the visual only - please retest specifically checking mechanical effects (e.g. does farmland actually stay dry, checked by right-clicking it with a hoe or watching its texture, not just watching rain fall visually) and report back if it's still actually broken.

### #261 — Rain Shield — the falling rain/snow visual itself is NOT locally hidden near the shield (you'll still see rain appear to fall through the shielded area even though it has no mechanical effect there)

Disclosed simplification - the client-side visual suppression 1.12.2 had would need patching deep inside a ~300-line vanilla rendering method's own loop body (confirmed via `javap -c`), not a simple return-value wrap like every other Batch 7 redirect - not implemented. Not a bug if reported.

### #262 — Ancient Furnace — rarely found naturally embedded (top face flush with the ground) in cold biomes (snowy tundra, taiga, mountains, etc.) - creative-menu obtainable too, for testing without world-searching

New this session.

### #265 — Ancient Furnace — after a multi-minute heat-up, it melts surface snow (and ice, to water) in a large radius around itself, reassigns that area's biome to its warmer counterpart (e.g. Snowy Tundra -> Plains), then explodes and is destroyed

**2026-09-27: now does real biome reassignment**, not just the visible melt - a previous session's claim that this was impossible in 1.14.4 turned out to be wrong (found a legitimate public-API way, see `AncientFurnaceTileEntity`'s javadoc for the full reasoning and exact biome mapping). Snow/ice melting is now gated on that same per-column biome match, matching 1.12.2's own logic, rather than melting unconditionally everywhere in radius. To actually verify: check spawn behavior changes (e.g. no more Polar Bears/Strays in the converted area) and, ideally, F3's biome readout - the visual grass/fog color may not update for an already-standing player without leaving and rejoining, that part is disclosed as not yet handled. Heating duration/explosion strength are hand-picked defaults, not verified against 1.12.2.

### #268 — Peace Candle — standalone decorative candle, creative-menu obtainable; needs solid ground underneath (auto-breaks and drops otherwise, like a torch)

New this session. No natural village generation yet - see feedback.

### #269 — Peace Candle — no hostile mobs naturally spawn within a 3-chunk radius of one (test by placing one down and waiting/forcing a spawn attempt nearby)

Reuses the existing `SpawnPlacementTransformer` coremod hook (shared with Lapis Lamp/Slime Cube), not a new one.

### #270 — Peace Candle — does NOT yet have a ~33% chance to generate inside a village church, unlike 1.12.2

Disclosed simplification, not a bug if reported - see `WIKI_FEATURE_STATUS.md`'s Peace Candle note: 1.14.4's fully data-driven village generator has no hardcoded piece classes left to hook a custom building into the way 1.12.2's ASM patch did, and hand-authoring the NBT structure data for a whole new building isn't feasible without in-game structure-block tooling this environment doesn't have.

### #271 — ~~Special Chest (Water variant) — naturally generates in an ocean monument's treasure/core room~~ (removed per request)

Added and removed the same session — Special Chest not valuable enough to keep migrating, including this ocean-monument coremod.

### #272 — Spectre Illuminator — right-click a block to place it; a visible item-icon billboard drifts from the clicked spot to hover above the tallest block in its chunk, then centers itself horizontally over the chunk

New this session. Needs a new `IBlockReaderTransformer` coremod (see `WIKI_FEATURE_STATUS.md`'s note). The visible billboard was added 2026-09-27 per explicit user request (it originally had no model at all, matching 1.12.2's own renderer) - reuses vanilla `SpriteRenderer` via `IRendersAsItem`, same mechanism as Thrown Golden Egg/Weather Egg.

### #273 — Spectre Illuminator — once centered, its whole chunk (and only that chunk) lights up to full block-light brightness, and a soft particle trail plays around it

**2026-09-27, three real bugs found and fixed in sequence, all reported by the user (sync, crash, client-drain event) - see the plan file for the full blow-by-blow.** User confirmed: chunk lights up correctly, no crash, no relog needed. Follow-up perf tuning, same day: briefly split into separate server/client drain rates, then unified into one shared `PER_TICK` constant per user request for consistency (48 → 256, two more requests). Not yet re-tested for smoothness at 256; it's a single easily-tunable constant, still ~325x below the crash threshold from the earlier bug regardless of value.

**2026-09-28, real bug found and fixed (reported by user, with server log): a world with many illuminators stalls the whole server for multiple seconds right when a batch of them settle/load together** (the user's log showed 14 illuminators relighting within under a second on world join, `Can't keep up! ... 44 ticks behind`, and a relight backlog past 2 million positions). Root cause: the per-tick *draining* (256/tick) was never the problem - it's cheap and was working exactly as designed. The actual stall was in `SpectreIlluminatorRelight.queue()` itself, called once per illuminator: it built the *entire* padded column (up to ~185,000 positions per the log, `(16+2×15)² × height`) as real, individually-allocated `BlockPos` objects in one synchronous loop before any draining even started. 14 illuminators settling together is therefore ~2 million object allocations dumped into one or two ticks - that allocation/GC burst is what actually blocked the server thread, not the bounded drain. Fixed by replacing the materialized position queue with a queue of lightweight bounds-descriptor "jobs" (six ints each) - `queue()` now only computes and stores those bounds (O(1), no loop), and `tick()` lazily generates each tick's `BlockPos` values on demand from whichever job is at the front, saving its cursor for next tick. Total positions checked, their order, and the per-tick drain rate are all byte-for-byte unchanged - only *when* each position gets allocated moved, from "all up front" to "the tick it's actually used." The backlog itself (queued lighting taking a while to visibly catch up with many illuminators placed at once) is expected and by design, not a bug on its own - it's the same total work as before, just spread out instead of dumped in one place; only the burst-allocation stall is what got fixed here. Please retest with the same many-illuminator setup and watch for `Can't keep up!` warnings specifically (the queue draining gradually over the following seconds is fine and expected).

### #274 — Spectre Illuminator — right-click the placed orb to collect it back as an item; the chunk goes dark again

**2026-09-27:** initial gap (pickup never re-triggered a relight sweep) fixed alongside #273. **User then reported a fourth real bug**: after pickup, most of the chunk went dark but the edge where the orb first settled and started forcing light stayed stuck lit. Root cause: the relight sweep's padding around the chunk boundary was only 1 block, but vanilla block light naturally propagates up to 15 blocks past a boundary once the neighboring chunk is uniformly flooded to level 14 (confirmed via the light engine's own neighbor-cascade behavior, ground-truthed earlier via `javap -c`) - anything beyond that 1-block pad kept relying on a stale bright value nothing ever told it to re-derive. Padding widened from 1 to 15 blocks (matching vanilla's own max light falloff distance) in `SpectreIlluminatorRelight`; drain rate also bumped 256→512/tick to keep the now-much-larger sweep (~7x more positions) draining in a comparable time, still far below the ~83,000-in-one-call threshold that caused the earlier crash. Not yet re-tested.

### #275 — Portkey — craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center)

New this session.

### #277 — Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the ground it stops despawning and stops glowing (primed); picking it back up before that resets the countdown next time it's dropped

Disclosed simplification: default vanilla glow color, not 1.12.2's custom magenta tint (would need a new coremod for one cosmetic detail) - see `PortkeyItem`'s javadoc. **2026-09-28: investigated, matches 1.12.2's own design exactly** - ground-truthed the real `ItemPortKey.hasEffect`: an *unbound* Portkey (no target set) glows forever regardless of its priming timer, by design; only a *bound* one stops glowing once primed. If this was tested unbound, the current behavior is correct as ported - please retest with a *bound* Portkey and confirm whether it still fails to stop glowing, or let me know if you'd like the always-glow-when-unbound behavior changed from the original.

### #278 — Portkey — once primed and bound, the next player to pick it up (not the one who dropped it, necessarily - anyone) gets teleported to the bound location instead of collecting it, landing on a safe nearby surface; picking up a not-yet-primed or unbound Portkey just collects it normally

Same safe-landing-spot search as 1.12.2 (5x5 column around the target, down to 10 blocks below it, first solid-topped spot with 2 air blocks above). Disclosed simplification: 1.12.2's full-screen HUD directional beam is dropped - see `PortkeyItem`'s javadoc for why. (The "camo" disguise system was also initially dropped here but re-implemented same-day - see #279-280.)

### #279 — Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - inventory, held, dropped on the ground, item frame), the donor item is returned unconsumed

New this session, added after initial research into whether a clean 1.14.4 path existed (it did) - see `PortkeyItemRenderer`'s javadoc. Uses a real `Item.Properties#setTEISR` custom renderer, the same mechanism vanilla uses for shulker boxes - no reflection, unlike 1.12.2's own implementation. **2026-09-28: real bug found - `portkey.json` (parented to `builtin/entity`) had no `display` transforms block at all.** Ground-truthed via `javap -c` that `ItemStackTileEntityRenderer#renderByItem` takes no `TransformType` in this version - all GUI/ground/hand/fixed scaling for a TESR-backed item comes from its OWN model's `display` block, applied by the caller *before* `renderByItem` runs (confirmed against vanilla's real `item/template_shulker_box.json`, the reference for every other `builtin/entity` item). With no `display` block, every context got an identity transform. Fixed by giving `portkey.json` the same transform numbers as the shulker box template. Please retest.

### #280 — Portkey — an un-camouflaged Portkey still shows its own plain icon correctly (in every context)

Regression check for #279's fix: rendering routes through a custom `ItemStackTileEntityRenderer` now instead of a flat icon model, so this is worth confirming didn't break. A real bug was already caught and fixed here via a `runClient` smoke test before this ever reached in-game testing (a doubled `item/item/` model path) - this row is about anything *that* test couldn't catch. **2026-09-28: same root cause and fix as #279** - the missing `display` transform block affected both the camo and plain-icon render paths identically. Please retest.

### #281 — Spectre Anchor — craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 Ectoplasm (center)

New this session.

### #282 — Spectre Anchor — combine it with any other single non-stackable item in a crafting table: consumes both, result is a copy of that item tagged "Anchored" (dark aqua tooltip line right under its name); dying with an Anchored item in your main inventory keeps it in your inventory on respawn instead of dropping it (with `keepInventory` off) - stacked/stackable items can't be anchored, matching 1.12.2

New this session. Needs a new `PlayerEntityTransformer` coremod (see `WIKI_FEATURE_STATUS.md`'s note) - `PlayerDropsEvent` no longer exists in this Forge version and no other event covers the death-drop code path, confirmed via `javap -c` before concluding a coremod was needed. Baubles doesn't exist in this port, so unlike 1.12.2 this only covers the main inventory (1.12.2's Baubles-slot handling is N/A here). Verified via the bytecode-verification harness (against a full Gradle-resolved runtime classpath) and a `runClient` boot smoke test; not yet tested in a real play session (needs an actual death to confirm the respawn carryover and the drop-skip).

### #283 — Basic Redstone Interface — craft: iron ingots at the 4 corners, redstone dust on the 4 edges, a Stable Ender Pearl in the center; right-click with a Redstone Tool to start linking, then right-click any block to bind it as the target (matches the Redstone Observer's own linking flow); plain right-click (without the tool) opens a read-only status GUI showing the bound coordinates or "No Target"

**2026-09-28: real bug found** - the block itself was registered but `ModItems.java` never called `registerItemForBlock` for it (or Advanced Redstone Interface), the only two blocks in the whole registration list missing that call, so neither had a `BlockItem` at all - no way to obtain, craft, or see either in creative regardless of the recipe/assets (which were already correct). Fixed by adding the missing registration call. Please retest - this should also unblock #285/#287.

### #284 — Basic/Advanced Redstone Interface — whatever redstone signal (weak or strong) is currently hitting the interface block itself gets mirrored onto its target position(s), as if a real redstone source were sitting there - e.g. redstone wire leading away from the target lights up, a comparator reading it responds, etc.; power updates live as the interface's own input changes, and stops immediately if the interface block is broken

The core mechanic, needs the two new coremods (see `WIKI_FEATURE_STATUS.md`'s Redstone Interface note) - `World.getRedstonePower`/`IWorldReader.getStrongPower` are patched to check a live per-TE registry (`RedstoneInterfaceTileEntity`) before falling back to vanilla's own computed value. Both coremods verified via the bytecode-verification harness (full Gradle-resolved classpath) with a clean pass on the first attempt.

### #285 — Advanced Redstone Interface — craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-click opens a GUI with 9 Position Filter slots instead of a single tool-bound target - drop up to 9 Position Filter items in to broadcast to all 9 positions simultaneously; removing one stops broadcasting to that position (matches the Basic interface's live power mirroring, just fanned out to multiple targets)

New this session. Matches 1.12.2's own inventory-of-Position-Filters design; item-type restriction (Position Filter only) enforced on the slot's backing `ItemStackHandler` rather than a dedicated slot class.

### #286 — Redstone Activator — right-click empty air to cycle its duration (2/20/100 ticks, shown both in the tooltip and as a texture swap on the item itself; shift-right-click cycles backwards); right-click any block to broadcast a strength-15 pulse there for the currently-selected duration, then it fades back to no signal on its own

New this session.

### #287 — Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom; sneak-right-click opens an "edit" GUI (9 Position Filter slots, same as the Advanced Interface, plus your normal inventory) to bind up to 9 targets; plain right-click opens a compact "use" GUI with one button per bound target, each firing a 20-tick strength-15 pulse at its position when pressed

New this session. Disclosed simplification: 1.12.2's edit GUI had a second row of purely cosmetic ghost/camo-icon slots (letting you override each button's icon with an arbitrary item, no functional effect either way) - dropped here, and the "use" screen shows plain text buttons (each button's own display name, truncated) rather than item icons, matching this port's existing `EntityDetectorScreen` convention of text buttons over icon sprites. **2026-09-28: investigated, no bug found** - `RedstoneRemoteEditContainer` adds all 9 `SlotItemHandler`s at the same coordinates/texture as `AdvancedRedstoneInterfaceContainer`, container-type/screen-factory registrations match, and the shared `advanced.png` texture does have visible slot-square art drawn at those coordinates. Note Advanced Redstone Interface's own GUI (#285) was itself never actually tested before now (blocked by #283's missing item) - possible this was a symptom of that block never having existed to compare against, or something not reproducible from static source review. Please retest on a fresh build now that #283 is fixed.

**2026-09-28 (later, same day): real bug actually found, from a user screenshot of the "use" screen looking like a blank white box.** `RedstoneRemoteUseScreen` never overrode `drawGuiContainerForegroundLayer` at all - `ContainerScreen`'s own default is a no-op, unlike every other ported screen in this project (standard convention: draw `this.title` via `this.font.drawString`), and unlike 1.12.2's own `GuiRedstoneRemoteUse`, which explicitly drew `"item.redstoneRemote.name"` there. With no title and a plain, undecorated background (see below), the screen had nothing to visually anchor it - plausibly also what the original "no slots visible" report above was actually seeing on the *edit* screen, which had the exact same gap. `AdvancedRedstoneInterfaceScreen` and `RedstoneRemoteEditScreen` had it too (all three ship from the same batch) - fixed all three the same way. **Separately confirmed NOT a bug:** the "use" screen's plain flat-gray background texture (no border/frame decoration) looked broken but is byte-for-byte what it should look like - extracted 1.12.2's own `redstoneremoteuse.png` directly from the `1.12.2` branch and it's the same plain gray panel; the original mod genuinely shipped an undecorated placeholder-looking texture for this one screen. `./gradlew build -x test` clean. Please retest.

### #288 — Magnetic Enchantment — appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mining a block with it puts drops straight in your inventory instead of on the ground

Ported via a plain `BlockEvent.HarvestDropsEvent` listener instead of 1.12.2's ASM hook (canceling the event clears its drop list before anything spawns) - no coremod needed, unlike the wiki's own "ASM?" note. Not build-verified in this sandbox (no Forge/Mojang Maven access); flag for retest.

### #289 — Block Breaker — craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way facing toward whichever way you were looking

Recipe/model/blockstate ported directly from 1.12.2's real assets (no new art needed).

### #290 — Block Breaker — left unpowered, continuously mines whatever's directly in front of it at real survival speed (respects the target block's actual hardness/tool-appropriateness via a `FakePlayer` holding an unbreakable Magnetic pickaxe), showing a real crack overlay while it works

**2026-09-28: real crash found** - server crashed with a `NullPointerException` in `ForgeHooks.onBlockBreakEvent` the instant it finished mining any block with no tile entity (e.g. grass). `FakePlayer` never gets a real `connection` assigned, and that method unconditionally calls `entityPlayer.connection.sendPacket(...)` on that code path. 1.12.2's original had a workaround for exactly this (a no-op `NetHandlerPlayServer` assigned in `initFakePlayer()`) that this port had dropped; ported the same fix, ground-truthed against `ServerPlayNetHandler`/`NetworkManager`/`PacketDirection` via `javap`. Drops land in the fake player's inventory via the Magnetic listener above, then get pushed into whatever inventory sits behind the breaker (or dropped in front if there's none/it's full), matching 1.12.2 exactly. Please retest.

### #292 — Block Destabilizer — craft: obsidian at the 4 corners, sand left/right of a diamond in the middle row, redstone above/below the diamond (ring pattern)

Recipe ported directly from 1.12.2 (sand metadata dropped, 1.14.4 has no red-sand-via-damage-value).

### #293 — Block Destabilizer — a redstone pulse flood-fills every block connected to the one directly in front of it that matches the same exact `BlockState` (not just the same block with different properties), up to 50 blocks, then drops the whole matched structure as falling-block entities top row first, nearest-to-the-machine first within each row

Spawns vanilla's own `net.minecraft.entity.item.FallingBlockEntity` directly (`shouldDropItem = false`) rather than porting 1.12.2's parallel `EntityFallingBlockSpecial` copy-of-vanilla class - see `BlockDestabilizerTileEntity`'s javadoc for why that's a clean equivalent, not a simplification.

### #294 — Block Destabilizer — GUI: Fuzzy toggle button makes the match ignore block-state variants and match by `Block` type only (e.g. matches every log rotation, every stained-glass color)

Disclosed simplification: 1.12.2's GUI used two custom image-toggle buttons; this port uses plain text buttons (`Lazy: On/Off`/`Fuzzy: On/Off`) matching `IgniterScreen`'s already-proven button style, at the original's compact 85x35 panel size (its real 1.12.2 background texture, reused as-is).

### #295 — Block Destabilizer — GUI: Lazy toggle remembers which connected blocks *didn't* match last time, so re-triggering doesn't re-scan the whole structure from scratch; Reset clears that memory

Lazy/Reset are disabled (no-op) while a search/drop cycle is already in progress, matching 1.12.2.

### #297 — Spectre Key — craft: Spectre Ingot at the 3 corners (top-left, middle-left, bottom-right), Stable Ender Pearl center-left; Spectre Ingot itself crafts from Lapis Lazuli + Gold Ingot + Ectoplasm (or the 9x bulk recipe: Lapis Block + Gold Block + 8x Ectoplasm); Ectoplasm has a 1/55 chance per random tick to drop from Spectre Leaves

New custom dimension (`randomthings:spectre`), registered via `DimensionManager.registerDimension` - auto-assigned ID, no more manual config ID the way 1.12.2's `Internals.SPECTRE_ID` needed.

### #298 — Spectre Key — hold right-click for 5 seconds (glowing cyan particle charge-up) to teleport into your own private Spectre Cube room; doing the same while already inside teleports you back to exactly where you left, in your original dimension

Real API gap found and worked around: this Forge build (28.2.26) has no `ITeleporter` hook at all (a later Forge addition) - `ServerWorld.getDefaultTeleporter()` always returns one plain `Teleporter` with no override point, and `PlayerList` no longer has 1.12.2's `transferPlayerToDimension(player, dim, customTeleporter)` overload either. Without a patch, generic dimension travel would search for/build a real nether portal at the destination - a new `TeleporterTransformer` coremod (`AsmHandler#overrideMakePortal`) intercepts `Teleporter.makePortal`'s method entry and skips it entirely whenever the destination world is the Spectre dimension, in either travel direction. Bytecode-verified via the standalone `CheckClassAdapter.verify` harness (clean) and confirmed via a real `runClient` boot that `Teleporter` actually gets transformed with no `VerifyError`/`LinkageError` when a world loads.

### #299 — Spectre Cube room — a 16x16 room made of unbreakable/explosion-immune Spectre Block, with a 2x2 unbreakable Spectre Core pedestal at the center; each new Spectre Key user gets their own room, laid out in a row (16 blocks apart) in the shared void dimension

Real bug found in 1.12.2's own `SpectreHandler#getSpectreCubeFromPos` while porting it (not a "looks wrong but intentional" case - traced actual numbers): a chunk-coordinate-based key was compared against a block-coordinate cube offset, off by a factor of 16, so the original could only ever correctly resolve the very *first* cube ever created - every player's second-or-later cube would fail to match, making the anti-trespass check constantly bounce them back to their own spawn tile even while standing in their own room. This port uses the evidently-intended direct block-coordinate comparison instead - see `SpectreHandler#getSpectreCubeFromPos`'s javadoc.

### #301 — Spectre Dimension — always dark (no skylight), flat gray-cyan fog, no mob spawns (void biome, empty chunk generator), can't be slept in/respawn-set, wandering outside your own cube (not creative) snaps you back to your own spawn tile every tick, or fully back home if you don't have a cube yet; the rest of the dimension outside your built cube is pure air/void, no stray structures generate

Disclosed simplification: reuses vanilla's own `Biomes.THE_VOID` instead of porting a new custom Biome class (matches this port's existing precedent of not introducing new Biome subclasses - Ancient Furnace's worldgen work reassigns existing biomes rather than adding one). The original's spawn-point customization via a since-dropped `ItemPositionFilter` item is also out of scope - the room always spawns at its center, the same fallback 1.12.2 used whenever no filter was set. **Real bug found and fixed, 2026-09-28 (reported by user): a layer of stone generated around the teleport-in point.** Root cause: `SpectreChunkGenerator` left `carve`/`decorate` un-overridden (concrete, not abstract, on the base `ChunkGenerator`), so vanilla's default decoration step ran every feature registered to `Biomes.THE_VOID`. **Not this mod's own Ancient Furnace** (double-checked: that feature's own "cold biomes only" gate correctly excludes THE_VOID, whose temperature is a non-cold `0.5F`) - the actual culprit is vanilla itself: `TheVoidBiome`'s constructor hard-codes a `Feature.VOID_START_PLATFORM` decoration (a ~33x33 flat Stone/Cobblestone disc at Y 3 around block (8, 3, 8) - the standard "spawn platform for a void world" mechanic), which happened to land right on top of the first Spectre Cube's own spawn point (8, 1, 8). Both `carve`/`decorate` are now real no-ops - see `SpectreChunkGenerator`'s javadoc.

### #302 — Spectre Dimension — joining/rejoining any world after having visited the Spectre dimension doesn't crash

**Real crash found and fixed, 2026-09-28 (reported by user, full crash log): `NullPointerException: Dimension type must not be null` in `DimensionManager.getWorld`, thrown from `PlayerList.initializeConnectionToPlayer` while a player connects.** Root cause: `ModDimensions.registerDimension` was called from `FMLCommonSetupEvent` (mod-loading time, once per process, before any world/save is chosen) using plain `registerDimension`. Ground-truthed from `DimensionManager`'s own source that every world load calls `readRegistry`, which unconditionally clears the *entire* dimension-type registry back to vanilla-only and repopulates it strictly from that specific world's own saved registry data - wiping out our earlier registration for any world that hadn't already saved a `randomthings:spectre` entry, and never restoring it (since nothing re-registered on `RegisterDimensionsEvent`, the hook that fires immediately after specifically for this purpose). Any later `DimensionType.getById` lookup for the dangling reference (including resolving a player's own persisted "last dimension" field, stored as a raw int - confirmed via `Entity#read`) then returned null. Fixed by moving registration to a `RegisterDimensionsEvent` listener (`MinecraftForge.EVENT_BUS`, not the mod bus) using idempotent `registerOrGetDimension` instead - see `ModDimensions`'s javadoc. Verified via `runClient` against the same save that produced the original crash report - no crash, no new crash report generated.

### #303 — Spectre Sword — craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) and enchantability (22) than a diamond sword, repairable with Spectre Ingot on an anvil

User initially reported all four tools as not craftable - turned out to be a stale build/client (the recipe JSON was correct all along); resolved with a fresh `./gradlew build` + restart, no code change needed. Disclosed simplification: 1.12.2 had an ASM hook recoloring this sword's enchant-glow tint to white whenever actually enchanted (not a forced-always-glow like Spectre Key/Portkey) - dropped for the same reason Redstone Observer's own red-glow recolor was dropped (cosmetic-only, no render-hook infrastructure for tint recoloring in this port yet). The sword still glows normally via a real anvil/table enchantment, just with vanilla's default tint. 1.12.2's `EntitySpirit` tie-in (only this sword can damage that otherwise near-invulnerable mob) isn't wired up since `EntitySpirit` itself isn't ported yet.

### #304 — Spectre Pickaxe — craft: 3 Spectre Ingot across the top row, Obsidian double-stacked below center; same durability/enchantability as the sword, harvest level 3 (diamond-tier), and increases block reach by 3 while held in the mainhand

**2026-09-28: real bug found (reported by user) - the tooltip line for the +3 reach modifier showed a raw untranslated key instead of a proper name.** Ground-truthed the cause: this exact Forge build's own `forge/lang/en_us.json` ships a mismatched translation for the Forge-added reach-distance attribute - a `generic.reachDistance` key with no `attribute.name.` prefix, plus an unrelated `attribute.name.generic.reach_distance` snake_case key that doesn't match `PlayerEntity.REACH_DISTANCE`'s real camelCase name (`generic.reachDistance`) either - so the actual runtime-built key (`attribute.name.generic.reachDistance`) had no working translation anywhere. Fixed by adding that exact key to this mod's own `en_us.json`. Please retest.

### #305 — Spectre Axe — craft: 2 Spectre Ingot + 1 Obsidian in an L (top-left/middle-left/middle-center), Obsidian bottom-center; 8 attack damage/-3 attack speed (matches 1.12.2's hardcoded override exactly - reproduced here as a 5.0 modifier on top of the tier's own 3.0 bonus), same +3 reach

Same reach-tooltip lang fix as #304 - see that row. Please retest.

### #306 — Spectre Shovel — craft: 1 Spectre Ingot top-center, Obsidian double-stacked below; same +3 reach

Same reach-tooltip lang fix as #304 - see that row. Please retest.

### #307 — Spectre String — craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4

Real 1.12.2 recipe, not invented - see this slice's own intro note above for the research mix-up around it.

### #308 — Spectre Coil (Normal/Redstone/Ender) — craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre Ingot/Glass; Redstone: Redstone Block/Redstone/Spectre String + a Normal coil; Ender: Stable Ender Pearl/Ender Pearl/Spectre String + a Redstone coil); can only be placed against a face that exposes a Forge Energy capability (auto-breaks and drops itself otherwise); once placed, continuously drains the placing player's Spectre energy pool (1024/4096/20480 FE per tick respectively) into whatever it's attached to

Needs a real FE-capable machine from another mod to fully verify the transfer - can't test end-to-end in an environment with no other tech mods installed. Placement-validity (attaches only to a valid face, drops otherwise) and the recipes/registration are code-verified.

### #310 — Spectre Energy Injector — craft: Obsidian at the 4 corners, Spectre Lens top-center, a vanilla Beacon center, Spectre String left/right/bottom-center; once placed by a player, exposes that player's Spectre energy pool as a receive-only Forge Energy capability on every side, so an external FE-producing machine can push energy into it

**2026-09-28: recipe added now that Spectre Lens exists (#312-313) - was registered with no recipe at all in the previous slice, blocked on this exact prerequisite.** Please retest craftability.

### #311 — Spectre Charger (Normal/Redstone/Ender/Genesis) — craft the first 3 tiers same escalating pattern as the Coils (Genesis has no recipe, creative-only); right-click to toggle on/off; while on and carried anywhere in your inventory, drains your Spectre energy pool each tick to top up any other Forge-Energy-capable item stack in that same inventory (instant/unlimited for Genesis)

Same external-mod caveat as #308 - needs an FE-capable item (e.g. RF armor/tools from another mod) in inventory to fully verify the actual charging, since nothing in this port itself consumes FE yet.

### #317 — Diaphanous Block — two real bugs found and fixed, 2026-09-28 (reported by user)

**Bug 1: only the inverted variant could be mined - the plain (non-inverted) one couldn't be broken or interacted with at all.** Root cause: `DiaphanousBlock#getShape` was wrongly gated on `inverted` the same way `getCollisionShape` is - but unlike 1.12.2, where `getCollisionBoundingBox` (physical collision) and the base `getBoundingBox` (used for the block-targeting raytrace, left untouched by `BlockBlockDiaphanous` and so always a normal full cube) were two separate hooks, 1.14.4 has no such split: `Block.shouldSideBeRendered`/`World`'s block-targeting raytrace and actual physical collision both read `getShape`. Gating it on `inverted` meant a non-inverted block had *no* shape at all for the game to detect the player was even looking at it - left-click mining and right-click interaction both silently did nothing. Fixed by making `getShape` always return a full cube (so the block is always targetable/mineable/interactable, matching 1.12.2's actual behavior) and leaving only `getCollisionShape` - real physical walk-through-or-not - gated on `inverted`.

**Bug 2: the inverted variant's name showed as a raw untranslated key (`item.randomthings.diaphanous_block_inverted`) instead of "Diaphanous Block".** Root cause: `DiaphanousBlockItem#getTranslationKey(ItemStack)` appends `"_inverted"` onto whatever `super.getTranslationKey(stack)` returns - but `BlockItem#getTranslationKey()` (which that bottoms out at) delegates to *the block's own* translation key (`block.randomthings.diaphanous_block`), not an `item.`-prefixed one the way a plain `Item` subclass would. The lang entry this was actually appending onto had the wrong prefix (`item.` instead of `block.`), so the computed key (`block.randomthings.diaphanous_block_inverted`) never matched anything in `en_us.json`. Fixed by adding the correctly-prefixed key and removing the two unused `item.randomthings.diaphanous_block*` entries (dead - `BlockItem` never actually looks up an `item.`-prefixed key for this item at all, confirmed via source).

Both fixed the same session they were reported, before the initial implementation had been tested at all - `./gradlew build -x test` clean. Please retest.

### #77 — Item Collector / #112-area — Advanced Item Collector — directional model orientation

**Real bug, found 2026-09-28 (reported by user).** `item_collector.json`/`advanced_item_collector.json`'s blockstate rotation values for the 4 horizontal facings (`north`/`south`/`east`/`west`) didn't match 1.12.2's real numbers at all - ground-truthed by extracting the actual 1.12.2 blockstate JSON from that branch: it uses `x: 90` uniformly for all 4 horizontal directions, varying only `y` (0/180/90/270 for north/south/east/west). This port's version instead used `x: 270` for three of them and `x: 90` for only one, with mismatched `y` values too - someone derived a new (wrong) rotation scheme rather than porting the real numbers. Fixed both blockstate files to the exact 1.12.2 values. Advanced Item Collector had the identical bug in the identical shape, caught by comparison even though its own checklist row (#112) never specifically tested visual orientation.

### #89 — Player Interface — armor-slot placement bug

**Real bug, found 2026-09-28 (reported by user): a chestplate inserted from the UP side landed in the legs slot instead of the chest slot.** Root cause: `getCapability`'s UP-side handler wraps `player.inventory` directly in a plain `InvWrapper`, and `PlayerInventory` never overrides `isItemValidForSlot` - it falls back to `IInventory`'s own default, which unconditionally returns `true` for every slot (confirmed via source). Without that restriction, `RangedWrapper.insertItem` just fills the first empty slot in the 36-39 armor range regardless of item type. Checked 1.12.2's real `TileEntityPlayerInterface` first: it hit the exact same problem and solved it with its own `IInventory` wrapper delegating `isItemValidForSlot` to the real `ContainerPlayer`'s per-slot `Slot.isItemValid`. Ported the same idea without needing a Container at all - a new `SlotValidatingInventory` computes the same restriction directly via `MobEntity.getSlotForItemStack`, checking that the item's real equipment slot matches the specific armor index (36=feet, 37=legs, 38=chest, 39=head, per `PlayerInventory.armorInventory`'s own ordering).

### #93 — Chat Detector — GUI buttons reportedly not clickable

**Investigated 2026-09-28, no bug confirmed in the click-routing code.** The screen has one button (the "consume" toggle) plus a focused `TextFieldWidget`; reported that the text field works but the button doesn't respond to clicks. Checked every layer: the button uses the exact same `addButton(new Button(...))` + `ISignalContainer.send`/`handle` pattern as `IgniterScreen`'s own mode-toggle button, which is confirmed working (#72/#73 PASS) - so the generic mechanism isn't broken. `TextFieldWidget#mouseClicked`'s real source (checked directly) correctly returns `false` and un-focuses itself when a click lands outside its own bounds, which is what should happen for a click on the button below it - it doesn't "eat" clicks meant for other widgets. No geometry overlap between the field (y 20-38) and the button (y 44-64) either. Nothing else in this screen differs meaningfully from other working button+field combinations in this project. Please retest and, if still broken, note: does the button visually highlight on hover, does its label text ever show/change at all (it's set dynamically from server state in `drawGuiContainerForegroundLayer`), and does anything print to the log on click.

### #108 — Entity Filter Item — right-click capture reportedly not working

**Investigated 2026-09-28, no bug found - matches 1.12.2's real mechanism exactly.** `itemInteractionForEntity` (right-click only) is the same single hook 1.12.2's own `ItemEntityFilter` used - ground-truthed via that branch's real source - neither version was ever wired to left-click/attack despite this checklist row's own "right-click/attack" wording (looks like an authoring slip, not a real requirement). Traced the actual vanilla call chain (`PlayerEntity#interactOn` → `ItemStack#interactWithEntity` → `Item#itemInteractionForEntity`) and confirmed a zombie's `processInitialInteract` (the one thing that could swallow the interaction before it reaches the item) is the unmodified `Entity` default, which always returns `false` - so the hook should fire normally. Please retest with an explicit right-click (not an attack) on a zombie with the filter in your main hand, and report back if it still doesn't capture.

### #54/#55 — Biome Glass / Biome Stone — reported as showing no biome variation

**Investigated 2026-09-28, no bug found in the tint code.** Both blocks implement `IRTBlockColor#colorMultiplier` by calling `BiomeColors.getGrassColor(worldIn, pos)` - the exact same real vanilla API grass blocks themselves use for their own (confirmed-working) per-biome tint, including the same blur-radius averaging. The generic bridge that wires `IRTBlockColor` up to Forge's real `BlockColors`/`ItemColors` system (`RandomThings#onBlockColorHandler`/`onItemColorHandler`) is proven working already - `CompressedSlimeBlock` uses the identical mechanism for its own (working) per-instance tint, just with a fixed lookup table instead of a biome sample, so the wiring itself isn't in question. The one thing this can't rule out without live testing: `BiomeColors.getGrassColor`'s real color comes from each biome's own configured grass-color value, and most temperate overworld biomes (Plains, Forest, etc.) are all fairly close shades of green - a real, working variation between two such biomes could easily look like "no change" at a glance, unlike the dramatic, unmistakable difference near a swamp, badlands, or jungle. Please retest specifically at one of those boundaries before concluding this is still broken.

### #56 — Colored Grass — reported as missing a crafting recipe

**Checked 2026-09-28, this is intentional and matches 1.12.2 exactly - not a bug.** 1.12.2's own real acquisition path for any non-white variant was `ItemGrassSeeds` (17 creative-tab variants: "normal" plus all 16 dye colors, right-click a Dirt block to plant the matching type) - but that item itself never had a real crafting recipe anywhere in 1.12.2, confirmed via that branch's real source; it was creative-only/`getSubItems`-only. A previous session found this (see `WIKI_FEATURE_STATUS.md`'s "matches original's own unreachability" note) and deliberately shipped just the one reachable-equivalent (white) variant instead of porting 16 equally-uncraftable creative-only ones. If the full 17-variant `ItemGrassSeeds`-equivalent is wanted anyway (still creative-only, matching 1.12.2), that's a real feature to build - just flagging it wouldn't be restoring a recipe that ever existed.

### #155 — Sound Recorder GUI — sound name text ran off-screen, no way to select a sound

**Real bug, found 2026-09-28 (reported by user).** Two compounding causes in `SoundNameList`, found by checking 1.12.2's real `GuiStringList` first: (1) `AbstractList.getRowWidth()` defaults to a hardcoded `220` (vanilla's own width for the full-screen lists - resource packs, server list - this class was designed for), wider than this whole GUI panel, so `getRowLeft()`'s centering math placed rows well left of where they should sit; (2) nothing clipped or truncated a sound name longer than the available row width - 1.12.2's own version explicitly `GL11.glScissor`-clipped each row specifically to prevent this, which this port's rewrite (targeting vanilla's modern `ExtendedList` instead of Forge's old `GuiScrollingList`) dropped without a replacement. Fixed by overriding `getRowWidth()` to the list's own real width, and by trimming drawn text to the actual available pixel width via `FontRenderer#trimStringToWidth` (pixel-accurate, unlike `ChunkAnalyzerScanResultList`'s existing fixed-character-count truncation, since sound names vary a lot in length). The "no way to select a sound" half of the report was very likely just a consequence of the rows being genuinely unreachable/invisible off-panel - the click handler itself (`onSelect.accept(sound)` → `container.send(0, ...)`) was already correct. `ChunkAnalyzerScanResultList` shares the same un-overridden `getRowWidth()` default but wasn't touched - its own rows are confirmed working (#145-148 PASS) with near-identical constructor parameters, so whatever visual offset it produces isn't currently a reported problem there. Please retest.

### #158 — Sound Dampener — reported muting sounds far past its stated 20-block radius

**Investigated 2026-09-28, no bug found - matches 1.12.2 exactly.** The mute check (`dampener.getPos().distanceSq(soundPosition) < 20 * 20`) is byte-for-byte identical to 1.12.2's own `RTEventHandler` logic, confirmed via that branch's real source. One thing worth clarifying on retest: the 20-block check is against the *sound's own origin position* (`sound.getX/Y/Z()`), not the listening player's distance from the dampener - a sound whose real source (a mob, a block) sits within 20 blocks of the dampener will mute for a player standing much farther away, since that player is still in range to *hear* it. If the actual sound source was genuinely far from the dampener and it still muted, that would be a real bug - please report the exact source-to-dampener distance if so.

### #180 — Floo Powder — reported crafting recipe not working

**Investigated 2026-09-28, no bug found - the recipe JSON is byte-for-byte identical in shape/ingredients to 1.12.2's own real recipe** (extracted directly from that branch: same 2x2 pattern, same 4 ingredients, same positions), just modernized to point at this port's own distinct `floo_powder` item instead of a metadata sub-item of a shared `ingredient` item the way 1.12.2 stored it. This checklist row's own "(4-way)" wording looks like an authoring mistake rather than a real 1.12.2 mechanic - the recipe is one fixed arrangement (Ender Pearl top-left, Redstone top-right, Gunpowder bottom-left, plain Beans bottom-right), not rotation-tolerant. No duplicate/conflicting recipe file found either. Please retest with that exact layout using plain Beans specifically (not Lesser/Magic Bean, a different item) and report back the precise items/positions if it still fails.

### #254 — Magic Hood — still showed faint potion particles while worn

**Real bug, found 2026-09-28 (reported by user).** `event.shouldHideParticles(true)` (the previous fix's own mechanism) doesn't actually suppress potion particles at all - ground-truthed from `LivingEntity.tick()`'s own decompiled source: that synced flag is what vanilla itself uses for genuinely *ambient* effects (Beacon/Conduit Power), which switches to the rarer, more transparent `AMBIENT_ENTITY_EFFECT` particle type at 1/5 the normal spawn chance - not zero. The real gate is the synced particle *color* (`POTION_EFFECTS`): `tick()` only ever spawns a particle when that value is greater than 0, and it's set directly from `event.getColor()`. Forcing that to exactly 0 in the same listener is what actually stops every particle from spawning. `shouldHideParticles(true)` is left in place too in case anything else reads it, but it was never the real switch.

### #265 — Ancient Furnace — biome change had no visible effect

**Real bug, found 2026-09-28 (reported by user: waited out a full heat-up, saw the snow melt and the explosion, but no visible biome/color change).** This class's own javadoc had already disclosed this exact gap as "not attempted this session" - the biome-array mutation was genuinely correct and server-authoritative, but nothing told an already-connected client's own separate copy of each chunk's biome data to update, so the visual grass/foliage/fog color never changed even though the real biome had. Fixed via a new `resyncBiomesToClients` helper: tracks every chunk actually touched by the warming pass and resends each one's real `SChunkDataPacket` (ground-truthed via source that it always serializes `Chunk#getBiomes()` regardless of the section filter passed, so a full resend is the correct mechanism, not something narrower) to every player currently tracking it - the same packet vanilla's own `ChunkManager#sendChunkData` sends when a chunk first loads for a player.

### #277 — Portkey — priming glint never stopped even once bound

**Real bug, found 2026-09-28 (reported by user).** Unlike `StableEnderpearlItem`'s otherwise-identical drop-counter (stored in `entityItem.getPersistentData()`, a server-only compound never meant to reach the client), Portkey's counter has to be client-visible - `hasEffect(ItemStack)` only ever receives the stack, never the entity, so it's stored on the stack's own tag instead (matching 1.12.2's own design choice too). But mutating that tag *in place* on the exact `ItemStack` instance the entity's data manager already had cached never actually re-synced it: `EntityDataManager.set` (ground-truthed via source) only marks its `ITEM` entry dirty when the new value is a genuinely different object from what's currently stored, and `ItemStack` doesn't override `equals()` - so handing back the same (now-mutated) reference always read as "no change," leaving the client stuck on whatever `dropCounter` value it had at spawn time. Fixed by mutating a copy and calling `setItem` with that copy instead of the tag in place.

### #279 — Portkey camo — icon still off-center after the earlier display-transform fix

**Second real bug found in this area, 2026-09-28 (reported by user).** The previous fix (giving `portkey.json` real display-transform numbers) was necessary but not sufficient. Root cause of what was left: `ItemRenderer.renderItem(ItemStack, IBakedModel)` - the exact method this renderer's own `renderByItem` is already being called *from* (confirmed via its own decompiled source) - unconditionally applies its own `GlStateManager.translatef(-0.5, -0.5, -0.5)` centering offset *before* ever checking whether the model is builtin-rendered, i.e. before `renderByItem` runs at all. Every overload this renderer calls back out to in order to actually draw the camo'd or plain stack (`renderItem(stack, TransformType)` and `renderItem(stack, IBakedModel)` alike) funnels through that same method again, applying that exact same -0.5 offset a *second* time on top of the first - a genuine double-translate, completely independent of the transform-numbers issue the earlier fix addressed. `ItemRenderer#renderModel` - the one true no-offset primitive - is private, so there's no way to skip the inner call's own offset directly; fixed by cancelling the *outer* one first (+0.5 on all three axes) so the inner call's own -0.5 is the only one left standing.

### #293 — Block Destabilizer — requested to stop sweeping up non-full blocks

**Requested behavior change (2026-09-28), not a 1.12.2 bug fix** - checked first per this repo's own working rule, and the real original had no such restriction either. Added an `isNormalCube` check (a real, non-deprecated `BlockState` method) alongside the existing exact-match/fuzzy-match check, both at the initial target (`initStart`) and during the BFS expansion (`stepSearch`), so redstone dust, torches, levers, and similar non-full-cube attachments are no longer matched or swept into the flood-fill at all - even one sitting directly on a matched full block is left behind untouched.

### #91 — Inventory Rerouter — no visual feedback for a side's current redirect

**Real bug, found 2026-09-28 (reported by user): "I cannot determine what behavior the side is expected to do."** Checked 1.12.2's real source first, per this repo's own working rule: the original genuinely did draw a per-face overlay decal (6 new directional-arrow textures, baked via a custom `IBakedModel` reading an `IExtendedBlockState` unlisted property) - this port's own class javadoc had already disclosed dropping that, citing this project's Plate family as an established precedent for the same class of simplification, except the Plate family was itself deleted entirely later in this project's history, so that precedent no longer actually exists to compare against. Rather than reintroduce a full custom baked-model/texture pipeline for 6 icons this project has no art assets for, fixed the underlying usability problem directly instead: right-clicking a face now tells the player via an action-bar message exactly what that face is now set to (a specific direction, or "disabled") every time it's cycled. Doesn't restore the original's at-a-glance visual, but directly answers "what does this side do" without new rendering infrastructure.

### #258/#261 — Rain Shield — visual rain/snow suppression (new feature, built per explicit request)

**User confirmed wanting this built** after #258's report turned out to actually be about the *visual* half (#261), which had been explicitly disclosed as not-yet-attempted - the *mechanical* suppression (#258 as originally scoped: wetting, snow/ice formation, fire-extinguish) was already verified correct in an earlier session via direct bytecode inspection of the existing coremod. Real implementation work, ground-truthed via `javap -c` on the real compiled class before writing anything: `GameRenderer.renderRainSnow(float)`'s per-column loop calls `World.getBiome(BlockPos)` exactly once (bytecode offset 368, `invokevirtual`), storing the result into the same local the very next instruction reads for its `biome.getPrecipitation() != RainType.NONE` gate - meaning redirecting that one call site cleanly controls everything downstream in that iteration (both the rain-vs-snow decision and the "does this particle exist at all" decision) without needing to touch the loop body itself. This project's usual "wrap the method's own return value" idiom doesn't fit a ~300-line method with the check inline mid-loop, so this took the same general shape as `TeleporterTransformer`'s already-established "early exit via redirect" family, but as a call-site swap rather than a prologue insertion: the new `GameRendererTransformer.js` retargets that exact `INVOKEVIRTUAL World.getBiome` instruction (and only that one instance, inside only that one method) to `AsmHandler.getBiomeForRainRender`, which returns the real biome normally or `Biomes.DESERT` - a real, already-registered vanilla biome with `RainType.NONE` baked into its own definition (confirmed via that class's real source) - whenever the column falls inside an active Rain Shield's radius. No other `World.getBiome` call anywhere else in the game is affected. Verified via `runClient`: the coremod loads, the transform applies (`Transforming net/minecraft/client/renderer/GameRenderer` with no `VerifyError`/`LinkageError`/`ClassFormatError`), and the client reaches a stable main menu with the crash-report count unchanged. Not visually confirmable without a real interactive session - please retest standing in rain/snow near an active shield.
