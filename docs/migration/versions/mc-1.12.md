# Minecraft 1.12 (final release 1.12.2) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2017-06-02 ✔ |
| Final release | 1.12.2 — 2017-09-18 ✔ |
| Java | 8 |
| Forge | 14.23.5.2864 ✔ (latest) |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP (SRG at runtime); ForgeGradle 2.3 |
| Upstream mod status | Branch `1.12.2` exists upstream ✔ — the **reference source of this port** |
| Tier | A — primary targets (large player base, loader support, reasonable tooling) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | — (reference) |

## Era

The most modded classic version; recipe JSON, advancements, registry events.

## What changes (relevant to this mod)

- JSON recipes (`_factories`/ore dictionary ingredients) and advancement unlocking recipes.
- Registry events (`RegistryEvent.Register`).
- Metadata variants still native.

## Impact on Random Things

- This is the source of truth for behavior: everything in this port was derived from it.
- The 17 ASM patches live here (`ClassTransformer`).

## Source and path

Already in the repo (`master`/`origin/1.12.2`). Nothing to port; maintain as reference.

## Verdict

Already supported upstream; keep as reference and for backports of fixes.

## To verify before starting

- [ ] Decide whether fixes found while porting (e.g. the Spectre cube coordinate bug) should be upstreamed.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
