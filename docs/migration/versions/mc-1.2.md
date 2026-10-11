# Minecraft 1.2 (final release 1.2.5) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2012-02-29 ✔ (1.2.1) |
| Final release | 1.2.5 — 2012-03-29 ✔ |
| Java | 6 |
| Forge | 3.4.9.171 ✔ |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP (SRG appears around here) |
| Upstream mod status | none ✔ |
| Tier | D — not feasible or not recommended (pre-1.7) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | XL for a small subset |

## Era

A big early-Forge era; many classic mods lived here.

## What changes (relevant to this mod)

- Numeric IDs, `BaseMod`/`ModLoader` or FML `@Mod` (early).
- The ore dictionary and Forge GUI/packet hooks appeared during the 1.2–1.4 era (exact build unverified).
- Tile entities, containers and `IGuiHandler` exist; no JSON recipes/models.

## Impact on Random Things

- Simple blocks and items are portable in concept; anything using capabilities, fluids, advanced rendering, dimensions or the 17 engine hooks is not.

## Source and path

Would be written from the 1.12.2 original, heavily cut.

## Verdict

Not recommended.

## To verify before starting

- [ ] Check which Forge hooks exist (ore dictionary, GUI handler, packet handling) before scoping any subset.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
