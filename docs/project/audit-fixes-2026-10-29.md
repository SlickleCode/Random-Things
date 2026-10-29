# Audit bug fixes (2026-10-29)

Written in a cloud session with no JDK 8, no Forge/Minecraft jars and no Maven access: **nothing here was compiled or run.** Only a parse-level check (`javac` syntax errors) and manual review were possible; the usual `javap` ground-truthing and `./gradlew build` still have to be done. Wiki pages describe the intended behaviour these fixes bring the code to.

Blocked: Peace Candle churches in all village types (see G2 below). Not changed on purpose: Imbuing Station and Inventory Tester keep their contents on break (the original also had no drop logic; only the Potion Vaporizer dropped).

Correction to the audit lists: the Super Lubricent Tincture recipe already requires a Water Bottle on this branch (`forge:nbt`), so it needed no fix.

---

# G1 notes
(a) Lotus: LotusSeedsItem.onItemUse now also requires groundState.getBlock() == Blocks.FARMLAND (added Blocks import). LotusBlock.isValidGround (survival check, used by BushBlock.isValidPosition and worldgen/neighbour updates) left unchanged so worldgen on grass/dirt in snowy biomes keeps working and does not pop off. 1.12.2: canSustainPlant(Plains) accepted grass/dirt/farmland for placement too, so this is a deliberate intent change. No test (no lotus-seed use test pattern). Unverified: farmland that is moisturised/dry both are Blocks.FARMLAND. Check: seeds on grass/dirt do nothing, on farmland plant; naturally generated lotus stays. Row: "Lotus Seeds plant only on farmland; worldgen lotus persists".
(b) Tincture recipe: already uses {"type":"forge:nbt","item":"minecraft:potion","nbt":{"Potion":"minecraft:water"}} on 1.14.4 (valid JSON); no change. No test/doc asserts the old form (docs/project/architecture-conventions.md mentions forge:nbt generally). Check: only water bottle crafts it, other potions do not.
(c) RedstoneObserverBlock + BasicRedstoneInterfaceBlock.onBlockActivated: return false (GUI not opened) when player.getHeldItem(handIn).getItem() instanceof RedstoneToolItem; vanilla then calls RedstoneToolItem.onItemUse. 1.12.2 returned false with the tool. Sneaking unchanged (vanilla skips onBlockActivated). Advanced interface untouched. No test (no pattern). Unverified: off-hand tool also counts (hand-based). Check: plain right click with tool links; bare hand opens GUI. Row: "Redstone Tool links Observer/Basic Interface with plain right click".
(d) OnlineDetectorTileEntity: 1.12.2 and 1.14.4 used PlayerList.getPlayerByUsername (case-insensitive). Now looks up, then requires found.getGameProfile().getName().equals(username) (ServerPlayerEntity import already present). Test added: RemainingBehaviorTests behavior.online_detector.matches_the_player_name_case_sensitively (skips if no case variant). Check: wrong-case name does not power. Row: "Online Detector matches name case sensitively".

---

# G2

## (a) Peace Candle vs slimes - DONE (unverified, cannot compile)
- Files: asm/AsmHandler.java (overrideSpawnResult), debug/test/CoremodTests.java (new test).
- Change: the Peace Candle range loop (unchanged code) moved out of the later MONSTER block into a new MONSTER block placed BEFORE the `type == EntityType.SLIME` early return. SLIME is MONSTER-classified, so candle range now denies slimes. Slime Cube, Lapis/Quartz Lamp logic untouched; outside candle range the result is identical to before (slimes still skip lamps, as before). JS transformer not touched.
- 1.12.2: modifyValidSpawningChunks removed chunks in candle range for all natural spawns, with no slime exemption (slime special case was a separate EntitySlime patch for slime chunks only).
- Test: coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk (direct overrideSpawnResult calls, same style as existing slime cube tests).
- Risk: none beyond unverified compile; ordering is the only change. In-game: Slime Cube (unpowered) + Peace Candle within 3 chunks -> no slimes; remove candle -> slimes spawn.
- Checklist row: Peace Candle within 3 chunks also stops slime spawns, even in an unpowered Slime Cube's chunk; Slime Cube/Lapis/Quartz unchanged elsewhere.

## (b) Churches in all villages - BLOCKED
- The repo references only the plains pool (`village/plains/houses`, `plains_temple_4`, PlainsVillagePools); no other village pool/church name or *VillagePools class appears anywhere in src/ or docs/. Per the brief I did not guess. No files changed for (b).
- Exact missing names needed: for each of desert, savanna, snowy, taiga: the xxxVillagePools class (to call init(), as PlainsVillagePools.init() is used), the houses pool ResourceLocation (village/<style>/houses), the vanilla church piece string in `Single[minecraft:village/<style>/houses/<style>_temple_N]` form, and the matching zombie pools (village/<style>/zombie/houses) and their church piece names, plus confirmation of the placement behaviour (RIGID vs TERRAIN_MATCHING) used when registering each pool.
- Once supplied, the change is mechanical: parametrize PeaceCandleChurchPool by (pool id, church piece string), install() once per style, keep PEACE_CANDLE_CHURCH shared. The class javadoc "Only the plains pool is wrapped" must be updated and RTConfig's PeaceCandle comment ("plains village churches") reworded.
- Note: the cobblestone template will look out of style in desert/snowy/savanna/taiga villages. Footprint also must match each style's church (the existing test only checks plains' temple_4 size).

---

