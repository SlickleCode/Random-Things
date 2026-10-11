## 2. Batch 1 — standalone blocks (no GUI/tile entity)

### #29 — Lapis Lamp

**UNBLOCKED, 2026-09-26**: root cause was structural, not a code bug - `MonsterEntity`'s own registered spawn-placement predicate (light-level check) exits early and returns `false` roughly 165 bytecode instructions *before* `ForgeHooks.canEntitySpawn` ever fires the `LivingSpawnEvent.CheckSpawn` listener this used to rely on, so an ALLOW override never got a chance to run (DENY, e.g. … (full writeup in git history)

### #30 — Quartz Lamp

Was PASS under the original coremod-based force-DENY mechanism, then broken (stopped blocking spawns) by the same-day light-value-trick detour described in #29, now reverted back to the original coremod mechanism (unchanged from what previously passed - no peaceful-related fix needed on the DENY side, forcing DENY in Peaceful is harmless since nothing would spawn there anyway). **CONFIRMED WORKING, 2026-09-27 (user).**

### #31 — Super Lubricent Ice

**2026-09-28, real bug found and fixed (reported by user): a boat resting on this block (or Super Lubricent Stone) accelerates without bound until it crashes the game, unless something physically stops it.** Root cause: the existing speed cap (`SuperLubricentPhysics.capHorizontalSpeed`) only ever ran from a `LivingUpdateEvent` listener, which exclusively fires for `LivingEntity` - a `BoatEntity` is a plain `Entity`, so it was never capped at … (full writeup in git history)

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

**UNBLOCKED, 2026-09-26**: the Mixin this depended on could never fully bootstrap from a mods-folder jar on this Forge version (see the old note this replaced, preserved in the plan file's Batch 7 history). Re-implemented the exact same three ground-truthed bytecode redirects as a Forge coremod instead (`transformer/FireBlockTransformer.js` + `lumien.randomthings.asm.AsmHandler`) - coremods don't have Mixin's launch-plugin-registration problem. … (full writeup in git history)

### #74 — ~~Igniter — Keep Ignited mode~~ (removed per request)

— removed the mode entirely (now just Toggle/Ignite), plus the recurring self-reschedule tick loop that only existed to serve it.

### #75 — Online Detector — emits redstone while the configured username is online

**2026-09-28, real bug found and fixed: hitting the inventory keybind while the username field was focused still closed the GUI, and typed text wasn't being saved.** Ground-truthed `INestedGuiEventHandler`/`Widget`/`TextFieldWidget`: `Screen#setFocusedDefault` only sets the *screen's* own input-routing pointer (`getFocused()`, used to route `keyPressed`/`charTyped` events to the right widget) - it does not touch the widget's own separate … (full writeup in git history)

### #76 — Item Collector — vacuums nearby dropped items into attached inventory

— root cause found for the vacuum half: the port called `getCapability(ITEM_HANDLER_CAPABILITY, facing.getOpposite())` to fetch the target inventory's handler, but ground-truthing 1.12.2's `TileEntityItemCollector.update()` shows it fetches with plain `facing` (only the existence check used `.getOpposite()`) — most inventories ignore the side and would still work, but ones that filter by side (hoppers, some modded chests) would silently return … (full writeup in git history)

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

