## 5. Batch 3 — remaining (non-block) items (continued)

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

**2026-09-27, three real bugs found and fixed in sequence, all reported by the user (sync, crash, client-drain event) - see the plan file for the full blow-by-blow.** User confirmed: chunk lights up correctly, no crash, no relog needed. Follow-up perf tuning, same day: briefly split into separate server/client drain rates, then unified into one shared `PER_TICK` constant per user request for consistency (48 → 256, two more requests). … (full writeup in git history)

### #274 — Spectre Illuminator — right-click the placed orb to collect it back as an item; the chunk goes dark again

**2026-09-27:** initial gap (pickup never re-triggered a relight sweep) fixed alongside #273. **User then reported a fourth real bug**: after pickup, most of the chunk went dark but the edge where the orb first settled and started forcing light stayed stuck lit. … (full writeup in git history)

### #275 — Portkey — craft: gunpowder/stable enderpearl/gunpowder (top row) + diamond (bottom center)

New this session.

### #277 — Portkey — drop it on the ground: it glows (enchant shimmer) while unbound or freshly dropped; after ~5 real seconds undisturbed on the ground it stops despawning and stops glowing (primed); picking it back up before that resets the countdown next time it's dropped

Disclosed simplification: default vanilla glow color, not 1.12.2's custom magenta tint (would need a new coremod for one cosmetic detail) - see `PortkeyItem`'s javadoc. **2026-09-28: investigated, matches 1.12.2's own design exactly** - ground-truthed the real `ItemPortKey.hasEffect`: an *unbound* Portkey (no target set) glows forever regardless of its priming timer, by design; only a *bound* one stops glowing once primed. If this was tested unbound, the current behavior is correct as ported - please retest with a *bound* Portkey and confirm whether it still fails to stop glowing, or let me know if you'd like the always-glow-when-unbound behavior changed from the original.

### #278 — Portkey — once primed and bound, the next player to pick it up (not the one who dropped it, necessarily - anyone) gets teleported to the bound location instead of collecting it, landing on a safe nearby surface; picking up a not-yet-primed or unbound Portkey just collects it normally

Same safe-landing-spot search as 1.12.2 (5x5 column around the target, down to 10 blocks below it, first solid-topped spot with 2 air blocks above). Disclosed simplification: 1.12.2's full-screen HUD directional beam is dropped - see `PortkeyItem`'s javadoc for why. (The "camo" disguise system was also initially dropped here but re-implemented same-day - see #279-280.)

### #279 — Portkey — combine a Portkey with any other single item in a crafting table: the Portkey is disguised as that item (in every context - inventory, held, dropped on the ground, item frame), the donor item is returned unconsumed

New this session, added after initial research into whether a clean 1.14.4 path existed (it did) - see `PortkeyItemRenderer`'s javadoc. Uses a real `Item.Properties#setTEISR` custom renderer, the same mechanism vanilla uses for shulker boxes - no reflection, unlike 1.12.2's own implementation. **2026-09-28: real bug found - `portkey.json` (parented to `builtin/entity`) had no `display` transforms block at all.** Ground-truthed via `javap -c` that `ItemStackTileEntityRenderer#renderByItem` takes no `TransformType` in this version - all GUI/ground/hand/fixed scaling for a TESR-backed item comes from its OWN model's `display` block, applied by the caller *before* `renderByItem` runs (confirmed against vanilla's real `item/template_shulker_box.json`, the reference for every other `builtin/entity` item). With no `display` block, every context got an identity transform. Fixed by giving `portkey.json` the same transform numbers as the shulker box template. Please retest.

### #280 — Portkey — an un-camouflaged Portkey still shows its own plain icon correctly (in every context)

Regression check for #279's fix: rendering routes through a custom `ItemStackTileEntityRenderer` now instead of a flat icon model, so this is worth confirming didn't break. A real bug was already caught and fixed here via a `runClient` smoke test before this ever reached in-game testing (a doubled `item/item/` model path) - this row is about anything *that* test couldn't catch. … (full writeup in git history)

### #281 — Spectre Anchor — craft: 6 iron ingots (top-middle, both sides of the middle row, all three of the bottom row) + 1 Ectoplasm (center)

New this session.

### #282 — Spectre Anchor — combine it with any other single non-stackable item in a crafting table: consumes both, result is a copy of that item tagged "Anchored" (dark aqua tooltip line right under its name); dying with an Anchored item in your main inventory keeps it in your inventory on respawn instead of dropping it (with `keepInventory` off) - stacked/stackable items can't be anchored, matching 1.12.2

