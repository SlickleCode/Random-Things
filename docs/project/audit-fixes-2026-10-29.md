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

---

# Round 2 (same day): missing features and more bug fixes on 1.21.4 (also unverified, nothing compiled)

Mirrored from commit `45eee77b` of branch 1.14.4. Same limits as round 1: no compiler, no NeoForge/Minecraft jars, only `syntaxcheck.sh` and a JSON check; every call was copied from code already on this branch. Rows AG1-AG11 are in `TESTING_CHECKLIST.md` section H.
Not done on purpose: the glow-in-the-dark Luminous glint rendering, Peace Candle churches, parity differences. Entity Detector: no code change (original behaviour), only the test of 1.14.4 was added.

## What changed on 1.21.4

| Fix | Files | Notes |
| --- | --- | --- |
| AG1 | `RandomThings` (new `RightClickBlock` listener) | Ectoplasm in hand, target `state.is(TagKey.create(Registries.BLOCK, ResourceLocation.parse("minecraft:saplings")))` (the `TagKey.create` shape of `DiviningRodItem`, `BlockState.is(TagKey)` as in `SpectreSaplingBlock`), not already a Spectre Sapling: server shrinks 1 unless `getAbilities().instabuild`, `setBlockAndUpdate`; event cancelled on both sides. |
| AG2 | `RandomThings` (second new listener) | Server only; paper in hand, clicked block is `RUNE_BASE` with a `RuneBaseTileEntity`; `RunePatternItem.setPattern(stack, getRuneData())`; single paper: `player.setItemInHand(event.getHand(), pattern)`, else `shrink(1)` + `player.addItem` with `player.drop(stack, false)` as fallback. |
| AG3 | new `recipes/LuminousPowderRecipe` (`CustomRecipe`, shape of `SpectreAnchorCombineRecipe`), `ModRecipeSerializers`, `ModItems.LUMINOUS_POWDER`, `ClientSetup` tooltip, `data/recipe/luminous_powder_glint.json`, lang `tooltip.randomthings.luminous_enchantment` | Flag `luminousEnchantment` in `minecraft:custom_data` via `StackTag` (like `spectreAnchor`). The recipe id `luminous_powder` is already the shaped crafting recipe of the powder itself, so the special recipe file is `luminous_powder_glint` (serializer id `luminous_powder`). "Enchanted" = some enchantment of the registry has level > 0: loops `level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).keySet()` and calls `EnchantmentHelper.getItemEnchantmentLevel(holder, stack)` (every piece copied from `BiomeUtil.ids`, `ModEnchantments.holder`, `MagneticLootModifier`), because no `isEnchanted()`/`ItemEnchantments` use exists on this branch. Glow rendering NOT done. |
| AG4 | `SpectreCoilBlock.CoilType.NUMBER`, `ModBlocks`, `ModItems`, new `item/block/NumberedSpectreCoilBlockItem` (`getName(ItemStack)`, shape of `DiaphanousBlockItem`), `ModTileEntityTypes`, `SpectreCoilTileEntity`, `LootHandler`, `RTConfig`, `ResourceAudit`; assets: `blockstates/`, `items/`, `models/item/spectre_coil_number.json`, loot table `blocks/spectre_coil_number.json`, lang | 128 FE/tick pushed directly like Genesis. Chests simple_dungeon 10, abandoned_mineshaft 8, end_city_treasure 30 behind `loot.NumberedSpectreCoils`. No recipe. Creative tab is automatic (every mod item). Shared block model/texture like the other tiers. |
| AG5 | new `item/BlackoutPowderItem`, `ModItems`, lang, `items/` + `models/item/blackout_powder.json`, texture copied from 1.14.4, `data/recipe/blackout_powder.json` | `useOn`: first lit orb of `ILLUMINATORS` in the clicked chunk gets `discard()` (the pickup path of `SpectreIlluminatorEntity.interact`, relights through `onRemovedFromLevel`), `ItemEntity` with the Illuminator next to the clicked face, one powder used unless creative. |
| AG6 | `FlooNetworkHandler.teleport` | `world.hasChunkAt(targetPos)` (as `RedstoneSignalHandler`) before the block entity lookup; unloaded: `return false`, registry untouched. |
| AG7 | `FlooNetworkHandler.hasFireplaceNamed`, `FlooSignItem`, lang `floo.info.duplicate_name`, test `behavior.floo.a_duplicate_sign_name_keeps_the_sign_and_the_bricks` | Message through `player.displayClientMessage(Component.translatable(..), true)` (action bar). |
| AG8 | `StableEnderpearlItem` | `if (player.level() != entityItem.level()) player.teleport(SpectreTeleporter.transition(player, (ServerLevel) entityItem.level()))` before `connection.teleport` - the exact pair `SpectreHandler.teleportPlayerBack` uses. Outdated "disclosed simplification" javadoc paragraph removed. |
| AG9 | `PositionFilterItem.getDimension` (String, `minecraft:overworld` when none is stored, `null` without position), `AdvancedRedstoneInterfaceTileEntity.refreshTargets`, `RedstoneRemoteActivateMessage` | Compared with `level.dimension().location().toString()`, the string `PositionFilterItem.setPosition` stores. |
| AG10 | `RedstoneObserverTileEntity.onLoad/refreshAfterLoad`, `RedstoneObserverBlock.tick` | `onLoad` (server, target set) calls `level.scheduleTick(pos, ModBlocks.REDSTONE_OBSERVER, 1)`; the block's `tick(BlockState, ServerLevel, BlockPos, RandomSource)` recomputes if `hasChunkAt(target)`. |
| AG11 | `AnalogEmitterTileEntity.updateInput` | `world.getSignal(pos.relative(facing), facing.getOpposite())`. |

