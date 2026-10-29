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

---

# Round 2 (same day): missing features and more bug fixes (also unverified, nothing compiled)

Added (matching 1.12.2): Spectre Sapling by right clicking a vanilla sapling (`minecraft:saplings` tag) with Ectoplasm; Runic Pattern from paper on a Rune Base; Luminous Powder recipe
(`LuminousPowderRecipe`, NBT flag `luminousEnchantment`, tooltip); Numbered Spectre Coil (`CoilType.NUMBER`, 128 FE/tick, loot only, config `loot.NumberedSpectreCoils`);
Blackout Powder item. Fixed: Floo no longer deletes a fireplace in an unloaded chunk, Floo duplicate sign names show a message, Stable Ender Pearl changes the bound player's dimension,
Advanced Redstone Interface and Redstone Remote respect a Position Filter's dimension, Redstone Observer recomputes after load, Analog Emitter reads its input from the opposite side again.
**Not done:** the glow-in-the-dark Luminous glint rendering (needs a new glint coremod hook that sets the lightmap to full bright; docs/project/coremods.md has the patterns; 1.12.2 used
`AsmHandler.preEnchantment/postEnchantment`). **No change needed:** Entity Detector with an empty Custom filter (the original behaves the same).
Notes: Numbered Coil chest chances are 10 / 8 / 30 (the original's `onlyFound` condition could never fire in chests, so it is not ported); Blackout Powder now consumes one powder (the original did not).
Per-fix notes follow.

---

# H1 notes

## (a) Spectre Sapling conversion
- RandomThings.java: new RightClickBlock listener. Ectoplasm in hand + target block in tag minecraft:saplings (BlockTags.getCollection().get(RL), same shape as DiviningRodItem) and not already SPECTRE_SAPLING -> server: shrink 1 (not creative), setBlockState(SPECTRE_SAPLING default); event cancelled both sides.
- 1.12.2: ItemIngredient.onItemUse, ore dict "treeSapling", shrink + setBlockState, returns SUCCESS (creative count is restored by ItemStack.onItemUse in 1.12, so creative check is equivalent).
- Unverified: tag minecraft:saplings exists in 1.14.4 (believed yes); setCanceled on RightClickBlock. No test added (MoreBehaviorTests posts a RightClickBlock but no pattern for item-in-hand).
- In game: ectoplasm on oak/spruce/... sapling -> Spectre Sapling, ectoplasm -1 (not in creative).
- Checklist row: Ectoplasm right-click on vanilla sapling converts it to a Spectre Sapling (consumes 1 Ectoplasm, not in creative).

## (b) Runic Pattern from paper
- RandomThings.java: new RightClickBlock listener (server only). Original trigger: PAPER in hand, right-click an existing placed Rune Base (Runic Dust) block (RTEventHandler ~l.605; the lang text "right clicking runic dust with a piece of paper"). Copies the 4x4 data via RunePatternItem.setPattern (this branch's byte format), consumes 1 paper; if single paper the pattern replaces it in the clicked hand, else shrink + addItemStackToInventory (dropItem fallback). Rune Base stays. Sneak pattern->paper untouched.
- Unverified: player.setHeldItem/addItemStackToInventory/dropItem (all used elsewhere in repo; addItemStackToInventory only on a debug inventory).
- In game: place dust, click it with paper -> Rune Pattern with that pattern; stack of paper -1.
- Checklist row: Right-clicking placed Runic Dust with paper creates a Rune Pattern of it (paper consumed).

## (c) Luminous Powder glint
- New recipes/LuminousPowderRecipe.java (SpecialRecipe, flag NBT byte "luminousEnchantment", exactly 1 powder + 1 enchanted not-yet-luminous item, powder consumed, result = copy count 1), registered in ModRecipeSerializers as luminous_powder; recipe JSON data/randomthings/recipes/luminous_powder_glint.json; ModItems.LUMINOUS_POWDER ObjectHolder; yellow "Luminous" tooltip at index 1 (ClientSetup tooltip listener, as 1.12.2) + lang tooltip.randomthings.luminous_enchantment.
- "Enchanted" check = NBT list "Enchantments" non-empty (no unproven isEnchanted()).
- Render part BLOCKED: 1.12.2 AsmHandler.preEnchantment/postEnchantment set lightmap to 240,240 around the glint pass (OpenGlHelper.lastBrightnessX/Y). This branch has no such hook (ItemRendererTransformer only supplies colour via GlintColors/ClientAsmHandler.enchantmentColorHook) and no lightmap API is used anywhere; needs a new/extended coremod hook (pre/post glint) calling the lightmap setter, reading LuminousPowderRecipe.isLuminous(renderingStack). Armour glint likewise.
- No test added (no recipe-serializer test exists).
- Checklist row: Luminous Powder + enchanted item (shapeless) yields item with "Luminous" tooltip (glow-in-dark glint render still TODO).

Files: RandomThings.java, recipes/LuminousPowderRecipe.java, recipes/ModRecipeSerializers.java, item/ModItems.java, client/ClientSetup.java, en_us.json, recipes/luminous_powder_glint.json. syntaxcheck ok.

---

# H2 notes (unverified, nothing compiled; parse check + JSON check passed)

## (a) Numbered Spectre Coils
- Files: SpectreCoilBlock (CoilType.NUMBER("number",128,0x00FF00)), ModBlocks (+SPECTRE_COIL_NUMBER holder/registration), ModItems (registers NumberedSpectreCoilBlockItem "spectre_coil_number"), ModTileEntityTypes (block added to the spectre_coil TE type), SpectreCoilTileEntity (NUMBER branch: receiveEnergy(rate) directly, no pool, like GENESIS; javadoc updated), new item/block/NumberedSpectreCoilBlockItem (display name "Spectre Coil Nr. %d", number 1..499 from Random(System.identityHashCode(stack)), as 1.12.2 ItemBlockSpectreCoil), LootHandler (+ABANDONED_MINESHAFT, END_CITY_TREASURE constants; injects into simple_dungeon 10, abandoned_mineshaft 8, end_city_treasure 30), RTConfig loot.NumberedSpectreCoils (default true; 1.12.2 key worldgen.NumberedSpectreCoils), ResourceAudit (NO_RECIPE_EXPECTED + the two new chest tables in the injected-table check), assets: blockstates/spectre_coil_number.json, models/item/spectre_coil_number.json (same shared model/texture as other tiers, none are tinted on this branch), loot_tables/blocks/spectre_coil_number.json, lang block.randomthings.spectre_coil_number. Creative tab: automatic (Item.Properties group). No recipe (original had none).
- 1.12.2: chests simple_dungeon/abandoned_mineshaft/end_city_treasure with weights 10/8/30 of 100, plus a `onlyFound` loot condition (killer is a non-fake player). In a chest context there is no killer so that condition is always false in 1.12.2, i.e. the coils effectively never generated. I did NOT port that condition (wiki says they can be found in chests). Energy: 1.12.2 generated Numbers.NumberedSpectreCoilEnergy (128) per tick directly, no pool, owner required; kept (owner==null coil does nothing, as the other tiers). The brief said "pool per player"; the original and wiki ("generate") do not use the pool for NUMBER, so I followed the original. The 128 is a constant (no energy config option added).
- Tests: UtilityBehaviorTests loot-injection test + ChecklistTests toggle test (#338 style) assert the new pool "randomthings:spectre_coil_number" on end_city_treasure. No coil test existed to update; no count-based registry tests found.
- Risks: vanilla table names chests/abandoned_mineshaft and chests/end_city_treasure (1.14.4 names; checked by the ResourceAudit loot-table check); getDisplayName override signature copied from EnderBucketItem; the TE type now lists 5 valid blocks.
- Check in game: /give the coil, name shows "Spectre Coil Nr. N", attach to an energy machine, it gives 128 FE/t; chests in end cities/mineshafts/dungeons contain it; config off removes the pool.
- Checklist row: Numbered Spectre Coil (loot only: dungeon 10%, mineshaft 8%, end city 30%; config NumberedSpectreCoils) is named "Spectre Coil Nr. N" and feeds 128 FE/t into an attached machine.

## (b) Blackout Powder
- Files: new item/BlackoutPowderItem, ModItems registration "blackout_powder" (next to luminous_powder), lang item.randomthings.blackout_powder, models/item/blackout_powder.json, textures/item/blackout_powder.png (copied from 1.12.2 items/blackoutpowder.png), data recipe recipes/blackout_powder.json (shaped DG/ M: ink_sac, coal_block, lapis_lazuli -> 4; 1.12.2 dye data 0 = ink sac, 4 = lapis).
- 1.12.2 (ItemIngredient.onItemUse): server side, if the chunk is illuminated, finds an EntitySpectreIlluminator in the chunk, setDead, spawns an EntityItem with the Spectre Illuminator at pos.offset(facing), toggles the chunk; the powder was NOT consumed. Here: onItemUse scans SpectreIlluminatorEntity.ILLUMINATORS for isInChunk(world, chunk), calls remove() (same path as the right-click pickup, onRemovedFromWorld relights), spawns the ItemEntity at pos.offset(face), consumes one powder unless creative (per brief; divergence from 1.12.2). Not in an illuminated chunk -> PASS.
- Test: none (no illuminator behaviour test pattern exists in debug/test).
- Risks: unverified compile; settling (not yet illuminated) orbs are ignored. Check: place illuminator, wait until lit, right-click a block in the chunk with the powder: orb gone, item dropped beside the block, chunk dark, one powder used (not in creative); recipe gives 4.
- Checklist row: Blackout Powder (ink sac + coal block + lapis) right-clicked on a block in an illuminated chunk removes the orb, drops the Spectre Illuminator and relights/darkens the chunk; costs one powder.

## Docs
- docs/project/wiki-feature-status.md (Spectre Coils row, history paragraph, Illuminator row), docs/project/scope-decisions.md (new "ported after all" line; it had no blackout line), docs/project/state-history-2026-10.md and root WIKI_FEATURE_STATUS.md (removed Blackout Powder from the will-not-migrate lists).

---

# H3 notes

## (a) Floo fireplace deleted on chunk unload
- File: handler/floo/FlooNetworkHandler.java (teleport). Added `world.isBlockLoaded(targetPos)` check before reading the TE; unloaded target -> return false, no teleport, registry untouched.
- 1.12.2: no loaded check (same bug in the original); removed entry on any non-master TE. Deliberate improvement.
- Risk: closest-name match that is unloaded blocks teleport entirely (does not fall through to the next-best name). Check: fireplace far away in unloaded chunk -> nothing happens, entry still exists after loading chunk.
- Checklist row: Floo: typing the name of a fireplace in an unloaded chunk does nothing and the fireplace still works once its chunk is loaded.

## (b) Floo duplicate sign name
- Files: FlooNetworkHandler (new `hasFireplaceNamed`), FlooSignItem (status message on failure), en_us.json `floo.info.duplicate_name`.
- 1.12.2: createFireplace false -> sign NOT consumed, bricks unchanged, no message (the port already did not consume; only the message was missing).
- Test: RemainingBehaviorTests `behavior.floo.a_duplicate_sign_name_keeps_the_sign_and_the_bricks` (unrun).
- Check: second sign with same name (any case) -> action bar message, sign kept, bricks unchanged.
- Row: Floo Sign: reusing an existing name (any case) shows a message and keeps the sign and bricks.

## (c) Stable Ender Pearl
- (1) Offline bound player: the ORIGINAL also falls into the random-teleport branch (getPlayerByUUID returns null -> else branch). Current port already matches; no change.
- (2) Cross dimension: StableEnderpearlItem now calls `player.changeDimension(entityItem.world.getDimension().getType())` when `player.dimension` differs, then setPlayerLocation (same pattern as SpectreHandler.teleportPlayerBack). Original used PlayerUtil.teleportPlayerToDimension. Removed the outdated "disclosed simplification" javadoc paragraph. UUID guard kept.
- Risk: changeDimension may return a new entity instance for ServerPlayerEntity in some versions (SpectreHandler keeps using the old reference too); unverified in game. No test (needs two dimensions).
- Check: bind pearl in Overworld, go to Nether, drop pearl in Overworld... i.e. drop in other dimension than the bound player; after 7s the player arrives at the pearl.
- Row: Stable Ender Pearl: a bound player in another dimension is transferred to the pearl's dimension and position.

## (d) Position Filter dimension
- Files: PositionFilterItem (new static `getDimension(ItemStack)`, MIN_VALUE if no position), AdvancedRedstoneInterfaceTileEntity.refreshTargets (target dropped if dimension != world dimension id), network/messages/RedstoneRemoteActivateMessage (remote logic lives here, not in RedstoneRemoteItem; no signal if dimension differs).
- 1.12.2 ignored the dimension (code used only x/y/z); change follows the wiki.
- Tests: none existing use Position Filters with Advanced Interface/Remote; none added (needs GUI inventory/network).
- Risk: filters created before dimension was stored have dimension 0 (Overworld) by default. Check: filter set in Nether placed in an Overworld interface does nothing.
- Row: Advanced Redstone Interface / Redstone Remote: Position Filters set in another dimension are ignored.

---

# H4 notes

## (a) Redstone Observer on load
- Files: tileentity/RedstoneObserverTileEntity.java (onLoad schedules a block tick, new refreshAfterLoad()), block/RedstoneObserverBlock.java (tick() override calling refreshAfterLoad; java.util.Random import).
- 1.12.2: weak/strong power maps were saved in NBT and the constructor ran updateRedstoneState. 1.14.4 does not save them, so output stayed 0 after reload until the target changed.
- Fix: onLoad (server, target set) schedules a 1-tick block tick (same getPendingBlockTicks().scheduleTick pattern as SticksBlock/IgniterBlock); the tick recomputes if the target chunk isLoaded (pattern used elsewhere). Running behaviour unchanged.
- Test: none (needs a save/reload; no pattern).
- Risk: unverified compile; if the target chunk is not loaded at that tick, no refresh until the target notifies. Check: link observer to lever/lamp setup, toggle lever, save and reload, output matches immediately.
- Row: "Redstone Observer output is correct right after world reload".

## (b) Analog Emitter input side
- File: tileentity/AnalogEmitterTileEntity.java updateInput: world.getRedstonePower(pos.offset(facing), facing.getOpposite()) (original line 45 semantics, same call as before).
- Tests: existing behavior.analog_emitter test uses a Redstone Block, which powers every side, so it does not encode the old side and still holds; no change. A side-discriminating test would need a directional source (repeater/dust) whose properties are not used elsewhere in this repo, so not added.
- Check: repeater/dust pointing into the emitter's input face powers it; sources on the other sides do not.
- Row: "Analog Emitter input side matches 1.12.2 (facing opposite query)".

## (c) Entity Detector empty Custom filter
- Original TileEntityEntityDetector: FilterPredicate returns false for everything when filterClass==null and no IEntityFilterItem; output = (!invert == found) -> powered == invert. So empty Custom + Invert is ON in 1.12.2 too (and OFF without invert).
- 1.14.4 code already returns `invert` for that case, identical truth table. NO code change (replicated original; the "permanently on" is original behaviour). Entity Filter item semantics untouched.
- Test added: UtilityBehaviorTests behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2 (off, then on after invert).
- Row: "Entity Detector: empty Custom filter is off; with Invert it is on (as 1.12.2)".
