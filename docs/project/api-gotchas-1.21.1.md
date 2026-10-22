# 1.21.1 API notes (NeoForge 21.1.264)

What changed from 1.20.6 and how this branch handles it. Companion to `api-gotchas-1.20.6.md` (data components, `StackTag`, registries, payloads all still apply).

## Toolchain
- NeoForge `21.1.264`, NeoGradle 7.0.192 / Gradle 8.13 / JDK 21 as on 1.20.6; `implementation 'net.neoforged:neoforge:21.1.264'`, `neoforge.mods.toml` ranges `[21.1.264,)` / `[1.21.1]`.
- Mapped classes for `javap`: `build/neoForm/neoFormJoined1.21.1-*/steps/packRecomp/output.jar` (NeoForge classes: the `neoforge-21.1.264-universal.jar` in the Gradle cache). Vanilla data (tags, enchantment JSON) is in `.gradle/caches/minecraft/versions/1.21.1/client.jar`.
- First client start in a fresh `run/` sits on the accessibility onboarding screen forever: the client autotest needs `run/options.txt` with `onboardAccessibility:false` (copy from another branch's `run/options.txt`).

## Code changes
- `ResourceLocation` constructors are private: `ResourceLocation.parse("ns:path")` / `fromNamespaceAndPath(ns, path)`. Converted mechanically (about 125 call sites).
- Rendering: `Tesselator.getInstance().begin(mode, format)` returns the `BufferBuilder`; vertices are `addVertex`/`setColor`/`setUv`/`setUv1` (overlay)/`setUv2` (light)/`setNormal`, there is no `endVertex()`; upload with `BufferUploader.drawWithShader(builder.buildOrThrow())`. `VertexConsumer` implementors (`AlphaVertexBuilder`) override the new names. `MultiBufferSource.BufferSource.fixedBuffers` holds `ByteBufferBuilder`s (`TintedGlintRenderType` registers `new ByteBufferBuilder(256)`; match the field by `ByteBufferBuilder`, the sibling `startedBuilders` map is `BufferBuilder`).
- `DyeColor.getTextureDiffuseColors()` (float[]) became `getTextureDiffuseColor()` (int): `DyeUtil.diffuse(color)` gives the float triple back.
- Enchantments are data-driven: Magnetic is `data/randomthings/enchantment/magnetic.json` (no effects; `MagneticLootModifier` does the pickup) plus optional entries in the vanilla tags `in_enchanting_table`, `on_random_loot`, `tradeable`. Code only holds the `ResourceKey` and looks the `Holder` up (`ModEnchantments.holder(registryAccess, MAGNETIC)`). **Divergence:** the `MagneticEnchantment` config toggle now only disables the effect (the loot modifier checks it); the enchantment itself is always obtainable.
- `IPlantable`/`PlantType` are gone: `IBlockExtension.canSustainPlant(state, level, pos, facing, plantState)` returns a `TriState`. `FertilizedDirtBlock` derives the old plant types from the plant block (crops/stems tilled, desert plants untilled, otherwise plains); Lotus seeds and the Spectre sapling ask the soil with their own block state and fall back to dirt/farmland on `DEFAULT`.
- `Entity.changeDimension(DimensionTransition)`: `SpectreTeleporter.transition(entity, level)` builds the filler transition (the handler overwrites the position afterwards).
- `HangingEntity` needs `calculateBoundingBox(pos, direction)` instead of `getWidth/getHeight` (Eclipsed Clock uses the Item Frame box, 12x12 px). `Mob.getExperienceReward()` -> `getBaseExperienceReward()`; `dropCustomDeathLoot(ServerLevel, DamageSource, boolean)`; `onRemovedFromWorld` -> `onRemovedFromLevel`; `Item.getUseDuration(ItemStack, LivingEntity)`.
- Recipes use `CraftingInput` (`matches(CraftingInput, Level)`, `assemble(CraftingInput, HolderLookup.Provider)`, `size()`); tests convert with `CraftingContainer.asCraftInput()`.
- `AttributeModifier(ResourceLocation id, amount, operation)` (no UUID/name); `ModelResourceLocation.standalone(rl)` for extra baked models (`ModelEvent.RegisterAdditional`, `ModelManager.getModel`); `ToolActions` -> `ItemAbilities` (`BlockToolModificationEvent.getItemAbility()`); `LivingHurtEvent` is gone: the imbue effects listen to `LivingIncomingDamageEvent` (cancelable, fires before armour).
- `EquipmentSlot.Type.ARMOR` -> `HUMANOID_ARMOR`; `Mob.getEquipmentSlotForItem` is an instance method (use `stack.getEquipmentSlot()` / `Equipable.get(stack)`).
- `ChunkGenerator.fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess)` has no executor; `SinglePoolElement.place(..., LiquidSettings, boolean)`.

## Data pack
- Folders are singular: `recipe/`, `loot_table/`, `structure/`, `tags/block/` (`ResourceAudit` lists `recipe`). `pack_format` 34.

## Coremods (all 14 still apply; one needed work)
- `ArmorLayerTransformer`: armor colour is an ARGB **int** (local 5) of the private `renderModel(PoseStack, MultiBufferSource, int, Model, int, ResourceLocation)`; `ClientAsmHandler.armorTintColor(int)` replaces it with the `rtDye` colour. NeoForge 21 renders through the 12-argument `renderArmorPiece` overload (the 6-argument one only forwards), so the rendering-stack hook is patched into **both** overloads; with only the 6-argument one patched the stack was empty at draw time and Dyeing-Machine armor drew untinted - no suite noticed, only the `armor_and_items` render scene.
- `ItemRendererTransformer` (`renderQuadList`, `ILOAD 11` then `ARGB32.alpha`) still matches unchanged.

## Tests
- `WorldgenTests.generateTerrainAndCount` (hand-driven in-memory overworld generation for the glowing-mushroom test) is **not ported**: 1.21 replaced the chunk pipeline (`ChunkStep`, `StaticCache2D<GenerationChunkHolder>`, `ProtoChunk.setPersistedStatus`). That one test skips with a message; the registration test for the mushroom feature still runs.
