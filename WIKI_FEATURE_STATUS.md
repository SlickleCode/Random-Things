# Wiki Feature Status

The public wiki (exported as 100 feature JSON files, `RT_Wiki_remake/data/features.zip`) is now
this port's **authoritative scope document**. Every feature below is one wiki page. Anything in the
1.12.2 codebase that ISN'T one of these 100 pages is tracked separately in "Not migrating" below,
not in this table.

Status legend:
- **DONE** — ported and verified (checklist row(s) marked PASS)
- **DONE-UNTESTED** — registered/implemented in the 1.14.4 branch, no verified in-game test yet
- **PARTIAL** — some sub-scope shipped, rest missing (noted per-row)
- **BLOCKED** — implemented but non-functional, blocked on the Mixin coremod batch (see
  `quirky-wobbling-gray.md`'s consolidated ASM/coremod batch)
- **BUGGY** — implemented, a real bug is open against it in `TESTING_CHECKLIST.md`
- **NOT STARTED** — no 1.14.4 code yet
- **REMOVED** — was ported, then explicitly deleted per user request

`ASM?` = Yes if 1.12.2 needed a `ClassTransformer` bytecode patch for this feature (see the
consolidated ASM/coremod batch in the plan file for exactly which patch and why).

| Wiki feature | 1.12.2 class(es) | Status | ASM? |
|---|---|---|---|
| Advanced Redstone Interface | AdvancedRedstoneInterfaceBlock, AdvancedRedstoneInterfaceTileEntity | DONE-UNTESTED (#283-286, 2026-09-27) | Yes (new `WorldRedstonePowerTransformer`/`WorldReaderStrongPowerTransformer` coremods - see Redstone Interface's own row for the full story) |
| Advanced Redstone Repeater | BlockAdvancedRedstoneRepeater, TileEntityAdvancedRedstoneRepeater | DONE | No |
| Advanced Redstone Torch | BlockAdvancedRedstoneTorch, TileEntityAdvancedRedstoneTorch | DONE-UNTESTED | No |
| Analog Emitter | BlockAnalogEmitter, TileEntityAnalogEmitter | DONE | No |
| Ancient Furnace | BlockAncientFurnace, TileEntityAncientFurnace, WorldGenAncientFurnace | DONE-UNTESTED (worldgen batch, #262-267) | No |
| Artificial End Portal | EntityArtificialEndPortal, ItemIngredient.EVIL_TEAR | DONE-UNTESTED (#238-243) | No |
| Beans | BlockBeanSprout/BeanStalk/Pod, ItemBean, ItemBeanStew | DONE | No |
| Biome Blocks | BlockBiomeStone, BlockBiomeGlass, ItemBiomeCrystal | DONE-UNTESTED | No |
| Biome Radar | BlockBiomeRadar, TileEntityBiomeRadar, ItemIngredient.BIOME_SENSOR | DONE-UNTESTED | No |
| Blaze and Steel | ItemBlazeAndSteel, BlockBlazingFire | DONE | No |
| Block Breaker | BlockBlockBreaker, TileEntityBlockBreaker | DONE-UNTESTED (#289-291, 2026-09-28) | No (uses the new Magnetic listener instead of a coremod - see Magnetic Enchantment's own row) |
| Block Destabilizer | BlockBlockDestabilizer, TileEntityBlockDestabilizer, EntityFallingBlockSpecial | DONE-UNTESTED (#292-296, 2026-09-28) | No (spawns vanilla's own `FallingBlockEntity` directly - see its own note below; dropped the cosmetic VertexLighterFlat glow overlay as a disclosed simplification) |
| Block of Sticks | BlockBlockOfSticks | DONE-UNTESTED | No |
| Chat Detector | BlockChatDetector, TileEntityChatDetector | BUGGY (#93 GUI-close bug) | No |
| Chunk Analyzer | ItemChunkAnalyzer | DONE (#145) | No |
| Colored Grass | BlockColoredGrass, ItemGrassSeeds | PARTIAL (single white variant only, matches original's own unreachability) | No |
| Compressed Slime Block | BlockCompressedSlimeBlock | DONE (#33) | No |
| Contact Button & Lever | BlockContactButton, BlockContactLever | DONE (#1-3, #34) | No |
| Creative Player Interface | BlockCreativePlayerInterface, TileEntityCreativePlayerInterface | DONE-UNTESTED (#244) | No |
| Custom Crafting Tables | BlockCustomWorkbench, ContainerCustomWorkbench | **REMOVED** (ported then explicitly deleted, #88) | No |
| Diaphanous Blocks | BlockBlockDiaphanous, TileEntityBlockDiaphanous | NOT STARTED (needs generic runtime block-model renderer) | No |
| Divining Rods | item/diviningrod/* | DONE | No |
| Dyeing Machine | BlockDyeingMachine, ContainerDyeingMachine | NOT STARTED | Yes (RenderItem+LayerArmorBase recolor) |
| Eclipsed Clock | ItemEclipsedClock, EntityEclipsedClock | DONE-UNTESTED (#193-205) | No |
| Ectoplasm | ItemIngredient.ECTO_PLASM (now "ectoplasm"), EntitySpirit | PARTIAL (item exists; the Spirit mob that drops it has no 1.14.4 entity yet) | No |
| Emerald Compass | ItemEmeraldCompass | DONE (#139-141) | No |
| Ender Bridge | BlockEnderBridge, BlockEnderAnchor, EntityEnderConnection | DONE-UNTESTED (#245-251) | No |
| Ender Bucket | ItemEnderBucket, ItemReinforcedEnderBucket | DONE-UNTESTED (#218-224) | No |
| Ender Letter | ItemEnderLetter, BlockEnderMailbox, EnderLetterHandler | DONE-UNTESTED (#206-217) | No |
| Entity Detector | BlockEntityDetector, TileEntityEntityDetector | DONE-UNTESTED | No |
| Entity Filter | ItemEntityFilter | DONE-UNTESTED | No |
| Escape Rope | ItemEscapeRope, EscapeRopeHandler | DONE-UNTESTED | Yes (RenderItem yellow enchant-glow - cosmetic only, not yet re-added) |
| Fertilized Dirt | BlockFertilizedDirt | DONE-UNTESTED | Yes (WorldGenAbstractTree.setDirtAt - clean replacement used) |
| Floo Teleportation | handler/floo/*, BlockFlooBrick, ItemFlooPouch/Sign/Token | DONE-UNTESTED (#180-192) | Yes (VertexLighterFlat glow - dropped, disclosed) |
| Fluid Display | BlockFluidDisplay, TileEntityFluidDisplay | DONE-UNTESTED (#233-237) | No |
| Glowing Mushrooms | BlockGlowingMushroom | DONE-UNTESTED | Yes (VertexLighterFlat glow - dropped, disclosed) |
| Golden Compass | ItemGoldenCompass | DONE (#137-138) | No |
| Golden Egg | ItemIngredient.GOLDEN_EGG, EntityGoldenEgg, EntityGoldenChicken | DONE-UNTESTED (#230-232) | No |
| Igniter | BlockIgniter, TileEntityIgniter | DONE (#72-73; Keep-Ignited mode removed per request, #74) | No |
| Imbuing Station | BlockImbuingStation, TileEntityImbuingStation | DONE-UNTESTED (#168-179) | No |
| Inventory Rerouter | BlockInventoryRerouter, TileEntityInventoryRerouter | DONE-UNTESTED | No |
| Inventory Tester | BlockInventoryTester, TileEntityInventoryTester | DONE-UNTESTED | No |
| Iron Dropper | BlockIronDropper, TileEntityIronDropper | DONE (#82-87) | No |
| Item Collector | BlockItemCollector, BlockAdvancedItemCollector | BUGGY (#76 texture issue) | No |
| Item Filter | ItemItemFilter, ContainerItemFilter | NOT STARTED (deliberately skipped, 2026-09-26 - see note below) | No |
| Lapis Glass | BlockLapisGlass | DONE (#21-23, 27) | No |
| Lapis Lamp | BlockLapisLamp | DONE (#29 - confirmed working 2026-09-27 after two reverts, see `AsmHandler#overrideSpawnResult`'s javadoc for the full story) | Yes (`EntitySpawnPlacementRegistry` coremod, shared with Slime Cube/Peace Candle - a 1.12.2-faithful `getLightValue`-per-side trick was tried instead but proved unreliable in this Forge version, see the javadoc) |
| Lava Charm | ItemLavaCharm | DONE (#127) | No |
| Lava Waders | ItemLavaWader | DONE, needs retest (#128-129, 133) | Yes (Block.addCollisionBoxesToList - port uses its own different design instead) |
| Light Redirector | BlockLightRedirector, TileEntityLightRedirector | NOT STARTED (needs generic runtime block-model renderer) | Yes (BlockRendererDispatcher) |
| Lotus | BlockLotus, LotusBlossomItem | DONE (#66-67.1) | No |
| Luminous Blocks | BlockBlockLuminous(Translucent) | DONE (#57-58) | No |
| Luminous Powder | ItemIngredient.LUMINOUS_POWDER (now "luminous_powder") | DONE-UNTESTED | No |
| Magic Hood | ItemMagicHood | DONE-UNTESTED (#252-255) | Partially - nametag half needed a coremod (confirmed no clean event exists in this Forge version); particle half found a real Forge event instead (`PotionColorCalculationEvent`), no ASM needed there |
| Magnetic Enchantment | EnchantmentMagnetic | DONE-UNTESTED (#288, 2026-09-28) | No (a plain `BlockEvent.HarvestDropsEvent` listener replaces the 1.12.2 ASM hook entirely - see the note below) |
| Notification Interface | BlockNotificationInterface, TileEntityNotificationInterface | BUGGY (#114, same GUI-open bug family) | No |
| Obsidian Skull | ItemObsidianSkull | DONE (#126); Baubles ring variant moot (Baubles dropped project-wide) | No |
| Obsidian Water Walking Boots | ItemObsidianWaterWalkingBoots | DONE (#131-132) | Yes (Block.addCollisionBoxesToList - port uses its own different design instead) |
| Online Detector | BlockOnlineDetector, TileEntityOnlineDetector | BUGGY (#75, GUI-close bug + text field not saving) | No |
| Peace Candle | BlockPeaceCandle, TileEntityPeaceCandle, WorldGenPeaceCandle | PARTIAL, DONE-UNTESTED (worldgen batch, #268-270 - mob-suppression only, no village-church generation, see note below) | No (reuses the existing `SpawnPlacementTransformer` coremod hook, not a new one) |
| Pitcher Plant | BlockPitcherPlant | DONE-UNTESTED (decorative only so far) | No |
| Platforms | BlockPlatform (6 wood types) | DONE-UNTESTED | No |
| Player Interface | BlockPlayerInterface, TileEntityPlayerInterface | DONE-UNTESTED | No |
| Portable Sound Dampener | ItemPortableSoundDampener | DONE-UNTESTED | No |
| Portkey | PortkeyItem | DONE-UNTESTED (#275-280, 2026-09-27) | Yes - only the custom magenta enchant-glow tint is dropped (would need a new coremod for one cosmetic detail; ships with the default vanilla glow color instead). The "camo" disguise system, initially also dropped, was re-implemented cleanly without reflection - see `PortkeyItemRenderer`'s own javadoc |
| Position Filter | ItemPositionFilter | DONE, needs retest (#136) | No |
| Potion Vaporizer | BlockPotionVaporizer, TileEntityPotionVaporizer | DONE (#96-99.1) | No |
| Quartz Glass | BlockQuartzGlass | DONE (#24-26, 28) | No |
| Quartz Lamp | BlockQuartzLamp | DONE (#30 - confirmed working 2026-09-27, back to the original mechanism after a same-day detour, see Lapis Lamp's note) | Yes (same `EntitySpawnPlacementRegistry` coremod as Lapis Lamp) |
| Rain Shield | BlockRainShield, TileEntityRainShield | PARTIAL, DONE-UNTESTED (#256-261 - mechanical suppression via coremod; client-side visual rain/snow rendering deliberately not ported, see #261) | Yes (World rain/snow suppression + EntityRenderer client rendering) |
| Rainbow Lamp | BlockRainbowLamp | DONE-UNTESTED | No |
| Redstone Activator | RedstoneActivatorItem | DONE-UNTESTED (#283-286, 2026-09-27) | Yes (World wireless signal, shared with Redstone Interface) |
| Redstone Interface | BasicRedstoneInterfaceBlock, RedstoneSignalHandler | DONE-UNTESTED (#283-286, 2026-09-27) | Yes (two new coremods: `WorldRedstonePowerTransformer`/`WorldReaderStrongPowerTransformer` patch `World.getRedstonePower`/`IWorldReader.getStrongPower` - the only way to make an arbitrary vanilla position appear powered with no real redstone source there, matching 1.12.2's own `World.getStrongPower`/`getRedstonePower` ASM patch; ground-truthed via `javap -c` that 1.14.4 moved strong-power computation onto a default `IWorldReader` method instead of a second `World` method - see `AsmHandler#overrideRedstonePower`/`overrideStrongPower`'s javadoc) |
| Redstone Observer | BlockRedstoneObserver, ItemRedstoneTool | DONE (#94-95) | Yes (RenderItem red enchant-glow - cosmetic detail, not yet re-added) |
| Redstone Remote | RedstoneRemoteItem | DONE-UNTESTED (#283-286, 2026-09-27) | Yes (World wireless signal, shared with Redstone Interface) - disclosed simplification: dropped the original's cosmetic ghost/camo-icon button row, see `RedstoneRemoteItem`'s javadoc |
| Runic Dust | ItemRuneDust, ItemRunePattern, BlockRuneBase | DONE-UNTESTED (#161-167) | Yes (old ModelRune ExtendedBlockState/VertexLighterFlat - replaced with a TESR, disclosed) |
| Sided Block of Redstone | BlockSidedRedstone | DONE (#50) | No |
| Slime Cube | BlockSlimeCube, TileEntitySlimeCube | DONE-UNTESTED (#110-111 - unblocked 2026-09-26, same `EntitySpawnPlacementRegistry` coremod as Lapis Lamp) | Yes (EntitySlime + WorldEntitySpawner) |
| Sound Box | BlockSoundBox, TileEntitySoundBox | DONE-UNTESTED | No |
| Sound Dampener | BlockSoundDampener, TileEntitySoundDampener | DONE-UNTESTED | No |
| Sound Pattern | ItemSoundPattern | DONE-UNTESTED | No |
| Sound Recorder | ItemSoundRecorder | DONE-UNTESTED | No |
| Spectre Anchor | ItemSpectreAnchor, SpectreAnchorCombineRecipe | DONE-UNTESTED (#281-282, 2026-09-27) | Yes (new `PlayerEntityTransformer` coremod redirecting `PlayerEntity.dropInventory()`'s call to `PlayerInventory.dropAllItems()` into `AsmHandler.dropAllItemsExceptAnchored` - `PlayerDropsEvent` no longer exists in this Forge version and `ItemTossEvent`/`LivingDropsEvent` don't cover the death-drop code path, confirmed via `javap -c` on all three before concluding a coremod was needed; verified via the standalone bytecode-verification harness against a full Gradle-resolved runtime classpath, and via a `runClient` boot smoke test) |
| Spectre Charger | BlockSpectreEnergyInjector | NOT STARTED (Spectre energy network) | No |
| Spectre Coils | BlockSpectreCoil, SpectreCoilHandler | NOT STARTED (Spectre energy network) | Yes (VertexLighterFlat glow) |
| Spectre Illuminator | SpectreIlluminatorItem, SpectreIlluminatorEntity | DONE-UNTESTED (#272-274, 2026-09-27) | Yes (new `IBlockReaderTransformer` coremod - `Block.getLightValue`'s 3-arg overload was removed in this Forge version, see `AsmHandler#overrideLightValue`'s javadoc) |
| Spectre Key | ItemSpectreKey, BlockSpectreCore, custom dimension | NOT STARTED | Yes (RenderItem cyan enchant-glow) |
| Spectre Lens | BlockSpectreLens, SpectreLensHandler | NOT STARTED | No |
| Spectre Sapling | block/spectretree/* | DONE (#36-39, simplified fixed-shape growth not worldgen-feature-based, disclosed divergence) | No |
| Spectre Tools | item/spectretools/* | NOT STARTED | Yes (RenderItem white enchant-glow, Spectre Sword) |
| Stable Ender Pearl | ItemStableEnderpearl | DONE (#124-125) | No |
| Stained Bricks | BlockStainedBrick | DONE (#52-53) | Yes (VertexLighterFlat glow - dropped, disclosed) |
| Summoning Pendulum | ItemSummoningPendulum | DONE-UNTESTED (#225-229) | No |
| Super Lubricent | BlockSuperLubricentIce/Platform, ItemIngredient.SUPERLUBRICENT_TINCTURE | DONE (#31-32, 113) | Yes (EntityLivingBase.travel friction - clean IForgeBlock.getSlipperiness replacement used) |
| Super Lubricent Boots | ItemSuperLubricentBoots | DONE-UNTESTED (#134-135 - unblocked 2026-09-26, switched from a Mixin that could never bootstrap on this Forge version to a coremod instead) | Yes (same friction hook) |
| Super Lubricent Stone | BlockSuperLubricentStone | DONE | Yes (same friction hook) |
| Time in a Bottle | ItemTimeInABottle, EntityTimeAccelerator | DONE-UNTESTED (#193-205) | No |
| Trigger Glass | BlockTriggerGlass | DONE (#15-20) | Yes (BlockFalling.canFallThrough - clean Block.canFallThrough override used) |
| Water Walking Boots | ItemWaterWalkingBoots | DONE, needs retest (#130) | Yes (Block.addCollisionBoxesToList - port uses its own different design instead) |
| Weather Eggs | ItemWeatherEgg, EntityThrownWeatherEgg | DONE-UNTESTED (#193-205) | No |

Rough tally: ~77 of 100 have some 1.14.4 code (many untested); ~22 not started; 1 removed per
explicit request.

**Magnetic Enchantment, Block Breaker, Block Destabilizer implemented (2026-09-28):** this session
ran with no Forge/Mojang Maven access at all (a cloud sandbox, not the usual local dev machine - see
`PORTING_PLAN.md`'s handoff note), so none of this could be `javap`-ground-truthed or build-verified
per this project's own working rule; flag everything below for a real retest before trusting it.
Picked up two of the "needs infrastructure" NOT-STARTED items in one pass since they share that
infrastructure:
- **Magnetic Enchantment** ported as a plain `Enchantment` (`EnchantmentType.DIGGER`, matching
  1.12.2's min/max enchantability and max-level-1 exactly) plus a `BlockEvent.HarvestDropsEvent`
  listener in `RandomThings` - canceling that event clears its own drop list before anything spawns
  (see `ForgeEventFactory.fireBlockHarvesting`), so the listener just redirects the copied drops into
  the harvester's inventory via `ItemHandlerHelper.giveItemToPlayer`. Replaces 1.12.2's ASM hook into
  `PlayerInteractionManager.tryHarvestBlock` (an `ItemCatcher`) entirely - no coremod needed, contrary
  to the wiki's own "ASM?" flag for this feature, because this Forge version's event already exposes
  the same pre-spawn mutable list.
- **Block Breaker** reuses that same listener rather than porting 1.12.2's own dedicated catch path:
  its `FakePlayer` holds an unbreakable pickaxe enchanted with Magnetic, so harvested drops land
  straight in the fake player's inventory the same way a real player's would, and the tile entity only
  has to drain that inventory into the target side afterward. The mining loop itself (per-tick real
  block-hardness accumulation via `BlockState#getPlayerRelativeBlockHardness`, a `FakePlayer` from
  `FakePlayerFactory`, `PlayerInteractionManager#tryHarvestBlock`) is the single riskiest piece of API
  surface in this batch to have shipped unverified.
- **Block Destabilizer** ported its BFS flood-fill/sorted-drop state machine near line-for-line, but
  spawns vanilla's own `net.minecraft.entity.item.FallingBlockEntity` directly instead of maintaining
  1.12.2's `EntityFallingBlockSpecial` (a parallel copy of vanilla's own falling-block entity that only
  existed to expose a public, settable `shouldDropItem` field) - that field is already public and
  mutable on this Forge version's vanilla class, so the custom entity class this feature was flagged
  as needing turns out to be unnecessary here. Disclosed simplifications: dropped the "lazy"/"fuzzy"
  toggle buttons' custom icon textures for plain text buttons (matching `IgniterScreen`'s own proven
  style) rather than porting a bespoke image-toggle-button widget, and dropped the always-on glow
  overlay (`VertexLighterFlat`-tinted texture layer in 1.12.2) since replicating it blind without build
  access risked shipping a subtly-wrong render layer for a purely cosmetic detail.

**Item Filter, deliberately skipped (2026-09-26):** its only two 1.12.2 consumers - Advanced Item
Collector and Filtered Super Lubricent Platform - are both already ported in this project, and both
already made their own disclosed simplification: a single built-in example-item filter slot (plain
item-type equality) instead of consuming the fully configurable `ItemItemFilter` item. Porting Item
Filter now would produce an item nothing in this codebase actually uses. User chose to leave it
NOT STARTED rather than build an orphaned item or retrofit the two already-shipped consumers - revisit
if a future feature actually needs the fully configurable version.

**Peace Candle's village-church generation, deferred (2026-09-27):** the wiki gives Peace Candle a
"~33% chance to generate inside a village church" - 1.12.2 built that church as a brand-new
`StructureVillagePieces` piece via ASM, since 1.12.2's village generator was hardcoded Java piece
classes. 1.14.4's village generator is a completely different, fully data-driven jigsaw/structure-
template system with no hardcoded piece classes left to hook into; reproducing a whole custom
building would mean hand-authoring NBT structure data and a template-pool override with no in-game
structure-block tooling available in this environment to build or export it correctly. Shipped
instead: the block and its "no natural mob spawning in a 3 chunk radius" behavior, fully working,
creative-menu only until natural generation exists. Revisit with real structure-block access.

**Spectre Illuminator, implemented (2026-09-27):** a floating orb placed by right-click that drifts to
hover above and center itself over its chunk, then lights the whole chunk up. Three disclosed
simplifications from 1.12.2's version, all covered in depth in `SpectreIlluminatorEntity`'s own javadoc:
(1) 1.12.2 tracked "is this chunk lit" as a separate persisted `WorldSavedData` registry mirrored to
clients via a custom network packet; collapsed here into the same static-live-instance-registry pattern
this port already uses for `SlimeCubeTileEntity`/`RainShieldTileEntity` (`SpectreIlluminatorEntity
.ILLUMINATORS`), since the entity's own ordinary position sync already gives the client everything it
needs - no separate persisted flag or packet at all. (2) The light-value override moved: 1.12.2
ASM-patched `Block.getLightValue(state, world, pos)` directly; that 3-argument overload doesn't exist in
this Forge version (`Block.getLightValue` now takes only a `BlockState`, confirmed via `javap -p`).
Ground-truthed via `javap -c` where block-light computation actually reads a light value in this version
instead - `BlockLightEngine.getLightValue(long)` calls `IBlockReader.getLightValue(BlockPos)`, a default
interface method inherited by every `IBlockReader` implementor with no override of its own - so the new
`IBlockReaderTransformer` coremod patches that one shared choke point instead (`AsmHandler
#overrideLightValue`), same "one dispatch point" pattern as `SpawnPlacementTransformer`. Verified via the
same standalone bytecode-verification harness used for the ocean-monument coremod earlier this session
(`CheckClassAdapter.verify` - zero errors) plus a real `runClient` boot confirming `Transforming
net/minecraft/world/IBlockReader` happens with no `VerifyError`/`LinkageError` and registries still
freeze cleanly afterward. (3) 1.12.2's magic-circle visual (`RenderSpectreIlluminator`) was built on this
mod's bespoke immediate-mode-GL `MKRRenderUtil` framework (not otherwise needed anywhere in this port -
see `EclipsedClockEntity`'s own disclosed simplification for the same framework); replaced with a plain
vanilla particle effect instead, same approach already used for `ArtificialEndPortalEntity`/
`EclipsedClockEntity`'s own cosmetic effects. Also fixed what looks like an authoring oversight in the
original: its item model was `"parent": "builtin/entity"` with no registered `ItemStackTileEntityRenderer`
ever hooked up to it, which would render blank - ported as a normal flat `item/generated` icon instead so
it's actually visible.

**Spectre Illuminator, real bug fixed (2026-09-27, same day, reported by user): no visual, no lighting.**
Two stacked bugs, both found by re-reading 1.12.2's actual client-side network handler
(`SpectreIlluminationClientHandler#setIlluminated`) rather than guessing. (1) `illuminated` was a plain
field synced only through NBT `readAdditional`/`writeAdditional` (save/load persistence) - the entity
spawn packet doesn't carry that, so the client's own copy of the entity never learned the server had set
it. Now a real synced `DataParameter<Boolean>`. (2) Even fixed, that alone wasn't enough - 1.12.2's own
client message handler *also* explicitly re-ran its light-recheck helper client-side upon receiving the
toggle, not just the server; simply knowing the flag changed doesn't itself force a client-side light
recompute. `relight()` (the `checkBlock` sweep) now runs from both sides: server on settling, client via
`notifyDataManagerChange` reacting to the synced value. Found and fixed the mirror-image gap on pickup
too while in there: `onRemovedFromWorld` wasn't relighting at all, so collecting the orb would have left
the chunk stuck lit - it's a generic Entity lifecycle hook (fires both sides), not server-only, so one
fix there covers both directions symmetrically, matching 1.12.2's own `toggleChunk` always relighting for
both the on and off transition.

**Same day, second real bug (reported by user via a crash log): the fix above crashed the client.**
`relight()`'s `checkBlock` sweep - ~83,000 positions (an 18x18x256 padded column) called synchronously in
one method call, a byte-for-byte port of 1.12.2's own `lightUpdateChunk` - overwhelmed this Forge
version's rewritten light engine (`ArrayIndexOutOfBoundsException` deep in `SectionLightStorage
.cancelSectionUpdates`/`LevelBasedGraph.bulkCancel`, a real engine bug, not a mod-code frame anywhere in
the stack). 1.12.2's old engine handled the same 83,000-call sweep fine because it was architecturally a
plain synchronous BFS with no internal capacity to overflow. Fixed with a new `SpectreIlluminatorRelight`
class: bounds the scanned height to just above the chunk's tallest block instead of the full 0-255, and
drains the remaining positions 256/tick via a new `WorldTickEvent` listener instead of one big burst -
matching this project's own established "drive a static queue off a tick event" pattern (same as
`EscapeRopeHandler`). Build clean, not yet re-tested in-game - third blind fix in a row for this one
feature.

**Same day, third real bug (found from diagnostic logging, not a guess): client never actually drained.**
User's retest log showed the server queue draining in ~5 seconds every time, but the client's own queue
(keyed by its separate `ClientWorld` instance, a different object from the server's `ServerWorld` even in
singleplayer) never drained at all - only relogging (a full fresh chunk resync) ever made the server's
already-correct light data visible. `WorldTickEvent` apparently doesn't fire for the client world in this
Forge version. Fixed by also draining via `ClientTickEvent` (already used elsewhere in this file for
`DiviningRodRenderer`). **User then confirmed live**: the chunk lit up without a relog, particles visible.

**Same day: user asked for a visible marker, and reported a fourth real bug (partial pickup revert).**
The entity had no visible model at all by design (matching 1.12.2's own renderer). Added one per request:
`SpectreIlluminatorEntity` now implements `IRendersAsItem` and renders via vanilla's own `SpriteRenderer`
(same mechanism as Thrown Golden Egg/Weather Egg) - since it already tracks the entity's real position,
this required no new animation code, the existing clicked-spot-to-chunk-center drift is the visual.
Separately, user reported that after picking the orb back up, the chunk edge where it first settled
stayed stuck lit while the rest went dark. Root cause: the relight sweep's chunk-boundary padding was
only 1 block, but vanilla block light naturally bleeds up to 15 blocks past a boundary once the interior
is uniformly flooded to level 14 - anything the bleed reached beyond that 1-block pad kept a stale bright
value nothing ever told to re-derive. Padding widened to 15 (vanilla's own max falloff distance) and the
drain rate bumped 256→512/tick to compensate for the ~7x larger sweep, still far below the crash
threshold from the second bug above. Perf-tuned further same day per user request: briefly split into
separate server/client drain rates (client-side `checkBlock` calls can trigger a chunk render-mesh
rebuild, server-side ones can't), then unified back into one shared `PER_TICK` constant for consistency
per a follow-up request, currently 256 for both sides after a final round of tuning requests.

**Time in a Bottle, real crash fixed (2026-09-27, reported by user): game crashed on use.**
`TimeAcceleratorEntity.target` was a plain field, set only by the constructor `TimeInABottleItem` calls
server-side - the client's own copy of the entity (built via the other constructor, the one the spawn
packet always uses) never had it set, so `tick()` calling `world.getTileEntity(null)` unconditionally
NPE'd immediately on the client. Fixed with a real synced `DataParameter<BlockPos>`, same pattern already
used for `EclipsedClockEntity`'s `HANGING_POS` and (independently, same session) `SpectreIlluminatorEntity
`'s `ILLUMINATED`. **User confirmed working** afterward - noted no visible entity, which was original,
disclosed-simplification behavior (no model at all, particles only, matching Spectre Illuminator's
original state too). Per user request, both `TimeAcceleratorEntity` and `SpectreIlluminatorEntity` got
the same treatment: `IRendersAsItem` + vanilla `SpriteRenderer` for a real item-icon billboard, needing
no new animation code since the billboard just tracks the entity's already-existing motion. Also per user
request: both "not enough stored time" cases (placing a new accelerator, upgrading an existing one)
now send the player a status message instead of silently doing nothing, matching this port's own
`sendStatusMessage` convention (`EnderMailboxBlock`/`EnderLetterItem`) - not something 1.12.2 itself did.

**Same day, two more real bugs plus a deliberate behavior change, all from one user message.** (1) The
new billboard clipped inside solid blocks, since the marker spawned dead-center in the clicked block -
very often solid. Fixed by offsetting the marker 0.7 blocks out along the actually-clicked face instead;
`TimeAcceleratorEntity.getTarget()` (which TE gets force-ticked) is unaffected, it's a separate field -
only the visual position moved. The "already placed one here" detection had to switch from a position-
based box scan (which the offset marker could now fall outside of) to matching by `getTarget()` directly.
(2) Per explicit user request: the Eclipsed Clock's Time-in-a-Bottle day-skip is no longer an instant
`setDayTime` jump (which is what both 1.12.2 and this port's first pass did) - it now animates, advancing
in large steps over several real seconds so the sky visibly time-lapses, purely server-side (the client
already gets the changing time through vanilla's own periodic sync, same mechanism as the ordinary day/
night cycle). (3) Real bug found and fixed (reported by user): the Eclipsed Clock's time-display label
sat about a block too high - `javap -c` on vanilla's own `renderLivingLabel` showed it already adds
`entity.getHeight() + 0.5` (≈1.0 for this entity) on top of the `y` it's given, and both call sites in
`EclipsedClockEntityRenderer` were stacking their own offset on top of that baked-in one instead of
accounting for it. Both now subtract 1.0 to compensate.

**Same day, one more round on the Time Accelerator billboard: all 6 faces, closer, wider particle
spread.** A single `SpriteRenderer` billboard can only sit at one fixed position, so "every side"
(explicit user request) needed a real custom renderer. Ground-truthed `SpriteRenderer#doRender`'s exact
transform sequence via `javap -c` rather than guessing, then wrote `TimeAcceleratorEntityRenderer` to
repeat that same sequence once per `Direction` (6 total), each offset 0.55 blocks from the entity's
position along that axis - just outside the block's own half-width, matching the "closer to the
surface" request. The marker's own position moved back to the plain block center (undoing the previous
round's single-face offset, no longer needed once every face gets its own icon) - `getTarget()` stayed
the only thing that matters for game logic throughout, so the "already placed one here" detection never
needed touching again. Particles widened from a tight radius around one point to spread across the
entire block's volume.

**Portkey implemented (2026-09-27):** right-click a block to bind the key to that spot (same "remember
where I clicked" idiom already proven by `PositionFilterItem`); drop it, and once it's sat undisturbed
for 5 real seconds it stops despawning and glows to show it's primed; the next player to pick it up gets
teleported to the bound location instead of collecting it (same safe-landing-spot search as 1.12.2 - a
5x5 column around the target, scanning down 10 blocks for a solid-topped position with 2 air blocks
above). The original's custom magenta enchant-glow tint needed its own ASM hook (`AsmHandler
#enchantmentColorHook`) - ships with the default vanilla glow color instead via a plain `Item#hasEffect`
override, no coremod needed (purely cosmetic, not worth a new coremod for). The original's full-screen
HUD directional beam (pointing at the bound location while held) is also dropped, in favor of the same
shift-to-reveal tooltip coordinates `PositionFilterItem` already uses for an identical need, rather than
porting a whole custom overlay-rendering system for one item. `Item.onEntityItemUpdate` (the dropped-item
tick hook this needs for its priming countdown) doesn't exist in this Forge build, same finding as
`StableEnderpearlItem`/`FlooTokenItem` - same fix, a `WorldTickEvent` listener scanning dropped
`ItemEntity`s of this type.

**Camo disguise system re-implemented, 2026-09-27 (same day, per explicit user request after research):**
initially dropped as a disclosed simplification, since 1.12.2's version worked via direct reflection into
its own internal item-model registry map with no 1.14.4 equivalent. Turned out 1.14.4 has a clean, fully
public mechanism purpose-built for exactly this - the same one vanilla itself uses for shulker boxes.
Confirmed via `javap -c` that `ItemRenderer.renderItem` checks `Item.getTileEntityItemStackRenderer()`
(set via `Item.Properties#setTEISR`) for *every* render context (inventory, held, dropped, item frame),
not just one. `PortkeyItemRenderer` reads the "camo" NBT and either renders the disguise stack directly
(a different item, no recursion risk) or - when there's no camo - renders a second, separate "special"
model (`randomthings:portkey_base`, registered via `ModelLoader#addSpecialModel` in a new
`ModelRegistryEvent` listener) to avoid recursing back into its own `builtin/entity` model. Caught and
fixed a real bug via the `runClient` smoke test before calling it done: the special model's resource
path initially double-included the "item/" folder prefix the loader already adds automatically
(`FileNotFoundException: .../models/item/item/portkey_base.json`) - fixed by passing just the bare name.
The camo-combine recipe (`PortkeyCamoRecipe`) reuses this port's own already-proven `SpecialRecipe`/
`SpecialRecipeSerializer` pattern (same shape as `GoldenCompassSetPositionRecipe`) instead of 1.12.2's
`SimpleRecipe` - the donor item is returned unconsumed, matching 1.12.2 exactly. No reflection anywhere
in this port's version. Full build clean, plus a second `runClient` smoke test confirming the fix (no
missing-model warnings). `TESTING_CHECKLIST.md` #279-280 added. Not yet tested in-game.

**Ancient Furnace's biome conversion, corrected (2026-09-27, same day as the note below):** an earlier
version of this note claimed real biome reassignment was impossible in 1.14.4 - that was wrong.
`Chunk.getBiomes()` returns the chunk's own live `Biome[]` array (not a defensive copy), and every
gameplay biome read goes through that same array fresh each call, so mutating it in place is a
legitimate public-API reassignment, no reflection needed. Now does the real thing: melts surface
snow/ice (as before) AND reassigns each converted column to its warmer biome counterpart, using the
same cold->warm mapping table 1.12.2's own `AncientFurnaceConversion` used, re-mapped onto 1.14.4's
renamed biome fields. **Disclosed gap that remains**: the mutation is immediately correct for
everything read server-side (spawn tables, weather, etc.), but an already-connected client won't see
the visual grass/foliage/fog color shift until the chunk reloads - that's driven by the client's own
separate copy of the biome data, and re-syncing it live would need the server to resend a full
`SChunkDataPacket`, not attempted this session. See `AncientFurnaceTileEntity`'s own javadoc for the
full reasoning and the exact biome mapping.

## Not migrating

Confirmed via full-text search: none of these appear in any of the 100 wiki pages. Split into what's
already shipped (kept, per explicit user decision) and what's simply skipped going forward.

**Already shipped, kept anyway (undocumented but working, user chose not to rip out):**
- **Bottle of Air** (`ItemBottleOfAir`) — drink-underwater breath refill, already PASSing (#123). Kept.

**Removed this session (2026-09-27):**
- **Plate family** (`block/plates/*` — Accelerator, Bouncy, Collection, Corrector, Directional
  Accelerator, Extraction, Filtered Redirector, Item Rejuvenator, Item Sealer, Processing, Redirector,
  Redstone Plate — 11 blocks). Undocumented bonus content; user decided it wasn't valuable enough to
  keep migrating. Deleted entirely (all 11 block classes + `PlateBlock` base, 3 tile entities, 3
  containers, 3 screens, and all registrations/blockstates/models/gui textures/loot tables/lang
  entries).
- **Special Chest** (`SpecialChestBlock`, `SpecialChestTileEntity`, its renderers, and the
  `OceanMonumentTransformer` coremod + `AsmHandler#placeSpecialChest` added earlier this same session
  to place it in ocean monuments) — a reskinned loot-chest delivery vessel for documented loot items,
  not a documented feature in its own right. User decided it wasn't valuable enough to keep. Deleted
  entirely, including the now-orphaned ocean-monument coremod/transformer JS and its `coremods.json`
  entry, and the `special_chest_water` loot table (chest + block variants).
- **Sakanade** (`SakanadeBlock`) — decorative shearable mushroom plant, dropped `sakanade_spores`.
  Deleted entirely (block, item registration, blockstate/models/textures/lang entries).

**Removed a prior session:**
- **Blood Rose** (`BloodRoseBlock`, `BloodRoseTileEntity`, the VFX framework it alone used) — not even
  1.12.2 tech debt; it was new content added directly on the 1.14.4 branch with no 1.12.2 source or
  wiki page. Deleted entirely (block/TE/worldgen feature/VFX handler/network message and all
  registrations, assets, lang entries) per explicit user decision.

**Not started, and will not be started (undocumented):**
- **Festival system** (`handler/festival/*`, triggered by feeding a villager `ItemIngredient
  .PRECIOUS_EMERALD`) — no wiki page.
- **Player Soul / Revive Circle** (`EntitySoul`, `EntityReviveCircle`) — a death-marker/revival
  mechanic spawned on player death; no wiki page describes any death/revival feature.
- **Rez Stone** (`ItemRezStone`) — was already a dead stub in 1.12.2 itself (constructor body is a
  commented-out TODO for loot registration).
- **Dungeon Chest Generator** (`ItemDungeonChestGenerator`) — developer/creative debug tooling, not a
  documented player feature.
- **Voxel Projector** (`BlockVoxelProjector`, `handler/magicavoxel/*`) — a runtime `.vox`-model
  renderer; unlike Diaphanous Blocks and Light Redirector (which share its "generic runtime
  block-model renderer" need but DO have wiki pages), Voxel Projector itself has none.
- **Nature Core** (`TileEntityNatureCore`, `WorldGenCores`, `BlockNatureCore`) — an autonomous
  world-generated plant/animal-breeding structure; no matching wiki title.
- **Third-party mod compatibility** (`handler/compability/{baubles,oc,te,tc,jei}/*`) — Baubles,
  OpenComputers, Thermal Expansion/Tinkers' Construct hooks, JEI integration. Not their own documented
  feature (mentioned only incidentally inside Redstone Interface's own page); Baubles support is
  already dropped project-wide per earlier decisions this port made independently.
- **`RTCommand`** — a `/randomthings` debug/admin console command, not a documented feature.
- **ASM bytecode-patching infrastructure itself** (`asm/ClassTransformer`, `asm/LoadingPlugin`, etc.) —
  pure tooling, not a feature; see the consolidated ASM/coremod batch in the plan file for what
  replaces it.

`BiomeSpectral`/custom-biome registration and `id_card` (`ItemIDCard`) are NOT separate entries — the
former folds under Spectre Key's page, the latter is a shared dependency referenced by both Chat
Detector's and Emerald Compass's pages, not an orphan.
