# Minecraft 1.8 (final release 1.8.9) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2014-09-02 ✔ |
| Final release | 1.8.9 — 2015-12-03 ✔ |
| Java | 8 |
| Forge | 11.15.1.2318 ✔ |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP (SRG at runtime) |
| Upstream mod status | Branch `1.8.9` exists upstream ✔ |
| Tier | B — feasible, secondary demand |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | L (re-port of 1.12.2 content to 1.8.9 APIs; rendering/capabilities gap) |

## Era

Beloved PvP/modding version; JSON models arrive.

## What changes (relevant to this mod)

- JSON blockstates/models/item models replace `IIcon` registration; `IBlockState` + `PropertyX` replace metadata.
- `BlockPos` and `EnumFacing` replace int coordinates and sides.
- `IMessage`-based networking; Forge fluid/energy APIs as in 1.7.
- Forge capability system is just appearing (verify exact build) — otherwise interface based.
- Java 8; ForgeGradle 2.x.

## Impact on Random Things

- Models/blockstates: the assets of the 1.12.2 original largely transfer, with differences in format.
- GUIs/containers: same as 1.12.2 style (`GuiContainer`, `IGuiHandler`).
- Capabilities may be missing in early builds — inventory/fluid/energy features need interface fallbacks.
- Coremods/ASM: classic transformers (the original's approach).

## Source and path

Upstream branch `1.8.9` (author only back-ported bug fixes there) + the 1.12.2 original for features.

## Verdict

Feasible; upstream branch already exists but is feature-limited. "Legacy tier B".

## To verify before starting

- [ ] Which Forge build introduced capabilities for 1.8.9.
- [ ] Feature set of the existing upstream 1.8.9 branch vs the 1.12.2 branch.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
