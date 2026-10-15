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

- **2026-10-15 - 1.16.5 -> 1.18.2 port started and the server side runs.** Plan and findings: [`../plans/1.18.2-port-plan.md`](../plans/1.18.2-port-plan.md); tools: [`../tools/README.md`](../tools/README.md).
  - Method that worked: a **javac-attributed source remapper** (`Remap.java`) over the 1.16.5 sources against the 1.16.5 jar rewrote 13,000 type/method/field/import references to Mojang names with zero attribution errors; the name maps came from the MCP csv + the 1.16.5 tsrg + Mojang `client.txt` (SRG ids are stable from 1.16 to 1.17+, so no 1.17 step is needed). Its one blind spot is overloads: the map is keyed by (class, name), so `BlockPos.offset(Direction)` became `offset`, `Level.setBlockState(pos, state)` became `setBlock`, the 4-argument `getCollisionShape` became `getBlockSupportShape`, `getEntitiesWithinAABB` became `getEntities`. Compile errors found all of them; fix with `errfix.py` (replace the token at the compiler's caret). A plain text/regex pass over member names is NOT safe (it renamed package segments and a field called `inventory`).
  - Things that cost time: regex edits using stale indices after inserting imports (corrupted ~19 block files, redone from the clean tree); a regex replacement written as `'\\1'` turning into a control character; compiler output written to a relative path; reading old test output after a failed compile.
  - 1.18 facts verified: `DimensionType`'s constructor is private (use datapack JSON); registries are frozen once the server starts (swap `Holder.Reference.value` by reflection); `ChunkGenerator` steps are futures and `StructureFeatureManager`/`Blender` query chunks up to 7-8 chunks out (a hand-driven generation test needs a wide region and a no-structures manager); `Biome` lookups return `Holder<Biome>`; `JigsawPlacement` rejects a pool element whose processor-list holder comes from another registry.
