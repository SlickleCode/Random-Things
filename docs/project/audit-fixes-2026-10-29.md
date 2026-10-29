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

---

# Round 2 (same day): missing features and more bug fixes, branch 1.18.2

Mirrored from the 1.14.4 commit `45eee77b`, again with no compiler, no jars and no network: **nothing was compiled or run**; only `syntaxcheck.sh` and JSON validation. Every call was copied from existing code on this branch (pattern in brackets). Checklist rows AG1-AG11, section H of `TESTING_CHECKLIST.md`.

## Per item (all DONE)

- **AG1 Spectre Sapling.** New `RightClickBlock` listener in `RandomThings`: Ectoplasm in hand, clicked state `is(TagKey<Block> minecraft:saplings)` (pattern: `DiviningRodItem.matches`), not already a Spectre Sapling; server shrinks one (not `getAbilities().instabuild`) and `setBlockAndUpdate`s `SPECTRE_SAPLING`; event cancelled.
- **AG2 Runic Pattern.** Second listener: paper on `RUNE_BASE` with a `RuneBaseTileEntity` (server only); `RunePatternItem.setPattern(stack, getRuneData())`; a single paper is replaced via `setItemInHand` (pattern: `RTDebugCommand`), otherwise `shrink(1)` and `getInventory().add` (pattern: `ImbueItem`), `drop(stack, false)` if full (pattern: `EscapeRopeHandler`).
- **AG3 Luminous Powder.** New `recipes/LuminousPowderRecipe` (`CustomRecipe` + `SimpleRecipeSerializer`, shape of `DiaphanousInvertRecipe`), registered as `luminous_powder`, data recipe `luminous_powder_glint.json`, `ModItems.LUMINOUS_POWDER`, yellow tooltip line at index 1 in `ClientSetup`, lang `tooltip.randomthings.luminous_enchantment`. Flag is written with `putBoolean` (a byte tag, proven in `DiaphanousInvertRecipe`). The glow rendering is NOT done.
- **AG4 Numbered Spectre Coil.** `CoilType.NUMBER("number", 128, 0x00FF00)`, `ModBlocks.SPECTRE_COIL_NUMBER`, added to the `spectre_coil` block entity type, `SpectreCoilTileEntity.tick` pushes `type.rate` directly (like Genesis, no pool, owner still required), `item/block/NumberedSpectreCoilBlockItem` (name "Spectre Coil Nr. N", override of `getName(ItemStack)` as in `BiomeCrystalItem`/`DiaphanousBlockItem`), `LootHandler` (+ `ABANDONED_MINESHAFT`, `END_CITY_TREASURE`; weights 10 / 8 / 30), `RTConfig.NUMBERED_SPECTRE_COILS` (`loot.NumberedSpectreCoils`, default true), blockstate / item model / block loot table cloned from `spectre_coil_genesis`, lang `block.randomthings.spectre_coil_number`. No recipe; the creative tab entry comes from the item's `Item.Properties.tab`. This branch has no `assets/<mod>/items/*.json` definitions.
- **AG5 Blackout Powder.** New `item/BlackoutPowderItem` (`useOn`): finds the illuminated orb of the clicked chunk in `SpectreIlluminatorEntity.ILLUMINATORS` (pattern: `SpectreIlluminatorItem`), `discard()`s it (relights via `onRemovedFromWorld`), drops a Spectre Illuminator `ItemEntity` next to the clicked face, shrinks one powder unless creative. Item model, texture (from 1.14.4), lang, shaped recipe `blackout_powder.json` (`DG` / ` M`, yields 4).
- **AG6 Floo unloaded chunk.** `FlooNetworkHandler.teleport` returns false before reading the block entity when `!world.hasChunkAt(targetPos)` (pattern: `EnderBridgeTileEntity`).
- **AG7 Floo duplicate name.** `FlooNetworkHandler.hasFireplaceNamed`; `FlooSignItem` sends `displayClientMessage(new TranslatableComponent("floo.info.duplicate_name", name), true)` (pattern: `EnderMailboxBlock`); lang key added. The sign was already not consumed.
- **AG8 Stable Ender Pearl.** If `player.level != entityItem.level`, `player.changeDimension((ServerLevel) entityItem.level, SpectreTeleporter.INSTANCE)` before `connection.teleport` (pattern: `SpectreHandler.teleportPlayerBack`). `SpectreTeleporter` is used because it never searches or builds a portal; its filler position is overwritten right after. Removed the outdated "disclosed simplification" javadoc paragraph.
- **AG9 Position Filter dimension.** On this branch the dimension is a string (`"minecraft:overworld"`), not an id: new `PositionFilterItem.getDimension` (null without a position, `minecraft:overworld` when the tag has none); `AdvancedRedstoneInterfaceTileEntity.refreshTargets` and `RedstoneRemoteActivateMessage` compare it with `level.dimension().location().toString()` (pattern: `RandomThings` Portkey check, `PositionFilterItem.useOn`).
- **AG10 Redstone Observer.** Differs from 1.14.4 on purpose: no scheduled tick from `onLoad`. A flag `refreshPending` (true on every new instance) plus a block entity ticker (`RedstoneObserverBlock.getTicker`, `Tickers.of`, pattern `ItemCollectorBlock`) calls `updateRedstoneState()` once the target chunk is loaded (`hasChunkAt`), then stops. Reason: in 1.18.2 the tick containers of a chunk may not be registered yet while its block entities load, and scheduling there is not proven in this code base.
- **AG11 Analog Emitter.** `updateInput` queries `getSignal(pos.relative(facing), facing.getOpposite())`.
- **Entity Detector.** No code change (same truth table as the original); added the test `behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2`.

## Tests and audits touched

- `ResourceAudit`: `spectre_coil_number` in `NO_RECIPE_EXPECTED`; the injected-table check also lists the mineshaft and end city tables.
- `UtilityBehaviorTests`: loot-injection test checks the end city pool; new entity detector test. `ChecklistTests`: toggle test for `NumberedSpectreCoils`. `RemainingBehaviorTests`: `behavior.floo.a_duplicate_sign_name_keeps_the_sign_and_the_bricks`. None run.
- No tests for AG1/AG2/AG5/AG8/AG9/AG10/AG11 (needs item-in-hand clicks, two dimensions or save and reload; no pattern exists).

## Unverified points

- Not compiled. New calls: `TagKey.create(Registry.BLOCK_REGISTRY, ...)` with `BlockState.is` (as in `DiviningRodItem`), `Level.hasChunkAt(BlockPos)`, `Player.displayClientMessage`, `ServerPlayer.changeDimension(ServerLevel, ITeleporter)`.
- AG8: `changeDimension` with `SpectreTeleporter` was only used for the Spectre dimension so far; the player is moved to the filler position (8.5, 1, 8.5) of the target dimension, loading that chunk, before being teleported to the pearl.
- AG10: the ticker runs for every Observer every tick (a boolean check); if the target chunk never loads it keeps polling.
- AG4: the item name comes from `System.identityHashCode(stack)`, so it can change between stacks / after a copy (as in 1.12.2).
- Not done: the glow-in-the-dark Luminous glint rendering; Peace Candle churches; the 1.12.2 `onlyFound` loot condition of the coils (never true in chests).
