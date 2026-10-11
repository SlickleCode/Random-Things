# 03 — Version notes: 1.12.2 → 1.14.4 (Forge 28.2.26)

**This document is version-specific on purpose.** It records what was actually found, verified with `javap`
against the mapped jar `forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3`. Use it (a) as a worked
example of *what kinds of things change* and (b) as a starting list of things to re-check for any new target.
Do not assume any row holds for another build; early builds of a version differ from later ones and from
online guides.

## 1. Structural differences (the shape of the work)

| Area | 1.12.2 | 1.14.4 | Consequence for the port |
| --- | --- | --- | --- |
| Item variants | Metadata / damage value sub-items | No metadata; every variant is its own item/block | One class per variant or a loop-registered family (16 colors via a `DyeColor` loop); all `"data": n` in JSON must be mapped |
| Ore dictionary | `forge:ore_dict` ingredients | Tags (`forge:stone`, `forge:dyes/*`, `forge:glass/colorless`...) or plain vanilla items | Recipes rewritten; compat via other mods dropped |
| Registration | `GameRegistry`, instance fields | `@ObjectHolder` classes + `RegistryEvent.Register` | Non-registry statics in holder classes crash |
| Block states | Block with metadata + `IBlockState` | Properties on the state; flattened vanilla blocks | Multiple variants → properties or discrete blocks |
| Models | Forge blockstate JSON (`forge_marker`), `ExtendedBlockState`, `IBakedModel` hacks | Vanilla blockstate/model JSON; no unlisted properties | Per-instance rendering → tile entity renderer + invisible block |
| Tile entity ticking | `ITickable` | `ITickableTileEntity` | Rename |
| GUI | `GuiHandler` + `GuiContainer` | `INamedContainerProvider` + `ContainerType` + `ContainerScreen`, `NetworkHooks.openGui` | Client-side container constructors take a packet buffer |
| Networking | `SimpleNetworkWrapper` | `SimpleChannel` | Collapsed into a generic signal message |
| Capabilities | `hasCapability`/`getCapability` | `LazyOptional` | Invalidate on removal |
| Loot | Java + loot JSON | Loot tables everywhere + global loot modifiers | 99 tables backfilled |
| Worldgen | `IWorldGenerator` | `Feature` + `GenerationStage`/`Placement` injected into biomes | Injected into all registered biomes at setup |
| Dimensions | `DimensionManager` | `ModDimension` + `RegisterDimensionsEvent` | Registration at world start |
| Lighting | Synchronous BFS | New lighting engine with scheduled updates | Mass `checkBlock` overloaded it |
| ASM | Custom `IClassTransformer` in the mod | Forge coremods (JS transformers); Mixin unavailable from a mod jar | See [04](04-coremods-and-bytecode-patching.md) |

## 2. APIs verified as **missing** in this build

(not exhaustive; each was found by trying to use it and checking with `javap`)

`Item.onEntityItemUpdate`, `Item.onUsingTick`, `Block.getLightValue(state, world, pos)` (only the
`IBlockReader` default `getLightValue(BlockPos)`), `RenderNameTagEvent`, `PlayerDropsEvent`, `ITeleporter`,
`enqueueWork`, `NBTIngredient` (use `IngredientNBT`), `StringNBT.valueOf`, `World.addEntity` (it lives on
`ServerWorld`), `hasCapability`, `Entity.motionX/Y/Z` (use `getMotion`), `Block.Properties.notSolid`
(`doesNotBlockMovement`), `IAnimals`, `BlockState.isSolidSide` (only `isSolid()`), `BlockState.isAir(world,pos)`,
`AxisAlignedBB` copy constructor, public getters for `LivingEntity` jumping / `BoatEntity.deltaRotation` /
`BeaconTileEntity` effects (reflect, with dev names), `BiomeLoadingEvent`, `RenderNameplateEvent`, any weather
cancel event.

## 3. APIs **renamed or moved**

