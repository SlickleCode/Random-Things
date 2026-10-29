# Audit bug fixes (2026-10-29) - branch 1.20.1

Mirrored from the 1.14.4 commit `03be7448` in a cloud session with no JDK, no Forge/Minecraft jars and no network: **nothing here was compiled or run.** Only a parse-level check (`syntaxcheck.sh`) and manual review were possible; the usual `./gradlew build` and both test suites still have to be run. Every call was copied from existing code on this branch (pattern sources below).

Blocked / not in scope: Peace Candle churches in all village types (blocked, as on 1.14.4). Not changed on purpose: Imbuing Station and Inventory Tester keep their contents on break (the original had no drop logic). Tincture recipe already needs a Water Bottle.

All 12 bugs existed on this branch (same code as 1.14.4 modulo mappings); none was ALREADY-OK, none BLOCKED.

| Fix | Files | Pattern copied from |
| --- | --- | --- |
| AF1 | `block/RedstoneObserverBlock.java`, `block/redstoneinterface/BasicRedstoneInterfaceBlock.java`: `use` returns `InteractionResult.PASS` when `player.getItemInHand(handIn)` is a `RedstoneToolItem` | `PASS` from `use`: `SpectreCoreBlock.java:61`; `getItemInHand`: `SpectreCoreBlock.java:64` |
| AF2 | `tileentity/OnlineDetectorTileEntity.java`: `getPlayerByName` then `found.getGameProfile().getName().equals(username)` (`ServerPlayer` import already present). Test in `RemainingBehaviorTests` | `getGameProfile().getName()`: `RemainingBehaviorTests.java:91` |
| AF3 | `item/LotusSeedsItem.java`: also requires `Blocks.FARMLAND` (import added). `LotusBlock` survival untouched | `Blocks.FARMLAND`: `BeanStalkBlock.java:116` |
| AF4 | `asm/AsmHandler.java`: Peace Candle loop moved into a MONSTER block before the `EntityType.SLIME` early return. Test in `CoremodTests` | existing loop, unchanged |
| AF5 | `handler/spectrelens/SpectreLensHandler.java`, `tileentity/SpectreLensTileEntity.java`: store keyed by dimension string (`level.dimension().location().toString()`) then owner; tick applies only if `player.level().dimension().location().toString()` equals it; NBT `dimension` string, entries without it dropped on load | dimension string: `PortkeyItem.java:95`, `RandomThings.java:400` (`contains`: `RainShieldTileEntity.java:85`) |
| AF6 | `handler/EnderLetterHandler.java`: `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()` (`ServerLevel` import removed) | `SpectreLensHandler.get`; `level.getServer()`: `FixtureWorld.java:111` |
| AF7 | `tileentity/SoundBoxTileEntity.java`, `tileentity/SlimeCubeTileEntity.java` (new `saveAdditional`/`load`, `CompoundTag` import): `powered` boolean | existing `saveAdditional`/`load` of `SoundBoxTileEntity` |
| AF8 | `tileentity/AdvancedRedstoneTorchTileEntity.java`: setters call `setChanged()` | `SoundBoxTileEntity.insertPattern` |
| AF9 | `block/PotionVaporizerBlock.java`: `onRemove` + `Containers.dropItemStack` over `itemHandler()` (imports added) | `IronDropperBlock.java:86-104` |
| AF10 | `item/EnderBucketItem.java`, `item/ReinforcedEnderBucketItem.java`: after `drainSource`, SIMULATE-fill via `getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).map(...)`; if it does not fit, `world.setBlock(pos, state, 3)` puts the source back (Reinforced keeps scanning). Lambda captures are `final` copies | capability call: `ReinforcedEnderBucketItem.java:184` (original line); `setBlock(.., 3)`: `TestCtx.java:65` |
| AF11 | `entity/EclipsedClockEntity.java` (drop gets `targetTime` tag, sneak wrap `((t % 24000) + 24000) % 24000`), `item/EclipsedClockItem.java` (reads `targetTime` on placement; local typed `EclipsedClockEntity`) | tag reads: client property in `EclipsedClockItem`; `getOrCreateTag`: `ItemInventoryHandler.java:32`; `spawnAtLocation` already used |
| AF12 | `item/StableEnderpearlItem.java`: needs `contains("player-uuid")`, `UUID.fromString` inside try/catch `IllegalArgumentException`, else unbound branch | -- |

## Unverified points

- Nothing compiled. In particular check: `InteractionResult.PASS` from `use` really lets `RedstoneToolItem.useOn` run for the plain right click (both hands: off-hand tool counts too); `ForgeCapabilities.FLUID_HANDLER_ITEM` SIMULATE fill on the bucket stack; `Level.getServer()` on the `Level` passed to `EnderLetterHandler.get` (always server side, as before the cast to `ServerLevel` would have failed otherwise).
- AF5: lenses are keyed by the dimension location string; old saves lose their entries until the lens block entity re-registers (within 80 ticks). `setRemoved` also fires on chunk unload (pre-existing).
- AF10: the restore is a set-then-reset of the source block (possible neighbour-update blip, flowing flag).
- Tests: `coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk` and `behavior.online_detector.matches_the_player_name_case_sensitively` added in the shape of the 1.14.4 ones; other fixes have no test.
