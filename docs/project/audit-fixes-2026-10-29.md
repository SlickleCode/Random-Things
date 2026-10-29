# Audit bug fixes (2026-10-29), branch 1.21.1

Mirrored from the 1.14.4 commit 03be7448 in a cloud session with no compiler, no NeoForge/Minecraft jars and no network: **nothing here was compiled or run.** Only a parse-level check (`syntaxcheck.sh`) and manual review were possible; `./gradlew build`, both suites and the checklist rows AF1-AF12 (section G of `TESTING_CHECKLIST.md`) are still to do. Every call was copied from existing code on this branch (named below).

Blocked / out of scope: Peace Candle churches in all village types (blocked on 1.14.4 too). Not changed on purpose: Imbuing Station and Inventory Tester drops, Tincture recipe, wiki-audit parity differences.

## Per fix
- AF1 Redstone Tool: `RedstoneObserverBlock.useWithoutItem` and `BasicRedstoneInterfaceBlock.useWithoutItem` return `InteractionResult.PASS` when `player.getMainHandItem().getItem()` is a `RedstoneToolItem`; vanilla then runs `RedstoneToolItem.useOn`. 1.21 only calls `useWithoutItem` for the main hand (see `debug/BlockUse.java`), so an off-hand tool never reaches it. Unverified: that PASS from `useWithoutItem` falls through to `Item.useOn` (it does in `debug/BlockUse` / vanilla `ServerPlayerGameMode`).
- AF2 Online Detector: `getPlayerByName` result must also satisfy `getGameProfile().getName().equals(username)`. Test `behavior.online_detector.matches_the_player_name_case_sensitively` (RemainingBehaviorTests).
- AF3 Lotus Seeds: `LotusSeedsItem.useOn` additionally requires `groundState.is(Blocks.FARMLAND)`; `LotusBlock.mayPlaceOn` (survival, worldgen) unchanged so natural lotus on grass/dirt stays.
- AF4 Peace Candle: the candle loop in `AsmHandler.overrideSpawnResult` moved into a MONSTER-category block before the `EntityType.SLIME` early return. Test `coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk` (CoremodTests).
- AF5 Spectre Lens: `SpectreLensHandler` stores dimension -> (owner -> lens); the dimension key is `level.dimension().location().toString()` (a String, as `BiomeRadarTileEntity` / `SpectreHandler` use it; 1.14.4 used the int dimension id). `addLens`/`removeLens` take the dimension string first; `SpectreLensTileEntity` passes its own level's. `tick` skips a lens unless `player.level().dimension().location().toString()` equals it. NBT entries carry a `dimension` string; entries without one are dropped on load. No test (no lens test pattern).
- AF6 Ender Letter: `EnderLetterHandler.get` uses `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()` (same as `SpectreLensHandler.get`); unused `ServerLevel` import removed.
- AF7 `SoundBoxTileEntity` writes/reads `powered`; `SlimeCubeTileEntity` got `saveAdditional`/`loadAdditional` for it (pattern of `OnlineDetectorTileEntity`; `CompoundTag` import added).
- AF8 `AdvancedRedstoneTorchTileEntity` setters call `setChanged()`.
- AF9 `PotionVaporizerBlock.onRemove` drops `itemHandler()` slots with `Containers.dropItemStack` when the block really changes (copied from `IronDropperBlock.onRemove`; imports added).
- AF10 Ender Bucket / Reinforced Ender Bucket: after `drainSource`, a SIMULATE fill on the bucket's `Capabilities.FluidHandler.ITEM` handler (null handler counts as 0); if the whole amount does not fit, `world.setBlock(next, state, 3)` puts the block back (Reinforced then continues the scan with `drained = null`). No lambdas, so no effectively-final issue. Risk: set-then-reset can cause a neighbour update blip.
- AF11 Eclipsed Clock: `EclipsedClockEntity.dropItem` writes `targetTime` via `StackTag.edit` into the dropped stack (the key `EclipsedClockItem`'s `time` property already reads); `EclipsedClockItem.useOn` restores it with `setTargetTime` (local type now `EclipsedClockEntity`, unused `HangingEntity` import removed); sneak-decrease wraps with `((t % 24000) + 24000) % 24000`.
- AF12 Stable Ender Pearl: only reads the UUID when `StackTag.contains(itemStack, "player-uuid")`, and `IllegalArgumentException` from `UUID.fromString` is caught (unbound branch).

## Unverified points
- Nothing compiled; the 19 changed .java files pass the parse-level check only.
- AF10 relies on `IFluidHandlerItem.fill(FluidStack, FluidAction)` returning the filled amount (as `finishPickup` already uses it).
- AF11: the item stack's `targetTime` lives in `custom_data` (StackTag), so it survives pickup, but is not shown in the tooltip.