| Old | New |
| --- | --- |
| `Material.GROUND` | `Material.EARTH` |
| `Potion` (effects) | `Effect` (+ icons in `textures/mob_effect/<name>.png`) |
| `SoundCategory`, `ActionResult` | `net.minecraft.util` |
| texture dirs `blocks/`, `items/` | `block/`, `item/` |
| `block/repeater_off`, `redstone_torch_on` | `block/repeater`, `redstone_torch` (`stone_slab_top` removed) |
| `ItemEntity.getEntityData()` | `Entity.getPersistentData()` |
| `Chunk.getHeightValue(x,z)` | `getTopBlockY(Heightmap.Type, x, z)` (returns the *topmost block's* Y; add 1 for old loop bounds) |
| `Block.onBlockActivated` | returns `boolean` |
| `getFlammability` | on `IForgeBlock` (and the vanilla fire catch logic is private) |

## 4. Behavior traps (compiles, runs, wrong)

- `TileEntity.getUpdateTag` default omits subclass data; `onDataPacket` default is a no-op.
- `BlockEvent.HarvestDropsEvent` exists but is **never fired**; use a global loot modifier.
- `LivingSpawnEvent.CheckSpawn` fires after the spawn predicate has rejected → can only DENY.
- `WorldTickEvent` never fires for the client world; use `ClientTickEvent` against `Minecraft.getInstance().world`.
- `FluidUtil.tryPickUpFluid` has no code path for plain fluid blocks.
- `CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY` is null until `FMLCommonSetupEvent` (search-tree build runs
  earlier).
- `ChestTileEntity.playSound` needs the chest `TYPE` property on its state (crash for custom chests).
- `World.getRedstonePower` (not `getWeakPower`) for effective weak power.
- `EffectiveSide.get()` returns CLIENT on non-FML threads; don't side-sniff in light logic.
- `WorldLightManager#checkBlock` flooded synchronously crashes; queue and drain.
- `Screen#setFocusedDefault` doesn't set the widget's own focus flag.
- `BoatEntity` momentum multiplies by slipperiness with no offset (zero friction = exactly 1.0 for boats,
  `1/0.91` for living entities); `Block.Properties.slipperiness` is the only friction hook.
- `PlayerInventory.isItemValidForSlot` default returns true for all slots.
- `FireBlock.tryCatchFire` is private; only coremod reaches the catch-chance numbers.
- `Block#shouldSideBeRendered` culling depends on `isSolid()` (render layer), not render type.
- `ArmorItem` durability is set unconditionally from its material.
- The Forge lang file shipped a mismatched `reachDistance` attribute translation key.
- `PlainsVillagePools.init()` only forces static init; pools are registered in `JigsawManager.REGISTRY`.
- `BlockItem.getTranslationKey` uses the block's key.
- Vanilla `REPEATER` is one block with a `POWERED` property (the old on/off pair is gone).
- `ModelLoader.addSpecialModel` (via `ModelRegistryEvent`) bakes helper models; pass bare names (no `item/` prefix).
- `Item.Properties.setTEISR` + `parent: builtin/entity` model **with a `display` block** for items with custom
  rendering; the renderer receives no transform type.

## 5. Things that turned out *easier* than expected

- Extending vanilla classes (`RotatedPillarBlock`, `LeavesBlock`, `CropsBlock`, `FireBlock`, `ProjectileItemEntity`)
  replaced hundreds of lines of hand-rolled logic.
- `ProjectileItemEntity` + `SpriteRenderer` gave thrown items free sync and rendering.
- Spawning vanilla `FallingBlockEntity` replaced the original's parallel copy-of-vanilla entity.
- Village churches via a *delegating pool wrapper* instead of structure-class patching.
- `IRTBlockColor`/`IRTItemColor` generic scan auto-wires tinting for all mod blocks/items.

## 6. Forward-looking pointers (from general Minecraft modding knowledge — **not verified by this project**)

Each of these is a *place to look first* in later versions; confirm everything with the target's own sources.

- Rendering pipeline: GL calls in renderers (`GlStateManager`) were replaced by `MatrixStack`/render types in later
  versions (≥ 1.15/1.16); tile entity renderers and item TEISRs change signature.
- Data-driven everything: biomes, dimensions, structures, tags, loot modifiers and recipes move toward JSON; Java
  registration of worldgen features changes shape.
- Official mappings replace MCP/SRG in later toolchains, which removes the dev/production name split.
- The built-in game-test framework only exists in much later versions; until then, an in-game harness like
  `docs/testing/` is the substitute.
- Capabilities, networking (channel builder) and registries (`DeferredRegister`) gained cleaner APIs; the pitfalls
  in [02](02-pitfall-catalog.md) about sync and lifecycle still apply.
- Lighting and chunk-loading APIs were reworked repeatedly; expect to redo the Spectre Illuminator and Ender Anchor
  equivalents rather than port them.
