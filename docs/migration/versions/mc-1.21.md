# Minecraft 1.21 (final release 1.21.11) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2024-06-13 ✔ |
| Final release | 1.21.11 — 2025-12-09 ✔ |
| Java | 21 |
| Forge | 61.2.1 ✔ (1.21.1: 52.1.16 ✔) |
| NeoForge | ✔ 21.11.45 (1.21.1: 21.1.256) |
| Fabric | ✔ (last version with Yarn mappings; 26.1 drops them ✔) |
| Mappings | Official Mojang names (Fabric: Yarn up to 1.21.11) |
| Upstream mod status | No upstream branch ✔ |
| Tier | A — primary targets (large player base, loader support, reasonable tooling) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | 1.21.1: L (≈0.5× from 1.20.6); 1.21.11: XL (≈0.8× from 1.21.1) |

## Era

A long line (12 releases ✔) in which almost every system was reworked; 1.21.11 is the last 1.x version ✔.

## What changes (relevant to this mod)

- 1.21: data-driven enchantments (datapack registry) — enchantments with effects defined as data/components.
- 1.21.2–1.21.4: recipe, entity and item-model reworks (item model definitions in `assets/<ns>/items/`; verify).
- 1.21.5: tools/weapons/armor become data components ✔; rendering abstracts OpenGL ✔.
- 1.21.6: serialization moves from `CompoundTag` to `ValueInput`/`ValueOutput` for block entities ✔; GUI blitting via render pipelines ✔.
- 1.21.9: FML refactor; Transfer API refactor on NeoForge ✔.
- 1.21.11: the final 1.x version ✔; Fabric still on Yarn.

## Impact on Random Things

- Enchantment: Magnetic Enchantment becomes a data-driven enchantment with an effect (the loot-modifier approach may still work).
- Block entities: every `read/write` NBT method is replaced by value I/O; all 40+ tile entities change.
- Rendering: TESRs, item renderers, particle and GUI drawing all move to the new pipeline; the item-model-definition format changes how property overrides (compasses, clocks, Sound Recorder states) are expressed.
- Capabilities/fluids/energy: replaced by NeoForge Transfer API on 1.21.9+ ✔.
- Choose NeoForge as primary loader for 1.21.x (Forge builds exist ✔ through 1.21.11).

## Source and path

This repo (forward from 1.20.6 or from a 1.20.1 intermediate).

## Verdict

Primary modern target. Port 1.21.1 first (large ecosystem), then 1.21.11 as the final 1.x version.

## To verify before starting

- [ ] Which 1.21.x release is "the" target: 1.21.1 (most used) vs 1.21.11 (final).
- [ ] Item-model-definition format and how property overrides map.
- [x] JS coremods: present on Forge through 1.21.10 and NeoForge ~1.21.5, **absent on Forge 61.2.1 (1.21.11) and NeoForge 21.8+ ✔ (2026-10-08)**; 1.21.11 needs Java transformers.

## Work log

- **2026-10-22: 1.21.1 ported on NeoForge 21.1.264** (branch `1.21.1`, cut from `1.20.6`): about 616 compile errors, all mechanical or small (ResourceLocation factories, vertex-builder rework, data-driven enchantment, plantable API, DimensionTransition, CraftingInput); suites green (server 380/0/4, client 382/0/2), GUI click test and 26 render scenes pass. One coremod needed rework (armor tint: int colour, NeoForge 21 overload split). Not done yet: real NeoForge install, desktop playtest, in-memory terrain-generation test. Plan `plans/1.21.1-port-plan.md`, notes `docs/project/api-gotchas-1.21.1.md`.
