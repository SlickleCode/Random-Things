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
