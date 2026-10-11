# Random Things 1.12.2 → Forge 1.14.4 port — project state

Condensed 2026-10-04 from a ~420 KB chronological log (full history: `git log -p -- PORTING_PLAN.md`;
per-bug writeups: `TESTING_NOTES.md`; per-feature status: `WIKI_FEATURE_STATUS.md`). This file is the
current truth: state, scope decisions, conventions, API gotchas, coremod map. `CLAUDE.md` has the
working rules. **Update this file whenever features change** (see CLAUDE.md).

## File map

| File | Purpose |
| --- | --- |
| `CLAUDE.md` | Working rules (javap-first, match 1.12.2, no auto-commit, update docs) |
| `PORTING_PLAN.md` | This file: state, decisions, conventions, gotchas |
| `WIKI_FEATURE_STATUS.md` | Authoritative per-feature table (100 wiki pages) + "Not migrating" list |
| `TESTING_CHECKLIST.md` | Numbered test rows with status (`PASS`/`FAIL`/`FIXED`/`UNTESTED`/`REMOVED`); user fills results |
| `TESTING_NOTES.md` | Root-cause / fix writeups keyed by checklist row `#` (not in the checklist's Feedback column) |
| `UNTESTED_AND_MANUAL_CHECKS.md` | Generated summary of open/untested rows |
| `GUI_TEST_LIST.md` | GUI screenshot-review tracker (complete) |
| `docs/testing/` | Automated test setup: running it, writing tests, dev-only isolation |
| `docs/migration/` | Reusable migration knowledge base (pitfalls, strategy, case studies, checklist) |
| `docs/migration/versions/` | Multi-version support catalog and per-version analysis. Scope (2026-10-08): 1.14.4 and up, Forge then NeoForge (Fabric later), 1.14.4 feature set only, branch per version, 1.15.2/1.17.1 skipped, keep coremods |

## Repo / toolchain

- Fork: `SlickleCode/Random-Things`. Branches: `1.14.4` = all port work (default); `master` = untouched
  1.12.2-era source (`git show origin/1.12.2:<path>` for any original file); `main` = README only.
- `source env/activate.sh` (pins JDK 8) then `./gradlew build` (`-x test` for a quick check). Forgot to
  source it → Gradle dies with a Groovy `ReflectionCache` error (picked up JDK 21).
- Forge pinned to **1.14.4-28.2.26** (bumped from 28.0.49 for ModLauncher 4.x; only break was
  `ItemEntity.getEntityData()` → `Entity.getPersistentData()`). Gradle 5.6.4 is the ceiling for
  ForgeGradle 3.0.197.
- Mapped jar for `javap`: `~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3/forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3-recomp.jar` (+ `-sources.jar`). `javap -c` bytecode disassembly is how every uncertain behavior gets settled.
- **Dev tooling & automated tests** (all dev-only; full docs in `docs/testing/`): enabled solely by the JVM
  property `-Drandomthings.debug=true` (`runClient` sets it; the old `[debug]` config options were removed).
  With it off nothing is registered or class-loaded (`debug/DevMode` -> `debug/DevTools`, see
  `docs/testing/isolation.md`). `RT_AUTOTEST=true [RT_AUTOTEST_NO_GUIS=true] [RT_AUTOTEST_FILTER=<text>]
  [RT_AUTOTEST_EXIT=true] ./gradlew runClient` opens the flat creative world "RTAutoTest" and runs, hands-free:
  (1) `client/ResourceAudit` -> `run/randomthings_audit.txt` (blockstates/models/textures incl. the game's own model
  parser, recipe loading + coverage, allowlist `NO_RECIPE_EXPECTED`), (2) `/rtdebug test` -> `run/randomthings_tests.txt`
  (`debug/test/BehaviorTests` machines + special recipes, `RoundTripTests` NBT/capability round trips for every tile
  entity/entity/item), (3) unless NO_GUIS, `/rtdebug allguis` screenshots to `run/screenshots/randomthings_debug/`.
  Interactive: `/rtdebug test [filter]`, `/rtdebug gui <id> [sneak]`, `/rtdebug allguis [delayTicks]`. `[RT-DEBUG]`
  lines in `run/logs/latest.log`; use `RTDebug.log(...)` for new diagnostics (no-op unless dev). Latest run
  (2026-10-07): audit 0 failures; tests 334 (all pass; 1 environment skip on each side) as of 2026-10-09; the Spectre cube lookup bug (#380) was found and fixed. The GUI pass only shows default states.
- **Knowledge base for future migrations:** `docs/migration/` (strategy, pitfall catalog, version notes, coremod
  guide, verification lessons, case studies, next-migration checklist, open-questions backlog).
- Always `runClient`-smoke-test new `@ObjectHolder` fields and coremods: a plain build won't catch them.

## Current state (2026-10-04)

- **Coverage:** every wiki-documented feature has 1.14.4 code except those marked `WILL NOT MIGRATE`/
  `REMOVED` in `WIKI_FEATURE_STATUS.md`. Most of it is `DONE-UNTESTED` (builds, boots, renders in the
  autotest; not play-verified). Last tallies (2026-10-03): PASS 196, UNTESTED 106, DONE 40, FIXED 10, FAIL 8.
- **Open issues:** Sound Dampener #158 (code matches 1.12.2; need the sound-source-to-dampener distance if still wrong).
  Confirmed working 2026-10-04: GUI layout audit (#120, all 28 GUIs reviewed), Sound Recorder GUI (#155), Colored Grass,
  Weather/Golden Egg sprites, Floo Powder recipe, Chat Detector button. Always check
  `TESTING_CHECKLIST.md` for new `FAIL` rows before starting new work; don't re-investigate a `FIXED` row
  without reading its `TESTING_NOTES.md` entry.
- **Latest change set (2026-10-07, uncommitted):** automated tests + dev isolation (see Dev tooling above).
  Found and fixed by them: **Magnetic Enchantment never worked** - `BlockEvent.HarvestDropsEvent` is never posted
  in this Forge build, so the old listener was dead code and the Block Breaker's drops fell on the ground; now a
  global loot modifier (`handler/MagneticLootModifier`, registered in `data/forge/loot_modifiers/`) (#288/#372, FIXED);
  64 missing per-color recipes restored (#367-370); Fluid Display item is an isometric cube (#366; first attempt used
  scalar `display.scale`, which the game rejects - arrays required); Fluid Display TE NBT now idempotent (#373).
  Logging cleanup: Spectre Illuminator INFO logs moved to `RTDebug.log` (dev-only), `printStackTrace` -> logger.
  Q11/Q12 (2026-10-10): stub coremod booted on 14 Forge/NeoForge builds - JS coremods work to Forge 1.21.10 and NeoForge 21.5, silently ignored on Forge 1.21.11 and NeoForge 21.8+; 1.15.2/1.17.1 skip stands (see backlog). Transcript review finished (2026-10-08; backlog Q5): pitfall catalog now has K14-K23 and L1-L19, `04` has the Mixin
  timeline. First real client-on-server session (2026-10-09, Q9): server crashed on an Ender Letter receiver packet (`PacketBuffer.readString()` is client-only; all reads now `readString(32767)`, #382); Portkey display transforms fixed (#381). Dedicated server (2026-10-08): `runServer` revealed the mod crashed every dedicated server at construction (client
  classes referenced from the main class); client wiring moved to `client/ClientSetup`, `IRendersAsItem` entities
  got `@OnlyIn(_interface)`; server now boots (#378). Client suite 317/317 after adding Contact Button/Lever, Imbuing Station and Block Destabilizer tests; those found that the
  **Imbuing Station never delivered its result** (#169, FIXED). The suite also runs headless on `runServer` (`debug/test/ServerAutoTest`, #379).
  Second round of tests (`debug/test/CoremodTests`, Observer): found that the **Wireless Redstone Interface never refreshed**
  when redstone next to it changed (listener compared the wrong event position; now `neighborChanged`, #376 FIXED) and
  confirmed at runtime that the spawn-dispatch, rain, death-drop and boots coremods apply. The audit also checks the vanilla
  loot tables LootHandler injects into (#377). Decisions (2026-10-07): keep dev classes in the release jar; dev features by
  JVM property only. Open questions: `docs/migration/open-questions-backlog.md`.
- **Previous change set (2026-10-04, commit `6a76190`):** text-field GUIs sync on every change instead of
  only from `removed()` (#360); Ender Letter has a 200-char multiline message body via
  `MultilineTextFieldWidget` (#361, **new feature, not in 1.12.2**; GUI 50px taller, `ender_letter.png`
  rebuilt; message stored in the letter's NBT `message`, sent by signal id 1, trimmed server-side);
  letter tooltip "Sneak + right-click an Ender Mailbox to send" and mailbox label "Incoming Mail" (#362);
  Notification Interface icon is a ghost/copy slot like the Remote's camo row (#363); "Target" label on
  Redstone Observer / Basic Redstone Interface only when a target is set, Remote use-screen hover shows the
  displayed item's anvil name (#364); Advanced Item Collector GUI trimmed 24px (#365). All `FIXED`,
  awaiting in-game retest.
- **Removed on request (don't re-port):** Plate family (11 blocks), Special Chest (+ ocean-monument
  coremod), Sakanade, Pitcher Plant, fire/water-protection family (Obsidian Skull, Lava Charm, Lava Wader,
  Water Walking Boots, Obsidian Water Walking Boots), Biome Glass, Biome Stone x5, Inventory Rerouter,
  Blood Rose (was never 1.12.2 content).
- **Will not migrate (final):** Item Filter (both consumers shipped with a built-in single-example-item
  filter), "Ender network"/Link Orb (abandoned stub in 1.12.2 itself), Blackout Powder, Collapse
  potion/effect/brewing, modded-ore divining rods and all third-party mod compat (Baubles, JEI,
  OpenComputers, Thaumcraft, Thermal Expansion), Obsidian Skull Ring (Baubles-only), Festival system,
  Player Soul/Revive Circle/Rez Stone, Dungeon Chest Generator, Voxel Projector, Nature Core, `RTCommand`.
  Full list: `WIKI_FEATURE_STATUS.md` "Not migrating".
- **Known disclosed gaps:** Ancient Furnace's biome change doesn't live-resync to already-connected
  clients until chunk reload; Spectre Energy Injector/Coil use plain cube models; Ender Bucket icon is
  static (no dynamic fluid-layer model); Biome Sensor's held-item biome HUD wasn't ported; Obsidian Skull
  Ring and the `Numbers` range tunables aren't ported. (Rain Shield's visual suppression was added later via
  `GameRendererTransformer`; the thrown Weather/Golden Egg sprite bug was a missing `createSpawnPacket`.)
- **Peace Candle village churches shipped 2026-09-29** via a delegating `JigsawPattern` wrapper
  (`PeaceCandleChurchPool`) + user-authored `peace_candle_church.nbt` (needs a jigsaw block at the door).

**Production names (2026-10-11):** transformers match MCP and SRG names and reflection uses `ObfuscationReflectionHelper`; static registries use `util/WeakSets`; both verified on a production server and a production client (`prod-test/`, see `docs/testing/README.md`; scenes `light_redirector` and `magic_hood` added for the render coremods).

**Test tooling (2026-10-11):** the GUI click-through and render-scene screenshot passes were backported from the 1.16.5 branch (`RT_AUTOTEST_CLICKS`, `RT_AUTOTEST_SCENES`; inventory in `docs/testing/test-inventory.md`).

## What to do next

1. Check `TESTING_CHECKLIST.md` for new `FAIL` rows; then the open issues above.
2. Otherwise the remaining work is in-game retesting of the `DONE-UNTESTED`/`FIXED` backlog (user does the
   playtesting). **Ask the user which area to tackle** rather than picking unprompted.
3. After every change: full `./gradlew build`, update `TESTING_CHECKLIST.md` + this file, leave uncommitted.

## Scope decisions (user-made, binding)

- **Wiki is the authoritative scope** (100 feature pages); 1.12.2 code without a wiki page is not migrated
  unless explicitly kept. Third-party compat is dropped; ore-dictionary → plain vanilla items or Forge tags
  (`forge:stone`, `forge:cobblestone`, `forge:glass`, `forge:glass/colorless`, `forge:slimeballs`,
  `forge:rods/wooden`, `forge:ingots/iron`, `forge:nuggets/gold`, `forge:gems/lapis`, `forge:dyes/*`).
- **Match 1.12.2 exactly unless the user says otherwise; flag divergences.** Deliberate divergences so far:
  Spectre Leaf drops Ectoplasm only (no sapling); Floo Powder is shapeless; Colored Grass named
  "<Color> Grass Block" + shovel preference; Potion Vaporizer/Block Destabilizer flood-fills pass through
  non-full blocks; Global Chat Detector has no op bypass; Rune Base left-click clears the whole block;
  Rune Base uses the real noisy texture by default (`FlatRunes` config for flat); Super Lubricent
  blocks/boots have a speed cap (blocks 0.35 b/t, boats 15°/t turn cap) that 1.12.2 never had; Trigger
  Glass chain limit config (default 20); Magnetic config-off keeps the enchant registered but unobtainable;
  Remote camo row implemented (reversed an earlier simplification); Ender Letter message body (new).
- **Standing simplifications** (not worth re-litigating): metadata sub-items → discrete items per variant;
  1.12.2 icon-sprite buttons → text buttons, then partly restored as `SpriteStateButton`/`ToggleIconButton`;
  bespoke particles → vanilla `RedstoneParticleData`/`ParticleTypes`; magic-circle GL framework dropped;
  `ItemFilter` replaced by single example-item slots; Crafting recipes' tags simplified as above.
- **Check the real 1.12.2 source before "fixing" anything that looks wrong** — several suspected bugs were
  intentional (see-through Plates, Lapis/Quartz Lamp always lit, Portkey glowing forever while unbound,
  Chest-less Colored Grass recipe, Golden Compass only reading NBT set by a recipe). Also check recipe/loot
  JSON directly, not just Java grep (data is referenced by numeric meta, not enum names).

## Architecture conventions

- **Registration:** `ModBlocks`/`ModItems`/`ModTileEntityTypes`/`ModContainerTypes`/`ModEntityTypes`/
  `ModEffects` with class-level `@ObjectHolder("randomthings")` + `RegistryEvent.Register`. **Any
  non-registry static field in an `@ObjectHolder` class crashes startup** — put maps/lists in plain
  helper classes (`RuneDustItems`, `DiviningRodScratch`). Blocks register before items. Reference
  `ModEffects` etc. from items via `Supplier`. Every block needs a `registerItemForBlock` call (Basic/
  Advanced Redstone Interface were once forgotten).
- **Event listeners** live inline in `RandomThings.java` (no `RTEventHandler`). Static `Set<TileEntity>`
  registries (Chat Detector, Redstone Observer, Slime Cube, Rain Shield, Peace Candle) mirror 1.12.2's
  `WeakHashMap` sets.
- **GUIs:** TE/item + `INamedContainerProvider` + `Container` + `ContainerScreen`. All custom signals use
  `ISignalContainer`/`ContainerSignalMessage` (`container.send(id, pb -> …)`, server handles in
  `handle(id, data)`); no per-GUI packets. Client-side container constructors take `(windowId, inv,
  PacketBuffer)` and bind to throwaway `ItemStackHandler`s (vanilla `DispenserContainer` pattern); real
  contents arrive via normal slot sync. Slot validation must live in a handler **shared by the TE and the
  client constructor** (`PotionVaporizerItemHandler`, `ImbuingStationItemHandler`) or the client side won't
  enforce it. Output slots: reject player inserts via `isItemValid`, but the TE must use `setStackInSlot`
  (`insertItem` calls `isItemValid`). Extra data (e.g. a text value) goes through
  `NetworkHooks.openGui(..., Consumer<PacketBuffer>)`. Item-backed inventories use
  `util/ItemInventoryHandler` (NBT-backed `ItemStackHandler`). Ghost slots: override `slotClick` to copy
  one of the held stack / clear when empty-handed; block shift-click and `canTakeStack`.
- **Text fields:** route keys straight to the focused field (`ContainerScreen.keyPressed` closes the GUI on
  any unconsumed key matching the inventory bind, incl. plain letters), let Escape through; call
  `setFocused2(true)` after `setFocusedDefault` for auto-focus; sync text to the server **on every change
  from `tick()`**, not only from `removed()` (races the close-window packet). `MultilineTextFieldWidget`
  is the custom multi-line box (1.14.4 has none). Only one input may hold focus — the screen enforces it in
  `mouseClicked`.
- **GUI layout:** draw titles in `drawGuiContainerForegroundLayer` (several screens once had no title);
  dimensions/textures come from the original `redstoneremoteuse.png` etc. via `git show`; texture edits are
  done by copying/inserting pixel rows (System.Drawing in PowerShell; no PIL). Scrollable lists: extend
  `ExtendedList` but override `render()` entirely (vanilla inlines dirt-background chrome; overriding only
  `renderBackground` is not enough) — see `ChunkAnalyzerScanResultList`/`SoundNameList`.
- **TE live sync needs all three:** `getUpdatePacket()`, `getUpdateTag()` that calls `write()` (default calls
  private `writeInternal()` and omits subclass fields), **and** an `onDataPacket` override calling
  `read(pkt.getNbtCompound())` (Forge's default is a no-op, so live updates are silently dropped until
  relog). Applied to Rune Base, Biome Radar, Fluid Display, Redstone Observer, Sound Dampener, Redstone
  Interfaces.
- **Per-instance dynamic rendering** (no `ExtendedBlockState`/unlisted properties in 1.14.4): use a
  `TileEntityRenderer` with `BlockRenderType.INVISIBLE` (Rune Base, Fluid Display, Biome Radar). Still add a
  minimal blockstate + empty model to silence `ModelBakery` warnings. In TESRs: set `color4f(1,1,1,1)`,
  blend func, and `disableLighting()` for raw `POSITION_COLOR` vertices (dispatcher enables lighting and
  leaves stale color); bake per-face brightness into vertex color (UP 1.0, DOWN 0.5, N/S 0.8, W/E 0.6);
  check vertex winding (outward normals) or faces vanish under culling. Items with custom rendering:
  `Item.Properties.setTEISR` + `parent: builtin/entity` model **with a `display` block** (Portkey).
  `ModelLoader.addSpecialModel` (via `ModelRegistryEvent`) bakes helper models; pass bare names, not
  `item/`-prefixed.
- **Item model state:** `addPropertyOverride`/`IItemPropertyGetter` still works (method `call`); used for
  compasses, clocks, Sound Recorder/Pattern, Redstone Activator. Dyeable/tinted items: `IRTBlockColor`/
  `IRTItemColor` auto-wired by generic `ColorHandlerEvent` listeners.
- **Recipes:** custom ones are `SpecialRecipe`/`SpecialRecipeSerializer` + a stub JSON
  `{"type":"randomthings:…"}`. Shaped rows must be **equal width** (pad with spaces). `forge:nbt` ingredient
  is `IngredientNBT` in this build. Imbuing recipes are Java-registered (`ImbuingRecipeHandler`).
- **Entities:** `ModEntityTypes` (use explicit `EntityType.Builder<T>` type witness); thrown items extend
  `ProjectileItemEntity` + `SpriteRenderer` (free sync/rendering); hanging entities need synced
  `DataParameter`s for position/facing; use `DataParameter`s instead of spawn-data. Replacement for the
  missing `Item.onEntityItemUpdate`: a `WorldTickEvent` scan of dropped stacks.
- **Client ticks:** `WorldTickEvent` **never fires for the client world** in this Forge — use
  `ClientTickEvent` against `Minecraft.getInstance().world` (Spectre Illuminator relight, boat cap).
- **Config:** `config/RTConfig` (`randomthings-common.toml`), read at use time, never at registry time.
  Sections: `worldgen`, `loot`, `features`, `numbers`, `visual`.
- **Worldgen:** `Feature`/`GenerationStage` registered in `registerWorldgenFeatures`; Ancient Furnace
  mutates `Chunk.getBiomes()` in place; Glowing Mushroom uses `CHANCE_HEIGHTMAP`; chunk loading uses
  `ServerWorld#forceChunk` via `handler/AnchorChunkLoader` (no tickets in 1.14.4); chest loot via
  `LootTableLoadEvent` (`handler/LootHandler`).
- **Dimension:** `ModDimension` registry entry + `RegisterDimensionsEvent` (not common setup — the world
  load clears the dimension registry) using `registerOrGetDimension`; `SpectreChunkGenerator` must override
  `carve`/`decorate` as no-ops (vanilla `THE_VOID` adds a stone platform); no `ITeleporter` in this build →
  `TeleporterTransformer` skips `makePortal`.
- **Side safety:** common classes must not reference client-only classes even behind `isRemote` guards
  (`BiomeColorUtil` is common; `client.util.RenderUtils` is not). Client-only asm hooks go in
  `ClientAsmHandler`, not `AsmHandler`.
- **FakePlayer** (Block Breaker): assign a no-op `connection` (`ServerPlayNetHandler` subclass), else
  `tryHarvestBlock` NPEs on blocks without TEs.

## Coremods (Forge JS transformers; Mixin is dead — never reintroduce it)

SpongePowered Mixin cannot bootstrap from a mod jar on this Forge (its `ILaunchPluginService` must be on the
launcher classpath before mods are scanned), and the dev client can't load manifest-declared configs either.
Both former Mixins (Blazing Fire, Super Lubricent Boots) were converted to coremods and all Mixin build
infrastructure deleted. Coremods are self-contained in the jar: `META-INF/coremods.json` →
`transformer/*.js`, hooks in `asm/AsmHandler` (common) / `ClientAsmHandler` (client).

| Transformer | Target | Purpose |
| --- | --- | --- |
| `VertexLighterFlat` | `VertexLighterFlat.processQuad` | Light-tint hook (placeholder tintIndex 12340) |
| `FireBlockTransformer` | `FireBlock.tryCatchFire` | Blazing Fire 4x catch chance / die-out / age growth |
| `SuperLubricentBootsTransformer` | `LivingEntity.travel` | Boots → max slipperiness unless sneaking |
| `SpawnPlacementTransformer` | `EntitySpawnPlacementRegistry` shared predicate dispatch | Slime Cube ALLOW, Lapis/Quartz Lamp, Peace Candle DENY (Lapis ALLOW guarded for Peaceful) |
| `MagicHoodTransformer` | `LivingRenderer.canRenderName` (6 returns) | Hide nametag |
| `RainShieldTransformer` | `World.isRainingAt` (4 returns) | Mechanical rain/snow suppression |
| `GameRendererTransformer` | biome lookup in rain render loop | Visual rain suppression |
| `IBlockReaderTransformer` | `IBlockReader.getLightValue(BlockPos)` | Spectre Illuminator light |
| `PlayerEntityTransformer` | `PlayerInventory.dropAllItems` in `dropInventory` | Spectre Anchor keeps items |
| `WorldRedstonePowerTransformer` / `WorldReaderStrongPowerTransformer` | `World.getRedstonePower` / `IWorldReader.getStrongPower` | Wireless Redstone Interface signals |
| `TeleporterTransformer` | `Teleporter.makePortal` | Skip portal carving for the Spectre dimension |
| `BlockRendererDispatcherTransformer` | block render dispatch | Light Redirector "periscope" |
| `ItemRendererTransformer` / `ArmorLayerTransformer` | item quad color / armor layer | Dyeing Machine recolor |

Patterns: wrap the value before an `IRETURN` (collect all return nodes by reference first, then insert); or
insert an early conditional return at method head. Verification (no harness exists in the repo; write a
throwaway one each time): load the real class bytes, run the shipped `.js` through Nashorn's `ScriptEngineManager`
(not `jjs`, whose `-cp` parsing breaks), re-serialize with `COMPUTE_FRAMES`, and run
`CheckClassAdapter.verify` with the exact `runtimeClasspath` (get it with a throwaway Gradle init-script
task). Then a `runClient` boot confirms `Transforming …` with no `VerifyError`/`LinkageError`. Classes only
loaded in a live world (spawn registry, teleporter) need this check.

## 1.14.4 / Forge 28.2.26 API gotchas

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

## Pitfalls that cost real time (generalized lessons)

- A fix applied once is often needed in several copies (shared slot validation, `getUpdateTag`,
  `ClientTickEvent` vs `WorldTickEvent`, text-field key routing across 4–5 screens). Grep for siblings.
- "Compiles and looks right" ≠ matches the original: re-read the original control flow line by line
  (missing `iterator.remove()`, wrong `playSound` player arg, missing unconditional top-of-method
  `detectAndSendChanges`).
- When a vanilla widget must lose its chrome, read the **caller's** bytecode, not just the obvious hook.
- Don't iterate blindly on something that can't be verified headlessly; get runtime evidence (logs,
  probes) and state clearly when a fix is unverified. Never report a bug fixed without user confirmation
  or runtime evidence.
- Keep scope tight: split multi-piece features into slices and say what was deferred. Ask via
  `AskUserQuestion` before building something whose consumers are already shipped (Item Filter).

## Slice history (one line each; details in git log / `TESTING_NOTES.md` / `WIKI_FEATURE_STATUS.md`)

- **Batch 1 (standalone blocks, 9 slices):** Lapis/Quartz glass+lamp, Super Lubricent family, Trigger Glass,
  Compressed Slime, Contact Button/Lever, Spectre Block, spectre tree set, Glowing Mushroom, Sided Redstone,
  Stained/Luminous bricks (16 colors), Bean family, Lotus, Blazing Fire, color infrastructure; later loot-table
  backfill (99 tables) and recipe backfill (45).
- **Batch 2 (tile-entity blocks, 14 slices):** Analog Emitter, Igniter, Online Detector, Item Collector,
  Adv. Redstone Repeater (bytecode-decoded scheduling), Iron Dropper, Custom Workbench, Player Interface,
  Inventory Tester, Chat Detector, Redstone Observer + Tool, Potion Vaporizer, Entity Detector + Entity
  Filter, Adv. Item Collector, Filtered Super Lubricent Platform, Notification Interface (first targeted
  packet + toast), ID Card + Global Chat Detector.
- **Batch 3 (items):** Bean Stew, Blaze and Steel, Bottle of Air, Stable Ender Pearl, Golden/Emerald Compass
  (+ Position Filter, first `SpecialRecipe`), Escape Rope, Chunk Analyzer (first item-only GUI).
- **Deferred subsystems:** Biome Radar; Sound-pattern family (Recorder/Pattern/Box/Dampeners); Imbuing
  Station + Rune Base/Dust/Pattern; Floo network (first custom entity).
- **Wiki-scoped slices (2026-09-26/28):** Eclipsed Clock + Weather Eggs + Time in a Bottle; Ender Letter/
  Mailbox; Ender Bucket(s); Summoning Pendulum, Golden Egg/Chicken, Fluid Display; Artificial End Portal;
  Ender Bridge/Anchor/Prismarine; Spectre Illuminator; Portkey (+ camo TEISR); Spectre Anchor; Redstone
  Wireless cluster (Basic/Advanced Interface, Activator, Remote); Magnetic Enchantment, Block Breaker, Block
  Destabilizer; Spectre Dimension (Key/Core/Ingot); Spectre Tools; Spectre energy network + Lens; Diaphanous
  Blocks + Light Redirector; Dyeing Machine; Spirit mob.
- **Batch 7 (coremods):** Mixin→coremod conversion; Slime Cube/Lapis Lamp spawn control; Magic Hood; Rain
  Shield; worldgen (Ancient Furnace, Peace Candle + churches, Ender Anchor chunk loading, Glowing Mushroom).
- **Config + polish (2026-09-29→10-04):** `RTConfig` toggles; HideCoordinates; Rune noise texture; Colored
  Grass completion + Grass Seeds; debug tooling; GUI audit passes; testing-pass removals; GUI polish (see
  Current state).
- **Bug-fix sagas worth knowing exist** (all in `TESTING_NOTES.md`): Mixin bootstrap investigation; Super
  Lubricent speed cap/boats; Spectre Illuminator relight (PADDING 15, lazy job queue, client-tick drain);
  TE live-sync; Lapis/Quartz Lamp mechanism flip-flop; Fluid Display render faces/lighting; Rune Base GL state;
  Special Chest `ChestType` crash; Block Breaker FakePlayer NPE; Spectre dimension registry crash;
  Time in a Bottle target sync.
