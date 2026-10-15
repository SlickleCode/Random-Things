# API gotchas: Forge 1.16.5-36.2.39 (MCP `20210309`)

Counterpart of [api-gotchas-1.14.4.md](api-gotchas-1.14.4.md) for the `1.16.5` branch. Everything here was verified against the mapped jar with `javap` and,
where it matters, in the running game (see the `mapping-lookup` skill for jar locations). The general API diff (TileEntity/Block signatures, MatrixStack
rendering, data-driven dimensions/biomes) is in [../migration/versions/mc-1.16.md](../migration/versions/mc-1.16.md); this page holds the **non-obvious
behaviour changes that cost real debugging time**.

## Rendering

- **A neighbour's face is culled against any block whose `BlockState.isSolid()` is true** (`Block.shouldSideBeRendered` -> `BlockState.isSolid`), and
  `isSolid` is the `Block.Properties` flag that defaults to true - it has nothing to do with the render layer. 1.14.4 got "not solid" for free because
  `isSolid = blocksMovement && getRenderLayer() == SOLID`; 1.16 removed `getRenderLayer()`, so every see-through or partial block must set
  `Block.Properties.notSolid()` itself, otherwise blocks next to it show holes (the Fluid Display next to sand). The render layer is separate
  (`RenderLayerSetup` -> `RenderTypeLookup.setRenderLayer`). 28 blocks carry `notSolid()`; guarded by
  `behavior.render.see_through_blocks_are_not_solid_so_neighbours_keep_their_faces`.
- **Enchant glint is drawn with `POSITION_TEX` render types** (`RenderType.getGlint/getEntityGlint/getArmorGlint/...`), so the glint colour is the **GL colour
  at draw time**, not a vertex colour, and glint layers are batched per render type. A per-item colour therefore needs its own `RenderType`
  (`TintedGlintRenderType` wraps the vanilla type and sets `RenderSystem.color4f` in its setup/clear). The glint texture is already purple; the tint multiplies
  it, so colours are normalised to full brightness.
- **Every custom `RenderType` used through the main buffer source needs its own `BufferBuilder` in `IRenderTypeBuffer.Impl.fixedBuffers`.** An item draws its
  normal and glint layers through one delegating vertex builder; a type without a dedicated buffer shares the source's single fallback builder, so asking for
  the second layer ends the first and the game crashes with `IllegalStateException: BufferBuilder not started` (seen on a Portkey in the hotbar).
  `fixedBuffers` is protected, so it is found by reflection on its generic type.
- Hooks for the glint are coremods: `RenderTypeTransformer` (the seven glint getters), `ItemRendererTransformer` (stack capture in
  `renderItem(ItemStack, TransformType, boolean, MatrixStack, IRenderTypeBuffer, int, int, IBakedModel)`), `ArmorLayerTransformer` (stack capture in
  `BipedArmorLayer.func_241739_a_`); see [coremods.md](coremods.md).
- Custom `TileEntityRenderer`s: constructor takes the dispatcher, `render(te, partialTicks, MatrixStack, IRenderTypeBuffer, light, overlay)`; hand-built
  geometry must restore GL state itself.

## Registries and world data

- **A world's biomes are data-pack instances, not the objects in `ForgeRegistries.BIOMES`.** `world.getBiome(pos)` returns an instance decoded from the world's
  dynamic registry, so `ForgeRegistries.BIOMES.getKey(biome)` returns **null** and `biome == ForgeRegistries.BIOMES.getValue(...)` is never true (the Biome Radar
  threw an NPE in `tick` and never found its biome). Resolve names through `((ServerWorld) world).func_241828_r().getRegistry(Registry.BIOME_KEY)`
  (client: `ClientWorld.func_241828_r()`); compare by `ResourceLocation`/`RegistryKey`. `AncientFurnaceTileEntity` and `BiomeRadarTileEntity` do this.
- **Dimensions in NBT/tooltips are registry-key strings** (`"minecraft:overworld"`, from `world.getDimensionKey().getLocation()`), not the 1.14 int id. Item tags
  such as the Portkey/Position Filter `dimension` are `putString`; scripts and tests must write `dimension:"minecraft:overworld"`.
- Entity attribute NBT names are `generic.max_health` etc. (1.14: `generic.maxHealth`); `Attributes` for custom entities are registered on
  `EntityAttributeCreationEvent`.
- Commands: structure names for `/locate` are lower case (`igloo`); `/locatebiome <biome>` exists (approximate - sampled on a coarse grid, biome edges are
  fuzzy). Command-block "enable" and permission levels behave as in vanilla; a world created without cheats rejects every slash command, including `/say`.
- `IServerWorldInfo` (not `IWorldInfo`) has `setThundering`; weather/time helpers used by the test world setup (`RTTestRunner.controlWorld`) go through
  `GameRules.DO_DAYLIGHT_CYCLE.set(false, server)` etc.
- Worldgen: features are `ConfiguredFeature`s added in `BiomeLoadingEvent`; jigsaw pools live in the server's dynamic registry (wrapped at
  `FMLServerAboutToStartEvent`, see `PeaceCandleChurchPool`).

## Coremods and names

- **Dev vs production method names:** at dev runtime a method that has an MCP name shows that name (`renderItem`), methods without one keep `func_...`; a
  production jar always has SRG. Transformers must match **both** (`"renderItem" || "func_229111_a_"`) and use `net/minecraft/...` class names in
  descriptors. Do not inject an `INVOKEVIRTUAL` of a vanilla method by its SRG name (`NoSuchMethodError` in dev): add a Java static helper
  (`ClientAsmHandler.setRenderingStackFromSlot`) and inject an `INVOKESTATIC` of it.
- **A JS transformer that inserts before a `RETURN` inside a `for` loop must step past the inserted instruction**, otherwise it re-matches the same node
  forever and the client hangs at startup right after "Developer tools enabled" (`jstack` shows `initializeCoreMod#transformer(<eval>:N)`).
- Forge 1.16.5-36.2.40 and newer cannot be used with ForgeGradle 4.1 (see [mc-1.16.md](../migration/versions/mc-1.16.md)); the port stays on 36.2.39.

## Testing and tooling

- The window title is `Minecraft* 1.16.5` (dev) - the helper matches on "Minecraft". The create-world screen differs from 1.14.4 (game mode button at
  (266,219); picking **Creative also turns cheats on**; More World Options at (586,389)); on the production client click the mode button slowly. `SCN_MC=1.16`
  selects all of this in `scn.py`.
- `server.properties` in `prod-test/1.16.5/` must say `level-name=RTServerTest`, `level-type=flat` for the production server suite (it was left pointing at the
  `RealGen` world once, which produces no report).
- `ParticleProbe` (dev client) counts `SpellParticle`s via reflection on `ParticleManager`'s type-to-queue map, found by generic type because the field name
  is obfuscated.
