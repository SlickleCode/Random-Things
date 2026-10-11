# Architecture conventions

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

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