Tests/audits touched: `ResourceAudit` (`spectre_coil_number` in `NO_RECIPE_EXPECTED`, the two new chest tables in the injected-table check), `UtilityBehaviorTests` (end city pool test + the Entity Detector empty-Custom-filter test of 1.14.4), `ChecklistTests` (pool vs `NUMBERED_SPECTRE_COILS` toggle), `RemainingBehaviorTests` (Floo duplicate name). `RoundTripTests` enumerates all items and picks the new ones up by itself. No test for AG1, AG2, AG3, AG5, AG8-AG11 (no pattern for them on this branch).

## Blocked / skipped

- Luminous glint rendering (out of scope).

## Unverified points

- AG1/AG2: that cancelling `RightClickBlock` stops the vanilla use of the held item on the server (NeoForge default cancellation result `PASS`: no swing animation); the existing listeners on this branch only act, never cancel, so there is no precedent. If the paper or Ectoplasm still gets used (e.g. Ectoplasm on the Spectre Core), check `getCancellationResult`.
- AG1: the vanilla tag `minecraft:saplings` (block tag) is believed to exist in 1.21.4, not read from the jar.
- AG3: the registry loop calls `getOrThrow(ResourceKey)` for ~40 enchantments per `matches` call (crafting grid change); fine for performance but unusual. `matches` also runs for every non-powder item in the grid, so a grid with two different items short-circuits before the loop only after the first enchanted check.
- AG3/AG4: the item name goes through `Item.getName(ItemStack)` like `DiaphanousBlockItem`; the registered `ITEM_NAME` component (lang key without argument) would show `Spectre Coil Nr. %d` anywhere vanilla reads the component directly instead of `getName` (e.g. some tooltips/recipe viewers).
- AG4: the block uses the shared coil model and an `items/spectre_coil_number.json` definition copied from Genesis; `useBlockDescriptionPrefix()` as for the other block items.
- AG8: `ServerPlayer.teleport(TeleportTransition)` moves the same instance (as `SpectreHandler` assumes); the filler position of `SpectreTeleporter.transition` is overwritten by `connection.teleport`. Nether/End portals' side effects (e.g. the End platform) are not triggered because `DO_NOTHING` is the post-transition.
- AG10: `onLoad` of a block entity on this branch is also called when the block is first placed (target null: nothing scheduled).
- AG5: settling (not yet lit) orbs are ignored, as on 1.14.4.
