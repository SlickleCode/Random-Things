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
| Advanced Redstone Interface | BlockAdvancedRedstoneInterface, TileEntityAdvancedRedstoneInterface | NOT STARTED | Yes (World wireless signal) |
| Advanced Redstone Repeater | BlockAdvancedRedstoneRepeater, TileEntityAdvancedRedstoneRepeater | DONE | No |
| Advanced Redstone Torch | BlockAdvancedRedstoneTorch, TileEntityAdvancedRedstoneTorch | DONE-UNTESTED | No |
| Analog Emitter | BlockAnalogEmitter, TileEntityAnalogEmitter | DONE | No |
| Ancient Furnace | BlockAncientFurnace, TileEntityAncientFurnace, WorldGenAncientFurnace | NOT STARTED | No |
| Artificial End Portal | EntityArtificialEndPortal, ItemIngredient.EVIL_TEAR | DONE-UNTESTED (#238-243) | No |
| Beans | BlockBeanSprout/BeanStalk/Pod, ItemBean, ItemBeanStew | DONE | No |
| Biome Blocks | BlockBiomeStone, BlockBiomeGlass, ItemBiomeCrystal | DONE-UNTESTED | No |
| Biome Radar | BlockBiomeRadar, TileEntityBiomeRadar, ItemIngredient.BIOME_SENSOR | DONE-UNTESTED | No |
| Blaze and Steel | ItemBlazeAndSteel, BlockBlazingFire | DONE | No |
| Block Breaker | BlockBlockBreaker, TileEntityBlockBreaker | NOT STARTED (needs enchantment system) | No |
| Block Destabilizer | BlockBlockDestabilizer, TileEntityBlockDestabilizer, EntityFallingBlockSpecial | NOT STARTED (needs new falling-entity type) | Yes (VertexLighterFlat glow) |
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
| Lapis Lamp | BlockLapisLamp | DONE-UNTESTED (#29 - unblocked 2026-09-26 via `EntitySpawnPlacementRegistry` coremod, shared with Slime Cube) | Yes (Block.getLightValue is Spectre Illuminator's, NOT this - lamp itself needs no ASM; the spawn-prevention half needed the shared coremod, now built) |
| Lava Charm | ItemLavaCharm | DONE (#127) | No |
| Lava Waders | ItemLavaWader | DONE, needs retest (#128-129, 133) | Yes (Block.addCollisionBoxesToList - port uses its own different design instead) |
| Light Redirector | BlockLightRedirector, TileEntityLightRedirector | NOT STARTED (needs generic runtime block-model renderer) | Yes (BlockRendererDispatcher) |
| Lotus | BlockLotus, LotusBlossomItem | DONE (#66-67.1) | No |
| Luminous Blocks | BlockBlockLuminous(Translucent) | DONE (#57-58) | No |
| Luminous Powder | ItemIngredient.LUMINOUS_POWDER (now "luminous_powder") | DONE-UNTESTED | No |
| Magic Hood | ItemMagicHood | DONE-UNTESTED (#252-255) | Partially - nametag half needed a coremod (confirmed no clean event exists in this Forge version); particle half found a real Forge event instead (`PotionColorCalculationEvent`), no ASM needed there |
| Magnetic Enchantment | EnchantmentMagnetic | NOT STARTED (no enchantment package exists yet) | Yes (PlayerInteractionManager.tryHarvestBlock) |
| Notification Interface | BlockNotificationInterface, TileEntityNotificationInterface | BUGGY (#114, same GUI-open bug family) | No |
| Obsidian Skull | ItemObsidianSkull | DONE (#126); Baubles ring variant moot (Baubles dropped project-wide) | No |
| Obsidian Water Walking Boots | ItemObsidianWaterWalkingBoots | DONE (#131-132) | Yes (Block.addCollisionBoxesToList - port uses its own different design instead) |
| Online Detector | BlockOnlineDetector, TileEntityOnlineDetector | BUGGY (#75, GUI-close bug + text field not saving) | No |
| Peace Candle | BlockPeaceCandle, TileEntityPeaceCandle, WorldGenPeaceCandle | NOT STARTED | Yes (WorldEntitySpawner mob-suppression + StructureVillagePieces$Church worldgen) |
| Pitcher Plant | BlockPitcherPlant | DONE-UNTESTED (decorative only so far) | No |
| Platforms | BlockPlatform (6 wood types) | DONE-UNTESTED | No |
| Player Interface | BlockPlayerInterface, TileEntityPlayerInterface | DONE-UNTESTED | No |
| Portable Sound Dampener | ItemPortableSoundDampener | DONE-UNTESTED | No |
| Portkey | ItemPortKey | NOT STARTED | Yes (RenderItem magenta enchant-glow) |
| Position Filter | ItemPositionFilter | DONE, needs retest (#136) | No |
| Potion Vaporizer | BlockPotionVaporizer, TileEntityPotionVaporizer | DONE (#96-99.1) | No |
| Quartz Glass | BlockQuartzGlass | DONE (#24-26, 28) | No |
| Quartz Lamp | BlockQuartzLamp | DONE (#30) | No |
| Rain Shield | BlockRainShield, TileEntityRainShield | PARTIAL, DONE-UNTESTED (#256-261 - mechanical suppression via coremod; client-side visual rain/snow rendering deliberately not ported, see #261) | Yes (World rain/snow suppression + EntityRenderer client rendering) |
| Rainbow Lamp | BlockRainbowLamp | DONE-UNTESTED | No |
| Redstone Activator | ItemRedstoneActivator | NOT STARTED | Yes (World wireless signal, shared with Redstone Interface) |
| Redstone Interface | BlockBasicRedstoneInterface, RedstoneSignalHandler | NOT STARTED | Yes (World wireless signal) |
| Redstone Observer | BlockRedstoneObserver, ItemRedstoneTool | DONE (#94-95) | Yes (RenderItem red enchant-glow - cosmetic detail, not yet re-added) |
| Redstone Remote | ItemRedstoneRemote | NOT STARTED | Yes (World wireless signal, shared with Redstone Interface) |
| Runic Dust | ItemRuneDust, ItemRunePattern, BlockRuneBase | DONE-UNTESTED (#161-167) | Yes (old ModelRune ExtendedBlockState/VertexLighterFlat - replaced with a TESR, disclosed) |
| Sided Block of Redstone | BlockSidedRedstone | DONE (#50) | No |
| Slime Cube | BlockSlimeCube, TileEntitySlimeCube | DONE-UNTESTED (#110-111 - unblocked 2026-09-26, same `EntitySpawnPlacementRegistry` coremod as Lapis Lamp) | Yes (EntitySlime + WorldEntitySpawner) |
| Sound Box | BlockSoundBox, TileEntitySoundBox | DONE-UNTESTED | No |
| Sound Dampener | BlockSoundDampener, TileEntitySoundDampener | DONE-UNTESTED | No |
| Sound Pattern | ItemSoundPattern | DONE-UNTESTED | No |
| Sound Recorder | ItemSoundRecorder | DONE-UNTESTED | No |
| Spectre Anchor | ItemSpectreAnchor | NOT STARTED | Yes (InventoryPlayer.dropAllItems) |
| Spectre Charger | BlockSpectreEnergyInjector | NOT STARTED (Spectre energy network) | No |
| Spectre Coils | BlockSpectreCoil, SpectreCoilHandler | NOT STARTED (Spectre energy network) | Yes (VertexLighterFlat glow) |
| Spectre Illuminator | ItemSpectreIlluminator, EntitySpectreIlluminator | NOT STARTED | Yes (Block.getLightValue - dynamic per-position lighting) |
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

Rough tally: ~72 of 100 have some 1.14.4 code (many untested); ~27 not started; 1 removed per
explicit request.

**Item Filter, deliberately skipped (2026-09-26):** its only two 1.12.2 consumers - Advanced Item
Collector and Filtered Super Lubricent Platform - are both already ported in this project, and both
already made their own disclosed simplification: a single built-in example-item filter slot (plain
item-type equality) instead of consuming the fully configurable `ItemItemFilter` item. Porting Item
Filter now would produce an item nothing in this codebase actually uses. User chose to leave it
NOT STARTED rather than build an orphaned item or retrofit the two already-shipped consumers - revisit
if a future feature actually needs the fully configurable version.

## Not migrating

Confirmed via full-text search: none of these appear in any of the 100 wiki pages. Split into what's
already shipped (kept, per explicit user decision) and what's simply skipped going forward.

**Already shipped, kept anyway (undocumented but working, user chose not to rip out):**
- **Plate family** (`block/plates/*` — Accelerator, Bouncy, Collection, Corrector, Directional
  Accelerator, Extraction, Filtered Redirector, Item Rejuvenator, Item Sealer, Processing, Redirector,
  Redstone Plate — 11 blocks). By far the largest undocumented chunk; already extensively tested
  (checklist rows 6-14, 40-48, 78-79, 109, 113). Kept as bonus content.
- **Special Chest** (`BlockSpecialChest`, `WorldGenOceanChest`) — a reskinned loot-chest delivery
  vessel for documented loot items (e.g. Water Walking Boots), not a documented feature in its own
  right. Kept.
- **Sakanade** (`BlockSakanade`, `WorldGenSakanade`) — decorative shearable mushroom plant. Kept.
- **Bottle of Air** (`ItemBottleOfAir`) — drink-underwater breath refill, already PASSing (#123). Kept.

**Removed this session:**
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
