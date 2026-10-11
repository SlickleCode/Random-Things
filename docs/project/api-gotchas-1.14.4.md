# 1.14.4 / Forge 28.2.26 API gotchas

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

Verified with `javap`; these differ from 1.12.2 *and* from most online 1.14 guides.

- **Missing:** `Item.onEntityItemUpdate`/`onUsingTick`, `Block.getLightValue(state,world,pos)` (only
  `getLightValue(state)`; use the `IBlockReader` default), `RenderNameTagEvent`, `PlayerDropsEvent`,
  `ITeleporter`, `enqueueWork`, `NBTIngredient`, `StringNBT.valueOf`, `World.addEntity` (it's on
  `ServerWorld`), `hasCapability` (use `LazyOptional`), `Entity.motionX/Y/Z` (use `getMotion`),
  `Block.Properties.notSolid` (`doesNotBlockMovement`), `IAnimals`, `BlockState.isSolidSide` (only
  `isSolid()`), `BlockState.isAir(world,pos)`, `AxisAlignedBB` copy constructor, `SpawnPlacement` hooks
  beyond the shared dispatch, public getters for `LivingEntity` jumping (reflect) / `BoatEntity.deltaRotation`
  / `BeaconTileEntity` effects (reflect with dev MCP names).
- **Renamed/moved:** `Material.GROUND`→`EARTH`; `Potion`→`Effect`, mob-effect icons come from
  `textures/mob_effect/<name>.png`; `SoundCategory` in `net.minecraft.util`; `ActionResult` in
  `net.minecraft.util`; texture dirs `blocks/items`→`block/item`; `block/repeater_off`→`block/repeater`,
  `redstone_torch_on`→`redstone_torch`, `stone_slab_top` gone; `ItemEntity.getEntityData`→`getPersistentData`;
  `Chunk.getHeightValue`→`getTopBlockY(Heightmap.Type, x, z)` which returns the topmost block's Y (add 1
  for 1.12.2 loop bounds); `Block.onBlockActivated` returns `boolean`; `getFlammability` lives on
  `IForgeBlock`.
- **Behavior traps:** `TileEntity.getUpdateTag` default omits subclass data; `onDataPacket` default is a
  no-op; `FluidUtil.tryPickUpFluid` has no code path for a plain fluid block (use `IFluidBlock`/
  `IBucketPickupHandler`); `CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY` is null until
  `FMLCommonSetupEvent` (guard it in `getDisplayName`, which runs during search-tree build);
  `ChestTileEntity.playSound` needs `ChestBlock.TYPE` on the state; `World.getRedstonePower` (not
  `getWeakPower`) for effective weak power; `EffectiveSide.get()` returns CLIENT on non-FML threads (don't
  use side-sniffing in light logic); `WorldLightManager#checkBlock` flooded synchronously crashes
  (`ArrayIndexOutOfBounds`) — queue and drain per tick; `BlockItem.getTranslationKey` uses the *block's*
  `block.` key; the Forge lang file ships a mismatched `reachDistance` attribute key (add
  `attribute.name.generic.reachDistance`); `FireBlock.tryCatchFire` is Forge-patch-private;
  `PlainsVillagePools.init()` just forces static init and pools are registered in
  `JigsawManager.REGISTRY` (wrap, don't hook); jigsaw `attachement_type` is genuinely misspelled;
  `Screen#setFocusedDefault` doesn't set the widget's own focus flag; `BoatEntity` momentum multiplies
  by slipperiness with no offset (true zero friction = exactly `1.0F` for boats, `1F/0.91F` for living
  entities); vanilla `REPEATER` is one block with a `POWERED` property; `Block.Properties.slipperiness`
  is the only friction hook (no Mixin needed for zero friction).
- **Gradle/runtime:** `runClient` is the only way to catch `@ObjectHolder`, coremod verify, and
  model-bake errors; recipe parse errors only appear at world load ("Parsing error loading recipe").

