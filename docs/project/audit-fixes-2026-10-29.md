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

---

# Round 2 (same day): missing features and more bug fixes - mirrored to branch 1.20.6

Ported from commit 45eee77b (branch 1.14.4) in the same kind of session: no JDK 21, no Gradle, no NeoForge/Minecraft jars. **Nothing here was compiled or run**; only `syntaxcheck.sh` (parse level) and JSON validation. Build, run both suites and retest rows AG1-AG11 (`TESTING_CHECKLIST.md`, section H).

| Item | Status | Files | Pattern copied from |
|---|---|---|---|
| AG1 Spectre Sapling | DONE | `RandomThings` (new `RightClickBlock` listener) | tag test `TagKey.create(Registries.BLOCK, ..)` + `BlockState.is(tag)` from `DiviningRodItem.matches`; `setCanceled(true)` on a `ICancellableEvent` from the `BlockEvent.EntityPlaceEvent` listener; `getAbilities().instabuild` from `SoundBoxBlock` |
| AG2 Runic Pattern from paper | DONE | `RandomThings` (second new listener) | `RunePatternItem.setPattern`, `RuneBaseTileEntity.getRuneData`, `getInventory().add` + `drop(stack, false)` from `EnderBucketItem`, `setItemInHand` from `BlockBreakerTileEntity` |
| AG3 Luminous Powder recipe + tooltip | DONE | new `recipes/LuminousPowderRecipe`, `ModRecipeSerializers`, `ModItems.LUMINOUS_POWDER` (new `@ObjectHolder`), `ClientSetup` tooltip, lang `tooltip.randomthings.luminous_enchantment`, `data/randomthings/recipes/luminous_powder_glint.json` | enchanted check: `for (Enchantment e : BuiltInRegistries.ENCHANTMENT)` (registry iteration as `for (Item item : BuiltInRegistries.ITEM)` in `ModItems`; `BuiltInRegistries.ENCHANTMENT` from `RegUtil.id(Enchantment)`) + `EnchantmentHelper.getItemEnchantmentLevel(Enchantment, ItemStack) > 0` as in `MagneticLootModifier`; recipe shape from `SpectreAnchorCombineRecipe` (`SimpleCraftingRecipeSerializer`, `CraftingContainer`); flag via `StackTag.edit/contains` |
| AG4 Numbered Spectre Coil | DONE | `SpectreCoilBlock` (`CoilType.NUMBER`), `ModBlocks`, `ModItems`, `ModTileEntityTypes`, `SpectreCoilTileEntity`, new `item/block/NumberedSpectreCoilBlockItem`, `LootHandler`, `RTConfig` (`loot.NumberedSpectreCoils`), `ResourceAudit`, `ChecklistTests`, `UtilityBehaviorTests`; resources: `blockstates/spectre_coil_number.json`, `models/item/spectre_coil_number.json`, `loot_tables/blocks/spectre_coil_number.json`, lang `block.randomthings.spectre_coil_number` | clone of `spectre_coil_genesis` files; display name override from `EnderBucketItem.getName` / `DiaphanousBlockItem`; creative tab is automatic (every mod item) |
| AG5 Blackout Powder | DONE | new `item/BlackoutPowderItem`, `ModItems`, lang, `models/item/blackout_powder.json`, `textures/item/blackout_powder.png` (from 1.14.4), `data/randomthings/recipes/blackout_powder.json` (result key `id`) | `SpectreIlluminatorItem.useOn` / `SpectreIlluminatorEntity.interact` (`discard()`), `ItemEntity` + `addFreshEntity` from `RuneBaseBlock.onRemove` |
| AG6 Floo unloaded chunk | DONE | `FlooNetworkHandler.teleport` | `Level.hasChunkAt(BlockPos)` from `EnderBridgeTileEntity` / `RedstoneSignalHandler` |
| AG7 Floo duplicate name | DONE | `FlooNetworkHandler.hasFireplaceNamed`, `FlooSignItem`, lang `floo.info.duplicate_name`, test `behavior.floo.a_duplicate_sign_name_keeps_the_sign_and_the_bricks` | `displayClientMessage(Component, boolean)` from `EnderMailboxBlock`; test shape from the neighbouring Floo test |
| AG8 Stable Ender Pearl dimension | DONE | `StableEnderpearlItem` | `player.changeDimension(ServerLevel, SpectreTeleporter.INSTANCE)` after a `player.level() != destination` check, as in `SpectreHandler.teleportPlayerBack` (the teleporter builds no portal; its filler position is overwritten by the `connection.teleport` that follows) |
| AG9 Position Filter dimension | DONE | `PositionFilterItem.getDimension` (String; missing = `minecraft:overworld`), `AdvancedRedstoneInterfaceTileEntity.refreshTargets`, `RedstoneRemoteActivateMessage` | the dimension is stored as `level.dimension().location().toString()` (`PositionFilterItem.useOn`), compared with the same expression |
| AG10 Redstone Observer after load | DONE | `RedstoneObserverTileEntity` (`onLoad`, `refreshAfterLoad`), `RedstoneObserverBlock.tick` | `onLoad` override of `FlooBrickTileEntity`; `level.scheduleTick(pos, block, delay)` from `IgniterBlock`; `tick(BlockState, ServerLevel, BlockPos, RandomSource)` from `BeanStalkBlock` |
| AG11 Analog Emitter | DONE | `AnalogEmitterTileEntity.updateInput` (`getSignal(.., facing.getOpposite())`) | the same call with the other direction |
| Entity Detector | no change | test `behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2` added to `UtilityBehaviorTests` | the neighbouring detector tests; the truth table in `checkSupposedPoweredState` already returns `invert` for an empty Custom filter |

