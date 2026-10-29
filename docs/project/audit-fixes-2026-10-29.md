# Audit bug fixes (2026-10-29), branch 1.16.5

Mirrored from the 1.14.4 commit 03be7448. Written in a cloud session with no JDK 8, no Forge/Minecraft jars and no Maven access: **nothing here was compiled or run.** Only a parse-level check (`javac` syntax errors) and manual review were possible; `./gradlew build` and both suites still have to be run. Every call was copied from code already on this branch.

Blocked: Peace Candle churches in all village types (the repo only references the plains pool). Not changed on purpose: Imbuing Station and Inventory Tester keep their contents on break (the original had no drop logic either).
The Super Lubricent Tincture recipe already needs a Water Bottle, so it needed no fix.

## What changed on 1.16.5 (differences from 1.14.4)
- AF1 `RedstoneObserverBlock`, `BasicRedstoneInterfaceBlock`: `onBlockActivated` returns `ActionResultType` here, so the Redstone Tool case returns `ActionResultType.PASS` (pattern: `BiomeRadarBlock`, `SpectreCoreBlock`) and vanilla then calls `RedstoneToolItem.onItemUse`.
- AF2 `OnlineDetectorTileEntity` + test `behavior.online_detector.matches_the_player_name_case_sensitively`: as on 1.14.4.
- AF3 `LotusSeedsItem`: as on 1.14.4 (`Blocks.FARMLAND` check; `LotusBlock` ground rule untouched so worldgen lotus survives).
- AF4 `AsmHandler.overrideSpawnResult`: candle loop moved before the slime early return (this branch's signature takes `IServerWorld`). Test `coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk`.
- AF5 `SpectreLensHandler`, `SpectreLensTileEntity`: lenses keyed dimension then owner. The dimension key is the registry-key string `world.getDimensionKey().getLocation().toString()` (the int id of 1.14.4 does not exist here; same call as `RandomThings.java:392`, `BiomeRadarTileEntity`), stored as NBT string `dimension`. Entries without `dimension` are dropped on load.
- AF6 `EnderLetterHandler.get`: `world.getServer().getWorld(World.OVERWORLD).getSavedData()` (same call as `SpectreLensHandler.get`); `ServerWorld` import removed.
- AF7 `SoundBoxTileEntity`, `SlimeCubeTileEntity`: `powered` saved; `read(BlockState, CompoundNBT)` signature of this branch (SlimeCube got new write/read overrides plus `BlockState`/`CompoundNBT` imports).
- AF8 `AdvancedRedstoneTorchTileEntity`: setters `markDirty()`.
- AF9 `PotionVaporizerBlock.onReplaced` (copied from `IronDropperBlock`).
- AF10 `EnderBucketItem`, `ReinforcedEnderBucketItem`: simulate-fill after the drain, put the block back with `world.setBlockState(next, state, 3)` when it does not fit. Same code as 1.14.4.
- AF11 `EclipsedClockEntity` (wrap to 0..23999, drop keeps `targetTime`), `EclipsedClockItem` (restores it on placement; the item model property already read `targetTime`).
- AF12 `StableEnderpearlItem`: guard on `player-uuid` plus `IllegalArgumentException` catch.

## Unverified
- Compile of everything above; the neighbour-update blip when a bucket puts a source block back; that returning `PASS` from `onBlockActivated` reaches `RedstoneToolItem.onItemUse` on 1.16.5 (vanilla `ServerPlayerInteractionManager` does so for non-consuming results, to be confirmed in game).
- Old-save behaviour of the Spectre Lens store (old entries dropped, lens re-registers within 80 ticks).

---

# Round 2 (same day): missing features and more bug fixes, branch 1.16.5 (also unverified, nothing compiled)

Mirrored from the 1.14.4 commit 45eee77b. Parse-level check (`syntaxcheck.sh`) and JSON load passed; no compiler, no jars. Every API call was copied from code already on this branch. Not done on purpose: the glow-in-the-dark Luminous glint rendering (the tooltip and recipe are there), Peace Candle churches.

## What changed on 1.16.5 (differences from 1.14.4)
- AG1/AG2 `RandomThings.java`: two new `RightClickBlock` listeners. The sapling tag lookup is `BlockTags.getCollection().get(new ResourceLocation("minecraft:saplings"))` typed `net.minecraft.tags.ITag<Block>` (pattern: `DiviningRodItem.matches`; 1.14.4 used `Tag<Block>`); import `net.minecraft.tags.BlockTags` added. Creative check `abilities.isCreativeMode`, `setHeldItem`, `addItemStackToInventory`, `dropItem(stack, false)` as already used here.
- AG3 `LuminousPowderRecipe` (same SpecialRecipe shape as `DiaphanousInvertRecipe`), serializer `luminous_powder`, data recipe `luminous_powder_glint.json`, `ModItems.LUMINOUS_POWDER`, tooltip in `ClientSetup` with `mergeStyle(TextFormatting.YELLOW)` (1.14.4 `applyTextStyle`), lang `tooltip.randomthings.luminous_enchantment`.
- AG4 `CoilType.NUMBER` (128 FE/tick, direct like Genesis), `SPECTRE_COIL_NUMBER`, `NumberedSpectreCoilBlockItem` (name via `getTranslationKey(stack)` as `EnderBucketItem`), tile entity type lists the block, `LootHandler` (+ABANDONED_MINESHAFT, END_CITY_TREASURE; 10/8/30), `RTConfig` `loot.NumberedSpectreCoils`, blockstate, item model, loot table, lang `Spectre Coil Nr. %d`; creative entry is automatic (`RT_ITEM_GROUP`). `ResourceAudit`: NO_RECIPE_EXPECTED + both new chest tables in the injected-table check. Tests: pool assertions in `ChecklistTests` (#338 toggle) and `UtilityBehaviorTests`.
- AG5 `BlackoutPowderItem` (identical to 1.14.4, `onItemUse(ItemUseContext)` is unchanged in 1.16.5), registered next to `luminous_powder`, lang, item model, texture (copied from 1.14.4), recipe `blackout_powder.json` (same `recipes/` folder and `item` ingredient format here).
- AG6/AG7 `FlooNetworkHandler.teleport` (`world.isBlockLoaded`), `hasFireplaceNamed`, `FlooSignItem` status message (`sendStatusMessage(ITextComponent, boolean)` as in `EnderMailboxBlock`), lang `floo.info.duplicate_name`. Test `behavior.floo.a_duplicate_sign_name_keeps_the_sign_and_the_bricks` (uses `Vector3d.copy`).
- AG8 `StableEnderpearlItem`: `if (player.world != entityItem.world) player.changeDimension((ServerWorld) entityItem.world, SpectreTeleporter.INSTANCE)` before `setPlayerLocation`, the exact pattern of `SpectreHandler.teleportPlayerBack` (no portal is searched or built). Outdated javadoc paragraph removed.
- AG9 `PositionFilterItem.getDimension` returns the registry-key string (null without a position, `minecraft:overworld` when no dimension is stored); compared with `world.getDimensionKey().getLocation().toString()` in `AdvancedRedstoneInterfaceTileEntity.refreshTargets` and `RedstoneRemoteActivateMessage`.
- AG10 `RedstoneObserverTileEntity.onLoad` schedules a tick (`getPendingBlockTicks().scheduleTick(pos, block, 1)` as `IgniterBlock`), `RedstoneObserverBlock.tick(BlockState, ServerWorld, BlockPos, Random)` calls `refreshAfterLoad()`.
- AG11 `AnalogEmitterTileEntity.updateInput`: `facing.getOpposite()`.
- Entity Detector: no code change; test `behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2` added to `UtilityBehaviorTests`.

## Blocked / skipped
Nothing blocked. Skipped: the Luminous glow rendering (out of scope), tests for AG1-AG5, AG8-AG11 (no pattern on this branch).

## Unverified
- Compile of everything above; `ITag.contains` on the sapling tag; `changeDimension(ServerWorld, ITeleporter)` for a player keeps the same `ServerPlayerEntity` reference (as `SpectreHandler` assumes); the Observer tick scheduled from `onLoad` during chunk load; the Numbered Coil name (`%d` from the stack identity) in the creative tab and tooltips.
