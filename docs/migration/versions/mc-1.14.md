# Minecraft 1.14 (final release 1.14.4) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2019-04-23 ✔ |
| Final release | 1.14.4 — 2019-07-19 ✔ |
| Java | 8 |
| Forge | 28.2.30 ✔ (this repo is pinned to 28.2.26) |
| NeoForge | n/a |
| Fabric | ✔ supported (earliest stable Fabric version) |
| Mappings | MCP snapshot; official available |
| Upstream mod status | `master` branch of upstream is an incomplete 1.14 port ✔; **this repo's `1.14.4` branch is the full port** |
| Tier | A — primary targets (large player base, loader support, reasonable tooling) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | — (in progress; playtesting backlog) |

## Era

First broadly used post-flattening version; containers, data-driven loot, new village/jigsaw worldgen.

## What changes (relevant to this mod)

- See [03-version-notes](../03-version-notes-1.12.2-to-1.14.4.md) — fully analysed.

## Impact on Random Things

- Done: 147 blocks, 241 items, 222 recipes, 28 GUIs, custom entities, dimension, 14 coremods. Mostly `DONE-UNTESTED`.

## Source and path

This repo.

## Verdict

Base for all forward ports. Finish playtesting first.

## To verify before starting

- [ ] Bump Forge from 28.2.26 to 28.2.30? (newer builds exist ✔) — test before relying on it.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
