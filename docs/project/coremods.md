# Coremods (Forge JS transformers)

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

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
| `ItemRendererTransformer` / `ArmorLayerTransformer` | item quad color / armor layer; (1.16.5) also capture the stack being drawn in `renderItem` / `BipedArmorLayer.func_241739_a_` | Dyeing Machine recolor; enchant-glint colour |
| `RenderTypeTransformer` (1.16.5 only) | the seven glint `RenderType` getters return `ClientAsmHandler.tintGlint(type)` (tinted wrapper type for stacks with a custom glint colour) | Dyeing Machine glint result, Redstone Tool, Spectre Key/Sword, Portkey, Escape Rope |

Patterns: wrap the value before an `IRETURN` (collect all return nodes by reference first, then insert); or
insert an early conditional return at method head. Verification (no harness exists in the repo; write a
throwaway one each time): load the real class bytes, run the shipped `.js` through Nashorn's `ScriptEngineManager`
(not `jjs`, whose `-cp` parsing breaks), re-serialize with `COMPUTE_FRAMES`, and run
`CheckClassAdapter.verify` with the exact `runtimeClasspath` (get it with a throwaway Gradle init-script
task). Then a `runClient` boot confirms `Transforming …` with no `VerifyError`/`LinkageError`. Classes only
loaded in a live world (spawn registry, teleporter) need this check.

