# 1.18.2 API gotchas (Forge 40.3.12, Mojang names)

Things that cost debugging time during the 1.16.5 -> 1.18.2 port (2026-10-15). Mapping/tool details: `docs/migration/plans/1.18.2-port-plan.md`, `docs/migration/tools/README.md`, skill `mapping-lookup`.

| Area | Gotcha | What to do |
| --- | --- | --- |
| Names | Mojang names everywhere in source; production members are SRG ids (`m_`/`f_`) | transformers and `ObfuscationReflectionHelper` use the id (+ Mojang name for dev); `docs/migration/tools/lookup.py` finds it |
| Block entities | constructor is `(pos, state)`; ticking is `EntityBlock.getTicker`, not an interface; `save`/`load` -> `saveAdditional(tag)` / `load(tag)` | `util/Tickers.of(...)`; sync tag: override `getUpdateTag()` to `saveWithoutMetadata()` |
| Update packets | `ClientboundBlockEntityDataPacket.getTag()` is **null** for an empty tag | load an empty `CompoundTag` instead (8 overrides do) |
| Events | `TickEvent.WorldTickEvent` is also posted for client levels | guard `event.world.isClientSide` before casting to `ServerLevel` |
| Rendering | all fixed-function GL is gone: a vertex format needs its matching shader (`POSITION_TEX_COLOR` -> `getPositionTexColorShader`, `POSITION_COLOR` -> `getPositionColorShader`, lines -> `getRendertypeLinesShader` + normals) or colours are ignored | set the shader with `RenderSystem.setShader` before drawing |
| Renderers | block-entity / entity renderers take a `Context` and are registered on `EntityRenderersEvent.RegisterRenderers`; `BlockEntityRenderer` is an interface | see `ClientSetup.registerRenderers` |
| Item renderers | `setISTER` is gone: `Item.initializeClient(Consumer<IItemRenderProperties>)` | `PortkeyItem` |
| Break particles | `addDestroyEffects` moved to `IBlockRenderProperties` (via `Block.initializeClient`) | `DiaphanousBlock` |
| Tool types | `ToolType`/`harvestTool` removed | `data/minecraft/tags/blocks/mineable/{shovel,axe}.json` |
| Biomes | lookups return `Holder<Biome>`; `Biome.getTemperature(pos)` is private (use `warmEnoughToRain(pos)`); chunk biomes live in each section's `PalettedContainer` (4x4x4 cells) | `AncientFurnaceTileEntity.setBiomeColumn`, `AsmHandler.getBiomeForRainRender` returns a `Holder` |
| Worldgen | features are `PlacedFeature`s (`RarityFilter`, `InSquarePlacement`, `HeightmapPlacement`, `BiomeFilter`) registered in `BuiltinRegistries`; `Feature.place(FeaturePlaceContext)` | `worldgen/ModFeatures` |
| Height | world is -64..320: never hard-code 0/255/256 | `level.getMinBuildHeight()/getMaxBuildHeight()`; Glowing Mushroom keeps the original roll from y 0 on purpose (4x rarer otherwise) |
| Dimension | `DimensionType` constructor is private | `data/randomthings/dimension_type/spectre.json` (min_y 0, height 256) + chunk generator codec registered in code; `ModDimensions.addToServer` still injects the `LevelStem` |
| Frozen registries | registries are frozen when the server starts | swap `Holder.Reference.value` (SRG `f_205752_`) by reflection (`PeaceCandleChurchPool`) |
| Jigsaw | pool elements must carry processor-list holders from the *world's* registry or the assembler rejects them ("not valid in current registry set") | build the piece per installed pool (`PeaceCandleChurchPool`) |
| Villages in tests | a hand-built `JigsawPlacement.addPieces` call produced no houses (cause unknown) | use `ConfiguredStructureFeature.generate` for `village_plains` |
| Generation in tests | steps are futures; `StructureFeatureManager`/`Blender`/carvers query chunks 7-8 chunks out and the real manager blocks on the server thread | wide placeholder regions + a structure manager that knows no structures (`WorldgenTests`) |
| SavedData | no `super(id)` constructor; `computeIfAbsent(loader, creator, id)` | static `loadFrom(CompoundTag)` + instance `readData` |
| Containers | `AbstractContainerMenu.clicked` returns void; `getCarried()` is on the menu | `NotificationInterfaceContainer` |
| Screens | no `Screen.buttons`/`addButton`: `addRenderableWidget`, `children()`; list entries need `getNarration()`, widgets `updateNarration` | `client/screen/ScreenWidgets` |
| Items | durability bar: `isBarVisible`/`getBarWidth` (0..13)/`getBarColor`; item property functions take a 4th `seed` argument | |
| Friction | `Block.getSlipperiness` -> `getFriction` (Forge) | coremod anchor in `LivingEntity.travel` is `BlockState.getFriction` |
| Entities | `Entity.remove()` -> `discard()`; `yRot/xRot/inventory/abilities` are accessors | |
| Level | `setBlock(pos, state)` 2-arg is `setBlockAndUpdate`; `getEntities(Class, AABB, pred)` is `getEntitiesOfClass`; server `getAllEntities()` | the name-based remap picked the wrong overload for these |
| Item pickup/fluids | `BucketPickup.takeLiquid` -> `pickupBlock` returns the bucket `ItemStack` | read the fluid from `state.getFluidState()` first |
| JS coremods | the sandbox blocks `Java.type('java.lang.System')` since 1.18.2 | only ASM/coremod API classes |
