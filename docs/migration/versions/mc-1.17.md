# Minecraft 1.17 (final release 1.17.1) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2021-06-08 ✔ |
| Final release | 1.17.1 — 2021-07-06 ✔ |
| Java | 16 |
| Forge | 37.1.1 ✔ |
| NeoForge | n/a |
| Fabric | ✔ |
| Mappings | Official Mojang names become the default in Forge |
| Upstream mod status | No upstream branch ✔ |
| Tier | C — feasible but low value / short-lived or very expensive |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | L (≈0.7×; mostly mechanical but touches everything) |

## Era

Caves & Cliffs part 1; short-lived but a big renaming event.

## What changes (relevant to this mod)

- Mojang names throughout: `TileEntity`→`BlockEntity`, `World`→`Level`, `BlockState` usage same; mass package moves.
- Block entities must supply a ticker (`BlockEntityTicker`) instead of `ITickableTileEntity`.
- Java 16; ForgeGradle 5.
- Rendering: `RenderType`/shaders reworked (core shader JSON).

## Impact on Random Things

- Mechanical mass rename across ~all classes (automatable with a mapping script).
- Ticking tile entities (Item Collector, Block Breaker, Iron Dropper, Spectre Illuminator relight) change to tickers.
- Coremod targets renamed (all Mojang names).

## Source and path

This repo (forward from 1.16.5).

## Verdict

Short-lived version; treat as a stepping stone to 1.18.2. "Optional".

## To verify before starting

- [ ] Whether JS coremods still load on Forge 37.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
