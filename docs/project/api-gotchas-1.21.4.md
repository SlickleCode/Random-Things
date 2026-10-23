# 1.21.4 API gotchas (NeoForge 21.4.161)

Everything that changed from 1.21.1 to 1.21.4 and how this branch handles it. Verify against the patched jar (`build/neoForm/neoFormJoined1.21.4-*/steps/packRecomp/output.jar`), not `steps/decompile/output.jar`, which is source before NeoForge's patches (e.g. NeoForge's `LivingEntity.travelInAir` calls `BlockState.getFriction(LevelReader, BlockPos, Entity)`).

## Registration
- `Block.Properties` / `Item.Properties` need `setId(ResourceKey)` before construction. `lib/RTIds` holds a thread-local id, `RTRegistry.register(name, Supplier)` sets it around the constructor; every block/item is registered lazily and uses `RTIds.block()/blockCopy()/item()`. `EntityType.Builder.build(ResourceKey)`.
- `BlockEntityType` has a public `(supplier, Set<Block>)` constructor. Registry API: `lookupOrThrow`, `getValue`, `get(ResourceKey)` returns `Optional<Reference>`, `getOrThrow`.

## Interaction and blocks
- `InteractionResult` replaces `InteractionResultHolder` and `ItemInteractionResult` (`SUCCESS`, `FAIL`, `PASS`, `TRY_WITH_EMPTY_HAND`, `SUCCESS.heldItemTransformedTo(stack)`).
- `DirectionProperty` is `EnumProperty<Direction>`. `neighborChanged(..., Orientation, boolean)`. `updateShape(state, LevelReader, ScheduledTickAccess, pos, dir, neighborPos, neighborState, RandomSource)`. `getMinBuildHeight/getMaxBuildHeight` are `getMinY()/getMaxY()+1`.

## Entities
- `Entity.hurtServer(ServerLevel, DamageSource, float)` is abstract; `spawnAtLocation/kill/customServerAiStep/getBaseExperienceReward/dropItem` take a `ServerLevel`. `Mob.getLootTable()` is final (override `dropFromLootTable`). `EntitySpawnReason` replaces `MobSpawnType`. `TeleportTransition` + `entity.teleport(...)` replace `changeDimension`. `ThrowableItemProjectile` constructors take an `ItemStack`. `Boat` is `AbstractBoat`.
- Furnace fuel is per level (`FuelValues`): `util/FuelHelper`, `client/ClientFuel`.

## Items, armor, tools
- `ToolMaterial` is a record (tool constructors `(material, damage, speed, props)`); spectre reach is added by `ItemAttributeModifierEvent` (`SpectreItemTier.addReach`). `ArmorMaterial` is a record with a `ResourceKey<EquipmentAsset>`: JSON in `assets/<ns>/equipment/<name>.json`, textures in `textures/entity/equipment/humanoid/`. Repair is an item tag (`tags/item/spectre_repair.json`).

## Recipes (data)
- Ingredients are plain strings (`"mod:item"`, `"#tag"`), results `{"id","count"}`, custom ingredients use `"neoforge:ingredient_type"`. The slime balls tag is `c:slime_balls`. `Recipe` no longer has `getResultItem`/`getIngredients` (the client sees displays only): `debug/test/RecipeProbe` reads them for the tests (a components ingredient needs its components on the test stack). `CustomRecipe.Serializer`, no `canCraftInDimensions`.

## Client rendering
- Item models are `assets/<ns>/items/<id>.json` definitions; model `overrides`, `ItemProperties`, item colour handlers and BEWLR are gone. Custom range/condition properties, `ItemTintSource`s and the Portkey model are registered in `client/RTItemModels` through the NeoForge events (`RegisterRangeSelectItemModelPropertyEvent`, `RegisterConditionalItemModelPropertyEvent`, `RegisterColorHandlersEvent.ItemTintSources`, `RegisterItemModelsEvent`).
- Entity renderers are `EntityRenderer<T, S extends EntityRenderState>` (`createRenderState/extractRenderState/render(S, PoseStack, MultiBufferSource, int)`). Items in renderers: `ItemModelResolver.updateForNonLiving` + `ItemStackRenderState`.
- GUI: `GuiGraphics.blit(RenderType::guiTextured, ...)`; `CoreShaders.POSITION_TEX` replaces `GameRenderer::getXShader`; `Toast` interface; `AbstractSelectionList` scrollbar accessors.
- Rain and snow drawing and splash particles moved into `WeatherEffectRenderer` (private `getPrecipitationAt`); `LivingEntityRenderer.shouldShowName(T, double)`.

## Coremods that changed
- `SpawnPlacementTransformer` (descriptor `EntitySpawnReason`), `SuperLubricentBootsTransformer` (scans all `LivingEntity` methods for `BlockState.getFriction`), `ArmorLayerTransformer` (`EquipmentLayerRenderer.renderLayers`, after `getArmorLayerTintColor`), `ItemRendererTransformer` (now `ItemModelResolver.appendItemLayers`, calls `ClientAsmHandler.dyeItemLayers`), `WorldRendererTransformer` (target `WeatherEffectRenderer.getPrecipitationAt`), `MagicHoodTransformer` (descriptor `(LivingEntity;D)Z`).

## Server test setup
- `run/server.properties`: flat world with a full `generator-settings` JSON including `layers`, and `pause-when-empty-seconds=-1` (otherwise the server stops ticking and the suite never reports).
