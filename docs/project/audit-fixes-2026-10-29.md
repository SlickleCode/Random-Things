# Audit bug fixes (2026-10-29) - mirrored to branch 1.20.6

Ported from commit 03be7448 (branch 1.14.4) in a session with no JDK 21, no Gradle and no NeoForge/Minecraft jars: **nothing here was compiled or run.** Only a parse-level check (`syntaxcheck.sh`) and review against code already on this branch were possible. Every API call was copied from a call used elsewhere on this branch; build, run both suites and retest rows AF1-AF12 (`TESTING_CHECKLIST.md`, section G) before trusting it.

Blocked / not in scope: Peace Candle churches in all village types (blocked on the 1.14.4 branch, not ported), Imbuing Station / Inventory Tester drops (the original had none), Tincture recipe (already a Water Bottle).

| Fix | Files | Change on 1.20.6 | Pattern copied from |
|---|---|---|---|
| AF1 | `block/RedstoneObserverBlock`, `block/redstoneinterface/BasicRedstoneInterfaceBlock` | `useWithoutItem` returns `InteractionResult.PASS` when the main or off hand holds a `RedstoneToolItem`, so vanilla goes on to `RedstoneToolItem.useOn` | `PASS` returns in `BiomeRadarBlock`/`FluidDisplayBlock`; `player.getOffhandItem()` in `RedstoneObserverLineRenderer` |
| AF2 | `tileentity/OnlineDetectorTileEntity` | `getPlayerByName` result must have `getGameProfile().getName().equals(username)`; test `behavior.online_detector.matches_the_player_name_case_sensitively` | `getGameProfile().getName()` in `EnderMailboxBlock`, existing online detector test |
| AF3 | `item/LotusSeedsItem` | `useOn` fails unless the clicked block is `Blocks.FARMLAND`; `LotusBlock.mayPlaceOn` unchanged so worldgen lotus survives | `Blocks.FARMLAND` uses in `BeanStalkBlock` |
| AF4 | `asm/AsmHandler.overrideSpawnResult`, `CoremodTests` | candle loop moved ahead of the `EntityType.SLIME` early return; test `coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk` | the moved loop itself, existing slime cube tests |
| AF5 | `handler/spectrelens/SpectreLensHandler`, `tileentity/SpectreLensTileEntity` | store is dimension -> owner -> entry; the dimension key is `level.dimension().location().toString()`; a lens applies only while `player.level().dimension()` matches; entries without `dimension` in the NBT are dropped on load | `level().dimension().location().toString()` in `BiomeRadarTileEntity`, `PortkeyItem`, `SpectreHandler` |
| AF6 | `handler/EnderLetterHandler` | `get(Level)` uses `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()`; the `ServerLevel` cast and import are gone | `SpectreLensHandler.get` |
| AF7 | `tileentity/SoundBoxTileEntity`, `tileentity/SlimeCubeTileEntity` | `powered` written in `saveAdditional`, read in `loadAdditional` (Slime Cube gets both overrides) | the neighbouring overrides in the same classes |
| AF8 | `tileentity/AdvancedRedstoneTorchTileEntity` | both setters call `setChanged()` | `AdvancedRedstoneRepeaterTileEntity` |
| AF9 | `block/PotionVaporizerBlock` | `onRemove` drops `itemHandler()` slots when the block really changes | `IronDropperBlock.onRemove` |
| AF10 | `item/EnderBucketItem`, `item/ReinforcedEnderBucketItem` | after `drainSource`, a `SIMULATE` fill must take the whole amount, otherwise `world.setBlock(pos, state, 3)` puts the block back and the scan goes on | the fill calls already in both classes; `setBlock(.., 3)` in `OnlineDetectorTileEntity` |
| AF11 | `entity/EclipsedClockEntity`, `item/EclipsedClockItem` | dropped item gets `targetTime` via `StackTag.edit`, placing restores it via `StackTag.contains/getInt`; sneak-decrease wraps with `((t % 24000) + 24000) % 24000` | `StackTag.edit` in `StableEnderpearlItem`, `StackTag.getInt` in the clock's own item property |
| AF12 | `item/StableEnderpearlItem` | custom data without `player-uuid`, or a malformed UUID, takes the unbound branch | `StackTag.contains` |

## Unverified points / risks
- Nothing compiled. Highest-risk spots: the lambda in `SpectreLensHandler` (`computeIfAbsent(.., d -> new HashMap<>())`, type inference), `StackTag.edit` in the clock entity (the captured `dropTargetTime` is a `final` copy).
- AF1: only the main hand's `useWithoutItem` is called by vanilla; a tool in the off hand with an empty main hand also passes (so linking works from the off hand). A tool in the off hand while the main hand holds something else makes the main item's `useOn` run first, as for any item. `ItemInteractionResult`/`useItemOn` is not touched. Behaviour of the dispatch (`PASS` from `useWithoutItem` reaches `Item.useOn`) is read from `debug/BlockUse` and `api-gotchas-1.20.6.md`, not from the vanilla source.
- AF5: `TE.setRemoved()` also fires on chunk unload (pre-existing), so a lens entry disappears until its chunk loads again.
- AF7: like the original commit, `updatePowerState` does not call `setChanged()`; the flag is saved with the next save of the chunk-dirty block entity.
- AF10: the restore is set-then-reset (neighbour update blip); Ender Bucket `SIMULATE` check runs on a count-1 copy of the stack.
- AF11: the item model's `time` property already reads `targetTime` from custom data, so the dropped stack's dial shows the stored time.
