# Minecraft 1.18 (final release 1.18.2) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2021-11-30 ✔ |
| Final release | 1.18.2 — 2022-02-28 ✔ |
| Java | 17 |
| Forge | 40.3.12 ✔ |
| NeoForge | n/a |
| Fabric | ✔ |
| Mappings | Official Mojang names |
| Upstream mod status | No upstream branch ✔ |
| Tier | B — feasible, secondary demand |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | M–L (≈0.5×) |

## Era

Caves & Cliffs part 2: new world height (−64..320), noise caves, new biome source.

## What changes (relevant to this mod)

- World height range −64..320; `Level.getMinBuildHeight()`; chunk sections/biomes per 4×4×4 cell.
- Worldgen: configured/placed features, noise-based terrain, new carvers.
- Java 17.

## Impact on Random Things

- Worldgen features (Ancient Furnace, mushrooms, Lotus) rewritten; Y-ranges (lighting sweep from 0–255, Glowing Mushroom depth, Spectre dimension height) change.
- Biome mutation (Ancient Furnace biome reassignment) is different: biomes now stored per 4³ cell in `PalettedContainer`.
- Spectre cube room placement/height limits.

## Source and path

This repo (forward from 1.17.1 or 1.16.5).

## Verdict

Good stability target (long-lived); tier B.

## To verify before starting

- [ ] Light engine API (checkBlock replacements).
- [ ] Chunk resend packets for biome changes.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