New this session. Needs a new `PlayerEntityTransformer` coremod (see `WIKI_FEATURE_STATUS.md`'s note) - `PlayerDropsEvent` no longer exists in this Forge version and no other event covers the death-drop code path, confirmed via `javap -c` before concluding a coremod was needed. Baubles doesn't exist in this port, so unlike 1.12.2 this only covers the main inventory (1.12.2's Baubles-slot handling is N/A here). … (full writeup in git history)

### #283 — Basic Redstone Interface — craft: iron ingots at the 4 corners, redstone dust on the 4 edges, a Stable Ender Pearl in the center; right-click with a Redstone Tool to start linking, then right-click any block to bind it as the target (matches the Redstone Observer's own linking flow); plain right-click (without the tool) opens a read-only status GUI showing the bound coordinates or "No Target"

**2026-09-28: real bug found** - the block itself was registered but `ModItems.java` never called `registerItemForBlock` for it (or Advanced Redstone Interface), the only two blocks in the whole registration list missing that call, so neither had a `BlockItem` at all - no way to obtain, craft, or see either in creative regardless of the recipe/assets (which were already correct). Fixed by adding the missing registration call. … (full writeup in git history)

### #284 — Basic/Advanced Redstone Interface — whatever redstone signal (weak or strong) is currently hitting the interface block itself gets mirrored onto its target position(s), as if a real redstone source were sitting there - e.g. redstone wire leading away from the target lights up, a comparator reading it responds, etc.; power updates live as the interface's own input changes, and stops immediately if the interface block is broken

The core mechanic, needs the two new coremods (see `WIKI_FEATURE_STATUS.md`'s Redstone Interface note) - `World.getRedstonePower`/`IWorldReader.getStrongPower` are patched to check a live per-TE registry (`RedstoneInterfaceTileEntity`) before falling back to vanilla's own computed value. Both coremods verified via the bytecode-verification harness (full Gradle-resolved classpath) with a clean pass on the first attempt.

### #283-285 — Wireless Redstone Interface refresh (2026-10-07)

**Real bug found by the behavior test `coremod.redstone_interface.mirrors_its_input_power_onto_the_bound_target`.** The
two power-query coremods (`World.getRedstonePower`, `IWorldReader.getStrongPower`) were fine, but the interface never
learned what power was hitting it: it refreshed from a `NeighborNotifyEvent` listener comparing the event position with its
own position. That event's position is the block that *changed* (the notifier), so the refresh only ran when the interface
block itself changed. 1.12.2 refreshed from the tile entity's `neighborChanged` (called by the block when a neighbor changes).
Fix: `neighborChanged` + `onBlockAdded` on `BasicRedstoneInterfaceBlock`/`AdvancedRedstoneInterfaceBlock` call the now-public
`RedstoneInterfaceTileEntity#updateRedstoneState`; the dead listener and its registration were removed. The Redstone Observer's
use of the same event is correct (it watches a remote target, the changed block). Needs a playtest with a real lever (#376).

### #285 — Advanced Redstone Interface — craft: redstone blocks + obsidian in a ring around a Basic Redstone Interface; right-click opens a GUI with 9 Position Filter slots instead of a single tool-bound target - drop up to 9 Position Filter items in to broadcast to all 9 positions simultaneously; removing one stops broadcasting to that position (matches the Basic interface's live power mirroring, just fanned out to multiple targets)

New this session. Matches 1.12.2's own inventory-of-Position-Filters design; item-type restriction (Position Filter only) enforced on the slot's backing `ItemStackHandler` rather than a dedicated slot class.

### #286 — Redstone Activator — right-click empty air to cycle its duration (2/20/100 ticks, shown both in the tooltip and as a texture swap on the item itself; shift-right-click cycles backwards); right-click any block to broadcast a strength-15 pulse there for the currently-selected duration, then it fades back to no signal on its own

New this session.

### #287 — Redstone Remote — craft: 3 Redstone Activators across the top, obsidian + a Stable Ender Pearl in the middle row, obsidian across the bottom; sneak-right-click opens an "edit" GUI (9 Position Filter slots, same as the Advanced Interface, plus your normal inventory) to bind up to 9 targets; plain right-click opens a compact "use" GUI with one button per bound target, each firing a 20-tick strength-15 pulse at its position when pressed

New this session. Disclosed simplification: 1.12.2's edit GUI had a second row of purely cosmetic ghost/camo-icon slots (letting you override each button's icon with an arbitrary item, no functional effect either way) - dropped here, and the "use" screen shows plain text buttons (each button's own display name, truncated) rather than item icons, matching this port's existing `EntityDetectorScreen` convention of text buttons over icon sprites. **2026-09-28: investigated, no bug found** - `RedstoneRemoteEditContainer` adds all 9 `SlotItemHandler`s at the same coordinates/texture as `AdvancedRedstoneInterfaceContainer`, container-type/screen-factory registrations match, and the shared `advanced.png` texture does have visible slot-square art drawn at those coordinates. Note Advanced Redstone Interface's own GUI (#285) was itself never actually tested before now (blocked by #283's missing item) - possible this was a symptom of that block never having existed to compare against, or something not reproducible from static source review. Please retest on a fresh build now that #283 is fixed.

**2026-09-28 (later, same day): real bug actually found, from a user screenshot of the "use" screen looking like a blank white box.** `RedstoneRemoteUseScreen` never overrode `drawGuiContainerForegroundLayer` at all - `ContainerScreen`'s own default is a no-op, unlike every other ported screen in this project (standard convention: draw `this.title` via `this.font.drawString`), and unlike 1.12.2's own `GuiRedstoneRemoteUse`, which explicitly drew `"item.redstoneRemote.name"` there. With no title and a plain, undecorated background (see below), the screen had nothing to visually anchor it - plausibly also what the original "no slots visible" report above was actually seeing on the *edit* screen, which had the exact same gap. `AdvancedRedstoneInterfaceScreen` and `RedstoneRemoteEditScreen` had it too (all three ship from the same batch) - fixed all three the same way. **Separately confirmed NOT a bug:** the "use" screen's plain flat-gray background texture (no border/frame decoration) looked broken but is byte-for-byte what it should look like - extracted 1.12.2's own `redstoneremoteuse.png` directly from the `1.12.2` branch and it's the same plain gray panel; the original mod genuinely shipped an undecorated placeholder-looking texture for this one screen. `./gradlew build -x test` clean. Please retest.

### #288 — Magnetic Enchantment — appears on pickaxes/axes/shovels from an enchanting table or anvil book, up to level 1; mining a block with it puts drops straight in your inventory instead of on the ground

**2026-10-07, real bug found by the automated Block Breaker test: this never worked.** The original port used a
`BlockEvent.HarvestDropsEvent` listener (comment claimed "canceling the event clears its drop list"). In Forge
1.14.4-28.2.26 nothing ever posts that event - `ForgeEventFactory.fireBlockHarvesting` has no caller anywhere in
the game (verified by searching the sources jar), so the listener was dead code. The Block Breaker (whose fake
player holds a Magnetic pickaxe) therefore left its drops on the ground instead of putting them in the chest behind
it. Fix: `handler/MagneticLootModifier`, a `GlobalLootModifierSerializer`/`LootModifier` registered via
`data/forge/loot_modifiers/global_loot_modifiers.json` + `data/randomthings/loot_modifiers/magnetic.json`. Block loot
is generated with `LootParameters.THIS_ENTITY` (the harvester) and `TOOL`; when the tool has Magnetic and the
harvester is a player the drops are handed to the player (`ItemHandlerHelper.giveItemToPlayer`) and the loot list is
emptied. Entity/chest loot has no TOOL parameter and is untouched. Config `MagneticEnchantment = false` still
disables the effect. Verified by `behavior.block_breaker.mines_front_block_into_rear_inventory`; needs a real-player
retest (see #372).

### #289 — Block Breaker — craft: cobblestone ring + iron pickaxe (top-center) + redstone torch (middle-center); placed 6-way facing toward whichever way you were looking

Recipe/model/blockstate ported directly from 1.12.2's real assets (no new art needed).

### #290 — Block Breaker — left unpowered, continuously mines whatever's directly in front of it at real survival speed (respects the target block's actual hardness/tool-appropriateness via a `FakePlayer` holding an unbreakable Magnetic pickaxe), showing a real crack overlay while it works

**2026-09-28: real crash found** - server crashed with a `NullPointerException` in `ForgeHooks.onBlockBreakEvent` the instant it finished mining any block with no tile entity (e.g. grass). `FakePlayer` never gets a real `connection` assigned, and that method unconditionally calls `entityPlayer.connection.sendPacket(...)` on that code path. … (full writeup in git history)

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

Real API gap found and worked around: this Forge build (28.2.26) has no `ITeleporter` hook at all (a later Forge addition) - `ServerWorld.getDefaultTeleporter()` always returns one plain `Teleporter` with no override point, and `PlayerList` no longer has 1.12.2's `transferPlayerToDimension(player, dim, customTeleporter)` overload either. … (full writeup in git history)

### #299 — Spectre Cube room — a 16x16 room made of unbreakable/explosion-immune Spectre Block, with a 2x2 unbreakable Spectre Core pedestal at the center; each new Spectre Key user gets their own room, laid out in a row (16 blocks apart) in the shared void dimension

Real bug found in 1.12.2's own `SpectreHandler#getSpectreCubeFromPos` while porting it (not a "looks wrong but intentional" case - traced actual numbers): a chunk-coordinate-based key was compared against a block-coordinate cube offset, off by a factor of 16, so the original could only ever correctly resolve the very *first* cube ever created - every player's second-or-later cube would fail to match, making the anti-trespass check constantly … (full writeup in git history)

### #301 — Spectre Dimension — always dark (no skylight), flat gray-cyan fog, no mob spawns (void biome, empty chunk generator), can't be slept in/respawn-set, wandering outside your own cube (not creative) snaps you back to your own spawn tile every tick, or fully back home if you don't have a cube yet; the rest of the dimension outside your built cube is pure air/void, no stray structures generate

Disclosed simplification: reuses vanilla's own `Biomes.THE_VOID` instead of porting a new custom Biome class (matches this port's existing precedent of not introducing new Biome subclasses - Ancient Furnace's worldgen work reassigns existing biomes rather than adding one). … (full writeup in git history)

### #302 — Spectre Dimension — joining/rejoining any world after having visited the Spectre dimension doesn't crash

**Real crash found and fixed, 2026-09-28 (reported by user, full crash log): `NullPointerException: Dimension type must not be null` in `DimensionManager.getWorld`, thrown from `PlayerList.initializeConnectionToPlayer` while a player connects.** Root cause: `ModDimensions.registerDimension` was called from `FMLCommonSetupEvent` (mod-loading time, once per process, before any world/save is chosen) using plain `registerDimension`. … (full writeup in git history)

### #303 — Spectre Sword — craft: 2 Spectre Ingot stacked center column, Obsidian bottom-center; more durability (2000 uses) and enchantability (22) than a diamond sword, repairable with Spectre Ingot on an anvil

User initially reported all four tools as not craftable - turned out to be a stale build/client (the recipe JSON was correct all along); resolved with a fresh `./gradlew build` + restart, no code change needed. Disclosed simplification: 1.12.2 had an ASM hook recoloring this sword's enchant-glow tint to white whenever actually enchanted (not a forced-always-glow like Spectre Key/Portkey) - dropped for the same reason Redstone Observer's own red-glow recolor was dropped (cosmetic-only, no render-hook infrastructure for tint recoloring in this port yet). The sword still glows normally via a real anvil/table enchantment, just with vanilla's default tint. 1.12.2's `EntitySpirit` tie-in (only this sword can damage that otherwise near-invulnerable mob) isn't wired up since `EntitySpirit` itself isn't ported yet.

### #304 — Spectre Pickaxe — craft: 3 Spectre Ingot across the top row, Obsidian double-stacked below center; same durability/enchantability as the sword, harvest level 3 (diamond-tier), and increases block reach by 3 while held in the mainhand

**2026-09-28: real bug found (reported by user) - the tooltip line for the +3 reach modifier showed a raw untranslated key instead of a proper name.** Ground-truthed the cause: this exact Forge build's own `forge/lang/en_us.json` ships a mismatched translation for the Forge-added reach-distance attribute - a `generic.reachDistance` key with no `attribute.name.` prefix, plus an unrelated `attribute.name.generic.reach_distance` snake_case key that … (full writeup in git history)

### #305 — Spectre Axe — craft: 2 Spectre Ingot + 1 Obsidian in an L (top-left/middle-left/middle-center), Obsidian bottom-center; 8 attack damage/-3 attack speed (matches 1.12.2's hardcoded override exactly - reproduced here as a 5.0 modifier on top of the tier's own 3.0 bonus), same +3 reach

Same reach-tooltip lang fix as #304 - see that row. Please retest.

### #306 — Spectre Shovel — craft: 1 Spectre Ingot top-center, Obsidian double-stacked below; same +3 reach

Same reach-tooltip lang fix as #304 - see that row. Please retest.

### #307 — Spectre String — craft: Ectoplasm at the 4 corners, String on the 4 edges, a Diamond in the center; makes 4

Real 1.12.2 recipe, not invented - see this slice's own intro note above for the research mix-up around it.

### #308 — Spectre Coil (Normal/Redstone/Ender) — craft each from the tier below it (Normal: Obsidian/Spectre String/Spectre Ingot/Glass; Redstone: Redstone Block/Redstone/Spectre String + a Normal coil; Ender: Stable Ender Pearl/Ender Pearl/Spectre String + a Redstone coil); can only be placed against a face that exposes a Forge Energy capability (auto-breaks and drops itself otherwise); once placed, continuously drains the placing player's Spectre energy pool (1024/4096/20480 FE per tick respectively) into whatever it's attached to

Needs a real FE-capable machine from another mod to fully verify the transfer - can't test end-to-end in an environment with no other tech mods installed. Placement-validity (attaches only to a valid face, drops otherwise) and the recipes/registration are code-verified.

