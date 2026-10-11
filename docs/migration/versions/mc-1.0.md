# Minecraft 1.0 (final release 1.0) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2011-11-17 ✔ |
| Final release | 1.0 — 2011-11-17 ✔ |
| Java | 6 |
| Forge | none ✔ (Forge's earliest build is for 1.1) |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | none (obfuscated; early MCP) |
| Upstream mod status | none ✔ (no upstream branch) |
| Tier | D — not feasible or not recommended (pre-1.7) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | N/A |

## Era

Release of the game. No standard mod loader: mods patched `minecraft.jar` directly or used Risugami's ModLoader.

## What changes (relevant to this mod)

- Numeric block/item IDs; no registry names.
- No shared mod API: no capabilities, no fluid/energy APIs, no JSON assets, no ore dictionary.
- Single-player and multiplayer are separate code bases (integrated server arrives in 1.3).

## Impact on Random Things

- Nearly every feature area (tile-entity GUIs, networking, recipes, dimension, wireless redstone) would be rewritten against raw vanilla internals.
- No Forge, so no hook points and no way to share a build with later versions.

## Source and path

Nothing reusable. Would be a ground-up re-implementation of a subset.

## Verdict

Not feasible / not recommended. Document as unsupported.

## To verify before starting

- [ ] Confirm there is real player demand before even scoping; the only loader option is ModLoader-era patching.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
