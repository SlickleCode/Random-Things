# Minecraft 1.13 (final release 1.13.2) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2018-07-18 ✔ |
| Final release | 1.13.2 — 2018-10-22 ✔ |
| Java | 8 |
| Forge | 25.0.223 ✔ (highest build seen) |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP (snapshot); official mappings appear later |
| Upstream mod status | No upstream branch ✔ |
| Tier | C — feasible but low value / short-lived or very expensive |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | M–L (regression of 1.14.4 code) |

## Era

"The Flattening": metadata removed, tags, new rendering assets, commands (Brigadier). Short-lived, Forge arrived late.

## What changes (relevant to this mod)

- Item/block metadata flattened into discrete items/blocks.
- Tags replace the ore dictionary; capabilities still use `hasCapability` (the `LazyOptional` form arrives in 1.14; unverified).
- Mod containers via `IGuiHandler`→ `INamedContainerProvider` only from 1.14.
- Forge for 1.13.2 started with an experimental 25.0.x and a different loading system (FML 2).

## Impact on Random Things

- Same flattening work already done for 1.14.4; this version is the 1.14.4 port minus later APIs.
- Most code in this repo would need to be regressed to the 1.13.2 API (GUIs, `ModDimension`, containers).

## Source and path

This repo (1.14.4) as the base, back-ported; or skip entirely.

## Verdict

Low value: short-lived version superseded by 1.14.4. "Optional / skip".

## To verify before starting

- [ ] Which 1.14.4 features (containers, `INamedContainerProvider`, `ModDimension`) are absent in 1.13.2.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
