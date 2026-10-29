# Audit bug fixes (2026-10-29) - branch 1.19.2

Mirror of the 12 fixes of commit `03be7448` (branch 1.14.4), written in a cloud session with no JDK, no Forge/Minecraft jars and no network: **nothing here was compiled or run.** Only the parse-level `syntaxcheck.sh` passed. Every API call was copied from existing code on this branch (Mojang names).

Blocked / not in scope: Peace Candle churches in all village types (blocked on 1.14.4 too); Imbuing Station / Inventory Tester drops (the original had none); Tincture recipe (already Water Bottle).

| Fix | Status | Files | Notes |
|---|---|---|---|
| AF1 | done | `RedstoneObserverBlock`, `BasicRedstoneInterfaceBlock` | `use` returns `InteractionResult.PASS` when the held item is a `RedstoneToolItem` (PASS is the 1.19.2 equivalent of `return false`: vanilla then runs the item's `useOn`). Unverified: PASS really falls through to `useOn` for the tool. |
| AF2 | done | `OnlineDetectorTileEntity`, test in `RemainingBehaviorTests` | `getPlayerByName` is case insensitive, exact name checked afterwards. |
| AF3 | done | `LotusSeedsItem`, `UseItemTests` | Needs `Blocks.FARMLAND`. `LotusBlock` survival untouched. Existing test `plant_on_grass_but_not_on_stone` asserted the old behaviour, rewritten to `plant_on_farmland_but_not_on_grass_or_stone`. |
| AF4 | done | `AsmHandler.overrideSpawnResult`, test in `CoremodTests` | Candle loop moved before the slime early return. |
| AF5 | done | `SpectreLensHandler`, `SpectreLensTileEntity` | Key is the dimension id string (`level.dimension().location().toString()`, as in `PortkeyItem`/`SpectreHandler`), NBT tag `dimension` is a string (1.14.4 used an int). Old entries without the tag are dropped on load. Not tested (no lens test pattern). |
| AF6 | done | `EnderLetterHandler` | `world.getServer().getLevel(Level.OVERWORLD).getDataStorage()` (as `SpectreLensHandler.get`); unused `ServerLevel` import removed. |
| AF7 | done | `SoundBoxTileEntity`, `SlimeCubeTileEntity` | `saveAdditional`/`load` write and read `powered` (Slime Cube got new overrides + `CompoundTag` import). |
| AF8 | done | `AdvancedRedstoneTorchTileEntity` | setters call `setChanged()`. |
| AF9 | done | `PotionVaporizerBlock` | `onRemove` + `Containers.dropItemStack`, copied from `IronDropperBlock`. |
| AF10 | done | `EnderBucketItem`, `ReinforcedEnderBucketItem` | SIMULATE fill after the drain; block put back with `world.setBlock(pos, state, 3)`. Possible neighbour-update blip. |
| AF11 | done | `EclipsedClockEntity`, `EclipsedClockItem` | Drop stack tag `targetTime`; placement restores it; sneak wrap `((t % 24000) + 24000) % 24000`. `HangingEntity` import in the item is now unused (left). |
| AF12 | done | `StableEnderpearlItem` | Missing/malformed `player-uuid` acts as unbound. |

Unverified: everything compiles and behaves in game (checklist rows AF1-AF12, section G of `TESTING_CHECKLIST.md`).
