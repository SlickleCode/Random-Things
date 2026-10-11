# Minecraft 1.3 (final release 1.3.2) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2012-07-31 ✔ (1.3.1) |
| Final release | 1.3.2 — 2012-08-15 ✔ |
| Java | 6 |
| Forge | 4.3.5.318 ✔ |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP |
| Upstream mod status | none ✔ |
| Tier | D — not feasible or not recommended (pre-1.7) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | XL for a subset |

## Era

Single-player now runs an integrated server, so client/server code split matters for the first time.

## What changes (relevant to this mod)

- Integrated server (sided code becomes mandatory).
- Numeric IDs; Forge 4.x.

## Impact on Random Things

- Same as 1.2.5 plus side-safety rules (see pitfall E1/E4).

## Source and path

From the 1.12.2 original.

## Verdict

Not recommended.

## To verify before starting

- [ ] As above.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
