# Audit bug fixes (2026-10-29), branch 1.21.4

Mirrored from commit `03be7448` of branch 1.14.4 (see `audit-fixes-2026-10-29.md` there for the intent of each fix). Written in a cloud session with no compiler, no NeoForge/Minecraft jars and no network: **nothing here was compiled or run.** Only a parse-level check (`syntaxcheck.sh`) was possible; every call was copied from code that already exists on this branch. `./gradlew build`, both suites and the rows AF1-AF12 (`TESTING_CHECKLIST.md` section G) still have to be done.

Blocked on every branch: Peace Candle churches in all village types. Not changed on purpose: Imbuing Station and Inventory Tester drops, the Tincture recipe, wiki-audit parity differences.

## What changed on 1.21.4

| Fix | Files | Notes |
| --- | --- | --- |
| AF1 | `RedstoneObserverBlock`, `BasicRedstoneInterfaceBlock` | `useWithoutItem` returns `InteractionResult.PASS` when the main hand holds a `RedstoneToolItem`, so the tool's `useOn` runs. Sneaking never reaches `useWithoutItem`. |
| AF2 | `OnlineDetectorTileEntity`, test in `RemainingBehaviorTests` | `getPlayerByName` ignores case; the found player's `getGameProfile().getName()` must equal the name. |
| AF3 | `LotusSeedsItem`, test `UseItemTests` | `useOn` also requires `Blocks.FARMLAND`. `LotusBlock.mayPlaceOn` (survival) untouched, worldgen lotus keeps surviving. The existing test `use_item.lotus_seeds.plant_on_grass_but_not_on_stone` expected grass to work and was rewritten (`plant_on_farmland_but_not_on_grass_or_stone`). |
| AF4 | `AsmHandler.overrideSpawnResult`, test in `CoremodTests` | Candle loop moved into its own MONSTER block before the `EntityType.SLIME` early return. |
| AF5 | `SpectreLensHandler`, `SpectreLensTileEntity` | Store is `Map<String dimension, Map<UUID, LensEntry>>`, dimension = `level.dimension().location().toString()` (same call as `BiomeRadarTileEntity`); tick applies a lens only if `player.level().dimension().location().toString()` equals it; NBT entries carry `dimension` (string), entries without it are dropped on load. `addLens`/`removeLens` take the dimension string first; the tile entity is their only caller. |
| AF6 | `EnderLetterHandler.get` | `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()...` (same store lookup as `SpectreLensHandler.get`); `ServerLevel` import removed. |
| AF7 | `SoundBoxTileEntity`, `SlimeCubeTileEntity` | `powered` written in `saveAdditional`, read in `loadAdditional` (Slime Cube had no save/load overrides before; `CompoundTag` import added). |
| AF8 | `AdvancedRedstoneTorchTileEntity` | Both setters call `setChanged()`. |
| AF9 | `PotionVaporizerBlock` | `onRemove` drops `itemHandler()` slots with `Containers.dropItemStack` when the block really changes (copy of `IronDropperBlock.onRemove`). |
| AF10 | `EnderBucketItem`, `ReinforcedEnderBucketItem` | After `drainSource`, `fill(drained, FluidAction.SIMULATE)` on the bucket's `IFluidHandlerItem`; if the full amount does not fit the source is put back with `world.setBlock(next, state, 3)` and the scan goes on. Ender Bucket probes a one-item copy of the stack, as `finishPickup` fills a copy. |
| AF11 | `EclipsedClockEntity`, `EclipsedClockItem` | Dropped stack gets `targetTime` via `StackTag.edit` (the item model already reads `StackTag` `targetTime`); placing restores it with `setTargetTime`; sneak-decrease wraps with `((t % 24000) + 24000) % 24000`. The local in the item is now `EclipsedClockEntity` (the `HangingEntity` import is unused but left). |
| AF12 | `StableEnderpearlItem` | Needs `StackTag.contains(.., "player-uuid")`, and `UUID.fromString` is inside a try/catch for `IllegalArgumentException`; otherwise the unbound branch runs. |

## Unverified points

- AF1: that vanilla 1.21.4 falls through from a `PASS` of `useWithoutItem` to the held item's `useOn` (relied on from the 1.21 interaction flow, not read in the jar). Test it with the Redstone Tool in game.
- AF1: only the main hand is considered (`useWithoutItem` has no hand argument and is only called for the main hand).
- AF2/AF12: `getGameProfile().getName()` and `StackTag.contains` are copied from other uses on this branch.
- AF5: dimension keyed by string instead of the numeric id of 1.14.4 (no ids in 1.21); `TE.setRemoved()` also fires on chunk unload (pre-existing), the entry is then removed until the chunk reloads.
- AF9: `onRemove` is the method name this branch already uses in `IronDropperBlock`.
- AF10: restoring the block is a set-then-reset, a neighbour update blip is possible; a flowing-water flag may differ.
- AF3: the rewritten lotus test and the new tests were not run.
