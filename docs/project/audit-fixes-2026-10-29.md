# Audit bug fixes (2026-10-29), branch 1.18.2

Mirrored from the 1.14.4 commit `03be7448` in a session with no compiler, no Forge/Minecraft jars and no network: **nothing here was compiled or run.** Only a parse-level check (`syntaxcheck.sh`, 19 files OK) and manual review were possible; `./gradlew build`, both suites and the checklist rows AF1-AF12 (`TESTING_CHECKLIST.md` section G) still have to be done. Every API call was copied from existing code on this branch (patterns below).

Blocked: Peace Candle churches in all village types (blocked on 1.14.4 too; needs the vanilla pool and church piece names of desert, savanna, snowy and taiga villages). Not changed on purpose: Imbuing Station and Inventory Tester drops, the Tincture recipe (already a Water Bottle), the parity differences of the wiki audit.

## Per fix (all DONE)

- **AF1 Redstone Tool.** `RedstoneObserverBlock.use` and `BasicRedstoneInterfaceBlock.use` return `InteractionResult.PASS` when `player.getItemInHand(handIn).getItem() instanceof RedstoneToolItem`, so vanilla goes on to `RedstoneToolItem.useOn` (PASS pattern: `FluidDisplayBlock.use`, `BiomeRadarBlock.use`). Off-hand tool counts too.
- **AF2 Online Detector.** `getPlayerByName` ignores case, so `OnlineDetectorTileEntity.tick` now also requires `found.getGameProfile().getName().equals(username)` (`ServerPlayer` was already imported; `getGameProfile().getName()` as in `EnderMailboxBlock`). Test `behavior.online_detector.matches_the_player_name_case_sensitively` in `RemainingBehaviorTests` (skips without a case variant).
- **AF3 Lotus Seeds.** `LotusSeedsItem.useOn` also requires `Blocks.FARMLAND` (import `Blocks` as in `EvilTearItem`). `LotusBlock.mayPlaceOn` untouched, so worldgen lotus on grass/dirt survives.
- **AF4 Peace Candle vs slimes.** `AsmHandler.overrideSpawnResult`: the candle loop moved into a new MONSTER block before the `EntityType.SLIME` early return. Test `coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk` in `CoremodTests`. The JS transformer is not touched.
- **AF5 Spectre Lens per dimension.** `SpectreLensHandler` stores `Map<String, Map<UUID, LensEntry>>` (dimension id, then owner) in the overworld store; `addLens`/`removeLens` take the dimension id string. The dimension key is `level.dimension().location().toString()` (pattern: `PortkeyItem`, `RandomThings.java:407`); `tick` applies a lens only if `player.level.dimension().location().toString()` equals it (the `player.level` comparison is the one at `RandomThings.java:407`). NBT entries carry a `dimension` string; old entries without it are dropped on load (the lens re-registers within 80 ticks). `SpectreLensTileEntity` passes its own dimension in all three calls. Unlike 1.14.4 the key is a string (1.14.4 used the int dimension id).
- **AF6 Ender Letter store.** `EnderLetterHandler.get(Level)` uses `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()` (pattern: `SpectreLensHandler.get`); the `ServerLevel` import is gone. All callers go through `get(Level)`. `Level.getServer()` is used the same way in `OnlineDetectorTileEntity`.
- **AF7 powered flags.** `SoundBoxTileEntity.saveAdditional`/`load` and new overrides in `SlimeCubeTileEntity` (import `CompoundTag`) write/read `powered`.
- **AF8 Advanced Redstone Torch.** Both setters call `setChanged()`.
- **AF9 Potion Vaporizer.** New `onRemove` override in `PotionVaporizerBlock`, copied from `IronDropperBlock.onRemove` (`Containers.dropItemStack` over `itemHandler()`, only when the block really changes); imports `Containers`, `IItemHandler` added.
- **AF10 Ender Buckets.** After `drainSource` the whole amount is checked with `fill(..., FluidAction.SIMULATE)` (same capability call as `finishPickup`); if it does not fit, `world.setBlock(next, state, 3)` puts the block back and the scan goes on. `EnderBucketItem` simulates on a one-item copy; `ReinforcedEnderBucketItem` simulates on the stack and uses `final` copies of `drained` for the lambdas.
- **AF11 Eclipsed Clock.** `EclipsedClockEntity.dropItem` puts `targetTime` into the dropped stack's tag (`getOrCreateTag().putInt`, pattern `FlooPouchItem`); `EclipsedClockItem.useOn` restores it through `setTargetTime`; the sneak decrease wraps with `((t % 24000) + 24000) % 24000`. The local in `useOn` is now `EclipsedClockEntity` (the unused `HangingEntity` import stays).
- **AF12 Stable Ender Pearl.** `StableEnderpearlItem.tickDroppedPearl` only reads the UUID when the tag contains `player-uuid` and catches `IllegalArgumentException`, falling back to the unbound branch.

## Unverified points

- Not compiled. Names used: `InteractionResult.PASS`, `Player.getItemInHand`, `ServerPlayer.getGameProfile`, `Level.getServer`, `Level.dimension().location()`, `setChanged`, `Containers.dropItemStack`, `Level.setBlock`.
- AF1: relies on vanilla 1.18.2 running `Item.useOn` when `Block.use` returns PASS.
- AF2: relies on `PlayerList.getPlayerByName` being case-insensitive on 1.18.2 (the reason the check exists); if it is already case-sensitive the extra check is harmless.
- AF10: putting the block back is a set-then-reset and may cause a neighbour-update blip; the flowing-water flag of the restored source is the state captured before draining.
- AF5/AF6: `TE.setRemoved()` also fires on chunk unload (pre-existing), so a lens entry is removed until the chunk reloads.
- Test names above only exist as dev tests; the suites have not been run.
