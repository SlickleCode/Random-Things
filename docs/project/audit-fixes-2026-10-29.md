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

---

# Round 2 (same day): missing features and more bug fixes - branch 1.19.2 (unverified, nothing compiled)

Port of 1.14.4 commit `45eee77b`. Only the parse-level `syntaxcheck.sh` passed; every API call was copied from existing code on this branch (Mojang names, Forge 43.5.2). Checklist rows AG1-AG11 (section H of `TESTING_CHECKLIST.md`).

| Item | Status | Files | Notes |
|---|---|---|---|
| AG1 Spectre Sapling | done | `RandomThings` | `RightClickBlock` listener; tag test `state.is(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("minecraft:saplings")))` (as `DiviningRodItem.matches`); creative check `getAbilities().instabuild`. |
| AG2 Runic Pattern | done | `RandomThings` | `RunePatternItem.setPattern`, `player.setItemInHand`, `getInventory().add`, `player.drop(stack, false)`. |
| AG3 Luminous Powder | done (no glow) | `LuminousPowderRecipe`, `ModRecipeSerializers`, `ModItems` (`LUMINOUS_POWDER` holder), `ClientSetup` tooltip, recipe `luminous_powder_glint.json`, lang | `CustomRecipe` + `SimpleRecipeSerializer` like `SpectreAnchorCombineRecipe`. The recipe file is `luminous_powder_glint` because `luminous_powder.json` is already the powder's own recipe. Glow rendering not done. |
| AG4 Numbered Spectre Coil | done | `SpectreCoilBlock` (`CoilType.NUMBER`), `SpectreCoilTileEntity`, `ModBlocks`, `ModItems`, `NumberedSpectreCoilBlockItem`, `ModTileEntityTypes`, `RTConfig`, `LootHandler`, blockstate, item model, loot table, lang, `ResourceAudit`, loot-pool tests | Reuses the `spectre_coil` block model/texture like Genesis. Creative tab: items use `.tab(RT_ITEM_GROUP)`, so the registration is the entry. Name via `getName(ItemStack)` + `getDescriptionId(stack)`. |
| AG5 Blackout Powder | done | `BlackoutPowderItem`, `ModItems`, recipe, item model, texture (copied from 1.14.4), lang | `useOn`, `discard()` (as `SpectreIlluminatorEntity.interact`), `addFreshEntity(new ItemEntity(...))`. |
| AG6 Floo unloaded chunk | done | `FlooNetworkHandler.teleport` | `world.hasChunkAt(BlockPos)`. |
| AG7 Floo duplicate name | done | `FlooNetworkHandler.hasFireplaceNamed`, `FlooSignItem`, lang `floo.info.duplicate_name`, test | Message via `player.displayClientMessage(Component.translatable(...), true)` (action bar). |
| AG8 Stable Ender Pearl | done | `StableEnderpearlItem` | Different `level` -> `ServerPlayer.teleportTo((ServerLevel) level, x, y, z, yRot, xRot)` (pattern from `ManualTestWorld`); same level keeps `connection.teleport`. The javadoc's "disclosed simplification" paragraph was removed. |
| AG9 Position Filter dimension | done | `PositionFilterItem.getDimension` (String, default `minecraft:overworld`), `AdvancedRedstoneInterfaceTileEntity`, `RedstoneRemoteActivateMessage` | On this branch the dimension tag is a string (`level.dimension().location().toString()`), not an int. |
| AG10 Redstone Observer | done | `RedstoneObserverTileEntity.onLoad/refreshAfterLoad`, `RedstoneObserverBlock.tick` | `level.scheduleTick(pos, block, 1)`, `tick(BlockState, ServerLevel, BlockPos, RandomSource)` as `SticksBlock`; `hasChunkAt(target)`. |
| AG11 Analog Emitter | done | `AnalogEmitterTileEntity.updateInput` | `getSignal(pos.relative(facing), facing.getOpposite())`. |
| Entity Detector | no change | test `behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2` added | |

Tests changed/added: `ResourceAudit` (`spectre_coil_number` in NO_RECIPE_EXPECTED, mineshaft/end city tables in the loot-table check), `ChecklistTests` + `UtilityBehaviorTests` (end city pool vs `NumberedSpectreCoils`), new `RemainingBehaviorTests` Floo duplicate-name test, new `UtilityBehaviorTests` Entity Detector test.

Unverified / risks: `ServerPlayer.teleportTo` for the cross-dimension pearl (side effects such as portal cooldown); `Level.scheduleTick` called from `BlockEntity.onLoad`; `PASS` return of `BlackoutPowderItem.useOn` on a miss; creative-tab and name of the numbered coil in game; old Position Filters whose `dimension` tag is not a string count as overworld.
