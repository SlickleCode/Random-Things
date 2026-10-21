# NeoForge 20.6.141 / Minecraft 1.20.6 - API surprises

Everything here cost time on the 1.20.1 -> 1.20.6 port (2026-10-20). Names are Mojang names **in production too**: NeoForge 20.5+ no longer reobfuscates, so there is no `reobfJar`, no `m_`/`f_` ids at runtime and `ObfuscationReflectionHelper` takes plain names (an SRG id such as `"f_205752_"` throws `UnableToFindFieldException` in dev, and would in a real install).

## Toolchain
- NeoGradle `net.neoforged.gradle.userdev` 7.0.192 needs **Gradle 8.13** (8.8 fails with "No matching variant ... plugin.api-version 8.13") and a **JDK 21** toolchain (`~/.jdks/temurin-21`, found by `env/activate.sh`).
- `runs { client { systemProperty ..., jvmArgument ..., modSource sourceSets.main } }`; `META-INF/neoforge.mods.toml` with explicit `neoforge`/`minecraft` dependency tables; the mod class gets `(IEventBus, ModContainer)` injected (`FMLJavaModLoadingContext` is deprecated).
- Decompiled/patched Minecraft sources for grepping: unzip `build/neoForm/neoFormJoined1.20.6-*/steps/patchUserDev/output.jar`; NeoForge's own sources are the `neoforge-20.6.141-sources.jar` in the Gradle cache. `javap` on `steps/packRecomp/output.jar` gives exact descriptors for coremod anchors.

## Registries
- `@ObjectHolder`, `IForgeRegistry` and `ForgeRegistries` are gone. `lib/ObjectHolder` + `lib/ObjectHolderInjector` fill the static `ModBlocks.X` ... fields at the end of each registry's `RegisterEvent` (a `Holder<T>` field gets the holder); `lib/RTRegistry<T>` (`register(name, value)` inside `RegisterEvent`) keeps the old `ModXxx.registerXxx(registry)` shape. Vanilla lookups: `BuiltInRegistries.ITEM.get(id)` etc.
- Everything that registers into a registry must do it in `RegisterEvent` (chunk generator codec, loot function type, data component types): doing it in `FMLCommonSetupEvent.enqueueWork` throws "Registry is already frozen".
- Mob effects are `Holder<MobEffect>` (`hasEffect`, `new MobEffectInstance(holder, ...)`); `ModEffects.IMBUE_*` are holders. Armor materials are `Holder<ArmorMaterial>` (a direct holder around a copy of Chain/Iron with an own texture layer works, no registration needed; the texture is `textures/models/armor/<layer>_layer_1.png`).
- Enchantments are still Java objects: `Enchantment.definition(supportedItemsTag, weight, maxLevel, minCost, maxCost, anvilCost, slots...)`.

