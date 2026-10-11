# Minecraft 1.15 (final release 1.15.2) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2019-12-09 ✔ |
| Final release | 1.15.2 — 2020-01-17 ✔ |
| Java | 8 |
| Forge | 31.2.62 ✔ |
| NeoForge | n/a |
| Fabric | ✔ |
| Mappings | MCP / official |
| Upstream mod status | No upstream branch ✔ |
| Tier | B — feasible, secondary demand |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | M (≈0.3× the 1.14.4 port) |

## Era

Small content update (bees) with a significant rendering refactor.

## What changes (relevant to this mod)

- Rendering moves to `MatrixStack`, `IRenderTypeBuffer`, `RenderType` (TESRs, entity renderers, item renderers all change signature).
- Lighting and biome-noise tweaks; Forge 31 refined loading.
- Item/block APIs largely stable vs 1.14.4.

## Impact on Random Things

- All TileEntityRenderers (Rune Base, Fluid Display, Biome Radar, Spirit, Portkey TEISR) and the item-renderer coremods (Dyeing Machine) change.
- GUI code mostly unchanged.
- Coremod targets (rendering classes: `GameRenderer`, `ItemRenderer`, `LivingRenderer`) change shape.

## Source and path

This repo (direct forward port).

## Verdict

Cheap forward step; but short-lived. Do it as a stepping stone to 1.16.5.

## To verify before starting

- [ ] Exact renderer signatures and which coremod targets still exist.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