Tests/audits touched: `ResourceAudit` (`NO_RECIPE_EXPECTED` + `spectre_coil_number`, injected-table check + the two new chest tables), `ChecklistTests` (toggle test + the new pool), `UtilityBehaviorTests` (loot injection test + the new pool, entity detector test), `RemainingBehaviorTests` (Floo duplicate test). No count-based registry assertions were found.

## Unverified points / risks (round 2)
- AG3: `ModItems.LUMINOUS_POWDER` did not exist on this branch and was added as an `@ObjectHolder` copy of `SPECTRE_ANCHOR`; looping over every registered enchantment is the only enchanted check proven here (a stack with only the Magnetic enchantment also counts, as in 1.21.4). No test or ResourceAudit change (neither enumerates special recipes here).
- Nothing compiled. Highest-risk spots: the two new `RightClickBlock` listeners (`event.getEntity()`, `event.getHand()`, `event.setCanceled(true)` on a `PlayerInteractEvent.RightClickBlock`; only the ICancellableEvent method is proven on this branch, not this event; no `setCancellationResult`, so the client still sees `PASS` and may also send a use-item packet, harmless for Ectoplasm and paper); `changeDimension(ServerLevel, ITeleporter)` for the pearl (the same call shape as `SpectreHandler`, but it was never run on this branch's NeoForge version by this session); `NumberedSpectreCoilBlockItem.getName` (`Component.translatable(key, int)`).
- AG1: also converts with the item in the off hand (the event fires per hand). Already-Spectre saplings are skipped.
- AG4: the lang value is a format string (`Spectre Coil Nr. %d`); the number comes from `System.identityHashCode(stack)` so it changes when the stack is copied (as in 1.12.2). The coil needs an owner like the other tiers (set when placed), otherwise it does nothing. The energy value 128 is a constant. No `onlyFound` loot condition (it could never fire in chests in 1.12.2).
- AG5: settling (not yet illuminated) orbs are ignored, the powder is used up (the original did not).
- AG6: a closest-name match in an unloaded chunk blocks the teleport entirely instead of falling through to the next name.
- AG8: only the bound-player branch changes; unbound/random targets are unchanged.
- AG9: filters saved before this change have the dimension string already (the 1.20.6 item stored it from the start); a filter without one counts as the overworld.
- AG10: the power maps are still not saved; the observer recomputes one tick after load and only if the target's chunk is loaded.