## Item data
- Item NBT is gone. `util/StackTag` keeps everything under the same keys in `minecraft:custom_data`; `get`/`getOrCreate` return **copies** - change a stack with `StackTag.edit(stack, tag -> ...)` or `StackTag.set`. `getTag().put(...)` patterns silently lose the write.
- Vanilla things that used to be NBT keys are components now: `Unbreakable`, dyed colour (`DYED_COLOR`), custom name (`CUSTOM_NAME`), potion (`POTION_CONTENTS`, `util/PotionHelper`), enchantments (`stack.enchant(enchantment, level)`), block state of a block item (`minecraft:block_state`, used by the Colored Grass loot table).
- Stacks need a registry provider to (de)serialize: `StackTag.saveStack` / `loadStack`, `StackTag.provider()` (server access, else the client level's). Item handlers: `serializeNBT(provider)` / `deserializeNBT(provider, tag)`.
- The Ender Bucket / Reinforced Ender Bucket store their fluid in the typed component `lib/ModDataComponents.FLUID` (`SimpleFluidContent`); the fluid handler is registered in `lib/ModCapabilities`.
- `Item.appendHoverText` takes `Item.TooltipContext` (no level in 1.20.6): tooltip code reads the client level through `client/ClientHooks.level()`.

## Capabilities and menus
- `getCapability` overrides and `LazyOptional` are gone. Providers are registered per block entity type / item in `lib/ModCapabilities` (`RegisterCapabilitiesEvent`); consumers use `util/CapUtil.itemHandler(be, side)` (= `level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side)`, null when absent). `EmptyHandler` and `IFluidBlock` no longer exist.
- Menus: `IMenuTypeExtension.create(factory)`, open with `serverPlayer.openMenu(provider[, pos or buf writer])` (`NetworkHooks` is gone); screens register in `RegisterMenuScreensEvent`.
- Entity spawn packets need no override any more (delete `getAddEntityPacket`); `defineSynchedData(SynchedEntityData.Builder)`.

## Networking
- `SimpleChannel` -> `CustomPacketPayload`. `network/IRTMessage` extends the payload interface and keeps `read/write(FriendlyByteBuf)`; `RTPacketHandler` builds one `Type` + `StreamCodec` per message class (reflection constructor) and registers them in `RegisterPayloadHandlersEvent` (`playBidirectional`; handlers run on the main thread). The sender is `(ServerPlayer) ctx.player()`. Items inside a message: `ItemStack.OPTIONAL_STREAM_CODEC` with a `RegistryFriendlyByteBuf`.
- `EclipsedClockAnimationMessage` was never registered in the old `SimpleChannel` list; it is registered now.

## Blocks, block entities, saved data
- `Block.use` split in two: `useWithoutItem(state, level, pos, player, hit)` and `useItemOn(stack, state, level, pos, player, hand, hit)` returning `ItemInteractionResult` (empty hands still reach `useItemOn`; `PASS_TO_DEFAULT_BLOCK_INTERACTION` falls through to `useWithoutItem`). `debug/BlockUse` reproduces the dispatch for tests.
- `BlockEntity.saveAdditional(tag, registries)` / `loadAdditional(tag, registries)`, `getUpdateTag(registries)`, `onDataPacket(net, pkt, registries)` (the default applies the tag - delete plain forwarders). `NbtUtils.writeBlockPos` writes an int array now: `util/BlockPosNbt` keeps the old `{X,Y,Z}` compound (saved worlds from 1.20.1 still load). `NbtUtils.loadUUID` needs the `IntArrayTag` (`compound.get(key)`, not `getCompound`).
- `SavedData`: `save(tag, registries)`, factory `new SavedData.Factory<>(X::new, X::loadFrom, null)` with `loadFrom(tag, registries)`.
- `BushBlock` and friends need `codec()` (`simpleCodec(p -> new X())`); `Properties.copy(b)` is `ofFullCopy(b)`; `BlockBehaviour` methods called on another block (`Blocks.WALL_TORCH.canSurvive`) are protected: go through a `BlockState`.
- `AABB(BlockPos, BlockPos)` is gone: `util/AABBs.of`.

## Data pack
- Recipe result key `item` -> `id`; tag ingredients `forge:*` -> `c:*` (`c:stones`, `c:cobblestones`, `c:glass_blocks`, `c:ingots/iron`, ...); `forge:nbt` ingredient -> `neoforge:components` (`items`, `components`, optional `strict`); loot `set_nbt` -> `set_components`; biome modifiers live in `data/<ns>/neoforge/biome_modifier` with type `neoforge:add_features`; global loot modifiers in `data/neoforge/loot_modifiers/`; dimension type `monster_spawn_light_level` is a flat int provider (`min_inclusive`/`max_inclusive` next to `type`); `pack_format` 32. `BiomeUtil` maps the old BiomeDictionary names to `minecraft:is_*` / `c:is_*` tags.
- Custom recipes (`CustomRecipe`): the constructor takes only the `CraftingBookCategory`; the id lives in the `RecipeHolder` (`holder.id()`, `holder.value()`); `Recipe.assemble(container, HolderLookup.Provider)`; 1.20.6 still uses `CraftingContainer` (`CraftingInput` is 1.21).
- Loot functions: `LootItemFunctionType<T>(MapCodec<T>)`, conditional functions take `List<LootItemCondition>`; `LootModifier` keeps the `LootItemCondition[]` constructor but its codec is a `MapCodec`; the loot-table listing is `server.reloadableRegistries().getKeys(Registries.LOOT_TABLE)`.

## Client
- `AbstractSelectionList` is a widget with `getX()/getY()/getRight()/getBottom()` and `renderWidget`; the old `x0/y0/x1/y1`, `setLeftPos`, `setRenderSelection` are gone (`SoundNameList`, `ChunkAnalyzerScanResultList`). `AbstractContainerScreen.render` already draws the background: remove manual `renderBackground(graphics)` calls (they double it). `EditBox.tick()` is gone; `mouseScrolled` has two scroll amounts; toast backgrounds are GUI sprites (`toast/system`).
- `ClientTickEvent.Post`, `LevelTickEvent.Post`, `ServerTickEvent.Post`, `PlayerTickEvent.Post` replace `TickEvent`; `LivingTickEvent` is `EntityTickEvent.Pre/Post`; `EntityItemPickupEvent` is `ItemEntityPickupEvent.Pre` (`setCanPickup(TriState.FALSE)`); `PotionColorCalculationEvent` is `EffectParticleModificationEvent` (`setVisible(false)`); `PlayerEvent.Clone` and `PlaySoundEvent` are no longer cancellable.

- `BlockBehaviour.randomTick` is empty by default (it used to call `tick`): blocks that relied on that must override `randomTick`. Screens must reset `RenderSystem.setShaderColor` after tinted blits (slot items are drawn afterwards and pick the colour up).

## Coremods (JS transformers)
- All 14 still load on NeoForge 20.6 and match Mojang names; anchors verified with `javap`: `renderBatched`, `getLightEmission`, `isRainingAt`, `shouldShowName(LivingEntity)`, the seven `RenderType` glint getters, `ItemRenderer.render/renderQuadList`, `LevelRenderer.renderSnowAndRain/tickRain`, `SpawnPlacements.checkSpawnRules`.
- `HumanoidArmorLayer`: the dyed colour is now the three float arguments of the private `renderModel(PoseStack, MultiBufferSource, int, Model, float, float, float, ResourceLocation)`; `ArmorLayerTransformer` patches that method's entry (`ClientAsmHandler.armorTintChannel`). The old `DyeableLeatherItem.getColor` anchor no longer exists and would have silently done nothing.
- `ItemRenderer.renderQuadList`: the colour int (local 11) is first read by `FastColor.ARGB32.alpha(i)` now (it used to be `i >> 16`); the rtDye patch anchors on that. A transformer whose anchor vanished does nothing and logs nothing - the scripted playtest found this one.
- `FireBlock.tryCatchFire` is `checkBurnOut` (the old name never matched on the Mojang-named branches 1.18.2-1.20.1, so Blazing Fire's catch-chance tweak did not apply there and the test skipped; fixed here, worth mirroring back).
