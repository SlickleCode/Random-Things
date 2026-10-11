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

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
