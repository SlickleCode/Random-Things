# Minecraft 1.16 (final release 1.16.5) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2020-06-23 ✔ |
| Final release | 1.16.5 — 2021-01-14 ✔ |
| Java | 8 (11 optional) |
| Forge | 36.2.42 ✔ (highest seen) |
| NeoForge | n/a |
| Fabric | ✔ |
| Mappings | Official Mojang mappings usable; MCP |
| Upstream mod status | No upstream branch ✔ |
| Tier | A — primary targets (large player base, loader support, reasonable tooling) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | L (≈0.6×) |

## Era

Nether update; long-lived modding version; Forge 36 stable.

## What changes (relevant to this mod)

- Dimensions, dimension types, biomes and worldgen become data-driven (JSON/codec) — `ModDimension` and `RegisterDimensionsEvent` approach is replaced.
- `BiomeLoadingEvent` (Forge) for adding features to biomes.
- `ActionResultType` replaces boolean returns for interaction; `TileEntity.read(BlockState, CompoundNBT)` signature.
- Mixin is available for mod authors (verify per Forge 36 docs) — would replace JS coremods.
- `MatrixStack`-based rendering from 1.15 continues.

## Impact on Random Things

- Spectre dimension: full redesign (data-driven dimension + chunk generator codec).
- Worldgen: Ancient Furnace, Glowing Mushroom, Lotus etc. use biome-loading event; Peace Candle church pool wrapper changes (pools are data/JSON-registry now).
- Chunk loading: tickets exist again (`ChunkManager`/`TicketType`).
- Coremods: still Forge JS coremods; consider switching to Mixin where possible.

## Source and path

This repo (forward port through 1.15.2).

## Verdict

High value (very popular version); recommended tier A.

## To verify before starting

- [ ] Mixin support in Forge 36.2.x.
- [ ] Which JS coremods still apply (target class names are largely the same, renderers changed).

## Work log

### 2026-10-11 - port started (branch `1.16.5`, plan: `../plans/1.16.5-port-plan.md`)

Toolchain findings (these cost real time; see also the pitfall catalog):
- **Forge 1.16.5-36.2.40 and newer cannot be used with ForgeGradle 4.1** (the dev client dies at model baking with
  `IllegalAccessError: TransformationMatrix.inverseVanilla()`): those builds renamed vanilla methods and rely on a
  coremod bouncer that the FG4 userdev binpatch does not match. The port uses **36.2.39** (last build before that
  change) with FG 4.1.x and Gradle 6.8.3. Moving to a later 1.16.5 build means moving to ForgeGradle 5/6 first.
- ForgeGradle 4.1 on Windows failed on the first run with `NoSuchFileException ... srg_to_snapshot_<mappings>.tsrg`; creating
  the missing parent directory under `~/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/<version>/`
  once fixes it. After a failed first run delete the generated `minecraft_user_repo/.../forge/1.16.5-*` folders so they are rebuilt.
- javac stops at 100 errors by default; `-Xmaxerrs 10000` in `build.gradle` gives the whole list.

API changes that mattered for this mod (all verified with `javap` on the mapped jar):
- `TileEntity.read(BlockState, CompoundNBT)`; `Block.onBlockActivated`, `Entity.processInitialInteract`, `Item.itemInteractionForEntity` return `ActionResultType`.
- Block `tick`/`randomTick` take `ServerWorld`; `IGrowable.grow` takes `ServerWorld`; `tickRate`, `getMaterial`, `getRenderLayer`, `causesSuffocation`, `lightValue(n)` are gone (render layers are `RenderTypeLookup.setRenderLayer`, light is `setLightLevel(state -> n)`, suffocation `setSuffocates`).
- Entities: `posX/posY/posZ` -> `getPosX()...`/`setRawPosition`; attributes are a static map registered on `EntityAttributeCreationEvent`; `doRender` -> `render(.., MatrixStack, IRenderTypeBuffer, light)`.
- Rendering: everything takes a `MatrixStack`; `GlStateManager` -> `RenderSystem`; block entity renderers register with `ClientRegistry.bindTileEntityRenderer(TileEntityType, ...)`; ISTER method is `func_239207_a_`.
- GUI: `Screen.render`, `blit`, `fill`, `drawString`, `renderTooltip`, `renderBackground` take a `MatrixStack`; titles and button labels are `ITextComponent`; `removed()` is `onClose()`; widget draw is `renderWidget`.
- Dimensions are data: `dimension_type` + `dimension` JSON plus a registered chunk-generator codec; teleporting uses `Entity.changeDimension(ServerWorld, ITeleporter)` (replaces the old Teleporter coremod).
- Biomes are data: biome keys are `RegistryKey<Biome>`, `BiomeDictionary` is keyed by them, features are registered `ConfiguredFeature`s added in `BiomeLoadingEvent`.
- Vanilla model parents were renamed with a `template_` prefix (`template_fire_floor`, `template_torch_wall`, ...).
- Jigsaw (village) pools are in the server's dynamic registry; wrap an entry at `FMLServerAboutToStartEvent`.