# G3 notes
## (a) Spectre Lens per dimension
Files: handler/spectrelens/SpectreLensHandler.java, tileentity/SpectreLensTileEntity.java (SpectreLensBlock unchanged: only calls setOwner).
Change: lenses stored as dimension id -> (owner UUID -> entry) in the single overworld store; addLens/removeLens take the dimension id (world.getDimension().getType().getId(), same call as BiomeRadarTileEntity); tick applies a lens only if the player's current dimension id equals the lens's. Same owner + same dimension overwrites; different players/dimensions coexist. NBT entries now carry "dimension" int.
Old saves: entries without "dimension" are dropped on load (lens TE re-registers itself within 80 ticks in its own dimension). Chosen as safest since the old entry has no dimension.
1.12.2: per-world storage, effects only when player.world == handler world.
Test: none (no existing lens test pattern in debug/test).
Risk: unverified compile; TE.remove() also fires on chunk unload (pre-existing) so the entry is removed until the chunk reloads.
Check: two lenses in overworld by one player: only one active; lens in Nether by same player works in the Nether only; other player's lens unaffected.
Checklist: "Spectre Lens: one per player per dimension; effects only while owner is in the lens's dimension."
## (b) Ender letter inbox
File: handler/EnderLetterHandler.java get(World) now uses world.getServer().getWorld(DimensionType.OVERWORLD).getSavedData() (same as SpectreLensHandler/RTWorldInformationHandler); ServerWorld import replaced. All callers (EnderMailboxBlock, EnderMailboxTileEntity, EnderMailboxContainer, debug tests) go through get(World), so no caller edits. Data name unchanged. Must be called with a server-side world (as before; client casting would have failed too).
Check: letter sent from Nether lands in inbox opened in Overworld. Checklist: "Ender Mailbox/Letters work across dimensions."
## (c) Powered flags
SoundBoxTileEntity, SlimeCubeTileEntity: write/read "powered" boolean. Slime Cube got new write/read overrides (none existed). Static `cubes` registry is populated in the constructor, which the load path also uses, so unaffected. Original 1.12.2 did not persist either flag (check not performed against source); fix is as requested. Test: none.
Check: powered sound box does not replay on reload. Checklist: "Sound Box / Slime Cube keep powered state across reload."
## (d) Advanced Redstone Torch
Setters call markDirty() (as AdvancedRedstoneRepeaterTileEntity). Check: change strengths, relog, values persist. Checklist: "Advanced Redstone Torch strengths persist after reload."

---

# G4 notes (branch 1.14.4)

## (a) Contents not dropped on break
- Files: block/PotionVaporizerBlock.java (onReplaced + InventoryHelper.spawnItemStack over itemHandler(), copied from IronDropperBlock; only when state.getBlock() != newState.getBlock()).
- 1.12.2: ONLY BlockPotionVaporizer.breakBlock dropped (InventoryHelper.dropInventoryItems). BlockImbuingStation and BlockInventoryTester have no breakBlock/drop in 1.12.2, so ImbuingStationBlock and InventoryTesterBlock were deliberately NOT changed (per "only where the original dropped"). If the user wants drops there anyway, same 15-line pattern; needs their go-ahead.
- Test: none (no matching test pattern; cannot compile). Risk: spawnItemStack on empty stacks is harmless. Check: fill vaporizer slots, break it -> items drop; rotate/replace-state does not duplicate.
- Checklist: "Potion Vaporizer drops its fuel/potion/output items when broken (Imbuing Station/Inventory Tester do not, as in 1.12.2) - FIXED"

## (b) Ender buckets
- Files: item/ReinforcedEnderBucketItem.java, item/EnderBucketItem.java.
- drainSource mutates the world (pickupFluid / IFluidBlock.drain EXECUTE) and cannot be pre-simulated without a new API, so after draining I SIMULATE-fill the bucket; if the whole amount does not fit (other fluid, full), the original block state is restored with world.setBlockState(pos, state, 3) and the scan continues (Reinforced) / continues (Ender). 1.12.2 used FluidUtil.tryPickUpFluid, which simulates before removing; behaviour now equals that.
- Test: none. Risk: restore is a set-then-reset (may cause a neighbour update blip, flowing-water flag); IFluidBlock restore is the same. Check: reinforced bucket with water, click lava -> lava stays, bucket unchanged; same when full.
- Checklist: "Reinforced Ender Bucket with other fluid/full does not remove the clicked source block - FIXED"

## (c) Eclipsed Clock
- Files: entity/EclipsedClockEntity.java (drop stack gets tag targetTime = current time; sneak-decrease wraps with ((t%24000)+24000)%24000), item/EclipsedClockItem.java (placing reads stack tag targetTime and sets the entity; no tag -> 0 as before; local var type now EclipsedClockEntity instead of HangingEntity, import left).
- 1.12.2: drop was a plain item (time lost) and placing never restored it; decrease used `% 24000` which goes negative in Java (original DID show negative). Wrapping to 0..23999 is a deliberate fix of that, as the brief asked.
- Test: none. Check: set time, break clock (non-creative), pick item up: dial shows the time; re-place -> same time; sneak-click at 0 -> 23:30-ish (23400 ticks... 24000-600=23400).
- Checklist: "Eclipsed Clock keeps its target time through break/pickup/place; sneaking below 0 wraps to the end of the day - FIXED"

## (d) Stable Ender Pearl
- Files: item/StableEnderpearlItem.java only (the null/UUID handling lives there; RandomThings.java untouched). Tag without "player-uuid" (or malformed uuid, IllegalArgumentException) now takes the unbound branch (random living entity within 10 blocks), same as no tag. 1.12.2 had the same unbound fallback.
- Test: none. Check: /give a stable pearl with empty tag {} , drop it: no exception after 7s, random-teleport fallback.
- Checklist: "Stable Ender Pearl with a tag lacking a player UUID acts unbound instead of throwing - FIXED"

All edited files passed syntaxcheck.sh.
