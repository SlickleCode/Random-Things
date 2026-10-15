# Coremods (Forge JS transformers)

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

SpongePowered Mixin cannot bootstrap from a mod jar on this Forge (its `ILaunchPluginService` must be on the
launcher classpath before mods are scanned), and the dev client can't load manifest-declared configs either.
Both former Mixins (Blazing Fire, Super Lubricent Boots) were converted to coremods and all Mixin build
infrastructure deleted. Coremods are self-contained in the jar: `META-INF/coremods.json` →
`transformer/*.js`, hooks in `asm/AsmHandler` (common) / `ClientAsmHandler` (client).

| Transformer | Target (1.18.2) | Purpose |
| --- | --- | --- |
| `VertexLighterFlat` | Forge `VertexLighterFlat.processQuad` (after `updateColor`; colour array is local 4) | Light-tint hook (placeholder tintIndex 12340) |
| `FireBlockTransformer` | `FireBlock.tryCatchFire` + `getFireTickDelay` call | Blazing Fire 4x catch chance / die-out / age growth |
| `SuperLubricentBootsTransformer` | `LivingEntity.travel`, the `BlockState.getFriction` call | Boots -> max slipperiness unless sneaking |
| `SpawnPlacementTransformer` | `SpawnPlacements.checkSpawnRules` (`m_21759_`) | Slime Cube ALLOW, Lapis/Quartz Lamp, Peace Candle DENY (Lapis ALLOW guarded for Peaceful) |
| `MagicHoodTransformer` | `LivingEntityRenderer.shouldShowName` (all returns) | Hide nametag |
| `RainShieldTransformer` | `Level.isRainingAt` (`m_46758_`, all returns) | Mechanical rain/snow suppression |
| `WorldRendererTransformer` | `LevelRenderer.renderSnowAndRain` / `tickRain`, the `getBiome` call (now returns `Holder<Biome>`) | Visual rain suppression |
| `IBlockReaderTransformer` | `BlockGetter.getLightEmission(BlockPos)` | Spectre Illuminator light |
| `PlayerEntityTransformer` | `Inventory.dropAll` call in `Player.dropEquipment` | Spectre Anchor keeps items |
| `WorldRedstonePowerTransformer` / `WorldReaderStrongPowerTransformer` | `Level.getSignal` / `LevelReader.getDirectSignal` | Wireless Redstone Interface signals |
| `BlockRendererDispatcherTransformer` | `BlockRenderDispatcher.renderBatched` (IModelData overload) | Light Redirector "periscope" |
| `ItemRendererTransformer` / `ArmorLayerTransformer` | `ItemRenderer.renderQuadList` item colour + capture the stack in `ItemRenderer.render`; `HumanoidArmorLayer.renderArmorPiece` (`DyeableLeatherItem.getColor`, `renderModel` channels) | Dyeing Machine recolor; enchant-glint colour |
| `RenderTypeTransformer` | the seven glint `RenderType` getters (`glint()`, `entityGlint()`, ...) return `ClientAsmHandler.tintGlint(type)` | Dyeing Machine glint result, Redstone Tool, Spectre Key/Sword, Portkey, Escape Rope |

Names are matched as Mojang name **or** SRG id (`m_...`); all anchors were re-read in the 1.18.2 bytecode on 2026-10-15. Verified so far: dev client/server only. **Not yet verified: production (SRG) jar, and the visual result of the glint/armor/render-dispatch transformers.** (`GameRendererTransformer` and `TeleporterTransformer` from the 1.14.4 table no longer exist.)

Patterns: wrap the value before an `IRETURN` (collect all return nodes by reference first, then insert); or
insert an early conditional return at method head. Verification (no harness exists in the repo; write a
throwaway one each time): load the real class bytes, run the shipped `.js` through Nashorn's `ScriptEngineManager`
(not `jjs`, whose `-cp` parsing breaks), re-serialize with `COMPUTE_FRAMES`, and run
`CheckClassAdapter.verify` with the exact `runtimeClasspath` (get it with a throwaway Gradle init-script
task). Then a `runClient` boot confirms `Transforming …` with no `VerifyError`/`LinkageError`. Classes only
loaded in a live world (spawn registry, teleporter) need this check.

